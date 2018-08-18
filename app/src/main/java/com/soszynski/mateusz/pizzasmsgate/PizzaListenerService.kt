package com.soszynski.mateusz.pizzasmsgate

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject


class PizzaListenerService : IntentService("PizzaListenerService") {
    private val TAG = "PizzaListenerService"
    private var server: WebServer? = null

    override fun onHandleIntent(intent: Intent?) {  }

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
            if(files!!.isNotEmpty()){
                files.forEach { t, u ->
                    Log.i(TAG, "File given by http: $u")
                }
            }
            var answer = "<html><body>\n<h1>Hello server</h1>\n</body></html>\n"

            return NanoHTTPD.Response(answer)
        }
    }
}
