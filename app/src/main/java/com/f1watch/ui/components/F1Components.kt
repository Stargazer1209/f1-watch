package com.f1watch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import com.f1watch.domain.CountdownResult
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1GreenLive
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun F1TopBar(
    title: String,
    onOpenCustomizer: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(F1Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = F1TextWhite,
                    letterSpacing = (-0.5).sp
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenCustomizer) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "小组件定制",
                    tint = F1TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = F1Dark,
            titleContentColor = F1TextWhite
        )
    )
}

@Composable
fun SessionStatusBadge(status: SessionStatus) {
    val (bgColor, textColor, text) = when (status) {
        SessionStatus.LIVE -> Triple(
            F1GreenLive.copy(alpha = 0.2f),
            F1GreenLive,
            "🟢 LIVE 进行中"
        )
        SessionStatus.COMPLETED -> Triple(
            F1TextMuted.copy(alpha = 0.2f),
            F1TextMuted,
            "已结束"
        )
        SessionStatus.UPCOMING -> Triple(
            F1Red.copy(alpha = 0.2f),
            F1Red,
            "待开始"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeroNextRaceCard(
    race: Race,
    countdown: CountdownResult,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onSyncCalendar: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Round + Flag + Country
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = race.circuit.flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROUND ${race.round}",
                        color = F1Red,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                SessionStatusBadge(
                    if (countdown.isLive) SessionStatus.LIVE else SessionStatus.UPCOMING
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = com.f1watch.util.F1Localization.getRaceName(race.raceName),
                fontWeight = FontWeight.Black,
                color = F1TextWhite,
                fontSize = 20.sp
            )

            Text(
                text = "📍 ${com.f1watch.util.F1Localization.getCircuitName(race.circuit.circuitName)} · ${com.f1watch.util.F1Localization.getLocality(race.circuit.locality)}",
                color = F1TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(F1CardSurface)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (countdown.isLive) "当前比赛状态" else "正赛倒计时",
                        color = F1TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = countdown.formattedCompact,
                        color = if (countdown.isLive) F1GreenLive else F1TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = countdown.formattedLong,
                        color = F1TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1CardSurface)
                        .clickable { onSyncCalendar() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = F1Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "一键加日历",
                        color = F1TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) F1Red.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleExpand() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isExpanded) "收起赛程" else "查看全周末赛程",
                        color = F1Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = F1Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Sessions List directly inside Hero Card!
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // High-Impact Track Outline Card (Responsive Aspect Ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.0f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(F1Dark)
                            .border(1.dp, F1CardBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = com.f1watch.ui.util.F1MediaHelper.getCircuitMapUrl(race.circuit.circuitId),
                            contentDescription = race.circuit.circuitName,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 28.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        // Location badge top-left
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(6.dp))
                                .background(F1CardSurface.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏁 ${com.f1watch.util.F1Localization.getCircuitName(race.circuit.circuitName)}",
                                color = F1TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "· ${com.f1watch.util.F1Localization.getLocality(race.circuit.locality)}",
                                color = F1TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "本站完整周末时间表 (已转为本地时间)",
                        color = F1TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    race.getAllSessions().forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(F1CardSurface.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (session.status == SessionStatus.LIVE) F1GreenLive else F1Red)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.f1watch.util.F1Localization.getSessionName(session.name),
                                    color = F1TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.sessionTime.formatLocalDayTime(),
                                    color = F1TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SessionStatusBadge(status = session.status)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
