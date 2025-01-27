package com.rouf.saht.common.helper

import android.content.Context
import androidx.core.content.ContextCompat
import com.rouf.saht.R

object BMIUtils {
    // Convert lbs to kg
    fun lbsToKg(lbs: Double): Double = lbs * 0.453592

    // Convert kg to lbs
    fun kgToLbs(kg: Double): Double = kg / 0.453592

    // Convert cm to feet
    fun cmToFeet(cm: Double): Double = cm / 30.48

    // Convert feet to cm
    fun feetToCm(feet: Double): Double = feet * 30.48

    // Convert cm to meter
    fun cmToMeter(cm: Double): Double {
        return cm / 100
    }

    // Calculate BMI
    fun calculateBMI(weightKg: Double, heightMeters: Double): Double {
        require(weightKg > 0 && heightMeters > 0) { "Weight and height must be positive values." }
        return weightKg / (heightMeters * heightMeters)
    }

    // BMI category
    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight: .. < 18.5"
            bmi in 18.5..24.9 -> "Normal: 18.5 < .. < 24.9"
            bmi in 25.0..29.9 -> "Overweight: 25.0 < .. < 29.9"
            else -> "Obesity"
        }
    }

    // Category based color
    fun getCategoryColor(context: Context, bmi: Double): Int {
        return when {
            bmi < 18.5 -> ContextCompat.getColor(context, R.color.underweight_color) // Underweight
            bmi in 18.5..24.9 -> ContextCompat.getColor(context, R.color.normal_color) // Normal weight
            bmi in 25.0..29.9 -> ContextCompat.getColor(context, R.color.overweight_color) // Overweight
            else -> ContextCompat.getColor(context, R.color.obesity_color) // Obesity
        }
    }
}