package com.soszynski.mateusz.pizzabutton

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    fun canSms(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermission(code: Int = 1) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.dialog_sms_privilege_needed))
        builder.setNeutralButton("OK") { _, _ ->
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    code)
        }
        builder.create()
        builder.show()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.thank_you), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.toast_sms_denied), Toast.LENGTH_SHORT).show()
                }
                updateSmsPermissionBox()
            }
        }
    }

    private fun updateSmsPermissionBox() {
        if (canSms()) {
            button_permission_box.setBackgroundResource(R.drawable.rounded_button_green)
            button_permission_box.text = getString(R.string.view_sms_permission_good_box)
        } else {
            button_permission_box.setBackgroundResource(R.drawable.rounded_button_red)
            button_permission_box.text = getString(R.string.view_sms_permission_bad_box)
        }
    }

    private fun updateBattery() {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val voltage = pref.getFloat("button_voltage", 4.2F)
        val percent = MathHelp().voltageToPercentage(voltage.toDouble())
        textViewBattery.text = "${getString(R.string.view_button_battery)}: $percent%"
        progressBarBattery.progress = percent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this, PizzaListenerService::class.java))


        updateSmsPermissionBox()
        if (!canSms()) {
            askForPermission()
        }

        updateBattery()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "button_voltage") {
                updateBattery()
            }
        }
        linearLayoutBattery.setOnClickListener {
            // this is stupid af
            updateBattery()
        }


        button_permission_box.setOnClickListener {
            if (!canSms()) {
                askForPermission()
            } else {
                Toast.makeText(this,
                        getString(R.string.view_sms_permission_good_box),
                        Toast.LENGTH_SHORT).show()
            }
        }

        // safer than normal click
        button_order.setOnLongClickListener {
            PizzaSenderService.startActionBuildAndSendMessage(this, true, false, false)
            return@setOnLongClickListener true
        }


        button_settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val SEND_RESULT_CHANNEL_ID = "SEND_RESULT"
            val sendResultChannel = NotificationChannel(
                    SEND_RESULT_CHANNEL_ID,
                    getString(R.string.notification_channel_send_result),
                    NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(sendResultChannel)

        }

    }

}
