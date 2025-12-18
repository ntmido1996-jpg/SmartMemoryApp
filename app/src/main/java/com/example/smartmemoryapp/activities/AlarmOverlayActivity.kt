package com.example.smartmemoryapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmemoryapp.R
import com.example.smartmemoryapp.SmartMemoryApp
import com.example.smartmemoryapp.utils.AlarmUtils

class AlarmOverlayActivity : AppCompatActivity() {
    private val TAG = "AlarmOverlayActivity"
    private lateinit var alarmTitleTextView: TextView
    private lateinit var dismissButton: Button
    private lateinit var snoozeButton: Button
    private lateinit var mediaPlayer: MediaPlayer
    private var taskText = ""
    private var audioPath: String? = null
    private var volume = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_overlay)

        initViews()
        setupAlarm()
        setupClickListeners()
    }

    private fun initViews() {
        alarmTitleTextView = findViewById(R.id.alarmTitleTextView)
        dismissButton = findViewById(R.id.dismissButton)
        snoozeButton = findViewById(R.id.snoozeButton)
    }

    private fun setupAlarm() {
        taskText = intent.getStringExtra("task_text") ?: ""
        audioPath = intent.getStringExtra("audio_path")
        volume = intent.getIntExtra("volume", 50)

        alarmTitleTextView.text = taskText
        playAlarmSound()
    }

    private fun playAlarmSound() {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.start()
            }

            if (audioPath != null && audioPath!!.isNotEmpty()) {
                mediaPlayer.setDataSource(audioPath)
            } else {
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://${packageName}/raw/default_alarm"))
            }

            mediaPlayer.prepare()
            mediaPlayer.setVolume(volume / 100f, volume / 100f)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            Toast.makeText(this, "Error playing alarm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        dismissButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.release()
            finishAndRemoveTask()
        }

        snoozeButton.setOnClickListener {
            showSnoozeOptions()
        }
    }

    private fun showSnoozeOptions() {
        val options = arrayOf("5 minutes", "10 minutes", "15 minutes")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Snooze for:")
        builder.setItems(options) { _, which ->
            val minutes = when (which) {
                0 -> 5
                1 -> 10
                2 -> 15
                else -> 5
            }
            AlarmUtils.snoozeAlarm(this, taskText, audioPath, volume, minutes)
            mediaPlayer.stop()
            mediaPlayer.release()
            finishAndRemoveTask()
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    companion object {
        fun startAlarm(context: Context, taskText: String, audioPath: String?, volume: Int) {
            if (!SmartMemoryApp.hasOverlayPermission(context)) {
                Toast.makeText(context, "Overlay permission required", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(context, AlarmOverlayActivity::class.java).apply {
                putExtra("task_text", taskText)
                putExtra("audio_path", audioPath)
                putExtra("volume", volume)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }
}
