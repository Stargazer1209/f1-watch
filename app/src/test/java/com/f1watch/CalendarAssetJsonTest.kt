package com.f1watch

import com.f1watch.data.model.LocalCalendarResponse
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
}
