package com.f1watch

import com.f1watch.data.api.ApiClient
import com.f1watch.domain.CountdownCalculator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

class ApiAndLogicIntegrationTest {

    // Proxy-enabled API client for testing
    private val testApi by lazy {
        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 7890))
        val okHttpClient = OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.jolpi.ca/ergast/")
            .client(okHttpClient)
            .addConverterFactory(ApiClient.json.asConverterFactory(contentType))
            .build()
            .create(com.f1watch.data.api.JolpicaF1Api::class.java)
    }

    @Test
    fun testLiveApiAndScheduleParsing() = runBlocking {
        println(">>> [DEBUG LOG] 1. Calling Jolpica API: getSeasonSchedule('current')...")
        val response = try {
            testApi.getSeasonSchedule("current")
        } catch (e: Exception) {
            println(">>> [DEBUG LOG] Fallback to direct call: ${e.message}")
            ApiClient.f1Api.getSeasonSchedule("current")
        }

        val racesDto = response.mrData.raceTable.races
        println(">>> [DEBUG LOG] Fetched ${racesDto.size} races for season ${response.mrData.raceTable.season}.")
        assertTrue("Races list should not be empty", racesDto.isNotEmpty())

        val domainRaces = racesDto.map { it.toDomainRace(emptyMap()) }
        val nextRace = domainRaces.firstOrNull { !it.isWeekendCompleted() } ?: domainRaces.lastOrNull()
        assertNotNull(nextRace)
        println(">>> [DEBUG LOG] Current/Next Grand Prix: Round ${nextRace?.round} - ${nextRace?.raceName} (${nextRace?.date})")

        val countdown = CountdownCalculator.calculate(nextRace)
        println(">>> [DEBUG LOG] Countdown: ${countdown.formattedCompact} | Target Session: ${countdown.targetSessionName}")

        // 2. Fetch Driver Standings
        println(">>> [DEBUG LOG] 2. Calling Jolpica API: getDriverStandings('current')...")
        val dsResponse = try {
            testApi.getDriverStandings("current")
        } catch (e: Exception) {
            ApiClient.f1Api.getDriverStandings("current")
        }
        val driverStandings = dsResponse.mrData.standingsTable.standingsLists.firstOrNull()?.driverStandings ?: emptyList()
        println(">>> [DEBUG LOG] Driver Standings count: ${driverStandings.size}")
        if (driverStandings.isNotEmpty()) {
            val leader = driverStandings[0]
            println(">>> [DEBUG LOG] Championship Leader: P1 ${leader.driver.fullName} (${leader.constructors.firstOrNull()?.name}) ${leader.points}pts, ${leader.wins} wins")
        }
        assertTrue(driverStandings.isNotEmpty())

        // 3. Fetch Constructor Standings
        println(">>> [DEBUG LOG] 3. Calling Jolpica API: getConstructorStandings('current')...")
        val csResponse = try {
            testApi.getConstructorStandings("current")
        } catch (e: Exception) {
            ApiClient.f1Api.getConstructorStandings("current")
        }
        val constructorStandings = csResponse.mrData.standingsTable.standingsLists.firstOrNull()?.constructorStandings ?: emptyList()
        println(">>> [DEBUG LOG] Constructor Standings count: ${constructorStandings.size}")
        if (constructorStandings.isNotEmpty()) {
            val leader = constructorStandings[0]
            println(">>> [DEBUG LOG] Constructors Leader: P1 ${leader.constructor.name} ${leader.points}pts, ${leader.wins} wins")
        }
        assertTrue(constructorStandings.isNotEmpty())

        // 4. Fetch Race Results for completed round 1
        println(">>> [DEBUG LOG] 4. Calling Jolpica API: getRaceResults('current', '1')...")
        val resResponse = try {
            testApi.getRaceResults("current", "1")
        } catch (e: Exception) {
            ApiClient.f1Api.getRaceResults("current", "1")
        }
        val results = resResponse.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
        println(">>> [DEBUG LOG] Round 1 Results count: ${results.size}")
        if (results.isNotEmpty()) {
            println(">>> [DEBUG LOG] Round 1 Podium:")
            results.take(3).forEach {
                println(">>> [DEBUG LOG]   P${it.position}: ${it.driver.fullName} (${it.constructor.name}) - ${it.points} pts")
            }
        }
        assertTrue(results.isNotEmpty())
    }
}
