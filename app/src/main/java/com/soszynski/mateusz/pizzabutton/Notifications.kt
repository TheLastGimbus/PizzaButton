package com.soszynski.mateusz.pizzabutton

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

class Notifications {

    val SEND_RESULT_CHANNEL_ID = "SEND_RESULT"
    val APP_RUNNING_CHANNEL_ID = "APP_RUNNING"

    val ID_BATTERY = 10
    val ID_STATUS = 0
    val ID_FOREGROUND = 30

    fun notifySendSuccess(context: Context) {
        val notifyPendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), 0)
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_local_pizza_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_message_sent_success))
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(true)

        notify(context, builder.build())
    }

    fun notifySendFail(context: Context) {
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_message_sent_fail))

        notify(context, builder.build())
    }

    fun notifySendFailWrongNumber(context: Context) {
        val notifyPendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, SettingsActivity::class.java), 0)
        val text = context.getString(R.string.notification_text_message_sent_fail_wrong_number)
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_message_sent_fail))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(notifyPendingIntent)

        notify(context, builder.build())
    }

    fun notifySendFailNoPermission(context: Context) {
        val pendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), 0)
        val text = context.getString(R.string.view_sms_permission_bad_box)
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_message_sent_fail))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        notify(context, builder.build())
    }

    fun notifySendFailSmsManagerFail(context: Context) {
        val pendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), 0)
        val text = context.getString(R.string.notification_text_message_sent_fail_sms_manager_fail)
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_message_sent_fail))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(pendingIntent)

        notify(context, builder.build())
    }


    fun notifyLowBattery(context: Context, percent: Int) {
        val text = context.getString(R.string.notification_text_low_battery)
                .replace("[percent]", percent.toString())
        val builder = NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setChannelId(SEND_RESULT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title_low_battery))
                .setContentText(text)

        notify(context, builder.build(), ID_BATTERY)
    }


    private fun notify(context: Context, notification: Notification, id: Int = 0) {
        val nm = NotificationManagerCompat.from(context)
        nm.notify(id, notification)
    }


    fun getForegroundNotification(context: Context): Notification {
        val builder = NotificationCompat.Builder(context)
                .setChannelId(APP_RUNNING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_local_pizza_black_24dp)
                .setContentTitle(context.getString(R.string.notification_title_foreground_running))
                .setPriority(NotificationCompat.PRIORITY_MIN)
        return builder.build()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSendResultChannel(context: Context) {
        val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val sendResultChannel = NotificationChannel(
                SEND_RESULT_CHANNEL_ID,
                context.getString(R.string.notification_channel_send_result),
                NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(sendResultChannel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAppRunningChannel(context: Context) {
        val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val appRunningChannel = NotificationChannel(
                APP_RUNNING_CHANNEL_ID,
                context.getString(R.string.notification_title_foreground_running),
                NotificationManager.IMPORTANCE_MIN)
        manager.createNotificationChannel(appRunningChannel)
    }

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createSendResultChannel(context)
            createAppRunningChannel(context)
        }
    }
}