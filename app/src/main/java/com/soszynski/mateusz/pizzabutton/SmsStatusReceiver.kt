package com.soszynski.mateusz.pizzabutton

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Notifications().notifySendSuccess(context)
            }
            else -> {
                Notifications().notifySendFailSmsManagerFail(context)
            }
        }
    }
}
