package com.rouf.saht.common.model

import com.github.mikephil.charting.data.Entry

data class HeartRateMonitorData(
    var duration: Int = 0,
    var unit: String = "",
    var sensitivityLevel: HeartRateMonitorSensitivity = HeartRateMonitorSensitivity.LOW,
    var bpm: Int = 0,
    var bpmGraphEntries: MutableList<Entry> = mutableListOf<Entry>(),
    var timeStamp: Long = System.currentTimeMillis(),
    var activityPerformed: String = "",
    var isResting: Boolean = false
)
