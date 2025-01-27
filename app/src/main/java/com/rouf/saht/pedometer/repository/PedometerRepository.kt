package com.rouf.saht.pedometer.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log

@Singleton
class PedometerRepository @Inject constructor() {

    private val TAG: String? = PedometerRepository::class.java.simpleName

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    suspend fun updateSteps(currentSteps: Int) {
        withContext(Dispatchers.Default) {
            _steps.value = currentSteps
            Log.d("PedometerRepos", "updateSteps: ${steps.value}")
        }
    }

    private val _calories = MutableStateFlow(0.0)
    val calories: StateFlow<Double> = _calories.asStateFlow()

    suspend fun updateCalories(caloriesBurnt: Double) {
        withContext(Dispatchers.Default) {
            _calories.value = caloriesBurnt
            Log.d("PedometerRepos", "update Calories: ${calories.value}")
        }
    }

    suspend fun resetData() {
        _steps.value = 0
        _calories.value = 0.0
        Log.d(TAG, "resetData: \n steps: ${steps.value} \n calories: ${calories.value} ")
    }

    fun getSteps(): Float {
        return steps.value.toFloat()
    }

}