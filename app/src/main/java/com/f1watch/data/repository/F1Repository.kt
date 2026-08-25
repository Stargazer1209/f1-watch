package com.f1watch.data.repository

import android.content.Context
import android.util.Log
import com.f1watch.data.api.ApiClient
import com.f1watch.data.local.CalendarAssetReader
import com.f1watch.data.model.ConstructorStanding
import com.f1watch.data.model.DriverStanding
import com.f1watch.data.model.LocalCalendarResponse
import com.f1watch.data.model.Race
import com.f1watch.data.model.RaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class F1Repository(private val context: Context) {

    companion object {
        private const val TAG = "F1Repository"
    }

    private val api = ApiClient.f1Api
    private val json = ApiClient.json
    private val mutex = Mutex()

    // Persistent disk cache files
    private val scheduleFile get() = File(context.filesDir, "cached_schedule_current.json")
    private val driverStandingsFile get() = File(context.filesDir, "cached_driver_standings.json")
    private val constructorStandingsFile get() = File(context.filesDir, "cached_constructor_standings.json")

    // In-memory cache
    private var cachedRaces: List<Race> = emptyList()
    private val cachedResults = mutableMapOf<String, List<RaceResult>>()
    private var cachedDriverStandings: List<DriverStanding> = emptyList()
    private var cachedConstructorStandings: List<ConstructorStanding> = emptyList()

    /**
     * Get season schedule with 3-tier fallback and online refresh.
     * @return Pair<List<Race>, Boolean> where Boolean indicates whether data was freshly synced from network
     */
    suspend fun getRacesWithSyncStatus(forceRefresh: Boolean = false, season: String = "current"): Pair<List<Race>, Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            var syncedOnline = false

            if (!forceRefresh && cachedRaces.isNotEmpty()) {
                return@withContext Pair(cachedRaces, false)
            }

            // Step 1: Try reading disk cache
            if (cachedRaces.isEmpty() && scheduleFile.exists()) {
                try {
                    val diskContent = scheduleFile.readText()
                    val saved = json.decodeFromString<LocalCalendarResponse>(diskContent)
                    if (saved.races.isNotEmpty()) {
                        cachedRaces = saved.races
                        Log.d(TAG, "Loaded ${cachedRaces.size} races from local disk cache.")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read disk cache: ${e.message}")
                }
            }

            // Step 2: Fall back to assets baseline if still empty
            if (cachedRaces.isEmpty()) {
                val assetRaces = CalendarAssetReader.loadDefaultCalendar(context)
                if (assetRaces.isNotEmpty()) {
                    cachedRaces = assetRaces
                    Log.d(TAG, "Loaded ${cachedRaces.size} races from assets baseline.")
                }
            }

            // Step 3: Fetch online dynamic data from Jolpica
            try {
                Log.d(TAG, "Fetching online schedule from Jolpica API (season=$season)...")
                val response = try {
                    api.getSeasonSchedule(season)
                } catch (e: Exception) {
                    api.getSeasonSchedule("2026")
                }

                val apiRaces = response.mrData.raceTable.races.map { dto ->
                    dto.toDomainRace()
                }

                if (apiRaces.isNotEmpty()) {
                    cachedRaces = apiRaces
                    syncedOnline = true
                    Log.d(TAG, "Successfully synced ${apiRaces.size} races online.")
                    try {
                        val toSave = LocalCalendarResponse(
                            season = response.mrData.raceTable.season.ifEmpty { "current" },
                            races = apiRaces
                        )
                        scheduleFile.writeText(json.encodeToString(LocalCalendarResponse.serializer(), toSave))
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to persist schedule to disk: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Online schedule fetch error: ${e.message}, using cached/asset data.")
            }

            return@withContext Pair(cachedRaces, syncedOnline)
        }
    }

    suspend fun getRaces(forceRefresh: Boolean = false, season: String = "current"): List<Race> {
        return getRacesWithSyncStatus(forceRefresh, season).first
    }

    suspend fun getRaceResultsWithSyncStatus(round: String, forceRefresh: Boolean = false, season: String = "current"): Pair<List<RaceResult>, Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            var syncedOnline = false

            if (!forceRefresh && cachedResults.containsKey(round) && cachedResults[round]?.isNotEmpty() == true) {
                return@withContext Pair(cachedResults[round] ?: emptyList(), false)
            }

            // If empty, load default asset baseline
            if (!cachedResults.containsKey(round) || cachedResults[round]?.isEmpty() == true) {
                val assetResults = CalendarAssetReader.loadDefaultRaceResults(context, round)
                if (assetResults.isNotEmpty()) {
                    cachedResults[round] = assetResults
                    Log.d(TAG, "Loaded Round $round results from asset baseline (${assetResults.size} drivers)")
                }
            }

            // Fetch online from API
            try {
                Log.d(TAG, "Fetching Round $round results from API...")
                val response = try {
                    api.getRaceResults(season, round)
                } catch (e: Exception) {
                    api.getRaceResults("2026", round)
                }

                val results = response.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
                if (results.isNotEmpty()) {
                    cachedResults[round] = results
                    syncedOnline = true
                    Log.d(TAG, "Successfully synced Round $round results online (${results.size} drivers)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Online race results fetch error for round $round: ${e.message}")
            }

            return@withContext Pair(cachedResults[round] ?: emptyList(), syncedOnline)
        }
    }

    suspend fun getRaceResults(round: String, forceRefresh: Boolean = false, season: String = "current"): List<RaceResult> {
        return getRaceResultsWithSyncStatus(round, forceRefresh, season).first
    }

    suspend fun getDriverStandingsWithSyncStatus(forceRefresh: Boolean = false, season: String = "current"): Pair<List<DriverStanding>, Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            var syncedOnline = false

            if (!forceRefresh && cachedDriverStandings.isNotEmpty()) {
                return@withContext Pair(cachedDriverStandings, false)
            }

            // 1. Try disk cache
            if (cachedDriverStandings.isEmpty() && driverStandingsFile.exists()) {
                try {
                    val content = driverStandingsFile.readText()
                    val saved = json.decodeFromString<List<DriverStanding>>(content)
                    if (saved.isNotEmpty()) {
                        cachedDriverStandings = saved
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read driver standings disk cache: ${e.message}")
                }
            }

            // 2. Try asset baseline
            if (cachedDriverStandings.isEmpty()) {
                val assetStandings = CalendarAssetReader.loadDefaultDriverStandings(context)
                if (assetStandings.isNotEmpty()) {
                    cachedDriverStandings = assetStandings
                    Log.d(TAG, "Loaded driver standings from asset baseline (${assetStandings.size} drivers)")
                }
            }

            // 3. Fetch online
            try {
                Log.d(TAG, "Fetching driver standings online...")
                val response = try {
                    api.getDriverStandings(season)
                } catch (e: Exception) {
                    api.getDriverStandings("2026")
                }

                val standings = response.mrData.standingsTable.standingsLists.firstOrNull()?.driverStandings ?: emptyList()
                if (standings.isNotEmpty()) {
                    cachedDriverStandings = standings
                    syncedOnline = true
                    Log.d(TAG, "Successfully synced ${standings.size} driver standings online.")
                    try {
                        driverStandingsFile.writeText(json.encodeToString(kotlinx.serialization.builtins.ListSerializer(DriverStanding.serializer()), standings))
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to write driver standings cache: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Online driver standings fetch error: ${e.message}")
            }

            return@withContext Pair(cachedDriverStandings, syncedOnline)
        }
    }

    suspend fun getDriverStandings(forceRefresh: Boolean = false, season: String = "current"): List<DriverStanding> {
        return getDriverStandingsWithSyncStatus(forceRefresh, season).first
    }

    suspend fun getConstructorStandingsWithSyncStatus(forceRefresh: Boolean = false, season: String = "current"): Pair<List<ConstructorStanding>, Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            var syncedOnline = false

            if (!forceRefresh && cachedConstructorStandings.isNotEmpty()) {
                return@withContext Pair(cachedConstructorStandings, false)
            }

            // 1. Try disk cache
            if (cachedConstructorStandings.isEmpty() && constructorStandingsFile.exists()) {
                try {
                    val content = constructorStandingsFile.readText()
                    val saved = json.decodeFromString<List<ConstructorStanding>>(content)
                    if (saved.isNotEmpty()) {
                        cachedConstructorStandings = saved
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read constructor standings disk cache: ${e.message}")
                }
            }

            // 2. Try asset baseline
            if (cachedConstructorStandings.isEmpty()) {
                val assetStandings = CalendarAssetReader.loadDefaultConstructorStandings(context)
                if (assetStandings.isNotEmpty()) {
                    cachedConstructorStandings = assetStandings
                    Log.d(TAG, "Loaded constructor standings from asset baseline (${assetStandings.size} teams)")
                }
            }

            // 3. Fetch online
            try {
                Log.d(TAG, "Fetching constructor standings online...")
                val response = try {
                    api.getConstructorStandings(season)
                } catch (e: Exception) {
                    api.getConstructorStandings("2026")
                }

                val standings = response.mrData.standingsTable.standingsLists.firstOrNull()?.constructorStandings ?: emptyList()
                if (standings.isNotEmpty()) {
                    cachedConstructorStandings = standings
                    syncedOnline = true
                    Log.d(TAG, "Successfully synced ${standings.size} constructor standings online.")
                    try {
                        constructorStandingsFile.writeText(json.encodeToString(kotlinx.serialization.builtins.ListSerializer(ConstructorStanding.serializer()), standings))
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to write constructor standings cache: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Online constructor standings fetch error: ${e.message}")
            }

            return@withContext Pair(cachedConstructorStandings, syncedOnline)
        }
    }

    suspend fun getConstructorStandings(forceRefresh: Boolean = false, season: String = "current"): List<ConstructorStanding> {
        return getConstructorStandingsWithSyncStatus(forceRefresh, season).first
    }

    suspend fun getNextOrCurrentRace(): Race? {
        val races = getRaces()
        return races.firstOrNull { !it.isWeekendCompleted() } ?: races.lastOrNull()
    }
}
