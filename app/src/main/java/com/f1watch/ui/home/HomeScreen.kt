package com.f1watch.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionStatus
import com.f1watch.domain.CountdownCalculator
import com.f1watch.domain.CountdownResult
import com.f1watch.ui.components.F1TimingCountdownBoard
import com.f1watch.ui.components.SessionStatusBadge
import com.f1watch.ui.schedule.ScheduleViewModel
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1GreenLive
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite
import com.f1watch.util.F1Localization
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    scheduleViewModel: ScheduleViewModel,
    themeColor: Color,
    onNavigateToTab: (Int) -> Unit
) {
    val state by scheduleViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    // Precision Ticking (Active only in Foreground Composition)
    var tickTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            tickTime = System.currentTimeMillis()
        }
    }

    val liveCountdown = remember(state.nextRace, tickTime) {
        CountdownCalculator.calculate(state.nextRace)
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            scheduleViewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        if (state.isLoading && state.races.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Hero Next Grand Prix Showcase Card
                item {
                    state.nextRace?.let { nextRace ->
                        HeroNextRaceShowcase(
                            race = nextRace,
                            countdown = liveCountdown,
                            themeColor = themeColor,
                            isExpanded = isExpanded,
                            onToggleExpand = { isExpanded = !isExpanded },
                            onSyncCalendar = { scheduleViewModel.syncRaceToCalendar(nextRace) }
                        )
                    }
                }

                // 2. Quick Bento Hub (大本营快捷入口)
                item {
                    PaddockBentoHub(
                        themeColor = themeColor,
                        onNavigateToTab = onNavigateToTab
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

/**
 * 破框释放、高视觉张力的大奖赛焦点看板
 */
@Composable
fun HeroNextRaceShowcase(
    race: Race,
    countdown: CountdownResult,
    themeColor: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSyncCalendar: () -> Unit
) {
    val mapUrl = com.f1watch.ui.util.F1MediaHelper.getCircuitMapUrl(race.circuit.circuitId)
    val localizedRaceName = F1Localization.getRaceName(race.raceName)
    val localizedCircuitName = F1Localization.getCircuitName(race.circuit.circuitName)
    val localizedLocality = F1Localization.getLocality(race.circuit.locality)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Flag + Round + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = race.circuit.flagEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROUND ${race.round}".uppercase(),
                        color = themeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                SessionStatusBadge(status = if (countdown.isLive) SessionStatus.LIVE else SessionStatus.UPCOMING)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grand Prix Title
            Text(
                text = localizedRaceName,
                color = F1TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp
            )

            // Circuit Location Subtitle
            Text(
                text = "📍 $localizedCircuitName · $localizedLocality",
                color = F1TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 破框释放：纯白大尺寸赛道轮廓艺术展台
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D0D14), Color(0xFF13131D))
                        )
                    )
                    .border(1.dp, Color(0xFF222230), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = mapUrl,
                    contentDescription = race.circuit.circuitName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                ) {
                    val state = painter.state
                    if (state is AsyncImagePainter.State.Success) {
                        SubcomposeAsyncImageContent()
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🏁", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = localizedCircuitName,
                                color = F1TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 目标阶段提示
            Text(
                text = if (countdown.isLive) "比赛进行中" else "距离 ${countdown.targetSessionName} 倒计时",
                color = F1TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // F1 精密记分板分块电子倒计时
            F1TimingCountdownBoard(
                countdown = countdown,
                themeColor = themeColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row: Add to Calendar & Expand Timetable Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSyncCalendar,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = F1TextWhite),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(F1CardBorder, F1CardBorder))
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "加日历",
                        tint = themeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "一键加日历", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleExpand() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "收起赛程" else "展开完整赛程",
                        color = themeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Sessions List
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = F1CardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

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
                                        .background(if (session.status == SessionStatus.LIVE) F1GreenLive else themeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = F1Localization.getSessionName(session.name),
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

/**
 * 首页下方 Bento Box 快捷大本营导航矩阵
 */
@Composable
fun PaddockBentoHub(
    themeColor: Color,
    onNavigateToTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "快捷大本营 / PADDOCK HUB",
            color = F1TextSecondary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bento 1: 赛历全览
            BentoNavTile(
                title = "2026 赛历",
                subtitle = "24站全赛季赛程",
                badge = "CAL",
                badgeColor = Color(0xFFE10600),
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )

            // Bento 2: 积分榜
            BentoNavTile(
                title = "世界积分榜",
                subtitle = "车手与车队排位",
                badge = "PTS",
                badgeColor = Color(0xFF0090FF),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(3) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bento 3: 比赛结果与领奖台
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onNavigateToTab(2) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = F1CardBg),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF38EF7D).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RES",
                            color = Color(0xFF38EF7D),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "最新分站赛果与领奖台",
                            color = F1TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "正赛成绩、杆位与最快圈速速览",
                            color = F1TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = F1TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BentoNavTile(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = F1TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = F1TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = F1TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
