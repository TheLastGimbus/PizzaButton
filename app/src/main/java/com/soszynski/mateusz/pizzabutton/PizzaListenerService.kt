package com.soszynski.mateusz.pizzabutton

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONException
import org.json.JSONObject


class PizzaListenerService : IntentService("PizzaListenerService"),
        SharedPreferences.OnSharedPreferenceChangeListener {


    private val TAG = "PizzaListenerService"
    private val TAG_MDNS = "PizzaListener_mDNS"
    val ACTION_ALARM_LOOP = "action_alarm_loop"

    private val PORT = 8182
    private var server: WebServer = WebServer()


    private fun getServicePI(idleAlarmLoop: Boolean = false): PendingIntent {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val intent = Intent(this, PizzaListenerService::class.java)
        if (idleAlarmLoop) {
            intent.action = ACTION_ALARM_LOOP
        }

        return if (
                pref.getBoolean("switch_preference_run_in_foreground", true) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            PendingIntent.getForegroundService(this, 0, intent, 0)
        } else {
            PendingIntent.getService(this, 0, intent, 0)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setIdleAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (2 * 60 * 1000), // 2 min
                getServicePI(true))
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "onHandleIntent action: ${intent?.action}")
        if (intent?.action == ACTION_ALARM_LOOP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setIdleAlarm()
            Log.i(TAG, "Idle alarm was received and set again")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onHandleIntent(intent)

        Log.d(TAG, "Server running: ${server.isAlive}")
        if (!server.isAlive) {
            server.closeAllConnections()
            server.stop()
            server.start()
        }
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setIdleAlarm()
            Log.i(TAG, "Idle alarm was set (onCreate)")
        } else {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    2 * 60 * 1000, // 2 min
                    getServicePI())
        }

        foregroundHandle()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.registerOnSharedPreferenceChangeListener(this)

        startService()

        server.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        endService()

        server.closeAllConnections()
        server.stop()
        Log.i(TAG, "onDestroy performed")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // for now, i don't know what the hell it does, but i've heard that it i will help so...
        return true
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "switch_preference_run_in_foreground") {
            foregroundHandle()
        }
    }

    private fun foregroundHandle() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val foreground = pref.getBoolean("switch_preference_run_in_foreground", true)
        if (foreground) {
            startForeground(Notifications().ID_FOREGROUND, Notifications().getForegroundNotification(this))
        } else {
            stopForeground(true)
        }
        Log.i(TAG, "Running in foreground: $foreground")
    }


    // mDNS stuff

    private val mRegistrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            Log.i(TAG_MDNS, "Service registered! \n" +
                    "Service name: ${NsdServiceInfo.serviceName} \n")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG_MDNS, "Registration failed!")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            Log.i(TAG_MDNS, "Service unregistered!")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG_MDNS, "Unregistration failed!")
        }
    }

    private fun startService() {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "pizza-sms-app"
            serviceType = "_pizza-app._tcp"
            port = PORT
        }

        val mNsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
        }
    }

    private fun endService() {
        val mNsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            unregisterService(mRegistrationListener)
        }
    }


    private inner class WebServer : NanoHTTPD(PORT) {

        override fun serve(uri: String?, method: NanoHTTPD.Method?,
                           header: Map<String, String>?,
                           parameters: Map<String, String>?,
                           files: Map<String, String>?): NanoHTTPD.Response {
            val pref =
                    PreferenceManager.getDefaultSharedPreferences(this@PizzaListenerService)
            if (files!!.isNotEmpty()) {
                val str = files[files.keys.first()]
                Log.i(TAG, "File received from http: $str")

                var main: Boolean = false
                var left: Boolean = false
                var right: Boolean = false
                var voltage: Double = 4.2
                try {
                    val json = JSONObject(str)
                    main = json.getBoolean("main")
                    left = json.getBoolean("left")
                    right = json.getBoolean("right")
                    voltage = json.getDouble("voltage")

                    pref.edit()
                            .putFloat("button_voltage", voltage.toFloat())
                            .apply()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                PizzaSenderService
                        .startActionBuildAndSendMessage(applicationContext, main, left, right)

                val percent = MathHelp().voltageToPercentage(voltage)
                if (percent < 60) {
                    Notifications().notifyLowBattery(this@PizzaListenerService, percent)
                }

                val updateWidget = Intent(this@PizzaListenerService, BatteryWidget::class.java)
                updateWidget.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val pendingIntent = PendingIntent.getBroadcast(
                        this@PizzaListenerService,
                        0,
                        updateWidget,
                        0)
                pendingIntent.send()
            }

            val json = JSONObject()
            json.put("ssid",
                    pref.getString("edit_text_preference_button_ssid", ""))
            json.put("password",
                    pref.getString("edit_text_preference_button_password", ""))
            json.put("button-software-version-app", "1.0")

            val jsonStr = json.toString()
            val res: NanoHTTPD.Response = NanoHTTPD.Response(jsonStr)
            res.mimeType = "text/json"
            Log.i(TAG, "Http response: $jsonStr")
            return res
        }

        override fun start() {
            super.start()
            Log.i(TAG, "Server started")
        }

        override fun stop() {
            super.stop()
            Log.i(TAG, "Server stopped")
        }
    }
}
