package com.dev.expressiveclock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.dev.expressiveclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        background(context) { store ->
            ids.forEach { ClockWidgetRenderer.update(context, manager, it) }
            store.sweepWidgetConfigs(allPlacedWidgetIds(context))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        background(context) { ClockWidgetRenderer.update(context, manager, appWidgetId) }
    }

    /** Also the cancelled-add path; the two are indistinguishable, and want the same cleanup. */
    override fun onDeleted(context: Context, ids: IntArray) {
        background(context) { it.deleteWidgetConfigs(ids) }
    }

    override fun onRestored(context: Context, oldIds: IntArray, newIds: IntArray) {
        background(context) { store ->
            store.remapWidgetConfigs(oldIds, newIds)
            val manager = AppWidgetManager.getInstance(context)
            newIds.forEach { ClockWidgetRenderer.update(context, manager, it) }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> refreshAll(context)
        }
    }

    private fun background(context: Context, work: suspend (ClockStore) -> Unit) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                work(ClockStore(app))
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        fun component(context: Context) = ComponentName(context, ClockWidgetProvider::class.java)

        /** Redraw every placed widget, as the app does when the theme or a setting changes. */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(component(app))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.Default).launch {
                ids.forEach { ClockWidgetRenderer.update(app, manager, it) }
            }
        }

        /** The ids of every widget this app currently has on a home screen across both providers. */
        fun allPlacedWidgetIds(context: Context): IntArray {
            val manager = AppWidgetManager.getInstance(context)
            val analog = manager.getAppWidgetIds(component(context))
            val digital = manager.getAppWidgetIds(DigitalClockWidgetProvider.component(context))
            return analog + digital
        }

        fun placedIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context).getAppWidgetIds(component(context))
    }
}
