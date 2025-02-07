package com.example.careplus.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.careplus.MainActivity
import com.example.careplus.R
import com.google.gson.Gson

object NotificationHelper {
    private const val CHANNEL_ID = "medication_notifications"
    private const val SERVICE_CHANNEL_ID = "pusher_service_channel"
    private const val NOTIFICATION_ID = 1
    private const val TAG = "NotificationHelper"

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create medication notifications channel
                createMedicationChannel(context)
                // Create service channel
                createServiceChannel(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel", e)
        }
    }

    private fun createMedicationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Notifications"
            val descriptionText = "Notifications for medication schedules"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Background Service"
            val descriptionText = "Keeps the app connected to receive notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(SERVICE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicationNotification(context: Context, jsonMessage: String) {
        try {
            Log.d(TAG, "Attempting to show notification with message: $jsonMessage")

            // Parse the JSON message to get meaningful data
            val medicationData = Gson().fromJson(jsonMessage, MedicationNotificationData::class.java)
            val notificationText = "Time to take your medication"

            // Create an explicit intent for MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_data", jsonMessage)
                // Add action to identify notification click
                action = "MEDICATION_NOTIFICATION"
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context, 
                medicationData.id, // Use unique ID for each notification
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Medication Reminder")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_MAX)  // Changed to MAX
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            Log.d(TAG, "About to display notification")
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Notification displayed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private data class MedicationNotificationData(
        val id: Int,
        val medication_id: Int,
        val patient_id: Int,
        val dose_time: String,
        val status: String,
        val medication: Medication
    )

    private data class Medication(
        val medication_name: String
    )
} 