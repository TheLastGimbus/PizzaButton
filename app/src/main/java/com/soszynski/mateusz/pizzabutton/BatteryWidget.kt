package com.soszynski.mateusz.pizzabutton

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class BatteryWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent!!.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, BatteryWidget::class.java))

            onUpdate(context!!, AppWidgetManager.getInstance(context), ids)
        }
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.battery_widget)

            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val voltage = pref.getFloat("button_voltage", 4.2F)
            val percent = MathHelp().voltageToPercentage(voltage.toDouble())

            views.setTextViewText(R.id.textViewBattery, "$percent%")
            views.setProgressBar(R.id.progressBarBattery, 100, percent, false)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

