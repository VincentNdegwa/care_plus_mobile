package com.example.careplus.workers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.careplus.R
import com.example.careplus.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Set foreground service notification
                setForeground(createForegroundInfo())

                val jsonData = inputData.getString("notification_data")
                if (jsonData != null) {
                    Log.d(TAG, "Processing notification from WorkManager: $jsonData")
                    
                    // Create a wake lock
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                    val wakeLock = powerManager.newWakeLock(
                        android.os.PowerManager.PARTIAL_WAKE_LOCK,
                        "CarePlus:NotificationWorkerWakeLock"
                    ).apply {
                        acquire(10 * 60 * 1000L) // 10 minutes
                    }

                    try {
                        NotificationHelper.showMedicationNotification(applicationContext, jsonData)
                        Result.success()
                    } finally {
                        wakeLock.release()
                    }
                } else {
                    Log.e(TAG, "No notification data provided")
                    Result.failure()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification in worker", e)
                Result.retry()
            }
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationHelper.createWorkerNotification(context)
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "NotificationWorker"
        private const val NOTIFICATION_ID = 1
    }
} 