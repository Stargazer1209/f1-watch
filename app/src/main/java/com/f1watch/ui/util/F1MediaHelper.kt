package com.f1watch.ui.util

object F1MediaHelper {

    private const val F1DB_SVG_BASE = "https://cdn.jsdelivr.net/gh/f1db/f1db@main/src/assets/circuits/white-outline"

    // TheSportsDB Official High-Definition Cloudflare R2 Transparent Cutouts
    private val driverCutoutMap = mapOf(
        "VER" to "https://r2.thesportsdb.com/images/media/player/cutout/fiyui61769371945.png",
        "HAM" to "https://r2.thesportsdb.com/images/media/player/cutout/exado51769372031.png",
        "LEC" to "https://r2.thesportsdb.com/images/media/player/cutout/t7asa51769372100.png",
        "NOR" to "https://r2.thesportsdb.com/images/media/player/cutout/og7n5r1769371379.png",
        "RUS" to "https://r2.thesportsdb.com/images/media/player/cutout/cmqswc1769371629.png",
        "PIA" to "https://r2.thesportsdb.com/images/media/player/cutout/ce3zpq1769371471.png",
        "ALO" to "https://r2.thesportsdb.com/images/media/player/cutout/rmczq21677931848.png",
        "SAI" to "https://r2.thesportsdb.com/images/media/player/cutout/4ojzdf1769372410.png",
        "ALB" to "https://r2.thesportsdb.com/images/media/player/cutout/x6mxpp1769372345.png",
        "GAS" to "https://r2.thesportsdb.com/images/media/player/cutout/6tef141769371181.png",
        "OCO" to "https://r2.thesportsdb.com/images/media/player/cutout/m7uhbe1769371819.png",
        "TSU" to "https://r2.thesportsdb.com/images/media/player/cutout/i2n4tt1769371881.png",
        "HUL" to "https://r2.thesportsdb.com/images/media/player/cutout/90zkg81773160744.png",
        "STR" to "https://r2.thesportsdb.com/images/media/player/cutout/25otva1679827137.png",
        "LAW" to "https://r2.thesportsdb.com/images/media/player/cutout/3wa7ut1769372613.png",
        "BEA" to "https://r2.thesportsdb.com/images/media/player/cutout/f4stqe1769371755.png",
        "COL" to "https://r2.thesportsdb.com/images/media/player/cutout/uztkh41769371230.png",
        "ANT" to "https://r2.thesportsdb.com/images/media/player/cutout/8s9ouj1769371565.png",
        "LIN" to "https://r2.thesportsdb.com/images/media/player/cutout/fspuvx1770026197.png",
        "BOR" to "https://r2.thesportsdb.com/images/media/player/cutout/dw6mi31773160637.png",
        "HAD" to "https://r2.thesportsdb.com/images/media/player/cutout/ifqmow1772534127.png",
        "DOO" to "https://r2.thesportsdb.com/images/media/player/cutout/4bbck31740659318.png"
    )

    // TheSportsDB Official High-Definition Car Equipment (Transparent Side-view)
    private val carEquipmentMap = mapOf(
        "aston_martin" to "https://r2.thesportsdb.com/images/media/team/equipment/73kp9v1773231447.png",
        "audi" to "https://r2.thesportsdb.com/images/media/team/equipment/ckb4zs1773231544.png",
        "sauber" to "https://r2.thesportsdb.com/images/media/team/equipment/ckb4zs1773231544.png",
        "alpine" to "https://r2.thesportsdb.com/images/media/team/equipment/udfzkv1773231604.png",
        "cadillac" to "https://r2.thesportsdb.com/images/media/team/equipment/5jhmyh1773231682.png",
        "mclaren" to "https://r2.thesportsdb.com/images/media/team/equipment/hq4x5r1773231742.png",
        "mercedes" to "https://r2.thesportsdb.com/images/media/team/equipment/1x45781773231834.png",
        "haas" to "https://r2.thesportsdb.com/images/media/team/equipment/ugpc041773231897.png",
        "red_bull" to "https://r2.thesportsdb.com/images/media/team/equipment/jtf07x1773231960.png",
        "ferrari" to "https://r2.thesportsdb.com/images/media/team/equipment/e2764s1773232017.png",
        "rb" to "https://r2.thesportsdb.com/images/media/team/equipment/xhh9mc1773232085.png",
        "williams" to "https://r2.thesportsdb.com/images/media/team/equipment/1x45781773231834.png"
    )

    // TheSportsDB Official High-Definition Team Badges (Logos)
    private val teamBadgeMap = mapOf(
        "aston_martin" to "https://r2.thesportsdb.com/images/media/team/badge/ez5rlk1740774066.png",
        "audi" to "https://r2.thesportsdb.com/images/media/team/badge/3uce6h1773158180.png",
        "sauber" to "https://r2.thesportsdb.com/images/media/team/badge/3uce6h1773158180.png",
        "alpine" to "https://r2.thesportsdb.com/images/media/team/badge/ozhoj31740774899.png",
        "cadillac" to "https://r2.thesportsdb.com/images/media/team/badge/2wqnjo1769429652.png",
        "mclaren" to "https://r2.thesportsdb.com/images/media/team/badge/kzqi7v1743602056.png",
        "mercedes" to "https://r2.thesportsdb.com/images/media/team/badge/6caw0r1744037679.png",
        "haas" to "https://r2.thesportsdb.com/images/media/team/badge/d934om1740775016.png",
        "red_bull" to "https://r2.thesportsdb.com/images/media/team/badge/7x8s3u1740774574.png",
        "ferrari" to "https://r2.thesportsdb.com/images/media/team/badge/2d3bms1740774780.png",
        "rb" to "https://r2.thesportsdb.com/images/media/team/badge/21z89e1740774643.png",
        "williams" to "https://r2.thesportsdb.com/images/media/team/badge/5k8m6p1740774431.png"
    )

    /**
     * Map circuitId to F1DB open-source pure-white SVG vector (via jsDelivr CDN)
     */
    fun getCircuitMapUrl(circuitId: String): String {
        val slug = when (circuitId.trim().lowercase()) {
            "albert_park", "melbourne", "australia" -> "melbourne"
            "shanghai", "china" -> "shanghai"
            "suzuka", "japan" -> "suzuka"
            "bahrain", "sakhir" -> "bahrain"
            "jeddah", "saudi_arabia", "saudi" -> "jeddah"
            "miami" -> "miami"
            "villeneuve", "montreal", "canada" -> "montreal"
            "monaco", "monte_carlo" -> "monaco"
            "catalunya", "barcelona", "spain" -> "catalunya"
            "red_bull_ring", "spielberg", "austria" -> "spielberg"
            "silverstone", "great_britain", "uk" -> "silverstone"
            "spa", "belgium", "francorchamps" -> "spa-francorchamps"
            "hungaroring", "budapest", "hungary" -> "hungaroring"
            "zandvoort", "netherlands", "dutch" -> "zandvoort"
            "monza", "italy", "italian" -> "monza"
            "madring", "madrid" -> "madring"
            "baku", "azerbaijan" -> "baku"
            "marina_bay", "singapore" -> "marina-bay"
            "americas", "cota", "austin", "usa" -> "austin"
            "rodriguez", "mexico", "mexicocity" -> "mexico-city"
            "interlagos", "brazil", "saopaulo", "sao_paulo" -> "interlagos"
            "vegas", "lasvegas", "las_vegas" -> "las-vegas"
            "losail", "lusail", "qatar" -> "lusail"
            "yas_marina", "yasmarina", "abu_dhabi", "abudhabi" -> "yas-marina"
            "sepang", "kuala_lumpur", "malaysia" -> "sepang"
            else -> circuitId.lowercase().replace("_", "-")
        }

        return "$F1DB_SVG_BASE/$slug-1.svg"
    }

    /**
     * Map driver code or familyName to TheSportsDB Cloudflare R2 transparent cutout portrait
     */
    fun getDriverHeadshotUrl(driverCode: String, driverFullName: String = ""): String {
        val codeUpper = driverCode.trim().uppercase()
        val mappedUrl = driverCutoutMap[codeUpper]
        if (mappedUrl != null) {
            return mappedUrl
        }

        // Fuzzy match by driver full name
        val lower = driverFullName.lowercase()
        for ((code, url) in driverCutoutMap) {
            if (code.lowercase() in lower) return url
        }

        // Fallback to empty string to trigger Compose native team badge avatar
        return ""
    }

    /**
     * Map constructorId to TheSportsDB Cloudflare R2 side-view car equipment image
     */
    fun getCarImageUrl(constructorId: String): String {
        val key = when (val raw = constructorId.trim().lowercase()) {
            "mclaren" -> "mclaren"
            "ferrari" -> "ferrari"
            "mercedes" -> "mercedes"
            "red_bull", "redbull", "red bull" -> "red_bull"
            "aston_martin", "astonmartin", "aston martin" -> "aston_martin"
            "alpine" -> "alpine"
            "williams" -> "williams"
            "sauber", "kick_sauber", "audi" -> "audi"
            "rb", "racing_bulls", "racing bulls", "alphatauri" -> "rb"
            "haas" -> "haas"
            "cadillac", "andretti" -> "cadillac"
            else -> raw
        }

        return carEquipmentMap[key] ?: carEquipmentMap["ferrari"]!!
    }

    /**
     * Map constructorId to TheSportsDB official HD team badge (logo)
     */
    fun getTeamBadgeUrl(constructorId: String): String {
        val key = when (val raw = constructorId.trim().lowercase()) {
            "mclaren" -> "mclaren"
            "ferrari" -> "ferrari"
            "mercedes" -> "mercedes"
            "red_bull", "redbull", "red bull" -> "red_bull"
            "aston_martin", "astonmartin", "aston martin" -> "aston_martin"
            "alpine" -> "alpine"
            "williams" -> "williams"
            "sauber", "kick_sauber", "audi" -> "audi"
            "rb", "racing_bulls", "racing bulls", "alphatauri" -> "rb"
            "haas" -> "haas"
            "cadillac", "andretti" -> "cadillac"
            else -> raw
        }

        return teamBadgeMap[key] ?: teamBadgeMap["ferrari"]!!
    }
}
