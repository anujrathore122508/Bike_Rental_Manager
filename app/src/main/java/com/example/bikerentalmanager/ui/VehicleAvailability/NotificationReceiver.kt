package com.example.bikerentalmanager.ui.VehicleAvailability

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.bikerentalmanager.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bikeName = intent.getStringExtra("bikeName")
        val notification = NotificationCompat.Builder(context, "BookingReminderChannel")
            .setContentTitle("Upcoming Booking")
            .setContentText("Aaj apki $bikeName ki booking hai.")
            .setSmallIcon(R.drawable.baseline_notifications_24) // Apna notification icon set karein
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}
