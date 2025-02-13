package com.example.careplus.ui.incoming

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.careplus.databinding.ActivityIncomingMedicationBinding
import androidx.work.WorkManager
import android.content.Intent
import com.example.careplus.MainActivity
import com.google.gson.Gson
import com.example.careplus.data.model.Schedule
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class IncomingMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure window to show over lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        binding = ActivityIncomingMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val medicationData = intent.getStringExtra("notification_data")?.let {
            Gson().fromJson(it, Schedule::class.java)
        }

        medicationData?.let { schedule ->
            // Format time
            val utcDateTime = LocalDateTime.parse(schedule.dose_time.replace(" ", "T"))
            val localDateTime = utcDateTime
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
            val formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

            binding.apply {
                scheduledTimeText.text = formattedTime
                medicationNameText.text = schedule.medication.medication_name
                dosageText.text = "${schedule.medication.dosage_quantity} - ${schedule.medication.dosage_strength}"
                frequencyText.text = schedule.medication.frequency
                stockText.text = "Available Stock: ${schedule.medication.stock} units"
                durationText.text = "Duration: ${schedule.medication.duration}"
                statusText.text = "Status: ${schedule.status}"
            }
        }

        binding.takeMedicationButton.setOnClickListener {
            stopAlarmAndNavigate()
        }

    }

    private fun stopAlarmAndNavigate() {
        // Stop alarm
        WorkManager.getInstance(applicationContext)
            .cancelAllWorkByTag("alarm_work")

        // Navigate to main activity with medication reminder fragment
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "MEDICATION_NOTIFICATION"
            putExtra("notification_data", intent.getStringExtra("notification_data"))
            putExtra("stop_alarm", true)
        }
        startActivity(intent)
    }
} 