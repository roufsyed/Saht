package com.rouf.saht.heartRate.repository

import androidx.lifecycle.LifecycleOwner
import com.rouf.saht.heartRate.data.HeartRateData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MonitorHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(lifecycleOwner: LifecycleOwner): Flow<HeartRateData> {
        repository.startHeartRateMonitoring(lifecycleOwner)
        return repository.getHeartRateData()
    }
}