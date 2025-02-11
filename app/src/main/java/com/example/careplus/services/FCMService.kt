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
        
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            remoteMessage.data["data"]?.let { jsonData ->
                try {
                    val data = Gson().fromJson(jsonData, FCMMedicationPayload::class.java)
                    handleNow(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing FCM data", e)
                }
            }
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Title: ${it.title}")
            Log.d(TAG, "Body: ${it.body}")
            Log.d(TAG, "Click Action: ${it.clickAction}")
            Log.d(TAG, "Channel ID: ${it.channelId}")
        }
    }

    private fun handleNow(payload: FCMMedicationPayload) {
        if (!sessionManager.isLoggedIn()) return

        when (payload.type) {
            "medication_reminder" -> {
                payload.payload?.let { medicationData ->
                    // Convert the medication data to JSON string
                    val jsonString = Gson().toJson(medicationData)
                    NotificationHelper.showMedicationNotification(
                        applicationContext,
                        jsonString
                    )
                }
            }
            // Add other notification types here
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
        if (sessionManager.isLoggedIn()) {
            viewModel.registerToken(token)
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}

