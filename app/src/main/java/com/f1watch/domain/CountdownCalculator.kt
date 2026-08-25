package com.f1watch.domain

import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import java.time.Duration
import java.time.Instant

data class CountdownResult(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isLive: Boolean,
    val targetSessionName: String,
    val formattedCompact: String,
    val formattedLong: String
)

object CountdownCalculator {

    fun calculate(race: Race?): CountdownResult {
        if (race == null) {
            return CountdownResult(
                days = 0, hours = 0, minutes = 0, seconds = 0,
                isLive = false, targetSessionName = "暂无比赛",
                formattedCompact = "--", formattedLong = "赛季已结束"
            )
        }

        val now = Instant.now()
        val allSessions = race.getAllSessions()
        
        // Find live session first
        val liveSession = allSessions.firstOrNull { it.status == SessionStatus.LIVE }
        if (liveSession != null) {
            return CountdownResult(
                days = 0, hours = 0, minutes = 0, seconds = 0,
                isLive = true,
                targetSessionName = liveSession.name,
                formattedCompact = "LIVE 进行中",
                formattedLong = "${liveSession.name} 正在进行中 🟢"
            )
        }

        // Next upcoming session
        val nextSession = allSessions.firstOrNull { it.status == SessionStatus.UPCOMING }
        val targetSession = nextSession ?: allSessions.last()
        val targetInstant = targetSession.sessionTime.toZonedDateTime()?.toInstant() ?: now

        val duration = Duration.between(now, targetInstant)
        if (duration.isNegative) {
            return CountdownResult(
                days = 0, hours = 0, minutes = 0, seconds = 0,
                isLive = false,
                targetSessionName = "已结束",
                formattedCompact = "本站已完赛",
                formattedLong = "本站大奖赛已全部完赛"
            )
        }

        val totalSeconds = duration.seconds
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val compact = when {
            days > 0 -> "${days}天 ${hours}小时"
            hours > 0 -> "${hours}小时 ${minutes}分"
            minutes > 0 -> "${minutes}分钟"
            else -> "${seconds}秒 即将开赛"
        }

        val long = when {
            days > 0 -> "距 ${targetSession.shortName} 开始还有 ${days} 天 ${hours} 小时"
            hours > 0 -> "距 ${targetSession.shortName} 开始还有 ${hours} 小时 ${minutes} 分钟"
            minutes > 0 -> "距 ${targetSession.shortName} 开始还有 ${minutes} 分钟"
            else -> "距 ${targetSession.shortName} 即将开始 (${seconds}秒)"
        }

        return CountdownResult(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isLive = false,
            targetSessionName = targetSession.name,
            formattedCompact = compact,
            formattedLong = long
        )
    }
}
