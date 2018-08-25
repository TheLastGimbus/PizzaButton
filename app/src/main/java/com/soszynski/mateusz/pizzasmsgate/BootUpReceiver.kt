package com.soszynski.mateusz.pizzasmsgate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, PizzaListenerService::class.java)
        context.startService(serviceIntent)
    }
}
