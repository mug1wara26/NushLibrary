package com.example.nushlibrary

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.example.nushlibrary.adminFragments.usersFragment.DAYS_IN_MILLIS

class NotificationPublisher: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dueDate: Long = intent!!.getLongExtra("due-date", 0)
        val id = intent.getIntExtra("notif-id", 0)
        val daysBeforeDue = intent.getIntExtra("days-before-due", 3)
        val daysAfterDue = intent.getIntExtra("days-after-due", 5)
        println("Received")
        createNotificationChannel(notificationManager)

        if (dueDate != 0L) {
            val notification = Notification.Builder(context, "com.example.nushlibrary")
            with(notification) {
                setContentTitle("Reminder to return book")
                setSmallIcon(android.R.drawable.ic_dialog_info)
                setChannelId("com.example.nushlibrary")
            }

            val millisFromDue = dueDate - System.currentTimeMillis()
            val daysFromDue = millisFromDue / DAYS_IN_MILLIS

            if (millisFromDue > 0) {
                if (daysFromDue <= daysBeforeDue)
                    notification.setContentText("Your book is due in $daysFromDue days")
            }
            else {
                if (daysFromDue * -1 <= daysAfterDue)
                    notification.setContentText("Your book is overdue by ${daysFromDue * -1} days")
            }

            notificationManager.notify(id, notification.build())
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val id = "com.example.nushlibrary"
        val name = "Book due reminder"
        val description = "Reminder to return book"

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 100, 200, 100, 200)
        notificationManager.createNotificationChannel(channel)
    }
}