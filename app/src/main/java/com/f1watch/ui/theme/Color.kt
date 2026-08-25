package com.f1watch.ui.theme

import androidx.compose.ui.graphics.Color

// F1 Brand Colors
val F1Red = Color(0xFFE10600)
val F1RedDark = Color(0xFFB30500)
val F1Dark = Color(0xFF15151E)
val F1CardBg = Color(0xFF1E1E28)
val F1CardSurface = Color(0xFF262636)
val F1CardBorder = Color(0xFF38384D)
val F1TextWhite = Color(0xFFFFFFFF)
val F1TextSecondary = Color(0xFFA0A0B2)
val F1TextMuted = Color(0xFF707085)
val F1GreenLive = Color(0xFF00D26A)

// Podium Colors
val PodiumGold = Color(0xFFFFD700)
val PodiumSilver = Color(0xFFDCDCDC)
val PodiumBronze = Color(0xFFCD7F32)
val FastestLapPurple = Color(0xFFB026FF)

// 2026 Team Primary Colors
object TeamColors {
    val Ferrari = Color(0xFFF91536)
    val McLaren = Color(0xFFFF8000)
    val RedBull = Color(0xFF3671C6)
    val Mercedes = Color(0xFF27F4D2)
    val AstonMartin = Color(0xFF229971)
    val Williams = Color(0xFF00A0DE)
    val Sauber = Color(0xFF52E252)
    val Alpine = Color(0xFF0093CC)
    val RB = Color(0xFF6692FF)
    val Haas = Color(0xFFB6BABD)
    val Cadillac = Color(0xFFD4AF37)

    val TeamThemeMap = mapOf(
        "f1_red" to F1Red,
        "ferrari" to Ferrari,
        "mclaren" to McLaren,
        "redbull" to RedBull,
        "mercedes" to Mercedes,
        "aston_martin" to AstonMartin,
        "williams" to Williams,
        "sauber" to Sauber,
        "alpine" to Alpine,
        "rb" to RB,
        "haas" to Haas,
        "cadillac" to Cadillac
    )

    val TeamNameMap = mapOf(
        "f1_red" to "F1 官方红",
        "ferrari" to "法拉利",
        "mclaren" to "迈凯伦",
        "redbull" to "红牛",
        "mercedes" to "梅赛德斯",
        "aston_martin" to "阿斯顿·马丁",
        "williams" to "威廉姆斯",
        "sauber" to "索伯",
        "alpine" to "阿尔派",
        "rb" to "小牛 RB",
        "haas" to "哈斯",
        "cadillac" to "凯迪拉克"
    )

    fun getColor(key: String): Color {
        return TeamThemeMap[key] ?: F1Red
    }

    fun getColorByConstructorName(name: String): Color {
        val lower = name.lowercase()
        return when {
            "ferrari" in lower -> Ferrari
            "mclaren" in lower -> McLaren
            "red bull" in lower -> RedBull
            "mercedes" in lower -> Mercedes
            "aston martin" in lower -> AstonMartin
            "williams" in lower -> Williams
            "sauber" in lower || "audi" in lower || "kick" in lower -> Sauber
            "alpine" in lower -> Alpine
            "rb" in lower || "toro rosso" in lower || "alphatauri" in lower -> RB
            "haas" in lower -> Haas
            "cadillac" in lower || "andretti" in lower -> Cadillac
            else -> F1Red
        }
    }
}
