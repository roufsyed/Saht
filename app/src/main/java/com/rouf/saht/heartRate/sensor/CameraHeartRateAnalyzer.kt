package com.rouf.saht.heartRate.sensor

import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import com.rouf.saht.heartRate.data.HeartRateData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CameraHeartRateAnalyzer {
    private var lastAnalysis = 0L
    private val analysisIntervalMs = 50L // 20fps
    private val redPixelQueue = ArrayDeque<Double>()
    private val timeWindow = 15000L // 15 seconds window for analysis
    private val meanRedValues = mutableListOf<Double>()
    private val timestamps = mutableListOf<Long>()

    @RequiresApi(35)
    fun analyze(image: ImageProxy, onBpmUpdate: (HeartRateData) -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysis < analysisIntervalMs) {
            image.close()
            return
        }

        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining()).also {
            buffer.get(it)
        }
        val pixels = data.map { it.toInt() and 0xFF }
        val redMean = pixels.average()

        redPixelQueue.add(redMean)
        timestamps.add(currentTime)

        // Keep only last 15 seconds of data
        while (timestamps.last() - timestamps.first() > timeWindow) {
            redPixelQueue.removeFirst()
            timestamps.removeFirst()
        }

        if (redPixelQueue.size > 300) { // At least 300 samples (15 seconds at 20fps)
            val bpm = calculateHeartRate(redPixelQueue.toDoubleArray(), timestamps.toLongArray())
            val confidence = calculateConfidence(redPixelQueue.toDoubleArray())

            onBpmUpdate(HeartRateData(
                timestamp = currentTime,
                bpm = bpm.toInt(),
                confidence = confidence
            ))
        }

        lastAnalysis = currentTime
        image.close()
    }

    private fun calculateHeartRate(values: DoubleArray, times: LongArray): Double {
        // Apply bandpass filter (0.5-4Hz, suitable for 30-240 BPM)
        val filtered = bandpassFilter(values, 20.0, 0.5, 4.0)

        // Find peaks
        val peaks = findPeaks(filtered)

        if (peaks.size < 2) return 0.0

        // Calculate average time between peaks
        val peakTimes = peaks.map { times[it] }
        val intervals = peakTimes.zipWithNext { a, b -> b - a }
        val averageInterval = intervals.average()

        // Convert to BPM
        return 60000.0 / averageInterval
    }

    private fun calculateConfidence(values: DoubleArray): Float {
        // Simplified confidence calculation based on signal quality
        val std = standardDeviation(values)
        val normalizedStd = (std / values.average()) * 100

        return when {
            normalizedStd < 1.0 -> 0.0f  // Too stable - probably no signal
            normalizedStd > 20.0 -> 0.0f // Too noisy
            normalizedStd > 10.0 -> 0.5f // Moderate quality
            else -> 1.0f               // Good quality
        }
    }

    private fun standardDeviation(values: DoubleArray): Double {
        val mean = values.average()
        val squaredDifferences = values.map { (it - mean) * (it - mean) }
        return sqrt(squaredDifferences.average())
    }

    // Bandpass Filter Implementation
    private fun bandpassFilter(values: DoubleArray, fps: Double, lowCut: Double, highCut: Double): DoubleArray {
        val nyquist = fps / 2
        val lowNormal = lowCut / nyquist
        val highNormal = highCut / nyquist

        val kernel = createBandpassKernel(30, lowNormal, highNormal)
        return convolve(values, kernel)
    }

    // Function to create a bandpass kernel using a windowed sinc function
    private fun createBandpassKernel(size: Int, lowNormal: Double, highNormal: Double): DoubleArray {
        val kernel = DoubleArray(size)
        val center = size / 2

        for (i in kernel.indices) {
            val x = i - center
            when {
                x == 0 -> kernel[i] = 2 * (highNormal - lowNormal) // Handle division by zero
                else -> {
                    val lowPass = sin(2 * PI * highNormal * x) / (PI * x)
                    val highPass = sin(2 * PI * lowNormal * x) / (PI * x)
                    kernel[i] = lowPass - highPass
                }
            }
            // Apply a Hamming window to reduce spectral leakage
            kernel[i] *= 0.54 - 0.46 * cos(2 * PI * i / size)
        }

        return kernel
    }

    // Function to convolve a signal with a kernel
    private fun convolve(values: DoubleArray, kernel: DoubleArray): DoubleArray {
        val result = DoubleArray(values.size)
        val kernelSize = kernel.size
        val halfKernel = kernelSize / 2

        for (i in values.indices) {
            var sum = 0.0
            for (j in kernel.indices) {
                val index = i + j - halfKernel
                if (index in values.indices) {
                    sum += values[index] * kernel[j]
                }
            }
            result[i] = sum
        }

        return result
    }

    private fun findPeaks(values: DoubleArray): List<Int> {
        val peaks = mutableListOf<Int>()
        for (i in 1 until values.size - 1) {
            if (values[i] > values[i-1] && values[i] > values[i+1]) {
                peaks.add(i)
            }
        }
        return peaks
    }
}