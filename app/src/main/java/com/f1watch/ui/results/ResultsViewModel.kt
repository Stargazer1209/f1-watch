package com.f1watch.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.f1watch.data.model.Race
import com.f1watch.data.model.RaceResult
import com.f1watch.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val races: List<Race> = emptyList(),
    val selectedRound: String = "1",
    val results: List<RaceResult> = emptyList(),
    val toastMessage: String? = null
)

class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = F1Repository(application)

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val races = repository.getRaces()
            val completedRaces = races.filter { it.isWeekendCompleted() }
            val defaultRound = completedRaces.lastOrNull()?.round ?: "1"

            _uiState.value = _uiState.value.copy(
                races = races,
                selectedRound = defaultRound
            )
            loadRoundResults(defaultRound, forceRefresh = false, isInitial = true)
        }
    }

    fun selectRound(round: String) {
        _uiState.value = _uiState.value.copy(selectedRound = round)
        loadRoundResults(round, forceRefresh = false, isInitial = false)
    }

    fun refreshCurrentRound() {
        loadRoundResults(_uiState.value.selectedRound, forceRefresh = true, isInitial = false)
    }

    fun loadRoundResults(round: String, forceRefresh: Boolean = false, isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            } else if (forceRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            }

            val (results, syncedOnline) = repository.getRaceResultsWithSyncStatus(round, forceRefresh = forceRefresh)
            
            val toast = if (forceRefresh) {
                if (syncedOnline) "第 $round 站成绩已成功从官方 API 刷新"
                else if (results.isNotEmpty()) "网络未连接或较慢，已展示第 $round 站本地完赛成绩"
                else "第 $round 站尚未开赛或暂无完赛数据"
            } else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                results = results,
                toastMessage = toast
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
