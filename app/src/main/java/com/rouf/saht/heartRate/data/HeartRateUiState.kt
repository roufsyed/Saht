package com.rouf.saht.heartRate.data

import androidx.annotation.Keep

@Keep
sealed class HeartRateUiState {
    data object Initial : HeartRateUiState()
    data object Loading : HeartRateUiState()
    data class Success(val data: HeartRateData) : HeartRateUiState()
    data class Error(val message: String) : HeartRateUiState()
}