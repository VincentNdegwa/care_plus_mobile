package com.example.careplus.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.careplus.MainActivity
import com.example.careplus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.os.Vibrator
import android.content.pm.ServiceInfo
import com.example.careplus.ui.incoming.IncomingMedicationActivity
import kotlinx.coroutines.delay

class AlarmWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var wakeLock: PowerManager.WakeLock? = null
        try {
            // Acquire wake lock to keep device awake
            wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or 
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                    "CarePlus:AlarmWakeLock"
                ).apply {
                    acquire(10 * 60 * 1000L) // 10 minutes max
                }
            }

            // Launch incoming medication screen
            val fullScreenIntent = Intent(context, IncomingMedicationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("notification_data", inputData.getString("notification_data"))
            }
            context.startActivity(fullScreenIntent)

            // Initialize and start media player
            mediaPlayer = MediaPlayer().apply {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            // Start vibration
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500), 0)
            }

            try {
                // Keep the worker running until explicitly cancelled
                while (!isStopped) {
                    delay(1000)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Alarm work was cancelled normally: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in AlarmWorker", e)
            Result.failure()
        } finally {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) stop()
                    release()
                }
                vibrator?.cancel()
                wakeLock?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error in cleanup", e)
            }
        }
    }

    companion object {
        private const val TAG = "AlarmWorker"
    }
} 