package com.rouf.saht.common.helper

class CalorieCalculator(
    private val weightKg: Float,
    private val isRunning: Boolean = false
) {

    companion object {
        private const val WALKING_CALORIES_PER_STEP = 0.04f
        private const val RUNNING_CALORIES_PER_STEP = 0.1f
    }

    fun calculateCalories(steps: Int): Float {
        val caloriesPerStep = if (isRunning) RUNNING_CALORIES_PER_STEP else WALKING_CALORIES_PER_STEP
        return steps * caloriesPerStep * (weightKg / 70)
    }

    fun getCalorieSummary(steps: Int): String {
        val caloriesBurned = calculateCalories(steps)
        val activityType = if (isRunning) "running" else "walking"
        return "You burned approximately %.2f kcal by $activityType $steps steps.".format(caloriesBurned)
    }
}
