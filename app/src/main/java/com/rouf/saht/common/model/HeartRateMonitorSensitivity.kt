package com.rouf.saht.common.model

enum class HeartRateMonitorSensitivity(val value: Int) {
    LOW(2),
    MEDIUM(3),
    HIGH(5);

    companion object {
        fun fromValue(value: Int): HeartRateMonitorSensitivity? {
            return values().find { it.value == value }
        }
    }
}