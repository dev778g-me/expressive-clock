package com.dev.expressiveclock.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dev.expressiveclock.data.ClockSettings
import com.dev.expressiveclock.data.ClockStore
import com.dev.expressiveclock.data.WidgetConfig
import com.dev.expressiveclock.data.WidgetType
import com.dev.expressiveclock.ui.theme.ClockTheme
import kotlinx.coroutines.launch

class ClockWidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val manager = AppWidgetManager.getInstance(this)
        val provider = manager.getAppWidgetInfo(appWidgetId)?.provider
        val isDigital = provider == DigitalClockWidgetProvider.component(this)
        val isAnalog = provider == ClockWidgetProvider.component(this)

        if (!isDigital && !isAnalog) {
            finish()
            return
        }

        val store = ClockStore(applicationContext)
        val defaultType = if (isDigital) WidgetType.DIGITAL else WidgetType.ANALOG

        lifecycleScope.launch {
            val savedConfig = store.widgetConfigNow(appWidgetId)
            val initialConfig = savedConfig ?: WidgetConfig(type = defaultType)

            setContent {
                val settings by store.settings.collectAsStateWithLifecycle(initialValue = ClockSettings())

                ClockTheme(settings.theme) {
                    WidgetConfigScreen(
                        initial = initialConfig,
                        settings = settings,
                        isNew = savedConfig == null,
                        onCancel = { finish() },
                        onSave = { config -> commit(appWidgetId, config) },
                    )
                }
            }
        }
    }

    private fun commit(appWidgetId: Int, config: WidgetConfig) {
        lifecycleScope.launch {
            val store = ClockStore(applicationContext)
            store.putWidgetConfig(appWidgetId, config)
            val manager = AppWidgetManager.getInstance(applicationContext)
            if (config.type == WidgetType.DIGITAL) {
                DigitalClockWidgetRenderer.update(applicationContext, manager, appWidgetId)
            } else {
                ClockWidgetRenderer.update(applicationContext, manager, appWidgetId)
            }
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
            )
            finish()
        }
    }
}
