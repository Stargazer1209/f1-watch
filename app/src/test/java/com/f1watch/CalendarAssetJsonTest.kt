package com.f1watch

import com.f1watch.data.model.ConstructorStanding
import com.f1watch.data.model.DriverStanding
import com.f1watch.data.model.LocalCalendarResponse
import com.f1watch.data.model.RaceResult
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CalendarAssetJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun testCalendarJsonIntegrity() {
        val file = File("src/main/assets/2026_calendar.json")
        assertTrue("2026_calendar.json file must exist in assets", file.exists())

        val content = file.readText()
        val calendar = json.decodeFromString<LocalCalendarResponse>(content)

        assertEquals("2026", calendar.season)
        assertEquals(23, calendar.races.size)

        // Check Round 1 Australia
        val round1 = calendar.races.first()
        assertEquals("1", round1.round)
        assertEquals("Australian Grand Prix", round1.raceName)
        assertEquals("albert_park", round1.circuit.circuitId)
        assertEquals("🇦🇺", round1.circuit.flagEmoji)
        assertNotNull(round1.firstPractice)
        assertNotNull(round1.qualifying)

        // Check Round 2 China Sprint
        val round2 = calendar.races[1]
        assertEquals("2", round2.round)
        assertEquals("Chinese Grand Prix", round2.raceName)
        assertEquals("🇨🇳", round2.circuit.flagEmoji)
        assertNotNull(round2.sprintQualifying)
        assertNotNull(round2.sprint)

        // Check Round 23 Final
        val lastRound = calendar.races.last()
        assertEquals("23", lastRound.round)
        assertNotNull(lastRound.raceName)
        assertTrue(lastRound.circuit.flagEmoji.isNotEmpty())
    }

    @Test
    fun testDriverStandingsJsonIntegrity() {
        val file = File("src/main/assets/2026_driver_standings.json")
        assertTrue("2026_driver_standings.json must exist in assets", file.exists())

        val content = file.readText()
        val standings = json.decodeFromString<List<DriverStanding>>(content)

        assertEquals(23, standings.size)

        val leader = standings.first()
        assertEquals("1", leader.position)
        assertNotNull(leader.driver.fullName)
        assertTrue(leader.points.toDouble() > 0)
        assertTrue(leader.constructors.isNotEmpty())
    }

    @Test
    fun testConstructorStandingsJsonIntegrity() {
        val file = File("src/main/assets/2026_constructor_standings.json")
        assertTrue("2026_constructor_standings.json must exist in assets", file.exists())

        val content = file.readText()
        val standings = json.decodeFromString<List<ConstructorStanding>>(content)

        assertEquals(11, standings.size)

        val leader = standings.first()
        assertEquals("1", leader.position)
        assertNotNull(leader.constructor.name)
        assertTrue(leader.points.toDouble() > 0)
    }

    @Test
    fun testRaceResultsJsonIntegrity() {
        val file = File("src/main/assets/2026_race_results.json")
        assertTrue("2026_race_results.json must exist in assets", file.exists())

        val content = file.readText()
        val resultsMap = json.decodeFromString<Map<String, List<RaceResult>>>(content)

        assertTrue("Should contain completed rounds 1 to 12", resultsMap.size >= 12)

        // Check Round 1 classification
        val round1Results = resultsMap["1"]
        assertNotNull("Round 1 results should exist", round1Results)
        assertTrue("Round 1 should have 20+ drivers classified", (round1Results?.size ?: 0) >= 20)

        val winner = round1Results?.first()
        assertEquals("1", winner?.position)
        assertEquals("25", winner?.points)
        assertNotNull(winner?.driver?.fullName)
        assertNotNull(winner?.constructor?.name)
    }
}
