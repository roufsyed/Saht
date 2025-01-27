package com.rouf.saht.common.helper

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Util {

    fun roundToTwoDecimalPlaces(value: Float): Double {
        return BigDecimal(value.toString()) // Convert to BigDecimal to handle precision
            .setScale(2, RoundingMode.HALF_UP) // Round to 2 decimal places
            .toDouble() // Convert back to Float
    }

    fun formatWithCommas(value: Int?): String {
        return try {
            if (value is Number) {
                NumberFormat.getNumberInstance(Locale.US).format(value)
            } else {
                throw IllegalArgumentException("Invalid number format: $value")
            }
        } catch (e: Exception) {
            "Invalid Input"
        }
    }

}
