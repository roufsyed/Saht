package com.rouf.saht.common.model

enum class Gender(val value: Int) {
    MALE(0),
    FEMALE(1);

    companion object {
        fun fromValue(value: Int): Gender? {
            return values().find { it.value == value }
        }
    }
}
