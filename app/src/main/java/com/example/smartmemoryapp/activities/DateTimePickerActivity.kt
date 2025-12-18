package com.example.smartmemoryapp.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmemoryapp.R
import java.util.*

class DateTimePickerActivity : AppCompatActivity() {
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var confirmButton: Button

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = 0
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_time_picker)

        initViews()
        setupClickListeners()
        initDateTime()
    }

    private fun initViews() {
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        confirmButton = findViewById(R.id.confirmButton)
    }

    private fun setupClickListeners() {
        dateTextView.setOnClickListener { showDatePicker() }
        timeTextView.setOnClickListener { showTimePicker() }
        confirmButton.setOnClickListener { confirmSelection() }
    }

    private fun initDateTime() {
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)

        updateDateTimeText()
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth
                updateDateTimeText()
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateDateTimeText()
            },
            selectedHour,
            selectedMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun updateDateTimeText() {
        val dateText = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
        val timeText = String.format("%02d:%02d", selectedHour, selectedMinute)
        dateTextView.text = "Date: $dateText"
        timeTextView.text = "Time: $timeText"
    }

    private fun confirmSelection() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
        val timestamp = calendar.timeInMillis

        if (timestamp < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra("selected_time", timestamp)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
