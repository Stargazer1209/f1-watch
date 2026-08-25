package com.f1watch.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.Coil
import coil.request.ImageRequest
import com.f1watch.MainActivity
import com.f1watch.data.local.PreferencesManager
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import com.f1watch.data.repository.F1Repository
import com.f1watch.domain.CountdownCalculator
import com.f1watch.ui.theme.TeamColors
import com.f1watch.ui.util.F1MediaHelper
import com.f1watch.util.F1Localization
import kotlinx.coroutines.flow.first

class F1ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = F1Repository(context)
        val prefs = PreferencesManager(context)

        val nextRace = repository.getNextOrCurrentRace()
        val countdown = CountdownCalculator.calculate(nextRace)
        val themeKey = prefs.widgetThemeFlow.first()
        val showPractice = prefs.showPracticeFlow.first()

        // Load circuit vector into software bitmap for Glance RemoteViews
        val circuitBitmap: Bitmap? = nextRace?.let { race ->
            try {
                val mapUrl = F1MediaHelper.getCircuitMapUrl(race.circuit.circuitId)
                val request = ImageRequest.Builder(context)
                    .data(mapUrl)
                    .size(240, 240)
                    .allowHardware(false)
                    .build()
                val result = Coil.imageLoader(context).execute(request)
                (result.drawable as? BitmapDrawable)?.bitmap ?: result.drawable?.toBitmap(240, 240)
            } catch (e: Exception) {
                null
            }
        }

        provideContent {
            GlanceTheme {
                ScheduleWidgetContent(
                    race = nextRace,
                    countdown = countdown.formattedCompact,
                    isLive = countdown.isLive,
                    themeKey = themeKey,
                    circuitBitmap = circuitBitmap,
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
    circuitBitmap: Bitmap?,
    showPractice: Boolean
) {
    val themeColor = TeamColors.getColor(themeKey)
    val bgColor = Color(0xFF15151E)
    val surfaceColor = Color(0xFF22222E)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColor)
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header Row: Flag + Round + Grand Prix Title + Countdown Pill
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = race?.circuit?.flagEmoji ?: "🏁",
                    style = TextStyle(fontSize = 15.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "R${race?.round ?: "1"} · ${F1Localization.getRaceName(race?.raceName ?: "2026 大奖赛")}",
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                // Countdown Pill
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(if (isLive) Color(0xFF00D26A) else themeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
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

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Body Row: Left (Track Map) + Right (Sessions Timeline)
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Track Outline Box
                Column(
                    modifier = GlanceModifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .cornerRadius(10.dp)
                        .background(Color(0xFF0D0D12))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (circuitBitmap != null) {
                        Image(
                            provider = ImageProvider(circuitBitmap),
                            contentDescription = "Track Map",
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                        )
                    } else {
                        Box(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏎️",
                                style = TextStyle(fontSize = 20.sp)
                            )
                        }
                    }

                    Text(
                        text = F1Localization.getCircuitName(race?.circuit?.circuitName ?: "赛道"),
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFA0A0B2)),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Right: Session Breakdown
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allSessions = race?.getAllSessions() ?: emptyList()
                    val filteredSessions = if (showPractice) allSessions else allSessions.filter {
                        it.shortName != "FP1" && it.shortName != "FP2" && it.shortName != "FP3"
                    }

                    filteredSessions.take(4).forEach { session ->
                        SessionRowGlance(
                            session = session,
                            themeColor = themeColor,
                            surfaceColor = surfaceColor
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionRowGlance(
    session: SessionItem,
    themeColor: Color,
    surfaceColor: Color
) {
    val isLive = session.status == SessionStatus.LIVE
    val isCompleted = session.status == SessionStatus.COMPLETED
    val dotColor = when (session.status) {
        SessionStatus.LIVE -> Color(0xFF00D26A)
        SessionStatus.COMPLETED -> Color(0xFF555566)
        SessionStatus.UPCOMING -> themeColor
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(5.dp)
            .background(if (isLive) themeColor.copy(alpha = 0.25f) else surfaceColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Dot
        Box(
            modifier = GlanceModifier
                .size(4.dp)
                .cornerRadius(2.dp)
                .background(dotColor)
        ) {}
        Spacer(modifier = GlanceModifier.width(5.dp))
        Text(
            text = F1Localization.getSessionName(session.name),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(if (isCompleted) Color(0xFF888899) else Color.White),
                fontSize = 9.sp,
                fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = session.sessionTime.formatLocalDayTime(),
            style = TextStyle(
                color = ColorProvider(if (isCompleted) Color(0xFF888899) else if (isLive) dotColor else Color(0xFFD0D0DD)),
                fontSize = 8.sp,
                fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
            )
        )
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
