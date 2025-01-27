package com.rouf.saht.heartRate.data

import com.rouf.saht.heartRate.repository.HeartRateRepository

data class HeartRateData(
    val timestamp: Long,
    val bpm: Int,
    val confidence: Float
) {
    fun isReliable() = confidence >= HeartRateRepository.MIN_CONFIDENCE_THRESHOLD
}