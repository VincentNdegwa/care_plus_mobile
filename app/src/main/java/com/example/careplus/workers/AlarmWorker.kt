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
            wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or 
                    PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "CarePlus:AlarmWakeLock"
                ).apply {
                    acquire(10 * 60 * 1000L) // 10 minutes
                }
            }

            // Initialize media player and vibrator before using them
            mediaPlayer = MediaPlayer()
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            // Launch incoming medication screen directly
            val fullScreenIntent = Intent(context, IncomingMedicationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("notification_data", inputData.getString("notification_data"))
            }
            context.startActivity(fullScreenIntent)

            // Start alarm and vibration
            startAlarmAndVibration()

            try {
                // Keep checking if work is cancelled
                var timeoutCounter = 0
                while (timeoutCounter < 60 && !isStopped) {
                    delay(1000)
                    timeoutCounter++
                }
            } catch (e: Exception) {
                Log.d(TAG, "Alarm work was cancelled normally: ${e.message} ")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in AlarmWorker", e)
            Result.failure()
        } finally {
            try {
                cleanup()
                wakeLock?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error in cleanup", e)
            }
        }
    }


    private fun startAlarmAndVibration() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer?.apply {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm and vibration", e)
        }
    }

    private fun cleanup() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in cleanup", e)
        }
    }

    companion object {
        private const val TAG = "AlarmWorker"
    }
} 