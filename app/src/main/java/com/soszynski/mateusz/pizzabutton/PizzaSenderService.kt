package com.soszynski.mateusz.pizzabutton

import android.Manifest
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.util.Log


class PizzaSenderService : IntentService("PizzaSenderService") {
    private val TAG = "PizzaSenderService"
    private val SENT = "SMS_SENT"

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SEND_MESSAGE -> {
                val number = intent.getStringExtra(NUMBER)
                val message = intent.getStringExtra(MESSAGE)
                handleActionSendMessage(number, message)
            }
            ACTION_BUILD_AND_SEND_MESSAGE -> {
                if (intent.getBooleanExtra(MAIN_BUTTON, false)) {
                    val pref = PreferenceManager.getDefaultSharedPreferences(this)
                    val number: String = pref.getString("edit_text_preference_number", "[num]")
                    val message: String =
                            pref.getString("edit_text_preference_message",
                                    getString(R.string.pref_placeholder_default_message))
                    handleActionSendMessage(number, message)
                }
            }
        }
    }

    private fun canSms(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun handleActionSendMessage(number: String, message: String) {
        if (!canSms()) {
            Notifications().notifySendFailNoPermission(this)
            return
        }
        if (!PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Notifications().notifySendFailWrongNumber(this)
            Log.i(TAG, "Sms not send because number is wrong!")
            return
        }

        val sentPI = PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, SmsStatusReceiver().javaClass),
                0)

        SmsManager.getDefault().sendTextMessage(
                number,
                null,
                message,
                sentPI,
                null)

        Log.i(TAG, "Sms passed to smsManager, number: $number , message: $message")
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
        fun startActionBuildAndSendMessage(
                context: Context, main: Boolean = true, left: Boolean = true, right: Boolean = true) {

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
