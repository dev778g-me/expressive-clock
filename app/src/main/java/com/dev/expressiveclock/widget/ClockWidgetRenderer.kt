package com.dev.expressiveclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.dev.expressiveclock.MainActivity
import com.dev.expressiveclock.R
import com.dev.expressiveclock.data.ClockStore
import com.dev.expressiveclock.data.DateMode
import com.dev.expressiveclock.data.DatePosition
import com.dev.expressiveclock.data.HandStyle
import com.dev.expressiveclock.data.WidgetConfig
import com.dev.expressiveclock.data.WidgetType
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object ClockWidgetRenderer {

    suspend fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val store = ClockStore(context)
        val config = store.widgetConfigNow(appWidgetId) ?: return
        if (config.type == WidgetType.DIGITAL) {
            DigitalClockWidgetRenderer.update(context, manager, appWidgetId)
            return
        }
        val settings = store.settingsNow()
        val options = manager.getAppWidgetOptions(appWidgetId)

        val views = build(context, config, settings, options)
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun build(
        context: Context,
        config: WidgetConfig,
        settings: com.dev.expressiveclock.data.ClockSettings,
        options: Bundle,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        fun px(dp: Int) = (dp * density).roundToInt().coerceAtLeast(1)

        val sizes: List<SizeF> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                ?.takeIf { it.isNotEmpty() }
                ?: emptyList()
        } else {
            emptyList()
        }

        if (sizes.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val map = sizes.associateWith { size ->
                one(context, config, settings, px(size.width.roundToInt()), px(size.height.roundToInt()))
            }
            return RemoteViews(map)
        }

        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minW)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minH)
        val portrait = one(context, config, settings, px(minW), px(maxH))
        val landscape = one(context, config, settings, px(maxW), px(minH))
        return RemoteViews(landscape, portrait)
    }

    private fun one(
        context: Context,
        config: WidgetConfig,
        settings: com.dev.expressiveclock.data.ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        val colours = WidgetColours.resolve(context, config, settings)
        val face = FaceRenderer.render(context, config, colours, w, h)
        val views = RemoteViews(context.packageName, R.layout.widget_clock)

        views.setImageViewIcon(R.id.face, face.bitmap.toIcon())

        views.setIcon(R.id.hands, "setDial", HandRenderer.blank().toIcon())

        val topmost = when {
            config.secondHand != HandStyle.OFF -> 2
            config.minuteHand != HandStyle.OFF -> 1
            else -> 0
        }
        val pinR = if (config.centrePin) DialGeometry.PIN_DIAMETER else 0f

        views.setIcon(
            R.id.hands, "setHourHand",
            HandRenderer.render(
                config.hourHand, colours.hourHand, face.rMin,
                DialGeometry.HOUR_LENGTH, DialGeometry.HOUR_WIDTH, DialGeometry.HOUR_TAIL,
                if (topmost == 0) pinR else 0f, colours.pin,
            ).toIcon(),
        )
        views.setIcon(
            R.id.hands, "setMinuteHand",
            HandRenderer.render(
                config.minuteHand, colours.minuteHand, face.rMin,
                DialGeometry.MINUTE_LENGTH, DialGeometry.MINUTE_WIDTH, DialGeometry.MINUTE_TAIL,
                if (topmost == 1) pinR else 0f, colours.pin,
            ).toIcon(),
        )
        if (config.secondHand == HandStyle.OFF) {
            views.setIcon(R.id.hands, "setSecondHand", null)
        } else {
            views.setIcon(
                R.id.hands, "setSecondHand",
                HandRenderer.render(
                    config.secondHand, colours.secondHand, face.rMin,
                    DialGeometry.SECOND_LENGTH, DialGeometry.SECOND_WIDTH, DialGeometry.SECOND_TAIL,
                    if (topmost == 2) pinR else 0f, colours.pin,
                ).toIcon(),
            )
        }

        applyDate(context, views, config, colours, face, w, h)

        views.setOnClickPendingIntent(
            android.R.id.background,
            android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    private fun applyDate(
        context: Context,
        views: RemoteViews,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
        w: Int,
        h: Int,
    ) {
        if (config.date == DateMode.NONE) {
            views.setViewVisibility(R.id.date, View.GONE)
            return
        }
        views.setViewVisibility(R.id.date, View.VISIBLE)

        val skeleton = when (config.date) {
            DateMode.DAY -> "d"
            DateMode.DAY_WEEKDAY -> "EEEd"
            DateMode.FULL -> "EEEdMMM"
            DateMode.NONE -> "d"
        }
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
        views.setCharSequence(R.id.date, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.date, "setFormat24Hour", pattern)
        views.setTextColor(R.id.date, colours.minor)

        val sizePx = 0.14f * face.rMin
        views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, sizePx)

        val angle = when (config.datePosition) {
            DatePosition.THREE -> 90f
            DatePosition.FOUR_THIRTY -> 135f
            DatePosition.SIX -> 180f
        }
        val (x, y) = if (config.date == DateMode.FULL) {
            w / 2f to face.cy + 0.42f * face.rMin
        } else {
            val a = Math.toRadians(angle.toDouble()).toFloat()
            face.cx + sin(a) * 0.55f * face.rMin to face.cy - cos(a) * 0.55f * face.rMin
        }
        views.setViewPadding(
            R.id.date_slot,
            (x - sizePx * 1.2f).roundToInt().coerceAtLeast(0),
            (y - sizePx * 0.7f).roundToInt().coerceAtLeast(0),
            0,
            0,
        )
    }

    private fun Bitmap.toIcon(): Icon {
        val out = ByteArrayOutputStream(BUFFER)
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        compress(format, 100, out)
        val bytes = out.toByteArray()
        return Icon.createWithData(bytes, 0, bytes.size)
    }

    private const val BUFFER = 64 * 1024
}
