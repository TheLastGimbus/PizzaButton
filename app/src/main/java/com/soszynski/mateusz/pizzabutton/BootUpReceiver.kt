package com.soszynski.mateusz.pizzabutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager

class BootUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        if (
                pref.getBoolean("switch_preference_run_in_foreground", true) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            context.startForegroundService(Intent(context, PizzaListenerService::class.java))
        } else {
            context.startService(Intent(context, PizzaListenerService::class.java))
        }

    }
}
