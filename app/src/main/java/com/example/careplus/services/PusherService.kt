package com.example.careplus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.careplus.data.SessionManager
import com.example.careplus.utils.NotificationHelper
import com.google.gson.Gson
import com.pusher.client.ChannelAuthorizer
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange

class PusherService : Service() {
    private lateinit var pusher: Pusher
    private lateinit var sessionManager: SessionManager
    private var patientId: String? = null
    private val gson = Gson()
    private val channels = mutableMapOf<String, Channel>()
    private val channelEvents = mutableMapOf<String, List<String>>()
    private val channelListeners = mutableMapOf<String, PrivateChannelEventListener>()

    private var isConnected = false
    private var isSubscribed = false

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

        return START_REDELIVER_INTENT
    }

    private fun setupPusher() {
        val options = PusherOptions().apply {
            setCluster("mt1")
            setChannelAuthorizer(createAuthorizer())
        }

        pusher = Pusher("b526271d9952b0873f03", options)
        
        pusher.connect(createConnectionListener())
    }

    private fun createAuthorizer() = ChannelAuthorizer { channelName, socketId ->
        val key = "b526271d9952b0873f03"
        val secret = "d4aa5ee8ab9dcc92930c"
        val stringToSign = "$socketId:$channelName"
        val signature = createHmacSha256(stringToSign, secret)
        "{\"auth\":\"$key:$signature\"}"
    }

    private fun createConnectionListener() = object : ConnectionEventListener {
        override fun onConnectionStateChange(change: ConnectionStateChange) {
            Log.d(TAG, "State changed from ${change.previousState} to ${change.currentState}")
            when (change.currentState) {
                ConnectionState.CONNECTED -> {
                    if (!isConnected || !isSubscribed) {
                        isConnected = true
                        patientId?.let { id -> 
                            subscribeToChannels(id)
                            isSubscribed = true
                        }
                    }
                }
                ConnectionState.DISCONNECTED -> {
                    isConnected = false
                    isSubscribed = false
                    clearSubscriptions()
                }
                else -> { /* Handle other states if needed */ }
            }
        }

        override fun onError(message: String, code: String?, e: Exception?) {
            Log.e(TAG, "Connection Error: $message, code: $code", e)
            if (message.contains("Existing subscription")) {
                Log.d(TAG, "Ignoring existing subscription message")
                isSubscribed = true
                return
            }
            e?.printStackTrace()
        }
    }

    private fun subscribeToChannels(patientId: String) {
        if (isSubscribed) {
            Log.d(TAG, "Already subscribed to channels")
            return
        }

        // Subscribe to medication channel
        subscribeToPrivateChannel(
            channelName = "private-medication.take.$patientId",
            events = listOf("medication.take"),
            onEvent = { handleMedicationEvent(it) }
        )
        
        // Add more channel subscriptions here as needed
        // Example:
        // subscribeToPrivateChannel(
        //     channelName = "private-appointments.$patientId",
        //     events = listOf("appointment.created", "appointment.updated"),
        //     onEvent = { handleAppointmentEvent(it) }
        // )
    }

    private fun subscribeToPrivateChannel(
        channelName: String,
        events: List<String>,
        onEvent: (PusherEvent) -> Unit
    ) {
        try {
            if (channels.containsKey(channelName)) {
                Log.d(TAG, "Already subscribed to channel: $channelName")
                return
            }

            val eventListener = createChannelListener(channelName, onEvent)
            val channel = pusher.subscribePrivate(channelName, eventListener)
            
            events.forEach { event ->
                channel.bind(event, eventListener)
            }
            
            channels[channelName] = channel
            channelEvents[channelName] = events
            channelListeners[channelName] = eventListener
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to channel: $channelName", e)
        }
    }

    private fun createChannelListener(
        channelName: String,
        onEvent: (PusherEvent) -> Unit
    ) = object : PrivateChannelEventListener {
        override fun onEvent(event: PusherEvent?) {
            event?.let {
                Log.d(TAG, "Received event: ${it.data}")
                Log.d(TAG, "Event name: ${it.eventName}")
                if (sessionManager.isLoggedIn()) {
                    onEvent(it)
                }
            }
        }

        override fun onSubscriptionSucceeded(channelName: String) {
            Log.d(TAG, "✅ Subscription succeeded for channel: $channelName")
        }

        override fun onAuthenticationFailure(message: String, e: Exception) {
            Log.e(TAG, "❌ Authentication failed: $message", e)
        }
    }

    private fun handleMedicationEvent(event: PusherEvent) {
        Log.d(TAG, "Handling medication event: ${event.data}")
        try {
            NotificationHelper.showMedicationNotification(applicationContext, event.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling medication event", e)
        }
    }

    private fun createHmacSha256(data: String, secret: String): String {
        return try {
            val algorithm = "HmacSHA256"
            val key = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), algorithm)
            val mac = javax.crypto.Mac.getInstance(algorithm)
            mac.init(key)
            bytesToHex(mac.doFinal(data.toByteArray()))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating HMAC", e)
            throw e
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        return bytes.joinToString("") { byte ->
            val i = byte.toInt()
            "${hexChars[i shr 4 and 0x0f]}${hexChars[i and 0x0f]}"
        }
    }

    private fun clearSubscriptions() {
        if (!isSubscribed) {
            return
        }

        channels.forEach { (channelName, channel) ->
            try {
                val listener = channelListeners[channelName]
                if (listener != null) {
                    channelEvents[channelName]?.forEach { event ->
                        channel.unbind(event, listener)
                    }
                }
                pusher.unsubscribe(channelName)
            } catch (e: Exception) {
                Log.e(TAG, "Error unsubscribing from channel: $channelName", e)
            }
        }
        channels.clear()
        channelEvents.clear()
        channelListeners.clear()
        isSubscribed = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isSubscribed = false
        clearSubscriptions()
        pusher.disconnect()
    }

    companion object {
        const val EXTRA_PATIENT_ID = "extra_patient_id"
        private const val TAG = "Pusher"
    }
} 