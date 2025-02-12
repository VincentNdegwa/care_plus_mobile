package com.example.careplus.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Cancel all alarm work
        WorkManager.getInstance(context).cancelAllWorkByTag("alarm_work")
    }
} 