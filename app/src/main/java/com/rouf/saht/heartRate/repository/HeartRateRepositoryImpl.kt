package com.rouf.saht.heartRate.repository

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.rouf.saht.heartRate.data.HeartRateData
import com.rouf.saht.heartRate.exception.CameraException
import com.rouf.saht.heartRate.sensor.CameraHeartRateAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HeartRateRepositoryImpl @Inject constructor(
    private val cameraProvider: ProcessCameraProvider
) : HeartRateRepository {
    private val _heartRateFlow = MutableStateFlow<HeartRateData?>(null)
    private var camera: Camera? = null
    private val analyzer = CameraHeartRateAnalyzer()

    override suspend fun getHeartRateData(): Flow<HeartRateData> = _heartRateFlow
        .filterNotNull()
        .flowOn(Dispatchers.IO)

    @RequiresApi(35)
    override suspend fun startHeartRateMonitoring(lifecycleOwner: LifecycleOwner) {
        withContext(Dispatchers.Main) {
            val preview = Preview.Builder().build()
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(
                        Dispatchers.IO.asExecutor()
                    ) { image ->
                        analyzer.analyze(image) { _heartRateFlow.value = it }
                    }
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                camera?.cameraControl?.enableTorch(true)
            } catch (e: Exception) {
                throw CameraException("Failed to start camera: ${e.message}")
            }
        }
    }

    override suspend fun stopHeartRateMonitoring() {
        camera?.cameraControl?.enableTorch(false)
        cameraProvider.unbindAll()
        camera = null
    }

    override fun isMonitoring(): Boolean = camera != null
}


