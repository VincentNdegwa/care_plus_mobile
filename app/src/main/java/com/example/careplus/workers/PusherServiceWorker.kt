package com.example.careplus.workers

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.example.careplus.data.SessionManager
import com.example.careplus.services.PusherService
import java.util.concurrent.TimeUnit

class PusherServiceWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val sessionManager = SessionManager(context)
        if (sessionManager.isLoggedIn()) {
            val serviceIntent = Intent(context, PusherService::class.java).apply {
                putExtra(PusherService.EXTRA_PATIENT_ID, sessionManager.getUser()?.patient?.id?.toString())
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
        return Result.success()
    }

    companion object {
        fun startPeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PusherServiceWorker>(
                15, TimeUnit.MINUTES, // Minimum interval allowed by Android
                5, TimeUnit.MINUTES  // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "PusherServiceWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }
    }
} 