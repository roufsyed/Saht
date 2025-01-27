package com.rouf.saht.heartRate.viewModel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rouf.saht.heartRate.data.HeartRateUiState
import com.rouf.saht.heartRate.repository.MonitorHeartRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    private val monitorHeartRateUseCase: MonitorHeartRateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeartRateUiState>(HeartRateUiState.Initial)
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null

    fun startMonitoring(lifecycleOwner: LifecycleOwner) {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            try {
                _uiState.value = HeartRateUiState.Loading
                monitorHeartRateUseCase(lifecycleOwner).collect { heartRateData ->
                    _uiState.value = HeartRateUiState.Success(heartRateData)
                }
            } catch (e: Exception) {
                _uiState.value = HeartRateUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = HeartRateUiState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
    }
}
