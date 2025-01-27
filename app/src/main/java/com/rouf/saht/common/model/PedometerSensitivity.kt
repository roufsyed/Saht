package com.rouf.saht.common.model

enum class PedometerSensitivity(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    companion object {
        fun fromValue(value: Int): PedometerSensitivity? {
            return values().find { it.value == value }
        }
    }
}