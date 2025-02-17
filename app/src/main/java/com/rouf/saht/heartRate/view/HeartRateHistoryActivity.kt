package com.rouf.saht.heartRate.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rouf.saht.databinding.ActivityHeartRateHistoryBinding
import com.rouf.saht.heartRate.viewModel.HeartRateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeartRateHistoryActivity : AppCompatActivity() {

    private val TAG: String = HeartRateHistoryActivity::class.java.simpleName
    private lateinit var binding: ActivityHeartRateHistoryBinding
    private lateinit var heartRateAdapter: HeartRateAdapter
    private lateinit var heartRateViewModel: HeartRateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHeartRateHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        heartRateViewModel = ViewModelProvider(this@HeartRateHistoryActivity)[HeartRateViewModel::class.java]

        getHeartRateData()
        observer()
    }

    private fun getHeartRateData() {
        lifecycleScope.launch {
            heartRateViewModel.getHeartRateMonitorData()

            heartRateAdapter = HeartRateAdapter(this@HeartRateHistoryActivity)
            binding.rvHeartRate.layoutManager = LinearLayoutManager(this@HeartRateHistoryActivity)
            binding.rvHeartRate.adapter = heartRateAdapter
        }
    }

    private fun observer() {
        heartRateViewModel.heartRateMonitorData.observe(this@HeartRateHistoryActivity) { heartRateList ->
            if (heartRateList.isNotEmpty()) {
                binding.llEmptyView.isVisible = false
                heartRateAdapter.submitList(heartRateList)
            } else {
                binding.llEmptyView.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getHeartRateData()
    }
}