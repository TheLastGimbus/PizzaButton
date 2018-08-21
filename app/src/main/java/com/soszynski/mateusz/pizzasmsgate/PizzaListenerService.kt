package com.soszynski.mateusz.pizzasmsgate

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONException
import org.json.JSONObject


class PizzaListenerService : IntentService("PizzaListenerService") {
    private val TAG = "PizzaListenerService"
    private var server: WebServer? = null

    override fun onHandleIntent(intent: Intent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        server = WebServer()
        server!!.start()
        Log.i(TAG, "Server started")
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.closeAllConnections()
        server?.stop()
        Log.i(TAG, "Server stopped due to onDestroy method")
    }


    private inner class WebServer : NanoHTTPD(8080) {

        override fun serve(uri: String?, method: NanoHTTPD.Method?,
                           header: Map<String, String>?,
                           parameters: Map<String, String>?,
                           files: Map<String, String>?): NanoHTTPD.Response {
            if (files!!.isNotEmpty()) {
                val pref =
                        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                files.forEach { t, u ->
                    try {
                        val json: JSONObject = JSONObject(u)
                        val main = json.getBoolean("main")
                        val left = json.getBoolean("left")
                        val right = json.getBoolean("right")
                        val voltage = json.getDouble("voltage")
                        pref.edit()
                                .putFloat("button_voltage", voltage.toFloat())
                                .apply()
                        PizzaSenderService
                                .startActionBuildAndSendMessage(applicationContext, main, left, right)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            var json: JSONObject = JSONObject()
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            json.put("ssid", pref.getString("edit_text_preference_button_ssid", ""))
            json.put("password", pref.getString("edit_text_preference_button_password", ""))

            val res: NanoHTTPD.Response = NanoHTTPD.Response(json.toString())
            res.mimeType = "text/json"
            return res
        }
    }
}
