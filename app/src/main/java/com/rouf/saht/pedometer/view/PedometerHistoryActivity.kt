package com.rouf.saht.pedometer.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rouf.saht.R
import com.rouf.saht.databinding.ActivityHeartRateHistoryBinding
import com.rouf.saht.databinding.ActivityPedometerHisotryBinding
import com.rouf.saht.heartRate.view.HeartRateAdapter
import com.rouf.saht.heartRate.view.HeartRateHistoryActivity
import com.rouf.saht.heartRate.viewModel.HeartRateViewModel
import com.rouf.saht.pedometer.viewModel.PedometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PedometerHistoryActivity : AppCompatActivity() {

    private val TAG: String = PedometerHistoryActivity::class.java.simpleName
    private lateinit var binding: ActivityPedometerHisotryBinding
    private lateinit var pedometerHistoryAdapter: PedometerHistoryAdapter
    private lateinit var pedometerViewModel: PedometerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPedometerHisotryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedometerViewModel = ViewModelProvider(this@PedometerHistoryActivity)[PedometerViewModel::class.java]

        getPedometerData()
        observer()
    }

    private fun observer() {
//        pedometerViewModel.heartRateMonitorData.observe(this@PedometerHistoryActivity) { heartRateList ->
//            if (heartRateList.isNotEmpty()) {
//                binding.llEmptyView.isVisible = false
//                heartRateAdapter.submitList(heartRateList)
//            } else {
//                binding.llEmptyView.isVisible = true
//            }
//        }
    }

    private fun getPedometerData() {
        lifecycleScope.launch {
            val pedometerList = pedometerViewModel.getPedometerListFromDB()

            pedometerHistoryAdapter = PedometerHistoryAdapter(this@PedometerHistoryActivity)
            binding.rvPedometer.layoutManager = LinearLayoutManager(this@PedometerHistoryActivity)
            binding.rvPedometer.adapter = pedometerHistoryAdapter

            if (pedometerList?.isNotEmpty() == true) {
                binding.llEmptyView.isVisible = false
                pedometerHistoryAdapter.submitList(pedometerList)
            } else {
                binding.llEmptyView.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPedometerData()
    }
}