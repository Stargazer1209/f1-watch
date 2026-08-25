package com.f1watch.ui.standings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.f1watch.data.model.ConstructorStanding
import com.f1watch.data.model.DriverStanding
import com.f1watch.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StandingsUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0: Drivers, 1: Constructors
    val driverStandings: List<DriverStanding> = emptyList(),
    val constructorStandings: List<ConstructorStanding> = emptyList()
)

class StandingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = F1Repository(application)

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    init {
        loadStandings()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun loadStandings(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val drivers = repository.getDriverStandings(forceRefresh = forceRefresh)
            val constructors = repository.getConstructorStandings(forceRefresh = forceRefresh)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                driverStandings = drivers,
                constructorStandings = constructors
            )
        }
    }
}
