package com.soszynski.mateusz.pizzabutton

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat

object SmallNotification {
    private const val NOTIFICATION_TAG = "MessageSent"

    fun notify(context: Context, message: String, text: String = "") {
        val smsIntent = Intent(Intent.ACTION_MAIN)
        smsIntent.addCategory(Intent.CATEGORY_DEFAULT)
        smsIntent.type = "vnd.android-dir/mms-sms"
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setChannelId("SEND_RESULT")
                .setSmallIcon(R.drawable.ic_notification_pizza)
                .setContentTitle(message)
        if (text.isNotEmpty()) {
            builder.setContentText(text)
        }

        notify(context, builder.build())
    }

    private fun notify(context: Context, notification: Notification) {
        val nm = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_TAG, 0, notification)
    }
}
