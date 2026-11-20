package app.expressiveclock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build

object WidgetEntryPoints {

    /** Open the editor for an already-placed widget. The ignored result is harmless: the activity
     *  persists and pushes on Save, and the host is not waiting on anything. */
    fun reconfigure(context: Context, appWidgetId: Int) {
        context.startActivity(
            Intent(context, ClockWidgetConfigActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
    }

    /** Whether the current launcher will honour a pin request at all. */
    fun canPin(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    fun requestPin(context: Context, isDigital: Boolean = false) {
        if (!canPin(context)) return
        val callback = PendingIntent.getBroadcast(
            context,
            if (isDigital) 1 else 0,
            Intent(context, WidgetPinnedReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val providerComponent = if (isDigital) {
            DigitalClockWidgetProvider.component(context)
        } else {
            ClockWidgetProvider.component(context)
        }
        AppWidgetManager.getInstance(context).requestPinAppWidget(
            providerComponent,
            null,
            callback,
        )
    }
}

/** Receives the pin callback and opens the editor for the id the launcher just created. */
class WidgetPinnedReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetEntryPoints.reconfigure(context.applicationContext, id)
        }
    }
}
