package com.f1watch

import com.f1watch.ui.util.F1MediaHelper
import org.junit.Assert.assertTrue
import org.junit.Test

class F1MediaHelperTest {

    @Test
    fun testAllCircuitMapUrlsF1DB() {
        val testCircuits = listOf(
            "albert_park" to "melbourne",
            "shanghai" to "shanghai",
            "suzuka" to "suzuka",
            "bahrain" to "bahrain",
            "miami" to "miami",
            "villeneuve" to "montreal",
            "monaco" to "monaco",
            "catalunya" to "catalunya",
            "red_bull_ring" to "spielberg",
            "silverstone" to "silverstone",
            "spa" to "spa-francorchamps",
            "hungaroring" to "hungaroring",
            "zandvoort" to "zandvoort",
            "monza" to "monza",
            "madring" to "madring",
            "baku" to "baku",
            "marina_bay" to "marina-bay",
            "americas" to "austin",
            "rodriguez" to "mexico-city",
            "interlagos" to "interlagos",
            "vegas" to "las-vegas",
            "losail" to "lusail",
            "yas_marina" to "yas-marina"
        )

        for ((circuitId, expectedSlug) in testCircuits) {
            val url = F1MediaHelper.getCircuitMapUrl(circuitId)
            assertTrue("URL should start with f1db jsDelivr CDN", url.contains("cdn.jsdelivr.net/gh/f1db/f1db@main"))
            assertTrue("URL should contain expected slug $expectedSlug-1.svg", url.contains("$expectedSlug-1.svg"))
        }
    }

    @Test
    fun testDriverHeadshotUrlsTheSportsDB() {
        val antUrl = F1MediaHelper.getDriverHeadshotUrl("ANT")
        assertTrue("Antonelli URL should point to TheSportsDB R2 cutout", antUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))

        val hamUrl = F1MediaHelper.getDriverHeadshotUrl("HAM")
        assertTrue("Hamilton URL should point to TheSportsDB R2 cutout", hamUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))

        val linUrl = F1MediaHelper.getDriverHeadshotUrl("LIN")
        assertTrue("Lindblad URL should point to TheSportsDB R2 cutout", linUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))

        val verUrl = F1MediaHelper.getDriverHeadshotUrl("VER")
        assertTrue("Verstappen URL should point to TheSportsDB R2 cutout", verUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))
    }

    @Test
    fun testCarLiveryUrlsTheSportsDB() {
        val ferrariUrl = F1MediaHelper.getCarImageUrl("ferrari")
        assertTrue(ferrariUrl.contains("r2.thesportsdb.com/images/media/team/equipment/"))

        val mclarenUrl = F1MediaHelper.getCarImageUrl("mclaren")
        assertTrue(mclarenUrl.contains("r2.thesportsdb.com/images/media/team/equipment/"))

        val redbullUrl = F1MediaHelper.getCarImageUrl("red_bull")
        assertTrue(redbullUrl.contains("r2.thesportsdb.com/images/media/team/equipment/"))

        val audiUrl = F1MediaHelper.getCarImageUrl("audi")
        assertTrue(audiUrl.contains("r2.thesportsdb.com/images/media/team/equipment/"))

        val cadillacUrl = F1MediaHelper.getCarImageUrl("cadillac")
        assertTrue(cadillacUrl.contains("r2.thesportsdb.com/images/media/team/equipment/"))
    }

    @Test
    fun testTeamBadgesTheSportsDB() {
        val ferrariBadge = F1MediaHelper.getTeamBadgeUrl("ferrari")
        assertTrue(ferrariBadge.contains("r2.thesportsdb.com/images/media/team/badge/"))

        val audiBadge = F1MediaHelper.getTeamBadgeUrl("audi")
        assertTrue(audiBadge.contains("r2.thesportsdb.com/images/media/team/badge/"))
    }
}
