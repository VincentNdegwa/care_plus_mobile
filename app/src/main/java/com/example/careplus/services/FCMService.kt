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
import java.util.concurrent.TimeUnit
import android.app.NotificationManager
import android.content.Intent
import androidx.work.OutOfQuotaPolicy
import com.example.careplus.workers.AlarmWorker
import android.app.KeyguardManager
import com.example.careplus.MainActivity

class FCMService : FirebaseMessagingService() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: NotificationViewModel
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        viewModel = NotificationViewModel(application)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            // Acquire wake lock first
            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "CarePlus:FCMWakeLock"
                ).apply {
                    acquire(10 * 60 * 1000L)
                }
            }
            
            if (remoteMessage.data.isNotEmpty()) {
                handleNow(remoteMessage.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM message", e)
        } finally {
            wakeLock?.release()
        }
    }

    private fun handleNow(data: Map<String, String>) {
        try {
            val nestedData = data["data"] ?: return
            Log.d(TAG, "Nested data: $nestedData")

            val fcmData = Gson().fromJson(nestedData, FCMMedicationPayload::class.java)
            
            if (fcmData.type == "medication_reminder") {
                val medicationJson = Gson().toJson(fcmData.payload)
                Log.d(TAG, "Medication data: $medicationJson")
                
                // Show notification
                NotificationHelper.showMedicationNotification(applicationContext, medicationJson)
                
                // Start alarm service directly
                startAlarmService()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM data", e)
        }
    }

    private fun startAlarmService() {
        try {
            // Turn on screen if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isKeyguardLocked) {
                    val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(fullScreenIntent)
                }
            }

            // Start alarm service
            val intent = Intent(this, AlarmService::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm service", e)
        }
    }

    override fun onNewToken(token: String) {
        viewModel.registerToken(token)
    }

    private fun scheduleAlarmService() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .addTag("alarm_work")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "alarm_service_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    companion object {
        private const val TAG = "FCMService"
        private const val FCM_DEFAULT_ID = 0  // Default FCM notification ID
    }
}

