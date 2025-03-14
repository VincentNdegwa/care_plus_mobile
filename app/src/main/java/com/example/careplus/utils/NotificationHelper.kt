package com.example.careplus.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.careplus.MainActivity
import com.example.careplus.R
import com.example.careplus.data.model.Schedule
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.careplus.services.FCMNotification
import com.google.gson.JsonObject

object NotificationHelper {
    private const val CHANNEL_ID = "medication_notifications"
    private const val TAG = "NotificationHelper"
    private const val WORKER_CHANNEL_ID = "medication_worker_channel"
    private const val CHANNEL_NAME = "Medication Reminders"

    fun showMedicationNotification(context: Context, jsonMessage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)

        var wakeLock: PowerManager.WakeLock? = null
        try {
            Log.d(TAG, "Attempting to show notification with message: $jsonMessage")

            val medicationData = Gson().fromJson(jsonMessage, Schedule::class.java)
            if (medicationData == null) {
                Log.e(TAG, "Failed to parse medication data")
                return
            }
            
            // Create activity intent with alarm stop action
            val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("notification_data", jsonMessage)
                putExtra("stop_alarm", true)
                action = "MEDICATION_NOTIFICATION"
            }
            
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                medicationData.id,
                fullScreenIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Format time
            val utcDateTime = LocalDateTime.parse(medicationData.dose_time.replace(" ", "T"))
            val localDateTime = utcDateTime
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
            val formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

            // Build notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Time to take your medication")
                .setContentText("${medicationData.medication.medication_name} - ${medicationData.medication.dosage_quantity} ${medicationData.medication.dosage_strength}")
                .setSubText("Scheduled for $formattedTime")
                .setStyle(NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Time to take your medication")
                    .bigText("${medicationData.medication.medication_name}\n" +
                            "Dose: ${medicationData.medication.dosage_quantity} ${medicationData.medication.dosage_strength}\n" +
                            "Scheduled for $formattedTime"))
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                .setLights(context.getColor(R.color.primary), 3000, 3000)
                .setColor(context.getColor(R.color.primary))
                .setOnlyAlertOnce(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

            // Show notification
            notificationManager.notify(medicationData.id, builder.build())

        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
            e.printStackTrace()
        } finally {
            wakeLock?.release()
        }
    }

    fun showNewDiagnosisNotification(
        context: Context,
        jsonMessage: String,
        notification: FCMNotification
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)

        try {
            Log.d(TAG, "Attempting to show new diagnosis notification with message: $jsonMessage")

            val payload = Gson().fromJson(jsonMessage, JsonObject::class.java)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_data", jsonMessage)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_diagnosis)
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())

        } catch (e: Exception) {
            Log.e(TAG, "Error showing new diagnosis notification", e)
        }
    }

    fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel exists first
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                Log.d(TAG, "Creating new notification channel: $CHANNEL_ID")
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for medication schedules"
                    enableLights(true)
                    lightColor = context.getColor(R.color.primary)
                    enableVibration(true)
                    setShowBadge(true)
                    setBypassDnd(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    importance = NotificationManager.IMPORTANCE_HIGH
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            } else {
                Log.d(TAG, "Notification channel already exists")
            }
        }
    }
} 