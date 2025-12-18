package com.example.smartmemoryapp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmemoryapp.R
import com.example.smartmemoryapp.SmartMemoryApp
import com.example.smartmemoryapp.services.AlarmService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUEST_CODE_AUDIO_PICKER = 102

    private lateinit var voiceButton: Button
    private lateinit var taskEditText: EditText
    private lateinit var alarmTimeTextView: TextView
    private lateinit var relativeButtonsLayout: LinearLayout
    private lateinit var customAlarmButton: Button
    private lateinit var audioButton: Button
    private lateinit var audioNameTextView: TextView
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var playPauseButton: Button
    private lateinit var saveButton: Button
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var mediaPlayer: MediaPlayer

    private var currentAudioUri: Uri? = null
    private var currentAudioFile: File? = null
    private var isPlaying = false
    private var tasks: MutableList<Task> = mutableListOf()

    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value == true }
            if (!allGranted) {
                Toast.makeText(this, "Permissions required for recording and storage", Toast.LENGTH_SHORT).show()
            }
        }

    private val audioPickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentAudioUri = result.data?.data
                currentAudioFile = getAudioFileFromUri(currentAudioUri)
                if (currentAudioFile != null) {
                    audioNameTextView.text = currentAudioFile?.name
                    Toast.makeText(this, "Audio selected: ${currentAudioFile?.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupPermissions()
        setupAudioPlayer()
        loadTasks()
        setupRecyclerView()
    }

    private fun initViews() {
        voiceButton = findViewById(R.id.voiceButton)
        taskEditText = findViewById(R.id.taskEditText)
        alarmTimeTextView = findViewById(R.id.alarmTimeTextView)
        relativeButtonsLayout = findViewById(R.id.relativeButtonsLayout)
        customAlarmButton = findViewById(R.id.customAlarmButton)
        audioButton = findViewById(R.id.audioButton)
        audioNameTextView = findViewById(R.id.audioNameTextView)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        playPauseButton = findViewById(R.id.playPauseButton)
        saveButton = findViewById(R.id.saveButton)
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView)

        voiceButton.setOnClickListener { startVoiceRecognition() }
        customAlarmButton.setOnClickListener { openDateTimePicker() }
        audioButton.setOnClickListener { openAudioPicker() }
        playPauseButton.setOnClickListener { toggleAudioPlayback() }
        saveButton.setOnClickListener { saveTask() }

        volumeSeekBar.max = 100
        volumeSeekBar.progress = 50
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.setVolume(progress / 100f, progress / 100f)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        alarmTimeTextView.text = "No alarm set"
        audioNameTextView.text = "No custom audio"
    }

    private fun setupPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val allGranted = permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (!allGranted) {
            permissionLauncher.launch(permissions)
        }
    }

    private fun setupAudioPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            playPauseButton.text = "Play"
        }
    }

    private fun startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDateTimePicker() {
        val intent = Intent(this, DateTimePickerActivity::class.java)
        dateTimePickerLauncher.launch(intent)
    }

    private val dateTimePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val timestamp = result.data?.getLongExtra("selected_time", 0) ?: 0
                if (timestamp > 0) {
                    alarmTimeTextView.text = "Alarm set for: ${formatDateTime(timestamp)}"
                    Toast.makeText(this, "Alarm time set", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        audioPickerLauncher.launch(intent)
    }

    private fun toggleAudioPlayback() {
        if (currentAudioFile == null) {
            Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            playPauseButton.text = "Play"
        } else {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(currentAudioFile?.absolutePath)
                mediaPlayer.prepare()
                mediaPlayer.setVolume(volumeSeekBar.progress / 100f, volumeSeekBar.progress / 100f)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
                isPlaying = true
                playPauseButton.text = "Pause"
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTask() {
        val taskText = taskEditText.text.toString().trim()
        if (taskText.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            return
        }

        val alarmTime = getSelectedAlarmTime()
        if (alarmTime == 0L) {
            Toast.makeText(this, "Please set an alarm time", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = System.currentTimeMillis(),
            text = taskText,
            alarmTime = alarmTime,
            audioPath = currentAudioFile?.absolutePath,
            volume = volumeSeekBar.progress
        )

        tasks.add(task)
        saveTasks()
        adapter.notifyDataSetChanged()
        AlarmService.scheduleAlarm(this, task)

        // Clear form
        taskEditText.text.clear()
        alarmTimeTextView.text = "No alarm set"
        audioNameTextView.text = "No custom audio"
        currentAudioUri = null
        currentAudioFile = null
        isPlaying = false
        mediaPlayer.reset()
        playPauseButton.text = "Play"

        Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun getSelectedAlarmTime(): Long {
        // This should be implemented to get the selected time from UI
        // For now, return a default value
        return System.currentTimeMillis() + 60000 // 1 minute from now
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun getAudioFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                if (columnIndex != -1) {
                    return File(it.getString(columnIndex))
                }
            }
        }
        return null
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(tasks, { task -> editTask(task) }, { task -> deleteTask(task) })
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = adapter
    }

    private fun editTask(task: Task) {
        // Implement edit task logic
        Toast.makeText(this, "Edit task: ${task.text}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTask(task: Task) {
        tasks.remove(task)
        saveTasks()
        adapter.notifyDataSetChanged()
        AlarmService.cancelAlarm(this, task.id)
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
    }

    private fun loadTasks() {
        // Load tasks from SharedPreferences or database
        // For now, initialize empty list
        tasks.clear()
    }

    private fun saveTasks() {
        // Save tasks to SharedPreferences or database
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    taskEditText.text = result?.get(0)?.let { android.text.Editable.Factory.getInstance().newEditable(it) }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    data class Task(
        val id: Long,
        val text: String,
        val alarmTime: Long,
        val audioPath: String?,
        val volume: Int
    )
}
