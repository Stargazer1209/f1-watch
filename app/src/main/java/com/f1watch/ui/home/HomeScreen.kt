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
import androidx.compose.ui.graphics.graphicsLayer
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
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading && state.races.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Hero Next Grand Prix Showcase
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

                // 2. 快捷菜单 (3 项功能直达)
                item {
                    QuickMenuHub(
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
 * 破框释放、高视觉张力的大奖赛焦点展台 (上下细线分割，全通透悬浮)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // --- 1. 上方赛事信息栏 (1~2 行精致排布) ---
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

        Spacer(modifier = Modifier.height(6.dp))

        // Grand Prix Title & Location
        Text(
            text = localizedRaceName,
            color = F1TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "📍 $localizedCircuitName · $localizedLocality",
            color = F1TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )

        // 上方浅色细线分割
        HorizontalDivider(
            color = Color(0xFF222234),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- 2. 中央视觉核心：破框悬浮·金属质感与立体微俯视赛道图 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .graphicsLayer {
                    rotationX = 12f
                    cameraDistance = 16f * density
                },
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
                        Text(text = "🏁", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
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

        // 中间浅色细线分割
        HorizontalDivider(
            color = Color(0xFF222234),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- 3. 下方倒计时与操作区 (机械翻牌式倒计时) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (countdown.isLive) "🟢 比赛进行中 (LIVE)" else "距离 ${countdown.targetSessionName} 倒计时",
                color = F1TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 机械翻牌式倒计时板 (Split-Flap)
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
        }

        // Expandable Sessions List
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                HorizontalDivider(color = Color(0xFF222234), thickness = 0.5.dp)
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

        // 底部浅色细线分割 (分隔开快捷菜单)
        HorizontalDivider(
            color = Color(0xFF222234),
            thickness = 0.8.dp,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )
    }
}

/**
 * 首页下方 3 项直达快捷菜单 (赛历 / 积分榜 / 赛果)
 */
@Composable
fun QuickMenuHub(
    themeColor: Color,
    onNavigateToTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Text(
            text = "快捷菜单",
            color = F1TextSecondary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )

        // 上排 2 项：赛历 & 积分榜
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickMenuTile(
                title = "赛历",
                badge = "CAL",
                badgeColor = Color(0xFFE10600),
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )

            QuickMenuTile(
                title = "积分榜",
                badge = "PTS",
                badgeColor = Color(0xFF0090FF),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(3) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 下排 1 项：分站赛果
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
                            .size(34.dp)
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
                    Text(
                        text = "赛果",
                        color = F1TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = F1TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun QuickMenuTile(
    title: String,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = F1TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = F1TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
