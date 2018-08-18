package com.soszynski.mateusz.pizzasmsgate

import android.Manifest
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.util.Log


class PizzaSenderService : IntentService("PizzaSenderService") {
    private val TAG = "PizzaSenderService"

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SEND_MESSAGE -> {
                val number = intent.getStringExtra(NUMBER)
                val message = intent.getStringExtra(MESSAGE)
                handleActionSendMessage(number, message)
            }
            ACTION_BUILD_AND_SEND_MESSAGE -> {
                if(intent.getBooleanExtra(MAIN_BUTTON, false)){
                    val pref = PreferenceManager.getDefaultSharedPreferences(this)
                    val number: String = pref.getString("edit_text_preference_number", "0")
                    val message: String =
                            pref.getString("edit_text_preference_message",
                                    getString(R.string.pref_default_message))
                    handleActionSendMessage(number, message)
                }
            }
        }
    }

    private fun canSms(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun handleActionSendMessage(number: String, message: String) {
        if(canSms()) {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                    number,
                    null,
                    message,
                    null,
                    null)
            Log.i(TAG, "Sms was sent, number: $number , message: $message")

            if(
                    PreferenceManager
                            .getDefaultSharedPreferences(applicationContext)
                            .getBoolean("notification_on_send", true)
            ) {
                SmallNotification.notify(this, getString(R.string.message_sent_notification_title))
            }
        }
        else{
            SmallNotification.notify(this, getString(R.string.message_not_sent_notification_title))
        }
    }


    companion object {
        const val ACTION_SEND_MESSAGE = "action_send_message"
        const val NUMBER = "number"
        const val MESSAGE = "message"

        const val ACTION_BUILD_AND_SEND_MESSAGE = "action_build_and_send_message"
        const val MAIN_BUTTON = "main_button"
        const val LEFT_BUTTON = "left_button"
        const val RIGHT_BUTTON = "right_button"

        @JvmStatic
        fun startActionBuildAndSendMessage(context: Context, main: Boolean, left: Boolean, right: Boolean) {
            val intent = Intent(context, PizzaSenderService::class.java).apply {
                action = ACTION_BUILD_AND_SEND_MESSAGE
                putExtra(MAIN_BUTTON, main)
                putExtra(LEFT_BUTTON, left)
                putExtra(RIGHT_BUTTON, right)
            }
            context.startService(intent)
        }

        @JvmStatic
        fun startActionSendMessage(context: Context, number: String, message: String) {
            val intent = Intent(context, PizzaSenderService::class.java).apply {
                action = ACTION_SEND_MESSAGE
                putExtra(NUMBER, number)
                putExtra(MESSAGE, message)
            }
            context.startService(intent)
        }
    }
}
