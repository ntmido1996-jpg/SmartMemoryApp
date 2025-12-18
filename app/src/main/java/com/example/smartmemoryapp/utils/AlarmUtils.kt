package com.example.smartmemoryapp.utils

import android.content.Context
import android.util.Log
import com.example.smartmemoryapp.services.AlarmService
import java.util.*

class AlarmUtils {
    companion object {
        private const val TAG = "AlarmUtils"

        fun snoozeAlarm(context: Context, taskText: String, audioPath: String?, volume: Int, minutes: Int) {
            Log.d(TAG, "Snoozing alarm for $minutes minutes")

            val snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000L)

            // Create a new task with snoozed time
            val snoozedTask = object : Any() {
                val id: Long = System.currentTimeMillis()
                val text: String = taskText
                val alarmTime: Long = snoozeTime
                val audioPath: String? = audioPath
                val volume: Int = volume
            }

            // Schedule the snoozed alarm
            AlarmService.scheduleAlarm(context, snoozedTask)
        }
    }
}
