package com.rouf.saht.common.helper

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtil {

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timestampToDateTime(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)

        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
        return localDateTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isSameDay(lastKnownTimeStamp: Long, presentTimeStamp: Long): Boolean {
        val zoneId = ZoneId.systemDefault()
        val date1 = Instant.ofEpochMilli(lastKnownTimeStamp).atZone(zoneId).toLocalDate()
        val date2 = Instant.ofEpochMilli(presentTimeStamp).atZone(zoneId).toLocalDate()
        return date1.isEqual(date2)
    }
}
