package com.example.myapp.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}