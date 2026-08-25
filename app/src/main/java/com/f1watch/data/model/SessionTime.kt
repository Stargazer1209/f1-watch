package com.f1watch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class SessionTime(
    @SerialName("date") val date: String = "",
    @SerialName("time") val time: String = ""
) {
    fun toUtcIsoString(): String {
        val cleanTime = if (time.endsWith("Z")) time else "${time}Z"
        return "${date}T$cleanTime"
    }

    fun toZonedDateTime(): ZonedDateTime? {
        return try {
            Instant.parse(toUtcIsoString()).atZone(ZoneId.systemDefault())
        } catch (e: Exception) {
            null
        }
    }

    fun formatLocalDayTime(): String {
        val zdt = toZonedDateTime() ?: return "$date $time"
        val formatter = DateTimeFormatter.ofPattern("M月d日 EEE HH:mm", Locale.SIMPLIFIED_CHINESE)
        return zdt.format(formatter)
    }

    fun formatLocalTimeOnly(): String {
        val zdt = toZonedDateTime() ?: return time
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return zdt.format(formatter)
    }

    fun formatLocalWeekdayTime(): String {
        val zdt = toZonedDateTime() ?: return "$date $time"
        val formatter = DateTimeFormatter.ofPattern("E HH:mm", Locale.SIMPLIFIED_CHINESE)
        return zdt.format(formatter)
    }
}

enum class SessionStatus {
    UPCOMING,
    LIVE,
    COMPLETED
}

data class SessionItem(
    val name: String,
    val shortName: String,
    val sessionTime: SessionTime,
    val status: SessionStatus
)
