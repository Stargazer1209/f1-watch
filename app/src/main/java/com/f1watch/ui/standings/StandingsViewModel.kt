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
    val isRefreshing: Boolean = false,
    val selectedTab: Int = 0, // 0: Drivers, 1: Constructors
    val driverStandings: List<DriverStanding> = emptyList(),
    val constructorStandings: List<ConstructorStanding> = emptyList(),
    val toastMessage: String? = null
)

class StandingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = F1Repository(application)

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    init {
        loadStandings(isInitial = true)
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun loadStandings(forceRefresh: Boolean = false, isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            } else if (forceRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            }

            val (drivers, driversSynced) = repository.getDriverStandingsWithSyncStatus(forceRefresh = forceRefresh)
            val (constructors, constructorsSynced) = repository.getConstructorStandingsWithSyncStatus(forceRefresh = forceRefresh)

            val syncedOnline = driversSynced || constructorsSynced
            val toast = if (forceRefresh) {
                if (syncedOnline) "积分榜已从官方 API 同步成功"
                else "网络未连接或较慢，已展示本地最新积分榜"
            } else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                driverStandings = drivers,
                constructorStandings = constructors,
                toastMessage = toast
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
