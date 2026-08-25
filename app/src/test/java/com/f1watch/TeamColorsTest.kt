package com.f1watch

import com.f1watch.ui.theme.TeamColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TeamColorsTest {

    @Test
    fun testAllTenTeamsHaveColors() {
        val keys = listOf(
            "f1_red", "ferrari", "mclaren", "redbull", "mercedes",
            "aston_martin", "williams", "sauber", "alpine", "rb", "haas"
        )

        for (key in keys) {
            val color = TeamColors.getColor(key)
            assertNotNull("Color for $key must exist", color)
        }
    }

    @Test
    fun testConstructorNameMatching() {
        assertEquals(TeamColors.Ferrari, TeamColors.getColorByConstructorName("Ferrari"))
        assertEquals(TeamColors.Ferrari, TeamColors.getColorByConstructorName("Scuderia Ferrari"))
        assertEquals(TeamColors.McLaren, TeamColors.getColorByConstructorName("McLaren Formula 1 Team"))
        assertEquals(TeamColors.RedBull, TeamColors.getColorByConstructorName("Red Bull Racing"))
        assertEquals(TeamColors.Mercedes, TeamColors.getColorByConstructorName("Mercedes-AMG PETRONAS F1 Team"))
        assertEquals(TeamColors.AstonMartin, TeamColors.getColorByConstructorName("Aston Martin Aramco F1 Team"))
        assertEquals(TeamColors.Sauber, TeamColors.getColorByConstructorName("Stake F1 Team Kick Sauber"))
    }
}
