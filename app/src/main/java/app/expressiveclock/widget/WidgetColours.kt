package app.expressiveclock.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import app.expressiveclock.data.ClockSettings
import app.expressiveclock.data.ColourSource
import app.expressiveclock.data.DarkMode
import app.expressiveclock.data.WidgetConfig
import app.expressiveclock.ui.theme.toAmoledBlack

object WidgetColours {

    fun resolve(context: Context, config: WidgetConfig, appSettings: ClockSettings): FaceRenderer.Colours {
        val dark = when {
            config.colour == ColourSource.FOLLOW_APP -> when (appSettings.theme.darkMode) {
                DarkMode.SYSTEM -> systemDark(context)
                DarkMode.LIGHT -> false
                DarkMode.DARK -> true
            }
            else -> systemDark(context)
        }

        val base: ColorScheme = when {
            config.colour == ColourSource.FOLLOW_APP -> {
                val t = appSettings.theme
                if (t.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamic(context, dark)
                } else if (dark) t.palette.dark else t.palette.light
            }
            config.colour == ColourSource.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamic(context, dark)
            else -> if (dark) config.palette.dark else config.palette.light
        }

        val amoled = config.colour == ColourSource.FOLLOW_APP && appSettings.theme.amoledBlack
        val scheme = if (dark && amoled) base.toAmoledBlack() else base

        val primaryAccent = if (config.analogPrimaryColor != null) {
            config.analogPrimaryColor.toInt()
        } else if (config.colour == ColourSource.PALETTE) {
            config.swatch.colorArgb.toInt()
        } else {
            scheme.primary.toArgb()
        }

        return FaceRenderer.Colours(
            face = scheme.surfaceContainerHigh.toArgb(),
            onFace = scheme.onSurface.toArgb(),
            minor = scheme.onSurfaceVariant.toArgb(),
            outline = scheme.outlineVariant.toArgb(),
            accent = primaryAccent,
            hourHand = primaryAccent,
            minuteHand = scheme.tertiary.toArgb(),
            secondHand = config.analogSecondHandColor?.toInt() ?: scheme.secondary.toArgb(),
            pin = scheme.onSurface.toArgb(),
        )
    }

    private fun dynamic(context: Context, dark: Boolean): ColorScheme =
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    private fun systemDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}
