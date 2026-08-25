package com.f1watch

import com.f1watch.data.model.Circuit
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionStatus
import com.f1watch.data.model.SessionTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceModelTest {

    @Test
    fun testStandardWeekendSessions() {
        val race = Race(
            round = "1",
            raceName = "Australian Grand Prix",
            date = "2026-03-08",
            time = "04:00:00Z",
            firstPractice = SessionTime("2026-03-06", "01:30:00Z"),
            secondPractice = SessionTime("2026-03-06", "05:00:00Z"),
            thirdPractice = SessionTime("2026-03-07", "01:30:00Z"),
            qualifying = SessionTime("2026-03-07", "05:00:00Z")
        )

        val sessions = race.getAllSessions()
        assertEquals(5, sessions.size)
        assertEquals("FP1", sessions[0].shortName)
        assertEquals("FP2", sessions[1].shortName)
        assertEquals("FP3", sessions[2].shortName)
        assertEquals("QUAL", sessions[3].shortName)
        assertEquals("RACE", sessions[4].shortName)
    }

    @Test
    fun testSprintWeekendSessions() {
        val sprintRace = Race(
            round = "2",
            raceName = "Chinese Grand Prix",
            date = "2026-03-22",
            time = "07:00:00Z",
            firstPractice = SessionTime("2026-03-20", "03:30:00Z"),
            sprintQualifying = SessionTime("2026-03-20", "07:30:00Z"),
            sprint = SessionTime("2026-03-21", "03:00:00Z"),
            qualifying = SessionTime("2026-03-21", "07:00:00Z")
        )

        val sessions = sprintRace.getAllSessions()
        assertEquals(5, sessions.size)
        assertEquals("FP1", sessions[0].shortName)
        assertEquals("SQ", sessions[1].shortName)
        assertEquals("SPR", sessions[2].shortName)
        assertEquals("QUAL", sessions[3].shortName)
        assertEquals("RACE", sessions[4].shortName)
    }

    @Test
    fun testSessionTimeConversion() {
        val sessionTime = SessionTime(date = "2026-03-22", time = "07:00:00Z")
        val zdt = sessionTime.toZonedDateTime()
        assertNotNull(zdt)
        assertTrue(sessionTime.formatLocalDayTime().isNotEmpty())
        assertTrue(sessionTime.formatLocalWeekdayTime().isNotEmpty())
    }
}
