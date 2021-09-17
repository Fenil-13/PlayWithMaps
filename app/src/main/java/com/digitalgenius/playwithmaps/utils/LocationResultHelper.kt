package com.digitalgenius.playwithmaps.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.digitalgenius.playwithmaps.App
import com.digitalgenius.playwithmaps.MainActivity
import com.digitalgenius.playwithmaps.R
import java.text.DateFormat
import java.util.*

class LocationResultHelper(
    private val context: Context,
    private val mLocationList: List<Location>
) {
    companion object{
        val KEY_LOCATION_RESULT = "key-location-results"


        fun getSavedLocationResult(): String? {
            val sharedPreferenceManager=SharedPreferenceManager.getInstance(null)
            return sharedPreferenceManager.getString(KEY_LOCATION_RESULT)
        }

    }

    fun getLocationText(): String {
        if (mLocationList.isEmpty()) {
            return "Location Not Receive"
        }
        val sb = StringBuilder()
        for (location: Location in mLocationList) {
            sb.append("(")
            sb.append(location.latitude)
            sb.append(", ${location.longitude}")
            sb.append(")")
            sb.append("\n")
        }
        return sb.toString()
    }

    fun showNotification() {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        var notificationBuilder: Notification.Builder? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = Notification.Builder(context, App.CHANNEL_ID)
                .setContentTitle(getLocationResultTitle())
                .setContentText(getLocationText())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent)
        }

        getNotificationManager().notify(0, notificationBuilder!!.build())


    }

    private fun getLocationResultTitle(): CharSequence? {
        val result = context.resources.getQuantityString(
            R.plurals.num_location_reported,
            mLocationList.size,
            mLocationList.size
        )
        return "$result : ${DateFormat.getDateTimeInstance().format(Date())}"
    }

    private fun getNotificationManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    fun saveLocationResults() {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(null)
        sharedPreferenceManager.putString(
            KEY_LOCATION_RESULT,
            "${getLocationResultTitle()}\n${getLocationText()}")

    }

}