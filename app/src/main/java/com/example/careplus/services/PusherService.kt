package com.example.careplus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.careplus.data.SessionManager
import com.example.careplus.utils.NotificationHelper
import com.pusher.client.ChannelAuthorizer
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange

class PusherService : Service() {
    private lateinit var pusher: Pusher
    private lateinit var sessionManager: SessionManager
    private var patientId: String? = null

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!sessionManager.isLoggedIn()) {
            stopSelf()
            return START_NOT_STICKY
        }

        intent?.getStringExtra(EXTRA_PATIENT_ID)?.let { id ->
            patientId = id
            setupPusher()
        } ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun setupPusher() {
        val authorizer = ChannelAuthorizer { channelName, socketId ->
            // Create proper auth signature for private channels
            val key = "b526271d9952b0873f03" // Your Pusher key
            val secret = "d4aa5ee8ab9dcc92930c" // Your Pusher secret

            // Create the string to sign: "socket_id:channel_name"
            val stringToSign = "$socketId:$channelName"

            // Create HMAC SHA256 signature
            val signature = createHmacSha256(stringToSign, secret)

            // Return proper auth format
            "{\"auth\":\"$key:$signature\"}"
        }

        val options = PusherOptions().apply {
            setCluster("mt1")
            setChannelAuthorizer(authorizer)
        }

        pusher = Pusher("b526271d9952b0873f03", options)
        
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("Pusher", "State changed from ${change.previousState} to ${change.currentState}")
                if (change.currentState == ConnectionState.CONNECTED) {
                    patientId?.let { id ->
                        Log.d("Pusher", "Connection established, attempting to subscribe with patient ID: $id")
                        subscribeToMedicationChannel(id)
                    }
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("Pusher", "Connection Error: $message, code: $code", e)
                e?.printStackTrace()
            }
        }, ConnectionState.ALL)
    }

    private fun subscribeToMedicationChannel(patientId: String) {
        val channelName = "private-medication.take.$patientId"
        Log.d("Pusher", "Attempting to subscribe to channel: $channelName")
        
        try {
            val eventListener = object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Log.d("Pusher", "Received event: ${event?.data}")
                    Log.d("Pusher", "Event name: ${event?.eventName}")
                    if (sessionManager.isLoggedIn()) {
                        event?.data?.let { sendNotification(it) }
                    }
                }

                override fun onSubscriptionSucceeded(channelName: String) {
                    Log.d("Pusher", "✅ Subscription succeeded for channel: $channelName")
                }

                override fun onAuthenticationFailure(message: String, e: Exception) {
                    Log.e("Pusher", "❌ Authentication failed: $message", e)
                    e.printStackTrace()
                }
            }

            val channel = pusher.subscribePrivate(channelName, eventListener)
            
            // Use the same eventListener for binding
            channel.bind("medication.take", eventListener)

        } catch (e: Exception) {
            Log.e("Pusher", "Error subscribing to channel", e)
            e.printStackTrace()
        }
    }

    private fun createHmacSha256(data: String, secret: String): String {
        try {
            val algorithm = "HmacSHA256"
            val key = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), algorithm)
            val mac = javax.crypto.Mac.getInstance(algorithm)
            mac.init(key)
            return bytesToHex(mac.doFinal(data.toByteArray()))
        } catch (e: Exception) {
            Log.e("Pusher", "Error creating HMAC", e)
            throw e
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach { byte ->
            val i = byte.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }

    private fun sendNotification(data: String) {
        NotificationHelper.showMedicationNotification(this, data)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pusher.disconnect()
    }

    companion object {
        const val EXTRA_PATIENT_ID = "extra_patient_id"
    }
} 