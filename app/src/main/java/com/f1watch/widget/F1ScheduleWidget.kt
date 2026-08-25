package com.f1watch.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.f1watch.MainActivity
import com.f1watch.data.local.PreferencesManager
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import com.f1watch.data.repository.F1Repository
import com.f1watch.domain.CountdownCalculator
import com.f1watch.ui.theme.TeamColors
import kotlinx.coroutines.flow.first

class F1ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = F1Repository(context)
        val prefs = PreferencesManager(context)

        val nextRace = repository.getNextOrCurrentRace()
        val countdown = CountdownCalculator.calculate(nextRace)
        val themeKey = prefs.widgetThemeFlow.first()
        val opacity = prefs.widgetOpacityFlow.first()
        val showPractice = prefs.showPracticeFlow.first()

        provideContent {
            GlanceTheme {
                ScheduleWidgetContent(
                    race = nextRace,
                    countdown = countdown.formattedCompact,
                    isLive = countdown.isLive,
                    themeKey = themeKey,
                    opacity = opacity,
                    showPractice = showPractice
                )
            }
        }
    }
}

@Composable
fun ScheduleWidgetContent(
    race: Race?,
    countdown: String,
    isLive: Boolean,
    themeKey: String,
    opacity: Float,
    showPractice: Boolean
) {
    val themeColor = TeamColors.getColor(themeKey)
    val bgColor = Color(0xFF15151E).copy(alpha = opacity)
    val surfaceColor = Color(0xFF252535).copy(alpha = opacity)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header Row: Flag + Round + Grand Prix Title + Dates
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = race?.circuit?.flagEmoji ?: "🏁",
                    style = TextStyle(fontSize = 18.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "ROUND ${race?.round ?: "1"} · ${race?.raceName ?: "2026 Grand Prix"}",
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "📍 ${race?.circuit?.circuitName ?: "Circuit"}",
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFA0A0B2)),
                            fontSize = 10.sp
                        )
                    )
                }

                // Countdown Pill
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(themeColor)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isLive) "LIVE" else countdown,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Sessions List
            val allSessions = race?.getAllSessions() ?: emptyList()
            val filteredSessions = if (showPractice) allSessions else allSessions.filter {
                it.shortName != "FP1" && it.shortName != "FP2" && it.shortName != "FP3"
            }

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filteredSessions.take(4).forEach { session ->
                    SessionPill(
                        session = session,
                        themeColor = themeColor,
                        surfaceColor = surfaceColor,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                }
            }

            if (filteredSessions.size > 4) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filteredSessions.drop(4).forEach { session ->
                        SessionPill(
                            session = session,
                            themeColor = themeColor,
                            surfaceColor = surfaceColor,
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionPill(
    session: SessionItem,
    themeColor: Color,
    surfaceColor: Color,
    modifier: GlanceModifier
) {
    val isLive = session.status == SessionStatus.LIVE
    val isCompleted = session.status == SessionStatus.COMPLETED

    val textColor = when {
        isLive -> Color(0xFF00D26A)
        isCompleted -> Color(0xFF707085)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .cornerRadius(8.dp)
            .background(surfaceColor)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                text = session.shortName,
                style = TextStyle(
                    color = if (isLive) ColorProvider(Color(0xFF00D26A)) else ColorProvider(themeColor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
            Text(
                text = session.sessionTime.formatLocalWeekdayTime(),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 9.sp,
                    fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

class F1ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = F1ScheduleWidget()

    companion object {
        suspend fun updateAll(context: Context) {
            F1ScheduleWidget().updateAll(context)
        }
    }
}
