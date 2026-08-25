package com.f1watch.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.f1watch.data.model.Race
import com.f1watch.data.repository.F1Repository
import com.f1watch.domain.CalendarSyncManager
import com.f1watch.domain.CountdownCalculator
import com.f1watch.domain.CountdownResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val races: List<Race> = emptyList(),
    val nextRace: Race? = null,
    val countdown: CountdownResult = CountdownCalculator.calculate(null),
    val expandedRound: String? = null,
    val isHeroExpanded: Boolean = false,
    val toastMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = F1Repository(application)

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule(isInitial = true)
        startCountdownTicker()
    }

    fun loadSchedule(forceRefresh: Boolean = false, isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            } else {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            }

            val (races, syncedOnline) = repository.getRacesWithSyncStatus(forceRefresh = forceRefresh)
            val next = repository.getNextOrCurrentRace()
            val countdown = CountdownCalculator.calculate(next)

            val toast = if (forceRefresh) {
                if (syncedOnline) "赛历已从官方 API 刷新成功 (共 ${races.size} 站)"
                else "网络未连接或较慢，已展示本地赛程 (共 ${races.size} 站)"
            } else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                races = races,
                nextRace = next,
                countdown = countdown,
                toastMessage = toast
            )
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                val next = _uiState.value.nextRace
                val countdown = CountdownCalculator.calculate(next)
                _uiState.value = _uiState.value.copy(countdown = countdown)

                val dynamicDelay = when {
                    countdown.days >= 1 -> 60_000L // 1 minute
                    countdown.hours >= 1 -> 30_000L // 30 seconds
                    countdown.minutes >= 1 -> 10_000L // 10 seconds
                    else -> 1_000L // 1 second
                }
                delay(dynamicDelay)
            }
        }
    }

    fun toggleHeroExpand() {
        _uiState.value = _uiState.value.copy(
            isHeroExpanded = !_uiState.value.isHeroExpanded
        )
    }

    fun toggleExpand(round: String) {
        val current = _uiState.value.expandedRound
        _uiState.value = _uiState.value.copy(
            expandedRound = if (current == round) null else round
        )
    }

    fun syncRaceToCalendar(race: Race) {
        val context = getApplication<Application>()
        val result = CalendarSyncManager.syncAllSessionsDirectly(context, race)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                toastMessage = "已将 ${race.raceName} 的全部赛程写入系统日历！"
            )
        } else {
            // Open via Intent fallback
            val mainSession = race.getAllSessions().lastOrNull()
            if (mainSession != null) {
                CalendarSyncManager.openCalendarInsertIntent(context, race, mainSession)
            }
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
