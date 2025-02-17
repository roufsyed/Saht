package com.rouf.saht.common.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PedometerData(
    var steps: Int? = null,
    var distanceMeters: Double,
    var caloriesBurned: Double,
    var startTime: Long,
    var endTime: Long,
    var totalExerciseDuration: Long = endTime - startTime,
    var timestamp: Long = System.currentTimeMillis()
) {
    val date: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

    val dayOfWeek: String
        get() = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
}

