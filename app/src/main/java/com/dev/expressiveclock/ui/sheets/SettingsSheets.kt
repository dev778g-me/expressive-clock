package com.dev.expressiveclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.expressiveclock.data.ClockSettings
import com.dev.expressiveclock.data.DarkMode
import com.dev.expressiveclock.data.HourFormat
import com.dev.expressiveclock.data.SILENT
import com.dev.expressiveclock.data.WeekStart
import com.dev.expressiveclock.data.formatPresetLabel
import com.dev.expressiveclock.ui.theme.Palette
import com.dev.expressiveclock.widget.ClockWidgetProvider
import com.dev.expressiveclock.widget.WidgetEntryPoints

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetTitle("Settings")

            SectionLabel("Appearance")
            ChoiceRow(
                title = "Theme",
                value = settings.theme.darkMode,
                options = DarkMode.entries,
                label = { it.label },
                onSelect = { value -> onChange { it.copy(theme = it.theme.copy(darkMode = value)) } },
            )
            SwitchRow(
                title = "Pure black",
                subtitle = "Use true black surfaces in dark mode",
                checked = settings.theme.amoledBlack,
                onChange = { value -> onChange { it.copy(theme = it.theme.copy(amoledBlack = value)) } },
            )
            PaletteRow(
                dynamic = settings.theme.dynamicColor,
                selected = settings.theme.palette,
                onDynamic = { onChange { it.copy(theme = it.theme.copy(dynamicColor = true)) } },
                onSelect = { value ->
                    onChange { it.copy(theme = it.theme.copy(palette = value, dynamicColor = false)) }
                },
            )

            SectionLabel("Alarms")
            ChoiceRow(
                title = "Silence after",
                value = settings.alarms.silenceAfterMinutes,
                options = listOf(1, 5, 10, 15, 20, 25, 30, 0),
                label = ::minutesLabel,
                onSelect = { value -> onChange { it.copy(alarms = it.alarms.copy(silenceAfterMinutes = value)) } },
            )
            ChoiceRow(
                title = "Snooze length",
                value = settings.alarms.snoozeMinutes,
                options = listOf(1, 5, 10, 15, 20, 30),
                label = ::minutesLabel,
                onSelect = { value -> onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = value)) } },
            )
            ChoiceRow(
                title = "Start week on",
                value = settings.alarms.weekStart,
                options = WeekStart.entries,
                label = { it.label },
                onSelect = { value -> onChange { it.copy(alarms = it.alarms.copy(weekStart = value)) } },
            )

            SectionLabel("Timers")
            val context = LocalContext.current
            var showPresetsDialog by remember { mutableStateOf(false) }
            val timerPicker = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    }
                    onChange { it.copy(timers = it.timers.copy(soundUri = picked?.toString() ?: SILENT)) }
                }
            }
            NavigateRow(
                title = "Timer sound",
                subtitle = when (val u = settings.timers.soundUri) {
                    null -> "Default alarm sound"
                    SILENT -> "Silent"
                    else -> runCatching {
                        RingtoneManager.getRingtone(context, Uri.parse(u))?.getTitle(context)
                    }.getOrNull() ?: "Default alarm sound"
                },
                onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Timer sound")
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        val currentUri = settings.timers.soundUri
                        if (currentUri != SILENT) {
                            val uriToPass = currentUri?.let(Uri::parse)
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uriToPass)
                        }
                    }
                    timerPicker.launch(intent)
                },
            )
            NavigateRow(
                title = "Timer presets",
                subtitle = settings.timers.presets.joinToString(", ") { formatPresetLabel(it) },
                onClick = { showPresetsDialog = true },
            )
            SwitchRow(
                title = "Timer vibration",
                checked = settings.timers.vibrate,
                onChange = { value -> onChange { it.copy(timers = it.timers.copy(vibrate = value)) } },
            )

            if (showPresetsDialog) {
                com.dev.expressiveclock.ui.sheets.TimerPresetsSheet(
                    presets = settings.timers.presets,
                    onDismiss = { showPresetsDialog = false },
                    onSave = { newPresets ->
                        onChange { it.copy(timers = it.timers.copy(presets = newPresets)) }
                        showPresetsDialog = false
                    },
                )
            }

            AboutRows()
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetTitle("Alarm settings")

            ChoiceRow(
                title = "Silence after",
                value = settings.alarms.silenceAfterMinutes,
                options = listOf(1, 5, 10, 15, 20, 25, 30, 0),
                label = ::minutesLabel,
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(silenceAfterMinutes = v)) } },
            )
            ChoiceRow(
                title = "Snooze length",
                value = settings.alarms.snoozeMinutes,
                options = listOf(1, 5, 10, 15, 20, 30),
                label = ::minutesLabel,
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = v)) } },
            )
            FullScreenIntentRow()
            ChoiceRow(
                title = "Start week on",
                value = settings.alarms.weekStart,
                options = WeekStart.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(weekStart = v)) } },
            )

        }
    }
}

@Composable
private fun WidgetRows() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (WidgetEntryPoints.canPin(context)) {
                        WidgetEntryPoints.requestPin(context, isDigital = false)
                    }
                },
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Analog Clock Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Analog Clock",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = "2 × 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = "A clean, expressive analog clock for your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (WidgetEntryPoints.canPin(context)) {
                        WidgetEntryPoints.requestPin(context, isDigital = true)
                    }
                },
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Digital Clock Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Digital Clock",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = "2 × 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = "A beautiful digital clock for your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FullScreenIntentRow() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val context = LocalContext.current
    val nm = context.getSystemService(android.app.NotificationManager::class.java)
    if (nm?.canUseFullScreenIntent() != false) return
    NavigateRow(
        title = "Full-screen alarms",
        subtitle = "Not allowed, so a ringing alarm will show as a banner instead",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(android.net.Uri.parse("package:${context.packageName}"))
                )
            }
        },
    )
}

@Composable
private fun PaletteRow(
    dynamic: Boolean,
    selected: Palette,
    onDynamic: () -> Unit,
    onSelect: (Palette) -> Unit,
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val context = LocalContext.current
    val wallpaper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        } else {
            androidx.compose.material3.dynamicLightColorScheme(context)
        }
    } else {
        null
    }

    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (wallpaper != null) {
            item(key = "wallpaper") {
                Swatch("Wallpaper", wallpaper, dynamic, onDynamic)
            }
        }
        items(Palette.entries, key = { it.name }) { palette ->
            Swatch(
                name = palette.displayName,
                scheme = if (dark) palette.dark else palette.light,
                selected = !dynamic && palette == selected,
                onClick = { onSelect(palette) },
            )
        }
    }
}

@Composable
private fun Swatch(
    name: String,
    scheme: androidx.compose.material3.ColorScheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
.background(scheme.primary)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetTitle("Appearance")
            ChoiceRow(
                title = "Theme",
                value = settings.theme.darkMode,
                options = DarkMode.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(theme = it.theme.copy(darkMode = v)) } },
            )
            SwitchRow(
                title = "Pure black",
                subtitle = "True black surfaces in dark mode, for OLED screens",
                checked = settings.theme.amoledBlack,
                onChange = { v -> onChange { it.copy(theme = it.theme.copy(amoledBlack = v)) } },
            )

            PaletteRow(
                dynamic = settings.theme.dynamicColor,
                selected = settings.theme.palette,
                onDynamic = { onChange { it.copy(theme = it.theme.copy(dynamicColor = true)) } },
                onSelect = { v ->
                    onChange { it.copy(theme = it.theme.copy(palette = v, dynamicColor = false)) }
                },
            )

            SectionLabel("Widgets")
            WidgetRows()

            AboutRows()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            SheetTitle("World clock settings")
            SwitchRow(
                title = "Display time with seconds",
                checked = settings.world.showSeconds,
                onChange = { v -> onChange { it.copy(world = it.world.copy(showSeconds = v)) } },
            )
            ChoiceRow(
                title = "Time format",
                value = settings.world.hourFormat,
                options = HourFormat.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(world = it.world.copy(hourFormat = v)) } },
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var showPresetsDialog by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            onChange { it.copy(timers = it.timers.copy(soundUri = picked?.toString() ?: SILENT)) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            SheetTitle("Timer settings")
            NavigateRow(
                title = "Timer sound",
                subtitle = when (val u = settings.timers.soundUri) {
                    null -> "Default alarm sound"
                    SILENT -> "Silent"
                    else -> runCatching {
                        RingtoneManager.getRingtone(context, Uri.parse(u))?.getTitle(context)
                    }.getOrNull() ?: "Default alarm sound"
                },
                onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Timer sound")
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        val currentUri = settings.timers.soundUri
                        if (currentUri != SILENT) {
                            val uriToPass = currentUri?.let(Uri::parse)
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uriToPass)
                        }
                    }
                    picker.launch(intent)
                },
            )
            NavigateRow(
                title = "Timer presets",
                subtitle = settings.timers.presets.joinToString(", ") { formatPresetLabel(it) },
                onClick = { showPresetsDialog = true },
            )
            SwitchRow(
                title = "Timer vibrate",
                checked = settings.timers.vibrate,
                onChange = { v -> onChange { it.copy(timers = it.timers.copy(vibrate = v)) } },
            )
        }
    }

    if (showPresetsDialog) {
        com.dev.expressiveclock.ui.sheets.TimerPresetsSheet(
            presets = settings.timers.presets,
            onDismiss = { showPresetsDialog = false },
            onSave = { newPresets ->
                onChange { it.copy(timers = it.timers.copy(presets = newPresets)) }
                showPresetsDialog = false
            },
        )
    }
}

/** 0 is never, 1 is singular. Both of these read wrong the moment they are not special-cased. */
private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}
