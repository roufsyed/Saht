package com.rouf.saht.common.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.rouf.saht.R

@RequiresApi(Build.VERSION_CODES.O)
class NotificationHelper(private val context: Context) {
    init {
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Channel for foreground service notifications"
        manager.createNotificationChannel(channel)
    }

    fun getServiceNotification(stepsData: String?, calorieData: String?): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("$stepsData    $calorieData")
            .setSubText("Your Health App")
            .setSmallIcon(R.drawable.ic_heart)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val CHANNEL_NAME = "Foreground Service"
    }
}