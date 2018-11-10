package com.soszynski.mateusz.pizzabutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, PizzaListenerService::class.java)
        context.startService(serviceIntent) // TODO: this crashes on >= 8.0 (luckly only when ram sucks)
    }
}
