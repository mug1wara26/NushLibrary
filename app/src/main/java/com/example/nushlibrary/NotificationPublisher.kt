package com.example.nushlibrary

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.example.nushlibrary.adminFragments.bookRecyclerView.DUE_TIME
import com.example.nushlibrary.adminFragments.bookRecyclerView.NOTIFY_ID
import com.example.nushlibrary.adminFragments.bookRecyclerView.daysAfterDue
import com.example.nushlibrary.adminFragments.bookRecyclerView.daysBeforeDue
import com.example.nushlibrary.adminFragments.usersFragment.DAYS_IN_MILLIS
import com.example.nushlibrary.dataClasses.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NotificationPublisher: BroadcastReceiver() {
    interface GetDueDateOnPostExecute {
        fun onPostExecute(dueDateTimeStamp: Long)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        println("Received")
        createNotificationChannel(notificationManager)

        val userId = intent?.getStringExtra("user-id")

        getDueDate(userId, object: GetDueDateOnPostExecute{
            override fun onPostExecute(dueDateTimeStamp: Long) {
                if (dueDateTimeStamp != 0L) {
                    val notification = Notification.Builder(context, "com.example.nushlibrary")
                    var notify = false

                    with(notification) {
                        setContentTitle("Reminder to return book")
                        setSmallIcon(android.R.drawable.ic_dialog_info)
                        setChannelId("com.example.nushlibrary")
                    }

                    val millisFromDue = dueDateTimeStamp - System.currentTimeMillis()
                    val daysFromDue = millisFromDue / DAYS_IN_MILLIS

                    if (millisFromDue > 0) {
                        if (daysFromDue <= daysBeforeDue){
                            notify = true
                            notification.setContentText("Your book is due in $daysFromDue days")
                        }
                    }
                    else {
                        if (daysFromDue * -1 <= daysAfterDue) {
                            notify = true
                            notification.setContentText("Your book is overdue by ${daysFromDue * -1} days")
                        }
                    }

                    if (notify) notificationManager.notify(NOTIFY_ID, notification.build())
                }
            }
        })
    }

    private fun getDueDate(userId: String?, listener: GetDueDateOnPostExecute) {
        if (userId != null) {
            userReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)

                    if (user != null) {
                        if (user.booksBorrowedTimeStamp.size != 0) {
                            val earliestTimeStamp = user.booksBorrowedTimeStamp.sortedWith(compareBy { it })[0]

                            listener.onPostExecute(earliestTimeStamp + DUE_TIME)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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