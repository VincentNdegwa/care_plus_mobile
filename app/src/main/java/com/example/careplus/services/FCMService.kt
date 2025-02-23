package com.example.careplus.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.careplus.utils.NotificationHelper
import com.example.careplus.data.SessionManager
import com.example.careplus.ui.notification.NotificationViewModel
import com.google.gson.Gson
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit
import androidx.work.OutOfQuotaPolicy
import com.example.careplus.workers.AlarmWorker
import com.google.gson.JsonParser
import com.google.gson.JsonObject

data class FCMNotification(
    val title: String,
    val body: String,
    val event: String,
    val receiver: String,
    val room_name: String?
)

class FCMService : FirebaseMessagingService() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: NotificationViewModel
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var medicationJson: String
    private val gson = Gson()
    private val jsonParser = JsonParser()

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        viewModel = NotificationViewModel(application)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (remoteMessage.data.isNotEmpty()) {
                handleNow(remoteMessage.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM message", e)
        }
    }

    private fun handleNow(data: Map<String, String>) {
        try {
            val nestedData = data["data"] ?: return
            Log.d(TAG, "Nested data: $nestedData")

            val jsonElement = jsonParser.parse(nestedData)
            val jsonObject = jsonElement.asJsonObject

            val type = jsonObject.get("type").asString

            val notification = gson.fromJson(
                jsonObject.get("notification"),
                FCMNotification::class.java
            )

            val payload = jsonObject.get("payload").asJsonObject

            when (type) {
                "new_diagnosis_notification" -> {
                    handleDiagnosisNotification(notification, payload)
                }
                "medication_reminder" -> {
                    handleMedicationReminder(notification, payload)
                }
                else -> {
                    Log.d(TAG, "Unknown notification type: $type")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM data", e)
        }
    }

    private fun handleDiagnosisNotification(
        notification: FCMNotification,
        payload: JsonObject
    ) {
        NotificationHelper.showNewDiagnosisNotification(applicationContext,
            payload.toString(), notification)
    }

    private fun handleMedicationReminder(
        notification: FCMNotification,
        payload: JsonObject
    ) {
        medicationJson = payload.toString()
        NotificationHelper.showMedicationNotification(applicationContext, medicationJson)
        scheduleAlarmWork()
    }

    private fun scheduleAlarmWork() {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
                .setExpedited(
                    OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
                )
                .setConstraints(constraints)
                .addTag("alarm_work")
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    4000,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(
                    workDataOf("notification_data" to medicationJson)
                )
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "alarm_service_${System.currentTimeMillis()}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

            Log.d(TAG, "Alarm work scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm work", e)
        }
    }

    override fun onNewToken(token: String) {
        viewModel.registerToken(token)
    }

    companion object {
        private const val TAG = "FCMService"
    }
}

