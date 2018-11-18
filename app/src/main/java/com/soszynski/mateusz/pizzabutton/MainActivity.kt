package com.soszynski.mateusz.pizzabutton

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (
                pref.getBoolean("switch_preference_run_in_foreground", true) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            startForegroundService(Intent(this, PizzaListenerService::class.java))
        } else {
            startService(Intent(this, PizzaListenerService::class.java))
        }


        updateSmsPermissionBox()
        if (!canSms()) {
            askForPermission()
        }

        updateBattery()
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

            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                v.vibrate(200)
            }

            Toast.makeText(this, getString(R.string.toast_sending), Toast.LENGTH_LONG).show()

            return@setOnLongClickListener false
        }


        button_settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        Notifications().createChannels(this)
    }

}
