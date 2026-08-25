package com.f1watch

import com.f1watch.data.model.Circuit
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionStatus
import com.f1watch.data.model.SessionTime
import com.f1watch.domain.CountdownCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CountdownCalculatorTest {

    @Test
    fun testNullRaceHandling() {
        val result = CountdownCalculator.calculate(null)
        assertEquals(0L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertFalse(result.isLive)
        assertEquals("暂无比赛", result.targetSessionName)
    }

    @Test
    fun testUpcomingRaceCountdown() {
        // Race far in future
        val futureRace = Race(
            round = "1",
            raceName = "Australian Grand Prix",
            circuit = Circuit(
                circuitId = "albert_park",
                circuitName = "Albert Park Circuit",
                locality = "Melbourne",
                country = "Australia",
                flagEmoji = "🇦🇺"
            ),
            date = "2028-03-20",
            time = "05:00:00Z",
            firstPractice = SessionTime(date = "2028-03-18", time = "01:30:00Z")
        )

        val result = CountdownCalculator.calculate(futureRace)
        assertFalse(result.isLive)
        assertTrue(result.days > 0)
        assertTrue(result.formattedLong.contains("FP1"))
        assertTrue(result.targetSessionName.contains("一练"))
    }

    @Test
    fun testPastRaceHandling() {
        // Race far in the past
        val pastRace = Race(
            round = "1",
            raceName = "Old GP",
            date = "2020-03-15",
            time = "05:00:00Z"
        )

        val result = CountdownCalculator.calculate(pastRace)
        assertFalse(result.isLive)
        assertEquals("本站已完赛", result.formattedCompact)
    }
}
