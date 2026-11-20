package app.expressiveclock.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.PaletteStyle
import com.materialkolor.material3.toColorScheme
import com.materialkolor.ktx.toDynamicScheme
import androidx.core.view.WindowCompat
import app.expressiveclock.data.DarkMode
import app.expressiveclock.data.ThemeSettings

@Composable
fun ClockTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val dark = when (settings.darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }

    val context = LocalContext.current
    val base = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        settings.dynamicColor -> if (dark) settings.palette.dark else settings.palette.light
        else -> settings.seedColor.toDynamicScheme(
            dark,
            settings.paletteStyle.toPaletteStyle(),
            settings.contrast.toContrastLevel().value,
            settings.colorSpec.toColorSpec(),
            com.materialkolor.dynamiccolor.DynamicScheme.Platform.PHONE,
        ).toColorScheme()
    }
    val amoled = dark && settings.amoledBlack
    val scheme = remember(base, amoled) { if (amoled) base.toAmoledBlack() else base }

    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(dark, activity) {
        val bar = if (dark) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = bar, navigationBarStyle = bar)
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = AndroidColor.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.navigationBarDividerColor = AndroidColor.TRANSPARENT
            }
        }
    }

    CompositionLocalProvider(
        LocalTonalElevationEnabled provides !amoled,
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            typography = remember { clockTypography() },
            content = content,
        )
    }
}

fun ColorScheme.toAmoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF141018),
    scrim = Color.Black,
)

fun String.toPaletteStyle(): PaletteStyle = try {
    PaletteStyle.Companion.fromName(this)
} catch (_: Exception) {
    PaletteStyle.TonalSpot
}

fun String.toColorSpec(): ColorSpec.SpecVersion = try {
    ColorSpec.SpecVersion.valueOf(this)
} catch (_: Exception) {
    ColorSpec.SpecVersion.SPEC_2021
}

fun String.toContrastLevel(): com.materialkolor.Contrast = try {
    com.materialkolor.Contrast.valueOf(this)
} catch (_: Exception) {
    com.materialkolor.Contrast.Default
}
