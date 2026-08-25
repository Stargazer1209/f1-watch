package com.f1watch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class Race(
    @SerialName("round") val round: String = "",
    @SerialName("raceName") val raceName: String = "",
    @SerialName("circuit") val circuit: Circuit = Circuit(),
    @SerialName("date") val date: String = "",
    @SerialName("time") val time: String = "",
    @SerialName("firstPractice") val firstPractice: SessionTime? = null,
    @SerialName("secondPractice") val secondPractice: SessionTime? = null,
    @SerialName("thirdPractice") val thirdPractice: SessionTime? = null,
    @SerialName("qualifying") val qualifying: SessionTime? = null,
    @SerialName("sprintQualifying") val sprintQualifying: SessionTime? = null,
    @SerialName("sprint") val sprint: SessionTime? = null
) {
    fun getMainRaceSession(): SessionTime {
        return SessionTime(date = date, time = time)
    }

    fun getAllSessions(): List<SessionItem> {
        val now = Instant.now()
        val list = mutableListOf<SessionItem>()

        firstPractice?.let {
            list.add(SessionItem("一练 (FP1)", "FP1", it, calculateStatus(it, now)))
        }

        if (sprintQualifying != null) {
            list.add(SessionItem("冲刺排位 (SQ)", "SQ", sprintQualifying, calculateStatus(sprintQualifying, now)))
            sprint?.let {
                list.add(SessionItem("冲刺赛 (SPR)", "SPR", it, calculateStatus(it, now)))
            }
        } else {
            secondPractice?.let {
                list.add(SessionItem("二练 (FP2)", "FP2", it, calculateStatus(it, now)))
            }
            thirdPractice?.let {
                list.add(SessionItem("三练 (FP3)", "FP3", it, calculateStatus(it, now)))
            }
        }

        qualifying?.let {
            list.add(SessionItem("排位赛 (QUAL)", "QUAL", it, calculateStatus(it, now)))
        }

        val main = getMainRaceSession()
        list.add(SessionItem("正赛 (RACE)", "RACE", main, calculateStatus(main, now, durationMinutes = 120)))

        return list
    }

    private fun calculateStatus(session: SessionTime, now: Instant, durationMinutes: Long = 60): SessionStatus {
        val zdt = session.toZonedDateTime() ?: return SessionStatus.UPCOMING
        val startInstant = zdt.toInstant()
        val endInstant = startInstant.plusSeconds(durationMinutes * 60)

        return when {
            now.isBefore(startInstant) -> SessionStatus.UPCOMING
            now.isAfter(endInstant) -> SessionStatus.COMPLETED
            else -> SessionStatus.LIVE
        }
    }

    fun isWeekendCompleted(): Boolean {
        val raceInstant = getMainRaceSession().toZonedDateTime()?.toInstant() ?: return false
        return Instant.now().isAfter(raceInstant.plusSeconds(7200))
    }

    fun isCurrentWeekend(): Boolean {
        val firstSessionInstant = getAllSessions().firstOrNull()?.sessionTime?.toZonedDateTime()?.toInstant()
        val raceEndInstant = getMainRaceSession().toZonedDateTime()?.toInstant()?.plusSeconds(7200)
        if (firstSessionInstant == null || raceEndInstant == null) return false
        val now = Instant.now()
        // Within 2 days before first practice until race end
        return now.isAfter(firstSessionInstant.minusSeconds(172800)) && now.isBefore(raceEndInstant)
    }

    fun getWeekendSpanString(): String {
        val firstDate = firstPractice?.date ?: date
        return "$firstDate - $date"
    }

    fun getNextUpcomingSession(): SessionItem? {
        val sessions = getAllSessions()
        return sessions.firstOrNull { it.status == SessionStatus.UPCOMING || it.status == SessionStatus.LIVE }
    }
}
