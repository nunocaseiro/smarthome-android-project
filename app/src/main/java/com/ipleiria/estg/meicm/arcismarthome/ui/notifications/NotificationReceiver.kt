package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ipleiria.estg.meicm.arcismarthome.MainActivity
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.models.AppData

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            ContextCompat.getSystemService(
                it, NotificationManager::class.java
            )
        } as NotificationManager

        sendNotification(context)
    }

    private fun sendNotification(applicationContext: Context) {

        val args = Bundle()
        args.putInt("notificationKey", AppData.instance.notification.value!!)

        val contentIntent = Intent(applicationContext, MainActivity::class.java)

        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "0")
            .setSmallIcon(R.drawable.ic_car_solid_24dp)
            .setContentTitle("Vehicle notification")
            .setContentText("New vehicle detected")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(AppData.instance.notification.value!!, notificationBuilder.build())
        }
    }
}