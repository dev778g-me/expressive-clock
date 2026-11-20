package com.dev.expressiveclock.ui.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.graphics.shapes.RoundedPolygon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dev.expressiveclock.overlay.OverlayAccessibilityService
import com.dev.expressiveclock.ui.screens.settings.joinedBlockShape

/**
 * Dedicated first-launch setup screen for Expressive Clock permissions.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isNotificationsGranted by remember { mutableStateOf(checkNotificationsGranted(context)) }
    var isFullScreenIntentGranted by remember { mutableStateOf(checkFullScreenIntentGranted(context)) }
    var isBackgroundGranted by remember { mutableStateOf(checkBackgroundGranted(context)) }
    var isAccessibilityGranted by remember { mutableStateOf(checkAccessibilityGranted(context)) }

    fun refreshStates() {
        isNotificationsGranted = checkNotificationsGranted(context)
        isFullScreenIntentGranted = checkFullScreenIntentGranted(context)
        isBackgroundGranted = checkBackgroundGranted(context)
        isAccessibilityGranted = checkAccessibilityGranted(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        refreshStates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(28.dp))

        Text(
            text = "Welcome to\nExpressive Clock",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "A few quick permissions will make sure alarms, timers and the Dynamic Island work reliably, even when the app is closed.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(28.dp))

        OnboardingSetupCard(
            title = "Notifications",
            description = "Allows Expressive Clock to show Alarm, Timer and Stopwatch notifications, including when the app is closed.",
            icon = Icons.Outlined.NotificationsNone,
            iconBgColor = Color(0xFFFFDADA),
            iconTintColor = Color(0xFFBA1A1A),
            buttonBgColor = Color(0xFFFFDAD6),
            buttonTextColor = Color(0xFF410002),
            isGranted = isNotificationsGranted,
            iconShape = MaterialShapes.Sunny,
            isFirst = true,
            isLast = false,
            onSetup = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    launchIntent(
                        context,
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                }
            },
        )

        Spacer(Modifier.height(2.dp))

        OnboardingSetupCard(
            title = "Full-screen alarms",
            description = "Allows ringing alarms to display full-screen over your lock screen. If unavailable, alarms will display as high-priority heads-up notifications with dismiss and snooze controls.",
            icon = Icons.Outlined.Alarm,
            iconBgColor = Color(0xFFFFDCCA),
            iconTintColor = Color(0xFF8B4A00),
            buttonBgColor = Color(0xFFFFDCCA),
            buttonTextColor = Color(0xFF3B1A00),
            isGranted = isFullScreenIntentGranted,
            iconShape = MaterialShapes.Cookie9Sided,
            onSetup = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    launchIntent(context, intent) {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    }
                } else {
                    launchIntent(
                        context,
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                }
            },
        )

        Spacer(Modifier.height(2.dp))

        OnboardingSetupCard(
            title = "Background operation",
            description = "Prevent Android from restricting Expressive Clock so your alarms, timers and stopwatch keep running reliably.",
            icon = Icons.Outlined.BatteryChargingFull,
            iconBgColor = Color(0xFFD8EED8),
            iconTintColor = Color(0xFF1E631E),
            buttonBgColor = Color(0xFFD8EED8),
            buttonTextColor = Color(0xFF072107),
            isGranted = isBackgroundGranted,
            iconShape = MaterialShapes.Puffy,
            onSetup = {
                Log.d("DynamicIslandOverlay", "SETUP BATTERY DIRECT REQUEST")
                @Suppress("BATTERY_CHARGING_STATE")
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                launchIntent(context, intent) {
                    Log.d("DynamicIslandOverlay", "SETUP BATTERY FALLBACK")
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                }
            },
        )

        Spacer(Modifier.height(2.dp))

        OnboardingSetupCard(
            title = "Accessibility (optional)",
            description = "Enable the Dynamic Island to show active timers and stopwatch controls over other apps, with interactive controls (e.g. pause, lap, dismiss). No personal data or screen content is read or collected.",
            icon = Icons.Outlined.AccessibilityNew,
            iconBgColor = Color(0xFFD7E3FF),
            iconTintColor = Color(0xFF13428B),
            buttonBgColor = Color(0xFFD7E3FF),
            buttonTextColor = Color(0xFF001B3F),
            isGranted = isAccessibilityGranted,
            iconShape = MaterialShapes.Flower,
            isFirst = false,
            isLast = true,
            onSetup = {
                Log.d("DynamicIslandOverlay", "SETUP ACCESSIBILITY REQUEST")
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                val launched = launchIntent(context, intent)
                if (launched) {
                    Toast.makeText(context, "Tap Expressive Clock and turn on Accessibility.", Toast.LENGTH_LONG).show()
                }
            },
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shapes = ButtonDefaults.shapes(),

        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shapes = ButtonDefaults.shapes(),
            onClick = onComplete
        ) {
            Text(
                text = "Set up later",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                ),
            )
        }

        Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OnboardingSetupCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    buttonBgColor: Color,
    buttonTextColor: Color,
    isGranted: Boolean,
    iconShape: RoundedPolygon = MaterialShapes.Heart,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onSetup: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(enabled = !isGranted, onClick = onSetup)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(iconShape.toShape())
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Button(
                        onClick = onSetup,
                        enabled = !isGranted,
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGranted) MaterialTheme.colorScheme.surfaceContainerHigh
                            else buttonBgColor,
                            contentColor = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else buttonTextColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        ),
                    ) {
                        Text(
                            text = if (isGranted) "Already granted" else "Set up",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            ),
                        )
                        if (!isGranted) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun checkNotificationsGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

private fun checkFullScreenIntentGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.canUseFullScreenIntent() ?: true
    } else {
        true
    }
}

private fun checkBackgroundGranted(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    return pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
}

fun checkAccessibilityGranted(context: Context): Boolean {
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

    val expectedComponent = ComponentName(context, OverlayAccessibilityService::class.java)
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledComponent = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponent != null && enabledComponent == expectedComponent) {
            return true
        }
    }
    return false
}

private fun launchIntent(context: Context, intent: Intent, fallbackProvider: (() -> Intent)? = null): Boolean {
    val pm = context.packageManager
    val isIgnoringBefore = checkBackgroundGranted(context)
    Log.d("ExpressiveClock.BatteryOptimization", "launchIntent: isIgnoringBefore=$isIgnoringBefore, action=${intent.action}, data=${intent.data}")

    val activity = context as? Activity
    val result = runCatching {
        val resolved = intent.resolveActivity(pm)
        Log.d("ExpressiveClock.BatteryOptimization", "launchIntent: intent.resolveActivity = $resolved")

        if (activity != null) {
            activity.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        Log.d("ExpressiveClock.BatteryOptimization", "launchIntent: startActivity SUCCESS for action=${intent.action}")
        true
    }.getOrElse { ex ->
        Log.e("ExpressiveClock.BatteryOptimization", "launchIntent: startActivity FAILED for action=${intent.action}", ex)
        val fallback = fallbackProvider?.invoke()
        if (fallback != null) {
            runCatching {
                val fallbackResolved = fallback.resolveActivity(pm)
                Log.d("ExpressiveClock.BatteryOptimization", "launchIntent: FALLBACK intent.resolveActivity = $fallbackResolved")
                if (activity != null) {
                    activity.startActivity(fallback)
                } else {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                }
                Log.d("ExpressiveClock.BatteryOptimization", "launchIntent: FALLBACK startActivity SUCCESS for action=${fallback.action}")
                true
            }.getOrElse { fallbackEx ->
                Log.e("ExpressiveClock.BatteryOptimization", "launchIntent: FALLBACK startActivity FAILED", fallbackEx)
                false
            }
        } else false
    }
    return result
}
