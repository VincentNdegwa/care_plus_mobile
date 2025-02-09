package com.example.careplus

import android.app.Application
import com.example.careplus.data.SessionManager
import com.example.careplus.workers.PusherServiceWorker
import com.example.careplus.utils.FCMManager

class CarePlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager
        if (SessionManager(this).isLoggedIn()) {
            PusherServiceWorker.startPeriodicWork(this)
            
            // Initialize FCM
            FCMManager.initialize(this)
        }
    }
} 