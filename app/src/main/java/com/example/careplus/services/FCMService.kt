package com.example.careplus.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.careplus.utils.NotificationHelper
import com.example.careplus.data.SessionManager
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
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
        // Send token to your server
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        sessionManager.saveFcmToken(token)
        
        // Only send if user is logged in
        if (sessionManager.isLoggedIn()) {
//            viewModelScope.launch {
//                try {
//                    // Call your API to update the token
//                    repository.updateFcmToken(token)
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to update FCM token on server", e)
//                }
//            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
} 