package com.pygeniusai.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pygeniusai.MainActivity
import com.pygeniusai.R

class CodeExecutionService : Service() {
    
    private val binder = LocalBinder()
    private var isRunning = false
    
    inner class LocalBinder : Binder() {
        fun getService(): CodeExecutionService = this@CodeExecutionService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_START -> startExecution()
            ACTION_STOP -> stopExecution()
        }
        
        return START_NOT_STICKY
    }
    
    private fun startExecution() {
        if (isRunning) return
        
        isRunning = true
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PyGenius AI")
            .setContentText("Python code is running...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun stopExecution() {
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Code Execution"
            val descriptionText = "Notifications for running Python code"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val CHANNEL_ID = "pygenius_execution"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.pygeniusai.START_EXECUTION"
        const val ACTION_STOP = "com.pygeniusai.STOP_EXECUTION"
        
        fun start(context: Context) {
            val intent = Intent(context, CodeExecutionService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, CodeExecutionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
