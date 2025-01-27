package com.rouf.saht.pedometer.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rouf.saht.R
import com.rouf.saht.common.helper.BMIUtils
import com.rouf.saht.databinding.FragmentPedometerBinding
import com.rouf.saht.common.helper.Util
import com.rouf.saht.common.model.PersonalInformation
import com.rouf.saht.pedometer.service.PedometerForegroundService
import com.rouf.saht.pedometer.viewModel.PedometerViewModel
import com.rouf.saht.setting.SettingsViewModel
import com.rouf.saht.setting.view.PersonalInformationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PedometerFragment : Fragment() {

    private val TAG = PedometerFragment::class.java.simpleName
    private lateinit var _binding: FragmentPedometerBinding
    private val binding get() = _binding!!

    private val ACTIVITY_RECOGNITION_PERMISSION_CODE = 123
    private val REQUEST_NOTIFICATION_PERMISSION: Int = 1

    private lateinit var pedometerViewModel: PedometerViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private var activeState: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPedometerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setRetainInstance(true)

        pedometerViewModel = ViewModelProvider(this@PedometerFragment)[PedometerViewModel::class.java]
        settingsViewModel = ViewModelProvider(this@PedometerFragment)[SettingsViewModel::class.java]

        activeState = pedometerViewModel.getActiveState()

        initViews()
        onClick()
        observers()

        return root
    }

    private fun initViews() {
        val stateActive = pedometerViewModel.getActiveState()
        val btnStartStop = binding.btnStartStop

        if (stateActive) {
            initViewActiveState()
        } else {
            initViewInActiveState()
        }

        setPedometerGoal()
    }

    private fun setBMI(personalInformation: PersonalInformation) {
        val heightStr = personalInformation.height
        val weightStr = personalInformation.weight

        // Validate height and weight
        if (heightStr.isNullOrEmpty() || weightStr.isNullOrEmpty()) {
            binding.tvBmi.text = getString(R.string.invalid_data) // Use a string resource for localization
            binding.tvBmi.setTextColor(requireContext().getColor(R.color.red_500)) // Set error color
            return
        }

        try {
            // Parse height and weight
            val height = heightStr.toDoubleOrNull()?.let { BMIUtils.cmToMeter(it) }
            val weight = weightStr.toDoubleOrNull()

            if (height == null || weight == null || height <= 0 || weight <= 0) {
                throw IllegalArgumentException("Invalid height or weight values")
            }

            // Calculate BMI
            val bmi = BMIUtils.calculateBMI(weight, height)
            val formattedBmi = String.format("%.1f", bmi)
            val bmiCategory = BMIUtils.getBMICategory(bmi)
            val categoryColor = BMIUtils.getCategoryColor(requireContext(), bmi)

            // Update UI
            binding.tvBmi.text = "Your BMI: $formattedBmi | Diagnosis: $bmiCategory"
            binding.tvBmi.setTextColor(categoryColor)
        } catch (e: Exception) {
            // Handle invalid data gracefully
            binding.tvBmi.text = getString(R.string.invalid_data)
            binding.tvBmi.setTextColor(requireContext().getColor(R.color.red_500))
        }
    }


    private fun setPedometerGoal() {
        val pedometerGoal = pedometerViewModel.pedometerGoal.value.toString()
        binding.tvGoalSteps.text = "/$pedometerGoal Steps"
    }

    override fun onResume() {
        super.onResume()
        setPedometerGoal()
        lifecycleScope.launch {
            settingsViewModel.getPersonalInformation()
            settingsViewModel.getPedometerSettings()
        }
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClick() {
        binding.btnStartStop.setOnClickListener {
            Toast.makeText(activity, "Long tap to perform operation", Toast.LENGTH_SHORT).show()
        }

        binding.btnReset.setOnClickListener {
            Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        binding.btnStartStop.setOnLongClickListener {
            if (isActivityRecognitionPermissionGranted()) {
                if (isNotificationPermissionGranted()) {
                    togglePedometer()
                } else {
                    requestActivityNotificationPermission()
                }
            } else {
                requestActivityRecognitionPermission()
            }
            return@setOnLongClickListener true
        }

        binding.btnReset.setOnLongClickListener {
            resetStepsState()
            stopForegroundService()
            initViewInActiveState()
            return@setOnLongClickListener true
        }

        binding.ivHistory.setOnLongClickListener {
//            loadData()
            return@setOnLongClickListener true
        }

        binding.tvBmi.setOnClickListener {
            val intent = Intent(activity, PersonalInformationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun togglePedometer() {
        val toggleText = binding.btnStartStop.text.toString()

        if (toggleText == "Start") {
            initViewActiveState()
            initiateForegroundService()
        } else {
            initViewInActiveState()
            stopForegroundService()
        }
    }



    private fun initViewInActiveState() {
        val btnStartStop = binding.btnStartStop

        btnStartStop.text = getString(R.string.start)
        btnStartStop.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_cornered_solid_red)
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_500)

        // setting bg_circular color
        val shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular) as GradientDrawable
        val strokeColor = ContextCompat.getColor(requireContext(), R.color.dark_grey)
        shapeDrawable.setStroke(8, strokeColor)
        binding.tvStepsTaken.background = shapeDrawable
    }

    private fun initViewActiveState() {
        val btnStartStop = binding.btnStartStop

        btnStartStop.text = getString(R.string.stop)
        btnStartStop.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_cornered_solid_red)
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_500)

        // setting bg_circular color
        val shapeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular) as GradientDrawable
        val strokeColor = ContextCompat.getColor(requireContext(), R.color.green_500)
        shapeDrawable.setStroke(8, strokeColor)
        binding.tvStepsTaken.background = shapeDrawable
    }


    private fun observers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pedometerViewModel.steps.collect { steps ->
                    updateStepCount(steps)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pedometerViewModel.calories.collect { caloriesBurnt->
                    updateCalorieCount(caloriesBurnt)
                }
            }
        }

        settingsViewModel.personalInformation.observe(viewLifecycleOwner){ personalInformation ->
            setBMI(personalInformation)
        }

        settingsViewModel.pedometerSettings.observe(viewLifecycleOwner){ pedometerSettings ->
            binding.tvGoalSteps.text = String.format("/ " + pedometerSettings.stepGoal.toString())
        }

    }

    private fun updateCalorieCount(caloriesBurnt: Double) {
        binding.tvCalories.text = "Calories \uD83D\uDD25: ${Util.roundToTwoDecimalPlaces(caloriesBurnt.toFloat())}"
    }

    private fun updateStepCount(stepCount: Int) {
        binding.tvStepsTaken.text = stepCount.toString()
    }

    private fun initiateForegroundService() {
        val startIntent = Intent(requireContext(), PedometerForegroundService::class.java)
        ContextCompat.startForegroundService(requireContext(), startIntent)
    }

    private fun stopForegroundService() {
        val stopIntent = Intent(requireContext(), PedometerForegroundService::class.java)
        requireContext().stopService(stopIntent)
    }

    private fun isActivityRecognitionPermissionGranted(): Boolean {
            return ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_PERMISSION_CODE
            )
        }
    }

    private fun requestActivityNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    private fun resetStepsState() {
        lifecycleScope.launch(Dispatchers.IO) {
            pedometerViewModel.resetData()
        }
    }

    private fun saveData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
//        editor.putFloat("key1", pedometerViewModel.stepCount.value?.toFloat() ?: 0f)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
    }
}
