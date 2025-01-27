package com.rouf.saht.pedometer.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensorManager(private val sensorManager: SensorManager) : SensorEventListener {

    private var stepSensor: Sensor? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private val TAG = StepSensorManager::class.java.simpleName

    private var listener: SensorEventListener? = null

    init {
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    fun registerListener(listener: SensorEventListener) {
        this.listener = listener
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG, "registerListener: Success")
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "unregisterListener: Success")

    }

    // Sensor event callback method
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            totalSteps = event.values[0]

            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            Log.d(TAG, "onSensorChanged: Current steps = $currentSteps")
            listener?.onSensorChanged(event)  // Forward the sensor event to the listener
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        listener?.onAccuracyChanged(sensor, accuracy)
    }

    fun resetStepCount() {
        previousTotalSteps = totalSteps
    }
}
