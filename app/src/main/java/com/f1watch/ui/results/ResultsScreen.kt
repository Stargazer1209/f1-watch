package com.f1watch.ui.results

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.model.RaceResult
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite
import com.f1watch.ui.theme.FastestLapPurple
import com.f1watch.ui.theme.PodiumBronze
import com.f1watch.ui.theme.PodiumGold
import com.f1watch.ui.theme.PodiumSilver
import com.f1watch.ui.theme.TeamColors

@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        // Round Selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.races) { race ->
                val isSelected = race.round == state.selectedRound
                val isCompleted = race.isWeekendCompleted()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) F1Red else F1CardBg)
                        .border(
                            1.dp,
                            if (isSelected) F1Red else F1CardBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectRound(race.round) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = race.circuit.flagEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "R${race.round}",
                            color = if (isSelected) Color.White else (if (isCompleted) F1TextWhite else F1TextMuted),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        val currentRace = state.races.firstOrNull { it.round == state.selectedRound }
        currentRace?.let { race ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${race.circuit.flagEmoji} ${com.f1watch.util.F1Localization.getRaceName(race.raceName)}",
                        fontWeight = FontWeight.Black,
                        color = F1TextWhite,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "📍 ${com.f1watch.util.F1Localization.getCircuitName(race.circuit.circuitName)} · ${race.date}",
                        color = F1TextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshCurrentRound() },
                    enabled = !state.isRefreshing,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            color = F1Red,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新本站成绩",
                            tint = F1TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = F1Red)
            }
        } else if (state.results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = F1TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "本站比赛暂无完赛数据",
                        color = F1TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "比赛结束后将通过官方 API 自动同步完赛成绩",
                        color = F1TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Podium Section if results >= 3
                if (state.results.size >= 3) {
                    item {
                        PodiumSection(
                            p1 = state.results[0],
                            p2 = state.results[1],
                            p3 = state.results[2]
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Text(
                        text = "全场完赛成绩榜",
                        fontWeight = FontWeight.Bold,
                        color = F1TextWhite,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(state.results) { result ->
                    ResultRowItem(result = result)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PodiumSection(p1: RaceResult, p2: RaceResult, p3: RaceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🏆 领奖台前三名",
                color = F1TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                PodiumStep(result = p2, position = "P2", color = PodiumSilver, height = 90)
                PodiumStep(result = p1, position = "P1", color = PodiumGold, height = 110)
                PodiumStep(result = p3, position = "P3", color = PodiumBronze, height = 80)
            }
        }
    }
}

@Composable
fun PodiumStep(result: RaceResult, position: String, color: Color, height: Int) {
    val teamColor = TeamColors.getColorByConstructorName(result.constructor.name)
    val localizedDriverName = com.f1watch.util.F1Localization.getDriverName(result.driver.fullName)
    val localizedTeamName = com.f1watch.util.F1Localization.getConstructorName(result.constructor.name)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(F1CardSurface)
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = com.f1watch.ui.util.F1MediaHelper.getDriverHeadshotUrl(result.driver.code.ifEmpty { result.driver.familyName }),
                contentDescription = localizedDriverName,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedDriverName,
            color = F1TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = localizedTeamName,
            color = teamColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(F1CardSurface)
                .border(
                    1.dp,
                    color,
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = position,
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "+${result.points} pts",
                    color = F1TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ResultRowItem(result: RaceResult) {
    val teamColor = TeamColors.getColorByConstructorName(result.constructor.name)
    val isFastest = result.fastestLap?.rank == "1"
    val localizedDriverName = com.f1watch.util.F1Localization.getDriverName(result.driver.fullName)
    val localizedTeamName = com.f1watch.util.F1Localization.getConstructorName(result.constructor.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(F1CardBg)
            .border(0.5.dp, F1CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = result.positionText.ifEmpty { result.position },
            color = when (result.position) {
                "1" -> PodiumGold
                "2" -> PodiumSilver
                "3" -> PodiumBronze
                else -> F1TextWhite
            },
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            modifier = Modifier.width(32.dp)
        )

        // Team Color Bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(teamColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Driver Headshot
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(F1CardSurface),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = com.f1watch.ui.util.F1MediaHelper.getDriverHeadshotUrl(result.driver.code.ifEmpty { result.driver.familyName }),
                contentDescription = localizedDriverName,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Driver name & Team
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizedDriverName,
                    color = F1TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isFastest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FastestLapPurple)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FL 最速圈",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Text(
                text = localizedTeamName,
                color = F1TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Time / Gap & Points
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = result.time?.time ?: result.status,
                color = F1TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "+${result.points} pts",
                color = F1Red,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
