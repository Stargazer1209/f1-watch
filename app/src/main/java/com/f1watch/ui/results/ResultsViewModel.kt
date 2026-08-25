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
    val races: List<Race> = emptyList(),
    val selectedRound: String = "1",
    val results: List<RaceResult> = emptyList()
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
            loadRoundResults(defaultRound)
        }
    }

    fun selectRound(round: String) {
        _uiState.value = _uiState.value.copy(selectedRound = round)
        loadRoundResults(round)
    }

    fun loadRoundResults(round: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = repository.getRaceResults(round, forceRefresh = forceRefresh)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                results = results
            )
        }
    }
}
