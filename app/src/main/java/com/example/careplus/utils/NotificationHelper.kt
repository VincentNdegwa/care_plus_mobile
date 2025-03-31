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
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.careplus.MainActivity
import com.example.careplus.R
import com.example.careplus.data.model.Schedule
import com.example.careplus.services.FCMNotification
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {
    private const val REMINDER_CHANNEL_ID = "medication_reminders"
    private const val NORMAL_CHANNEL_ID = "normal_notifications"
    private const val TAG = "NotificationHelper"
    private const val WORKER_CHANNEL_ID = "medication_worker_channel"
    private const val REMINDER_CHANNEL_NAME = "Medication Reminders"
    private const val NORMAL_CHANNEL_NAME = "General Notifications"

    private fun createNotificationChannels(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete existing channels first to ensure clean setup
//            notificationManager.deleteNotificationChannel(NORMAL_CHANNEL_ID)
//            notificationManager.deleteNotificationChannel(REMINDER_CHANNEL_ID)

            // Create reminder channel with alarm sound
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for medication schedules"
                enableLights(true)
                lightColor = context.getColor(R.color.primary)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            // Create normal channel with same importance as reminder channel
            val normalChannel = NotificationChannel(
                NORMAL_CHANNEL_ID,
                NORMAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // This is crucial for heads-up notifications
            ).apply {
                description = "General notifications"
                enableLights(true)
                lightColor = context.getColor(R.color.primary)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)  // Changed to EVENT for higher priority
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            }

            // Create the channels
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(normalChannel)
        }
    }

    fun showMedicationNotification(context: Context, jsonMessage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels(context, notificationManager)

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
            val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
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

    private fun showNormalNotification(
        context: Context,
        jsonMessage: String,
        notification: FCMNotification,
        notificationType: String,
        flags: Int = Intent.FLAG_ACTIVITY_SINGLE_TOP,
        pendingIntentFlags: Int = PendingIntent.FLAG_IMMUTABLE,
        priority: Int = NotificationCompat.PRIORITY_MAX,
        icon: Int = R.drawable.ic_diagnosis,
        useBigTextStyle: Boolean = true
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels(context, notificationManager)

        try {
            Log.d(TAG, "Attempting to show $notificationType notification with message: $jsonMessage")

            val intent = Intent(context, MainActivity::class.java).apply {
                this.flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("notification_data", jsonMessage)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),  // Use unique ID for each notification
                intent,
                pendingIntentFlags or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, NORMAL_CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)  // Changed to EVENT
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 500, 200, 500))  // Match channel vibration pattern
                .setOngoing(true)

            if (useBigTextStyle) {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            }

            val notificationId = notificationType.hashCode()
            notificationManager.notify(notificationId, builder.build())

        } catch (e: Exception) {
            Log.e(TAG, "Error showing $notificationType notification", e)
        }
    }

    fun showNewMedicationNotification(
        context: Context,
        jsonMessage: String,
        notification: FCMNotification
    ) {
        showNormalNotification(
            context = context,
            jsonMessage = jsonMessage,
            notification = notification,
            notificationType = "new medication",
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP,
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE,
            priority = NotificationCompat.PRIORITY_MAX,
            icon = R.drawable.ic_diagnosis,
            useBigTextStyle = true
        )
    }

    fun showNewDiagnosisNotification(
        context: Context,
        jsonMessage: String,
        notification: FCMNotification
    ) {
        showNormalNotification(
            context = context,
            jsonMessage = jsonMessage,
            notification = notification,
            notificationType = "new diagnosis",
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            priority = NotificationCompat.PRIORITY_MAX,
            icon = R.drawable.ic_diagnosis,
            useBigTextStyle = true
        )
    }
}