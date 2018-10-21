package com.soszynski.mateusz.pizzabutton

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.preference.PreferenceManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONException
import org.json.JSONObject


class PizzaListenerService : IntentService("PizzaListenerService") {
    private val TAG = "PizzaListenerService"
    private val TAG_MDNS = "MDNS"
    private val PORT = 8182
    private var server: WebServer? = null

    override fun onHandleIntent(intent: Intent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    // mDNS stuff

    private val mRegistrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            Log.i(TAG_MDNS, "Service registered! Name: ${NsdServiceInfo.serviceName}")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG_MDNS, "Registration failed!")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG_MDNS, "Unregistration failed!")
        }
    }

    fun startService() {
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

    fun endService() {
        val mNsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            unregisterService(mRegistrationListener)
        }
    }


    override fun onCreate() {
        super.onCreate()
        startService()

        server = WebServer()
        server!!.start()
        Log.i(TAG, "Server started")
    }

    override fun onDestroy() {
        super.onDestroy()
        endService()

        server?.closeAllConnections()
        server?.stop()
        Log.i(TAG, "Server stopped due to onDestroy method")
    }


    private inner class WebServer : NanoHTTPD(PORT) {

        override fun serve(uri: String?, method: NanoHTTPD.Method?,
                           header: Map<String, String>?,
                           parameters: Map<String, String>?,
                           files: Map<String, String>?): NanoHTTPD.Response {
            if (files!!.isNotEmpty()) {
                val pref =
                        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val str = files[files.keys.first()]
                Log.i(TAG, "File received from http: $str")
                try {
                    val json: JSONObject = JSONObject(str)
                    val main = json.getBoolean("main")
                    val left = json.getBoolean("left")
                    val right = json.getBoolean("right")
                    val voltage = json.getDouble("voltage")
                    val softVerDevice = json.getString("button-software-version-device")
                    pref.edit()
                            .putFloat("button_voltage", voltage.toFloat())
                            .putString("button_software_version_device", softVerDevice)
                            .apply()
                    PizzaSenderService
                            .startActionBuildAndSendMessage(applicationContext, main, left, right)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            var json: JSONObject = JSONObject()
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
    }
}
