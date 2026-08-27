package com.f1watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.local.PreferencesManager
import com.f1watch.ui.components.F1TopBar
import com.f1watch.ui.results.ResultsScreen
import com.f1watch.ui.results.ResultsViewModel
import com.f1watch.ui.schedule.ScheduleScreen
import com.f1watch.ui.schedule.ScheduleViewModel
import com.f1watch.ui.settings.SettingsScreen
import com.f1watch.ui.standings.StandingsScreen
import com.f1watch.ui.standings.StandingsViewModel
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1WatchTheme

import androidx.compose.material.icons.filled.Home
import com.f1watch.ui.home.HomeScreen
import com.f1watch.ui.components.AmbientGlowBackground
import com.f1watch.ui.theme.TeamColors

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
                MainAppContainer(
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
fun MainAppContainer(
    preferencesManager: PreferencesManager,
    scheduleViewModel: ScheduleViewModel,
    resultsViewModel: ResultsViewModel,
    standingsViewModel: StandingsViewModel
) {
    var isSettingsOpen by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) }

    val scheduleState by scheduleViewModel.uiState.collectAsState()
    val currentThemeKey by preferencesManager.widgetThemeFlow.collectAsState(initial = "f1_red")
    val themeColor = remember(currentThemeKey) { TeamColors.getColor(currentThemeKey) }

    val tabTitles = listOf("首页", "赛历", "赛果", "积分榜")

    BackHandler(enabled = isSettingsOpen) {
        isSettingsOpen = false
    }

    AnimatedContent(
        targetState = isSettingsOpen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SettingsNavigation"
    ) { showSettings ->
        if (showSettings) {
            SettingsScreen(
                preferencesManager = preferencesManager,
                nextRace = scheduleState.nextRace,
                onNavigateBack = { isSettingsOpen = false },
                onClearCacheAndResync = {
                    scheduleViewModel.loadSchedule(forceRefresh = true)
                    resultsViewModel.loadData()
                    standingsViewModel.loadStandings(forceRefresh = true)
                }
            )
        } else {
            AmbientGlowBackground(themeColor = themeColor) {
                Scaffold(
                    topBar = {
                        F1TopBar(
                            title = tabTitles[currentTab],
                            onOpenCustomizer = { isSettingsOpen = true }
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
                                icon = { Icon(Icons.Default.Home, contentDescription = "首页大本营") },
                                label = {
                                    Text(
                                        text = "首页",
                                        fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = themeColor,
                                    selectedTextColor = themeColor,
                                    indicatorColor = themeColor.copy(alpha = 0.15f),
                                    unselectedIconColor = F1TextMuted,
                                    unselectedTextColor = F1TextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "赛历") },
                                label = {
                                    Text(
                                        text = "赛历",
                                        fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = themeColor,
                                    selectedTextColor = themeColor,
                                    indicatorColor = themeColor.copy(alpha = 0.15f),
                                    unselectedIconColor = F1TextMuted,
                                    unselectedTextColor = F1TextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "比赛结果") },
                                label = {
                                    Text(
                                        text = "赛果",
                                        fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = themeColor,
                                    selectedTextColor = themeColor,
                                    indicatorColor = themeColor.copy(alpha = 0.15f),
                                    unselectedIconColor = F1TextMuted,
                                    unselectedTextColor = F1TextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "车手与车队") },
                                label = {
                                    Text(
                                        text = "积分榜",
                                        fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = themeColor,
                                    selectedTextColor = themeColor,
                                    indicatorColor = themeColor.copy(alpha = 0.15f),
                                    unselectedIconColor = F1TextMuted,
                                    unselectedTextColor = F1TextMuted
                                )
                            )
                        }
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                scheduleViewModel = scheduleViewModel,
                                themeColor = themeColor,
                                onNavigateToTab = { currentTab = it }
                            )
                            1 -> ScheduleScreen(
                                viewModel = scheduleViewModel,
                                onSelectRaceDetail = { /* Handled via expandable cards */ }
                            )
                            2 -> ResultsScreen(
                                viewModel = resultsViewModel
                            )
                            3 -> StandingsScreen(
                                viewModel = standingsViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
