package com.example.smartmemoryapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartmemoryapp.R
import com.example.smartmemoryapp.activities.AlarmOverlayActivity

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm triggered")

        val taskId = intent.getLongExtra("task_id", 0)
        val taskText = intent.getStringExtra("task_text") ?: ""
        val audioPath = intent.getStringExtra("audio_path")
        val volume = intent.getIntExtra("volume", 50)

        // Show full-screen overlay
        AlarmOverlayActivity.startAlarm(context, taskText, audioPath, volume)

        // Also show notification
        showNotification(context, taskText)
    }

    private fun showNotification(context: Context, taskText: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm")
            .setContentText(taskText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(1, notification)
    }
}
