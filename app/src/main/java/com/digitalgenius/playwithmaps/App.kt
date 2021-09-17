package com.digitalgenius.playwithmaps

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.digitalgenius.playwithmaps.utils.SharedPreferenceManager


class App : Application() {
    companion object{
        const val CHANNEL_ID = "default-channel"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "LocationChannel",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)

            SharedPreferenceManager.getInstance(applicationContext)
        }
    }
}