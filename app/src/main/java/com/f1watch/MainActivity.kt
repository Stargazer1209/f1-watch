package com.f1watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.local.PreferencesManager
import com.f1watch.ui.components.F1TopBar
import com.f1watch.ui.results.ResultsScreen
import com.f1watch.ui.results.ResultsViewModel
import com.f1watch.ui.schedule.ScheduleScreen
import com.f1watch.ui.schedule.ScheduleViewModel
import com.f1watch.ui.settings.WidgetCustomizerSheet
import com.f1watch.ui.standings.StandingsScreen
import com.f1watch.ui.standings.StandingsViewModel
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextWhite
import com.f1watch.ui.theme.F1WatchTheme

class MainActivity : ComponentActivity() {

    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val resultsViewModel: ResultsViewModel by viewModels()
    private val standingsViewModel: StandingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(this)

        setContent {
            F1WatchTheme {
                MainAppScreen(
                    preferencesManager = preferencesManager,
                    scheduleViewModel = scheduleViewModel,
                    resultsViewModel = resultsViewModel,
                    standingsViewModel = standingsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    preferencesManager: PreferencesManager,
    scheduleViewModel: ScheduleViewModel,
    resultsViewModel: ResultsViewModel,
    standingsViewModel: StandingsViewModel
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showCustomizer by remember { mutableStateOf(false) }

    val scheduleState by scheduleViewModel.uiState.collectAsState()

    val tabTitles = listOf("2026 赛历", "比赛结果", "车手与车队榜")

    Scaffold(
        topBar = {
            F1TopBar(
                title = tabTitles[currentTab],
                onOpenCustomizer = { showCustomizer = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = F1CardBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "赛历") },
                    label = {
                        Text(
                            text = "赛历",
                            fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1Red,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red.copy(alpha = 0.15f),
                        unselectedIconColor = F1TextMuted,
                        unselectedTextColor = F1TextMuted
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "比赛结果") },
                    label = {
                        Text(
                            text = "赛果",
                            fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1Red,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red.copy(alpha = 0.15f),
                        unselectedIconColor = F1TextMuted,
                        unselectedTextColor = F1TextMuted
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "车手与车队") },
                    label = {
                        Text(
                            text = "积分榜",
                            fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1Red,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red.copy(alpha = 0.15f),
                        unselectedIconColor = F1TextMuted,
                        unselectedTextColor = F1TextMuted
                    )
                )
            }
        },
        containerColor = F1Dark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(F1Dark)
        ) {
            when (currentTab) {
                0 -> ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onSelectRaceDetail = { /* Handled via expandable cards */ }
                )
                1 -> ResultsScreen(
                    viewModel = resultsViewModel
                )
                2 -> StandingsScreen(
                    viewModel = standingsViewModel
                )
            }

            if (showCustomizer) {
                WidgetCustomizerSheet(
                    preferencesManager = preferencesManager,
                    nextRace = scheduleState.nextRace,
                    onDismiss = { showCustomizer = false }
                )
            }
        }
    }
}
