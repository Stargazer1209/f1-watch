package com.f1watch.ui.standings

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.model.ConstructorStanding
import com.f1watch.data.model.DriverStanding
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite
import com.f1watch.ui.theme.PodiumBronze
import com.f1watch.ui.theme.PodiumGold
import com.f1watch.ui.theme.PodiumSilver
import com.f1watch.ui.theme.TeamColors

@Composable
fun StandingsScreen(
    viewModel: StandingsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = F1Dark,
            contentColor = F1TextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = F1Red,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = {
                    Text(
                        text = "车手积分榜",
                        fontWeight = if (state.selectedTab == 0) FontWeight.Black else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (state.selectedTab == 0) F1TextWhite else F1TextMuted
                    )
                }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = {
                    Text(
                        text = "车队积分榜",
                        fontWeight = if (state.selectedTab == 1) FontWeight.Black else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (state.selectedTab == 1) F1TextWhite else F1TextMuted
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.selectedTab == 0) "2026 车手世界冠军积分排名" else "2026 车队世界冠军积分排名",
                color = F1TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { viewModel.loadStandings(forceRefresh = true) },
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

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = F1Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (state.selectedTab == 0) {
                    if (state.driverStandings.isEmpty()) {
                        item {
                            EmptyStandingsNotice(title = "赛季初积分榜即将更新")
                        }
                    } else {
                        items(state.driverStandings) { driverStanding ->
                            DriverStandingRow(standing = driverStanding)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                } else {
                    if (state.constructorStandings.isEmpty()) {
                        item {
                            EmptyStandingsNotice(title = "赛季初车队积分榜即将更新")
                        }
                    } else {
                        items(state.constructorStandings) { constructorStanding ->
                            ConstructorStandingRow(standing = constructorStanding)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DriverStandingRow(standing: DriverStanding) {
    val constructorName = standing.constructors.firstOrNull()?.name ?: "Unknown"
    val teamColor = TeamColors.getColorByConstructorName(constructorName)
    val localizedDriverName = com.f1watch.util.F1Localization.getDriverName(standing.driver.fullName)
    val localizedConstructorName = com.f1watch.util.F1Localization.getConstructorName(constructorName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(F1CardBg)
            .border(0.5.dp, F1CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = standing.positionText.ifEmpty { standing.position },
            color = when (standing.position) {
                "1" -> PodiumGold
                "2" -> PodiumSilver
                "3" -> PodiumBronze
                else -> F1TextWhite
            },
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            modifier = Modifier.width(28.dp)
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

        // Driver Headshot Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(F1CardSurface),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = com.f1watch.ui.util.F1MediaHelper.getDriverHeadshotUrl(standing.driver.code.ifEmpty { standing.driver.familyName }),
                contentDescription = localizedDriverName,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Driver details
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
                if (standing.driver.permanentNumber.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${standing.driver.permanentNumber}",
                        color = F1TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Text(
                text = localizedConstructorName,
                color = F1TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Points & Wins
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${standing.points} PTS",
                color = F1TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            if (standing.wins.toIntOrNull() ?: 0 > 0) {
                Text(
                    text = "${standing.wins} 胜场",
                    color = PodiumGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConstructorStandingRow(standing: ConstructorStanding) {
    val teamColor = TeamColors.getColorByConstructorName(standing.constructor.name)
    val localizedTeamName = com.f1watch.util.F1Localization.getConstructorName(standing.constructor.name)
    val localizedCountry = com.f1watch.util.F1Localization.getCountry(standing.constructor.nationality)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Text(
                    text = standing.positionText.ifEmpty { standing.position },
                    color = when (standing.position) {
                        "1" -> PodiumGold
                        "2" -> PodiumSilver
                        "3" -> PodiumBronze
                        else -> F1TextWhite
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.width(28.dp)
                )

                // Team Color Bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(teamColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Constructor Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizedTeamName,
                        color = F1TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (localizedTeamName != standing.constructor.name) {
                        Text(
                            text = "${standing.constructor.name} · $localizedCountry",
                            color = F1TextSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = localizedCountry,
                            color = F1TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Points & Wins
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${standing.points} PTS",
                        color = F1TextWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    if (standing.wins.toIntOrNull() ?: 0 > 0) {
                        Text(
                            text = "${standing.wins} 胜场",
                            color = PodiumGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Large Panoramic Car Livery Image (Responsive Crop Blank Padding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4.0f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0C12)),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = com.f1watch.ui.util.F1MediaHelper.getCarImageUrl(standing.constructor.constructorId),
                    contentDescription = standing.constructor.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                )
            }
        }
    }
}

@Composable
fun EmptyStandingsNotice(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🏎️",
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = F1TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "正赛完赛后将实时从 Jolpica F1 API 自动抓取更新积分",
                color = F1TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
