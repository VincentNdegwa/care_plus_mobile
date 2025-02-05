package com.example.careplus

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.careplus.data.SessionManager
import com.example.careplus.services.PusherService
import com.example.careplus.utils.NotificationHelper

class CarePlusApp : Application() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)

        NotificationHelper.createNotificationChannel(this)
        
        // Only start Pusher service if user is logged in
        if (sessionManager.isLoggedIn()) {
            sessionManager.getUser()?.let { user ->
                startPusherService(user.patient?.id.toString())
                Log.e("CarePlusApp", "Logged in and starting pusher service...")
            }
        }
    }

    private fun startPusherService(patientId: String) {
        Intent(this, PusherService::class.java).also { intent ->
            intent.putExtra(PusherService.EXTRA_PATIENT_ID, patientId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }


    companion object {
        private var instance: CarePlusApp? = null
        fun getInstance(): CarePlusApp = instance ?: throw IllegalStateException("Application not created")
    }
} 