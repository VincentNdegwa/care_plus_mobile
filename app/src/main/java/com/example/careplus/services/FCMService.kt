package com.example.careplus.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.careplus.utils.NotificationHelper
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.fcm.FCMMedicationPayload
import kotlinx.coroutines.launch
import com.example.careplus.ui.notification.NotificationViewModel
import com.google.gson.Gson
import android.content.Context
import android.os.PowerManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import com.example.careplus.workers.NotificationWorker
import java.util.concurrent.TimeUnit
import android.app.NotificationManager

class FCMService : FirebaseMessagingService() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: NotificationViewModel

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        viewModel = NotificationViewModel(application)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            // Log the entire message for debugging
            Log.d(TAG, "Message received - Data: ${remoteMessage.data}, Notification: ${remoteMessage.notification}")
            
            // Always handle as data message, ignore notification payload
            if (remoteMessage.data.isNotEmpty()) {
                // Cancel any default notification that might have been created
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(FCM_DEFAULT_ID)
                
                handleNow(remoteMessage.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM message", e)
        }
    }

    private fun handleNow(data: Map<String, String>) {
        try {
            val nestedData = data["data"] ?: return
            Log.d(TAG, "Processing data payload: $nestedData")

            val fcmData = Gson().fromJson(nestedData, FCMMedicationPayload::class.java)
            
            if (fcmData.type == "medication_reminder") {
                val medicationJson = Gson().toJson(fcmData.payload)
                Log.d(TAG, "Processing medication reminder: $medicationJson")
                
                if (isDeviceIdle(applicationContext)) {
                    // Schedule WorkManager task for when device is more active
                    scheduleNotification(medicationJson)
                } else {
                    // Show notification immediately
                    NotificationHelper.showMedicationNotification(applicationContext, medicationJson)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM data", e)
        }
    }

    private fun isDeviceIdle(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isDeviceIdleMode
        } else {
            false
        }
    }

    private fun scheduleNotification(jsonData: String) {
        val workData = workDataOf("notification_data" to jsonData)
        
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "medication_notification_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    override fun onNewToken(token: String) {
        viewModel.registerToken(token)
    }

    companion object {
        private const val TAG = "FCMService"
        private const val FCM_DEFAULT_ID = 0  // Default FCM notification ID
    }
}

