package com.rouf.saht.pedometer.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rouf.saht.common.helper.CalorieCalculator
import com.rouf.saht.common.helper.NotificationHelper
import com.rouf.saht.common.helper.Util
import com.rouf.saht.common.model.HeartRateMonitorSettings
import com.rouf.saht.common.model.PedometerSettings
import com.rouf.saht.pedometer.repository.PedometerRepository
import com.rouf.saht.setting.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class PedometerForegroundService: Service(), SensorEventListener {

    companion object {
        private const val TAG = "PedometerForegroundService"
        private const val NOTIFICATION_ID = 1

        const val VERY_HIGH_SENSITIVITY = SensorManager.SENSOR_DELAY_FASTEST  // 0 microseconds
        const val HIGH_SENSITIVITY = SensorManager.SENSOR_DELAY_GAME          // 20,000 microseconds
        const val MEDIUM_SENSITIVITY = SensorManager.SENSOR_DELAY_UI          // 60,000 microseconds
        const val LOW_SENSITIVITY = SensorManager.SENSOR_DELAY_NORMAL         // 200,000 microseconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var sensitivity = LOW_SENSITIVITY
    private var listener: SensorEventListener? = null

    private val calorieCalculator = CalorieCalculator(weightKg = 70f, isRunning = false)

    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var stepOffset = 0 // Tracks the steps at reset
    private var isReset = false

    @Inject
    lateinit var pedometerRepository: PedometerRepository
    private lateinit var pedometerSettings: PedometerSettings
    private lateinit var settingsViewModel: SettingsViewModel


    init {
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        Log.d(TAG, "onCreate: \n totalSteps: ${totalSteps} \n prevtotalSteps: ${previousTotalSteps} ")

        settingsViewModel = ViewModelProvider(this@PedometerForegroundService)[SettingsViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO) {
            pedometerSettings = settingsViewModel.pedometerSettings()
        }



        // Start foreground service immediately
        val notificationHelper = NotificationHelper(this)
        val initialNotification = notificationHelper.getServiceNotification("Steps: 0" ,"Calories Burnt: 0.0")
        startForeground(NOTIFICATION_ID, initialNotification)

//        loadData()
        initializeStepSensor()
        setSensitivity(LOW_SENSITIVITY)
    }

    private fun setSensitivity(newSensitivity: Int) {
        sensitivity = newSensitivity
        sensorManager?.unregisterListener(this)
        val registered = sensorManager?.registerListener(this, stepSensor, sensitivity)
        Log.i(TAG, "Changed sensitivity to $sensitivity, registration: $registered")
    }


    private fun initializeStepSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e(TAG, "No step counter sensor available on this device")
            stopSelf()
        } else {
            sensorManager?.registerListener(this, stepSensor, LOW_SENSITIVITY)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            totalSteps = it.values[0]

            if (!isReset) {
                // First run or no reset
                stepOffset = totalSteps.roundToInt() // Set initial offset
                isReset = true // Mark as initialized
            }

            val currentSteps: Int = (totalSteps - stepOffset).roundToInt()

            val caloriesBurned = Util.roundToTwoDecimalPlaces(
                    calorieCalculator.calculateCalories(currentSteps)
                )

            Log.d(TAG, "Steps taken: $currentSteps")

            // Update the notification with current step count
            val notificationHelper = NotificationHelper(this)
            val notification = notificationHelper.getServiceNotification("Steps: $currentSteps", "Calories: $caloriesBurned")

            // Update existing notification instead of calling startForeground again
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

//            saveData() // Save the steps persistently

            serviceScope.launch {
                pedometerRepository.updateSteps(currentSteps)
                pedometerRepository.updateCalories(caloriesBurned)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onDestroy() {
        serviceScope.cancel()
        sensorManager?.unregisterListener(this)
        Log.d(TAG, "resetData: \n totalSteps: ${totalSteps} \n previousTotalSteps: ${previousTotalSteps} ")
        Log.i(TAG, "Service destroyed")

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
//        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//        previousTotalSteps = sharedPreferences.getFloat("key1", 0f)

        previousTotalSteps = pedometerRepository.getSteps()
        Log.i(TAG, "Loaded previous steps: $previousTotalSteps")
    }
}
