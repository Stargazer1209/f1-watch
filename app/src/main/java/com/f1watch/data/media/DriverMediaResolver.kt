package com.f1watch.data.media

import android.content.Context
import com.f1watch.data.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Serializable
data class TheSportsDbSearchResponse(
    val player: List<TheSportsDbPlayerItem>? = null
)

@Serializable
data class TheSportsDbPlayerItem(
    val idPlayer: String? = null,
    val strPlayer: String? = null,
    val strTeam: String? = null,
    val strSport: String? = null,
    val strCutout: String? = null,
    val strThumb: String? = null,
    val strPosition: String? = null
)

@Serializable
data class DriverMediaCacheEntry(
    val driverCode: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class DriverMediaCacheData(
    val entries: Map<String, DriverMediaCacheEntry> = emptyMap()
)

object DriverMediaResolver {

    private const val THE_SPORTS_DB_SEARCH_URL = "https://www.thesportsdb.com/api/v1/json/123/searchplayers.php"
    private const val CACHE_FILE_NAME = "drivers-cache.json"
    private const val BACKOFF_MILLIS = 60_000L // 60s host-level backoff upon network failure

    private var appContext: Context? = null
    private val isDiskLoaded = AtomicBoolean(false)
    private val diskMutex = Mutex()

    // Level 2 In-Memory Cache: driverCode (uppercase) -> cutout URL (or empty string for negative cache)
    private val memoryCache = ConcurrentHashMap<String, String>()

    // Concurrency Deduplication: in-flight Deferred tasks per query key
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<String>>()

    // Host-level rate limiting / failure backoff timestamp
    private val lastFailureTimestamp = AtomicLong(0L)

    private val resolverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Initialize DriverMediaResolver with Application Context.
     * Triggers asynchronous loading of the local disk cache into memory.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        loadDiskCacheAsync()
    }

    /**
     * Synchronous lookup in memory or already loaded disk cache.
     * Returns cached URL if present (or empty string if negatively cached), otherwise null.
     */
    fun getCachedUrl(driverCode: String): String? {
        val key = driverCode.trim().uppercase()
        if (key.isEmpty()) return null

        // Check memory cache first
        memoryCache[key]?.let { return it }

        // If disk cache has not been loaded yet and context exists, try loading synchronously if fast
        if (!isDiskLoaded.get() && appContext != null) {
            loadDiskCacheSync()
            return memoryCache[key]
        }

        return null
    }

    /**
     * Asynchronously resolve a driver headshot URL:
     * 1. Check in-memory / disk cache.
     * 2. Deduplicate in-flight requests for the same driver.
     * 3. Respect 60s failure backoff window.
     * 4. Query TheSportsDB search API for player cutout.
     * 5. Persist to memory and disk cache.
     */
    suspend fun resolveDriverHeadshot(driverCode: String, driverFullName: String = ""): String {
        val codeKey = driverCode.trim().uppercase()
        val queryKey = if (codeKey.isNotEmpty()) codeKey else driverFullName.trim().lowercase()
        if (queryKey.isEmpty()) return ""

        // 1. Check cache hit
        getCachedUrl(queryKey)?.let { return it }

        // 2. Deduplicate concurrent in-flight requests for the same driver key
        val deferred = inFlightRequests.compute(queryKey) { _, existingDeferred ->
            if (existingDeferred != null && existingDeferred.isActive) {
                existingDeferred
            } else {
                resolverScope.async {
                    try {
                        fetchAndCacheDriverHeadshot(queryKey, driverFullName.trim())
                    } finally {
                        inFlightRequests.remove(queryKey)
                    }
                }
            }
        } ?: return ""

        return try {
            deferred.await()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Perform the actual network fetch against TheSportsDB search API with backoff guard.
     */
    private suspend fun fetchAndCacheDriverHeadshot(key: String, driverFullName: String): String {
        val now = System.currentTimeMillis()
        val lastFail = lastFailureTimestamp.get()
        if (now - lastFail < BACKOFF_MILLIS) {
            // Under 60s failure backoff, silently fallback to empty string
            return ""
        }

        val searchTerm = driverFullName.ifEmpty { key }
        if (searchTerm.isEmpty()) return ""

        try {
            val cutoutUrl = queryTheSportsDb(searchTerm)
            val finalUrl = cutoutUrl ?: ""

            // Store in memory cache
            memoryCache[key] = finalUrl

            // Persist to disk cache
            persistEntryToDisk(key, finalUrl)

            return finalUrl
        } catch (e: Exception) {
            // Trigger 60-second backoff for TheSportsDB search requests
            lastFailureTimestamp.set(System.currentTimeMillis())
            return ""
        }
    }

    /**
     * Execute HTTP GET to TheSportsDB player search endpoint and extract cutout URL.
     */
    private suspend fun queryTheSportsDb(searchTerm: String): String? = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(searchTerm, "UTF-8")
        val requestUrl = "$THE_SPORTS_DB_SEARCH_URL?p=$encoded"

        val request = Request.Builder()
            .url(requestUrl)
            .header("User-Agent", "F1Watch/1.0")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.io.IOException("TheSportsDB API request failed with HTTP ${response.code}")
            }
            val bodyString = response.body?.string() ?: return@use null
            val searchResponse = ApiClient.json.decodeFromString(TheSportsDbSearchResponse.serializer(), bodyString)
            findMatchingCutout(searchResponse.player)
        }
    }

    /**
     * Filter player entries for Racing / Motorsport / F1 drivers with valid cutout images.
     */
    fun findMatchingCutout(players: List<TheSportsDbPlayerItem>?): String? {
        if (players.isNullOrEmpty()) return null

        // 1. Prefer items where sport is Motorsport/Racing or team/position relates to F1/Driver
        val racingDriver = players.firstOrNull { item ->
            val sport = item.strSport.orEmpty()
            val team = item.strTeam.orEmpty()
            val position = item.strPosition.orEmpty()

            val isRacing = sport.contains("Motorsport", ignoreCase = true) ||
                    sport.contains("Racing", ignoreCase = true) ||
                    sport.contains("Formula", ignoreCase = true) ||
                    team.contains("F1", ignoreCase = true) ||
                    team.contains("Racing", ignoreCase = true) ||
                    position.contains("Driver", ignoreCase = true)

            isRacing && !item.strCutout.isNullOrBlank()
        }

        if (racingDriver != null) {
            return racingDriver.strCutout
        }

        // 2. Fallback to the first entry that has a non-blank cutout
        return players.firstOrNull { !it.strCutout.isNullOrBlank() }?.strCutout
    }

    /**
     * Synchronously load disk cache into memory cache (safe for offline testing / startup).
     */
    private fun loadDiskCacheSync() {
        val context = appContext ?: return
        if (isDiskLoaded.getAndSet(true)) return

        try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                val jsonString = cacheFile.readText()
                val data = ApiClient.json.decodeFromString(DriverMediaCacheData.serializer(), jsonString)
                for ((k, entry) in data.entries) {
                    memoryCache[k] = entry.url
                }
            }
        } catch (e: Exception) {
            // Ignore corrupted cache file
        }
    }

    private fun loadDiskCacheAsync() {
        resolverScope.async {
            loadDiskCacheSync()
        }
    }

    /**
     * Atomically write an entry update to local disk cache.
     */
    private suspend fun persistEntryToDisk(key: String, url: String) = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext
        diskMutex.withLock {
            try {
                val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
                val existingEntries = if (cacheFile.exists()) {
                    try {
                        val jsonString = cacheFile.readText()
                        ApiClient.json.decodeFromString(DriverMediaCacheData.serializer(), jsonString).entries.toMutableMap()
                    } catch (e: Exception) {
                        mutableMapOf()
                    }
                } else {
                    mutableMapOf()
                }

                existingEntries[key] = DriverMediaCacheEntry(
                    driverCode = key,
                    url = url,
                    timestamp = System.currentTimeMillis()
                )

                val updatedData = DriverMediaCacheData(entries = existingEntries)
                val jsonString = ApiClient.json.encodeToString(DriverMediaCacheData.serializer(), updatedData)

                // Atomic write with tmp file
                val tmpFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
                tmpFile.writeText(jsonString)
                tmpFile.renameTo(cacheFile)
            } catch (e: Exception) {
                // Disk write failures should not crash the app
            }
        }
    }

    /**
     * For unit testing: directly put entry into memory cache.
     */
    fun putInMemoryCacheForTest(key: String, url: String) {
        memoryCache[key.trim().uppercase()] = url
    }

    /**
     * For unit testing: clear all caches and backoff state.
     */
    fun clearCacheForTest() {
        memoryCache.clear()
        inFlightRequests.clear()
        lastFailureTimestamp.set(0L)
        isDiskLoaded.set(false)
    }
}
