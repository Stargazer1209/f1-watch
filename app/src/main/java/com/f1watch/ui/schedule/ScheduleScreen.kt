package com.f1watch.ui.schedule

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import com.f1watch.ui.components.HeroNextRaceCard
import com.f1watch.ui.components.SessionStatusBadge
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

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onSelectRaceDetail: (Race) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading && state.races.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = F1Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Season Full Schedule Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2026 赛季赛历 (共 ${state.races.size} 站)",
                            color = F1TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { viewModel.loadSchedule(forceRefresh = true) },
                            enabled = !state.isRefreshing,
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    color = F1Red,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新",
                                    tint = F1TextSecondary
                                )
                            }
                        }
                    }
                }

                // All Races
                items(state.races, key = { it.round }) { race ->
                    val isExpanded = state.expandedRound == race.round
                    RaceCardItem(
                        race = race,
                        isExpanded = isExpanded,
                        onToggleExpand = { viewModel.toggleExpand(race.round) },
                        onSyncCalendar = { viewModel.syncRaceToCalendar(race) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun RaceCardItem(
    race: Race,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSyncCalendar: () -> Unit
) {
    val isCompleted = race.isWeekendCompleted()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) F1CardBg.copy(alpha = 0.6f) else F1CardBg
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = race.circuit.flagEmoji,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "R${race.round}",
                                color = F1Red,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            if (race.sprint != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(F1Red.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SPRINT 冲刺周末",
                                        color = F1Red,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = F1Localization.getRaceName(race.raceName),
                            color = if (isCompleted) F1TextSecondary else F1TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = race.date,
                        color = F1TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = F1TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = F1CardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // High-Impact Track Outline Card (Responsive Aspect Ratio with Graceful Fallback)
                    com.f1watch.ui.components.CircuitMapCard(
                        circuitId = race.circuit.circuitId,
                        circuitName = race.circuit.circuitName,
                        locality = race.circuit.locality
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sessions Breakdown
                    race.getAllSessions().forEach { session ->
                        SessionRowItem(session = session)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Calendar button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(F1CardSurface)
                            .clickable { onSyncCalendar() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = F1Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "将该大奖赛写入系统日历",
                            color = F1TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionRowItem(session: SessionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(F1CardSurface.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        when (session.status) {
                            SessionStatus.LIVE -> F1GreenLive
                            SessionStatus.COMPLETED -> F1TextMuted
                            SessionStatus.UPCOMING -> F1Red
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = F1Localization.getSessionName(session.name),
                color = F1TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = session.sessionTime.formatLocalDayTime(),
                color = F1TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            SessionStatusBadge(session.status)
        }
    }
}
