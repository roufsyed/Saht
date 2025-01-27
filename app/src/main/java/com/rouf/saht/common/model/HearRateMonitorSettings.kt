package com.rouf.saht.common.model

data class HeartRateMonitorSettings(
    var duration: Int,
    val unit: String,
    var sensitivityLevel: HeartRateMonitorSensitivity,
)
