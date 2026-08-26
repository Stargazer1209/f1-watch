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

        val perUrl = F1MediaHelper.getDriverHeadshotUrl("PER")
        assertTrue("Perez URL should point to TheSportsDB R2 render", perUrl.contains("r2.thesportsdb.com/images/media/player/render/"))

        val botUrl = F1MediaHelper.getDriverHeadshotUrl("BOT")
        assertTrue("Bottas URL should point to TheSportsDB R2 cutout", botUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))
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

    @Test
    fun testDriverHeadshotFallbackAndCacheResolution() {
        com.f1watch.data.media.DriverMediaResolver.clearCacheForTest()

        // 1. Unknown driver not in static map or cache returns empty string
        val unknownBeforeCache = F1MediaHelper.getDriverHeadshotUrl("XYZ_UNKNOWN", "Unknown Driver")
        org.junit.Assert.assertEquals("", unknownBeforeCache)

        // 2. Put resolved URL in DriverMediaResolver cache (simulating async resolver completion)
        val mockCutoutUrl = "https://r2.thesportsdb.com/images/media/player/cutout/mock_cutout.png"
        com.f1watch.data.media.DriverMediaResolver.putInMemoryCacheForTest("XYZ_UNKNOWN", mockCutoutUrl)

        // 3. F1MediaHelper synchronous query now hits Tier 2 cache immediately
        val unknownAfterCache = F1MediaHelper.getDriverHeadshotUrl("XYZ_UNKNOWN", "Unknown Driver")
        org.junit.Assert.assertEquals(mockCutoutUrl, unknownAfterCache)

        com.f1watch.data.media.DriverMediaResolver.clearCacheForTest()
    }

    @Test
    fun testCutoutMatchingLogic() {
        val players = listOf(
            com.f1watch.data.media.TheSportsDbPlayerItem(
                idPlayer = "1",
                strPlayer = "Some Footballer",
                strSport = "Soccer",
                strTeam = "FC Barcelona",
                strCutout = "https://cutout_football.png"
            ),
            com.f1watch.data.media.TheSportsDbPlayerItem(
                idPlayer = "2",
                strPlayer = "Rookie Driver",
                strSport = "Motorsport",
                strTeam = "Scuderia Ferrari",
                strCutout = "https://cutout_racing.png"
            )
        )

        val matched = com.f1watch.data.media.DriverMediaResolver.findMatchingCutout(players)
        org.junit.Assert.assertEquals("https://cutout_racing.png", matched)
    }

    @Test
    fun testDriverFullNameMatching() {
        val perezUrl = F1MediaHelper.getDriverHeadshotUrl("", "Sergio Perez")
        assertTrue("Perez by full name should resolve", perezUrl.contains("r2.thesportsdb.com/images/media/player/render/"))

        val bottasUrl = F1MediaHelper.getDriverHeadshotUrl("", "Valtteri Bottas")
        assertTrue("Bottas by full name should resolve", bottasUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))

        val verstappenUrl = F1MediaHelper.getDriverHeadshotUrl("", "Max Verstappen")
        assertTrue("Verstappen by full name should resolve", verstappenUrl.contains("r2.thesportsdb.com/images/media/player/cutout/"))
    }
}

