package com.example.careplus.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.careplus.utils.NotificationHelper
import com.example.careplus.data.SessionManager
import kotlinx.coroutines.launch
import com.example.careplus.ui.notification.NotificationViewModel

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
        Log.d(TAG, "Raw Message: $remoteMessage")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleNow(remoteMessage.data)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Title: ${it.title}")
            Log.d(TAG, "Body: ${it.body}")
            Log.d(TAG, "Click Action: ${it.clickAction}")
            Log.d(TAG, "Channel ID: ${it.channelId}")
        }
    }

    private fun handleNow(data: Map<String, String>) {
        if (!sessionManager.isLoggedIn()) return

        when (data["type"]) {
            "medication_reminder" -> {
                NotificationHelper.showMedicationNotification(
                    applicationContext,
                    data["payload"] ?: return
                )
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