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
import com.f1watch.data.repository.F1Repository
import com.f1watch.domain.CountdownCalculator
import com.f1watch.ui.theme.TeamColors
import kotlinx.coroutines.flow.first

class F1CompactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = F1Repository(context)
        val prefs = PreferencesManager(context)

        val nextRace = repository.getNextOrCurrentRace()
        val countdown = CountdownCalculator.calculate(nextRace)
        val themeKey = prefs.widgetThemeFlow.first()
        val opacity = prefs.widgetOpacityFlow.first()

        provideContent {
            GlanceTheme {
                CompactWidgetContent(
                    race = nextRace,
                    countdown = countdown.formattedCompact,
                    isLive = countdown.isLive,
                    nextSessionText = if (countdown.isLive) countdown.targetSessionName else "下一阶段: ${countdown.targetSessionName}",
                    themeKey = themeKey,
                    opacity = opacity
                )
            }
        }
    }
}

@Composable
fun CompactWidgetContent(
    race: Race?,
    countdown: String,
    isLive: Boolean,
    nextSessionText: String,
    themeKey: String,
    opacity: Float
) {
    val themeColor = TeamColors.getColor(themeKey)
    val bgColor = Color(0xFF15151E)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: Flag + Round + Race Name
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = race?.circuit?.flagEmoji ?: "🏁",
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "R${race?.round ?: "1"} · ${com.f1watch.util.F1Localization.getCountry(race?.circuit?.country ?: "F1")}",
                    style = TextStyle(
                        color = ColorProvider(themeColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = com.f1watch.util.F1Localization.getRaceName(race?.raceName ?: "2026 大奖赛"),
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Main Countdown Focus
            Text(
                text = if (isLive) "LIVE 正在进行" else "正赛倒计时",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFA0A0B2)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(1.dp))

            Text(
                text = countdown,
                style = TextStyle(
                    color = ColorProvider(if (isLive) Color(0xFF00D26A) else Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Next Session preview
            Text(
                text = nextSessionText,
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(themeColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

class F1CompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = F1CompactWidget()

    companion object {
        suspend fun updateAll(context: Context) {
            F1CompactWidget().updateAll(context)
        }
    }
}
