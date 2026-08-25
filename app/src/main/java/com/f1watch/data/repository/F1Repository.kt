package com.f1watch.data.repository

import android.content.Context
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
     * Get season schedule:
     * 1. If in-memory cache is present and not forceRefresh, return it immediately.
     * 2. If in-memory is empty, read local persistent disk cache (which contains latest online sync).
     * 3. If disk cache doesn't exist (e.g. first launch), load built-in 2026 asset as zero-delay baseline.
     * 4. Fetch latest online schedule (handling mid-season schedule shifts/cancellations), update memory and persist to disk.
     */
    suspend fun getRaces(forceRefresh: Boolean = false, season: String = "current"): List<Race> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!forceRefresh && cachedRaces.isNotEmpty()) {
                return@withContext cachedRaces
            }

            // Step 1: Try reading disk cache
            if (cachedRaces.isEmpty() && scheduleFile.exists()) {
                try {
                    val diskContent = scheduleFile.readText()
                    val saved = json.decodeFromString<LocalCalendarResponse>(diskContent)
                    if (saved.races.isNotEmpty()) {
                        cachedRaces = saved.races
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Step 2: If still empty, fall back to assets baseline
            if (cachedRaces.isEmpty()) {
                val assetRaces = CalendarAssetReader.loadDefaultCalendar(context)
                if (assetRaces.isNotEmpty()) {
                    cachedRaces = assetRaces
                }
            }

            // Step 3: Fetch online dynamic data from Jolpica
            try {
                val response = try {
                    api.getSeasonSchedule(season)
                } catch (e: Exception) {
                    // Fallback to specific 2026 season if "current" is transitioning
                    api.getSeasonSchedule("2026")
                }

                val apiRaces = response.mrData.raceTable.races.map { dto ->
                    dto.toDomainRace(emptyMap())
                }

                if (apiRaces.isNotEmpty()) {
                    cachedRaces = apiRaces
                    // Persist to disk so offline restarts retain latest calendar updates
                    try {
                        val toSave = LocalCalendarResponse(
                            season = response.mrData.raceTable.season.ifEmpty { "current" },
                            races = apiRaces
                        )
                        scheduleFile.writeText(json.encodeToString(LocalCalendarResponse.serializer(), toSave))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext cachedRaces
        }
    }

    suspend fun getRaceResults(round: String, forceRefresh: Boolean = false, season: String = "current"): List<RaceResult> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!forceRefresh && cachedResults.containsKey(round)) {
                return@withContext cachedResults[round] ?: emptyList()
            }

            try {
                val response = try {
                    api.getRaceResults(season, round)
                } catch (e: Exception) {
                    api.getRaceResults("2026", round)
                }

                val results = response.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
                if (results.isNotEmpty()) {
                    cachedResults[round] = results
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext cachedResults[round] ?: emptyList()
        }
    }

    suspend fun getDriverStandings(forceRefresh: Boolean = false, season: String = "current"): List<DriverStanding> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!forceRefresh && cachedDriverStandings.isNotEmpty()) {
                return@withContext cachedDriverStandings
            }

            // Try reading disk cache
            if (cachedDriverStandings.isEmpty() && driverStandingsFile.exists()) {
                try {
                    val content = driverStandingsFile.readText()
                    val saved = json.decodeFromString<List<DriverStanding>>(content)
                    if (saved.isNotEmpty()) {
                        cachedDriverStandings = saved
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val response = try {
                    api.getDriverStandings(season)
                } catch (e: Exception) {
                    api.getDriverStandings("2026")
                }

                val standings = response.mrData.standingsTable.standingsLists.firstOrNull()?.driverStandings ?: emptyList()
                if (standings.isNotEmpty()) {
                    cachedDriverStandings = standings
                    try {
                        driverStandingsFile.writeText(json.encodeToString(kotlinx.serialization.builtins.ListSerializer(DriverStanding.serializer()), standings))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext cachedDriverStandings
        }
    }

    suspend fun getConstructorStandings(forceRefresh: Boolean = false, season: String = "current"): List<ConstructorStanding> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!forceRefresh && cachedConstructorStandings.isNotEmpty()) {
                return@withContext cachedConstructorStandings
            }

            // Try reading disk cache
            if (cachedConstructorStandings.isEmpty() && constructorStandingsFile.exists()) {
                try {
                    val content = constructorStandingsFile.readText()
                    val saved = json.decodeFromString<List<ConstructorStanding>>(content)
                    if (saved.isNotEmpty()) {
                        cachedConstructorStandings = saved
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val response = try {
                    api.getConstructorStandings(season)
                } catch (e: Exception) {
                    api.getConstructorStandings("2026")
                }

                val standings = response.mrData.standingsTable.standingsLists.firstOrNull()?.constructorStandings ?: emptyList()
                if (standings.isNotEmpty()) {
                    cachedConstructorStandings = standings
                    try {
                        constructorStandingsFile.writeText(json.encodeToString(kotlinx.serialization.builtins.ListSerializer(ConstructorStanding.serializer()), standings))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext cachedConstructorStandings
        }
    }

    suspend fun getNextOrCurrentRace(): Race? {
        val races = getRaces()
        return races.firstOrNull { !it.isWeekendCompleted() } ?: races.lastOrNull()
    }
}
