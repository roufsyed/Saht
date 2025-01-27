package com.rouf.saht.common.model

data class PedometerSettings(
    var stepGoal: Int,               // Target step count goal for the user
    val unit: String,                // Measurement unit (e.g., "steps", "kilometers", "miles")
    var sensitivityLevel: PedometerSensitivity,       // Sensitivity of the pedometer (e.g., 1-10)
)
