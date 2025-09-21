package com.halitbarut.vakit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halitbarut.vakit.domain.repository.PrayerRepository
import com.halitbarut.vakit.navigation.VakitDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshStartDestination()
    }

    private fun refreshStartDestination() {
        viewModelScope.launch {
            val hasExistingStats = prayerRepository.getPrayerStats() != null
            _uiState.value = MainUiState(
                isLoading = false,
                startDestination = if (hasExistingStats) {
                    VakitDestination.Dashboard.route
                } else {
                    VakitDestination.Onboarding.route
                },
            )
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: String = VakitDestination.Onboarding.route,
)
