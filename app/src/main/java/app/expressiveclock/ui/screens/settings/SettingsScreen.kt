package app.expressiveclock.ui.screens.settings

import android.R
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.outlined.Home
import app.expressiveclock.data.DefaultStartupPage
import app.expressiveclock.data.SILENT
import app.expressiveclock.data.formatPresetLabel
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Expand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.ScrollField
import androidx.compose.material3.rememberScrollFieldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.expressiveclock.ui.screens.checkAccessibilityGranted
import app.expressiveclock.ui.screens.themePresetLabel
import app.expressiveclock.ui.sheets.AlarmSheetShape
import app.expressiveclock.ui.sheets.SheetHeaderButton
import app.expressiveclock.ui.sheets.ThemePresetSheet
import app.expressiveclock.ui.sheets.TimerPresetsSheet
import app.expressiveclock.data.ClockSettings
import app.expressiveclock.data.DEFAULT_TIMER_PRESETS
import app.expressiveclock.data.DarkMode
import app.expressiveclock.data.FACTORY_OVERLAY_HEIGHT_OFFSET
import app.expressiveclock.data.FACTORY_OVERLAY_OFFSET_X
import app.expressiveclock.data.FACTORY_OVERLAY_OFFSET_Y
import app.expressiveclock.data.FACTORY_OVERLAY_WIDTH_OFFSET
import app.expressiveclock.data.WeekStart
import app.expressiveclock.overlay.OverlayAccessibilityService
import app.expressiveclock.overlay.OverlayManager
import app.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import app.expressiveclock.ui.responsive.TwoPaneLayout
import androidx.core.net.toUri

@Composable
fun SettingsScreen(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onBack: () -> Unit,
    onReopenOnboarding: () -> Unit = {},
) {
    val context = LocalContext.current
    var picker by remember { mutableStateOf<SettingsPicker?>(null) }
    var showThemePresets by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showAccessibilityPromptDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val fadeDistance = with(LocalDensity.current) { 32.dp.toPx() }
    val headerFalloff by animateFloatAsState(
        targetValue = (scrollState.value / fadeDistance).coerceIn(0f, 1f),
        label = "settingsHeaderFalloff",
    )

    val appUpdateManager = remember(context) { AppUpdateManagerFactory.create(context) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    DisposableEffect(appUpdateManager) {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Toast.makeText(context, "Update ready to install.", Toast.LENGTH_LONG).show()
                appUpdateManager.completeUpdate()
            }
        }
        appUpdateManager.registerListener(listener)
        onDispose {
            appUpdateManager.unregisterListener(listener)
        }
    }

    fun checkForUpdates() {
        isCheckingUpdates = true
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            isCheckingUpdates = false
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                val activity = context as? Activity
                if (activity != null) {
                    runCatching {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            activity,
                            9001,
                        )
                    }.onFailure {
                        Toast.makeText(context, "Couldn't check for updates.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Couldn't check for updates.", Toast.LENGTH_SHORT).show()
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Toast.makeText(context, "An update is already in progress.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "You're up to date", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { ex ->
            isCheckingUpdates = false
            Log.e("SettingsScreen", "Failed to check for updates", ex)
            Toast.makeText(context, "Couldn't check for updates.", Toast.LENGTH_SHORT).show()
        }
    }

    val timerSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val uriString = picked?.toString() ?: SILENT
            onChange { it.copy(timers = it.timers.copy(soundUri = uriString)) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },

            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val category: SettingsCategory? = null
                SettingsCard(title = "Appearance") {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expandedSettingsTile()
                            .clip(joinedBlockShape(isFirst = true, isLast = false))
                            .padding(vertical = 1.dp),
                        shapes = ListItemDefaults.shapes(
                            shape = joinedBlockShape(isFirst = true, isLast = false),
                        ),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        leadingContent = {
                            SettingIcon(Icons.Outlined.SettingsBrightness)
                        },
                        supportingContent = {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            ) {
                                DarkMode.entries.forEachIndexed { index, mode ->
                                    SegmentedButton(
                                        selected = settings.theme.darkMode == mode,
                                        onClick = {
                                            onChange { it.copy(theme = it.theme.copy(darkMode = mode)) }
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = DarkMode.entries.size,
                                        ),
                                    ) {
                                        Text(mode.label, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        },
                    ) {
                        Text("Theme")
                    }
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Contrast,
                        title = "Pure black",
                        subtitle = "Use true black surfaces in dark mode",
                        checked = settings.theme.amoledBlack,
                        isFirst = false,
                        isLast = false,
                        onCheckedChange = { value ->
                            onChange { it.copy(theme = it.theme.copy(amoledBlack = value)) }
                        },
                    )
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Palette,
                        title = "Dynamic theme",
                        subtitle = "Use wallpaper colours",
                        checked = settings.theme.dynamicColor,
                        isFirst = false,
                        isLast = settings.theme.dynamicColor,
                        onCheckedChange = { value ->
                            onChange { it.copy(theme = it.theme.copy(dynamicColor = value)) }
                        },
                    )
                    AnimatedVisibility(
                        visible = !settings.theme.dynamicColor,
                        enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                        exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SettingsNavigateRow(
                            icon = Icons.Outlined.Palette,
                            title = "Theme presets",
                            subtitle = themePresetLabel(settings.theme),
                            isFirst = false,
                            isLast = true,
                            onClick = { showThemePresets = true },
                        )
                    }
                }

                if (showThemePresets) {
                    ThemePresetSheet(
                        theme = settings.theme,
                        isDark = when (settings.theme.darkMode) {
                            DarkMode.SYSTEM -> isSystemInDarkTheme()
                            DarkMode.LIGHT -> false
                            DarkMode.DARK -> true
                        },
                        onThemeChange = { next -> onChange { it.copy(theme = next) } },
                        onDismiss = { showThemePresets = false },
                    )
                }

                SettingsCard(title = "Alarms") {
                    SettingsNavigateRow(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Silence after",
                        subtitle = minutesLabel(settings.alarms.silenceAfterMinutes),
                        isFirst = true,
                        onClick = { picker = SettingsPicker.SILENCE },
                    )
    SettingsNavigateRow(
                        icon = Icons.Outlined.Snooze,
                        title = "Snooze length",
                        subtitle = minutesLabel(settings.alarms.snoozeMinutes),
                        onClick = { picker = SettingsPicker.SNOOZE },
                    )
    SettingsNavigateRow(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Start week on",
                        subtitle = settings.alarms.weekStart.label,
                        isLast = true,
                        onClick = { picker = SettingsPicker.WEEK_START },
                    )
                }

                SettingsCard(title = "Timers") {
                    val timerSoundSubtitle = remember(settings.timers.soundUri) {
                        when (val u = settings.timers.soundUri) {
                            null -> "Default alarm sound"
                            SILENT -> "Silent"
                            else -> runCatching {
                                RingtoneManager.getRingtone(context, u.toUri())?.getTitle(context)
                            }.getOrNull() ?: "Default alarm sound"
                        }
                    }
                    SettingsNavigateRow(
                        icon = Icons.Outlined.MusicNote,
                        title = "Timer sound",
                        subtitle = timerSoundSubtitle,
                        isFirst = true,
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
                            timerSoundLauncher.launch(intent)
                        },
                    )
    SettingsNavigateRow(
                        icon = Icons.Outlined.Timer,
                        title = "Timer presets",
                        subtitle = settings.timers.presets.joinToString(", ") { formatPresetLabel(it) },
                        onClick = { picker = SettingsPicker.PRESETS },
                    )
    SettingsSwitchRow(
                        icon = Icons.Outlined.HourglassEmpty,
                        title = "Timer vibration",
                        subtitle = "Vibrate when a timer finishes",
                        checked = settings.timers.vibrate,
                        isFirst = false,
                        isLast = false,
                        onCheckedChange = { value ->
                            onChange { it.copy(timers = it.timers.copy(vibrate = value)) }
                        },
                    )
    val isAccConnected = checkAccessibilityGranted(context)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Timer,
                        title = "Dynamic Island overlay",
                        subtitle = "Show timer & stopwatch controls over other apps",
                        checked = settings.theme.dynamicIsland,
                        isLast = true,
                        onCheckedChange = { enable ->
                            if (enable) {
                                onChange { it.copy(theme = it.theme.copy(dynamicIsland = true)) }
                                if (!isAccConnected) {
                                    showAccessibilityPromptDialog = true
                                }
                            } else {
                                onChange { it.copy(theme = it.theme.copy(dynamicIsland = false)) }
                            }
                        },
                    )
                    AnimatedVisibility(
                        visible = settings.theme.dynamicIsland,
                        enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                        exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    ) {
                        Column {
    SettingsNavigateRow(
                                icon = Icons.Outlined.Timer,
                                title = "Position calibration",
                                subtitle = "Fine-tune horizontal & vertical alignment",
                                onClick = { showCalibrationDialog = true },
                            )
                            if (!isAccConnected) {
    SettingsNavigateRow(
                                    icon = Icons.Outlined.Timer,
                                    title = "Enable status bar overlay",
                                    subtitle = "Draw above system status bars via Accessibility",
                                    onClick = { showAccessibilityPromptDialog = true },
                                )
                            }
                        }
                    }
                }

                SettingsCard(title = "MISCELLANEOUS") {
                    SettingsNavigateRow(
                        icon = Icons.Outlined.Home,
                        title = "Default startup page",
                        subtitle = settings.defaultStartupPage.label,
                        isFirst = true,
                        onClick = { picker = SettingsPicker.STARTUP_PAGE },
                    )
    SettingsNavigateRow(
                        icon = Icons.Outlined.HourglassEmpty,
                        title = "Show setup again",
                        subtitle = "Reopen the first-launch permissions setup",
                        isLast = true,
                        onClick = onReopenOnboarding,
                    )
                }

                SettingsCard(title = "About") {
                    SettingsNavigateRow(
                        icon = SimpleIconsGithub,
                        title = "View source on GitHub",
                        subtitle = "Star the project and read the code",
                        isFirst = true,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/dev778g-me/expressive-clock"),
                            )
                            runCatching { context.startActivity(intent) }
                                .onFailure {
                                    Toast.makeText(context, "Couldn't open GitHub.", Toast.LENGTH_SHORT).show()
                                }
                        },
                    )
    SettingsNavigateRow(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy",
                        subtitle = "No accounts, no telemetry, no network",
                        onClick = { picker = SettingsPicker.PRIVACY },
                    )

    SettingsNavigateRow(
                        icon = Icons.Outlined.Info,
                        title = "App version",
                        subtitle = "Clock ${app.expressiveclock.BuildConfig.VERSION_NAME}",
                        isLast = true,
                        onClick = {},
                    )
                }

            Spacer(Modifier.height(4.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

    picker?.let { current ->
        when (current) {
            SettingsPicker.THEME -> ThemePickerDialog(
                value = settings.theme.darkMode,
                onDismiss = { picker = null },
                onSelect = { value ->
                    onChange { it.copy(theme = it.theme.copy(darkMode = value)) }
                    picker = null
                },
            )
            SettingsPicker.SILENCE -> ChoicePickerSheet(
                title = "Silence after",
                options = listOf(1, 5, 10, 15, 20, 25, 30, 0),
                selected = settings.alarms.silenceAfterMinutes,
                label = ::minutesLabel,
                onDismiss = { picker = null },
                onSelect = { value ->
                    onChange { it.copy(alarms = it.alarms.copy(silenceAfterMinutes = value)) }
                    picker = null
                },
            )
            SettingsPicker.SNOOZE -> ChoicePickerSheet(
                title = "Snooze length",
                options = listOf(1, 5, 10, 15, 20, 30),
                selected = settings.alarms.snoozeMinutes,
                label = ::minutesLabel,
                onDismiss = { picker = null },
                onSelect = { value ->
                    onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = value)) }
                    picker = null
                },
            )
            SettingsPicker.WEEK_START -> ChoicePickerSheet(
                title = "Start week on",
                options = WeekStart.entries,
                selected = settings.alarms.weekStart,
                label = { it.label },
                onDismiss = { picker = null },
                onSelect = { value ->
                    onChange { it.copy(alarms = it.alarms.copy(weekStart = value)) }
                    picker = null
                },
            )
            SettingsPicker.PRESETS -> TimerPresetsSheet(
                presets = settings.timers.presets,
                onDismiss = { picker = null },
                onSave = { newPresets ->
                    onChange { it.copy(timers = it.timers.copy(presets = newPresets)) }
                    picker = null
                },
            )
            SettingsPicker.STARTUP_PAGE -> ChoicePickerSheet(
                title = "Default startup page",
                options = DefaultStartupPage.entries,
                selected = settings.defaultStartupPage,
                label = { it.label },
                onDismiss = { picker = null },
                onSelect = { selected ->
                    onChange { it.copy(defaultStartupPage = selected) }
                    picker = null
                },
            )
            SettingsPicker.PRIVACY -> PrivacyDialog(onDismiss = { picker = null })
        }
    }

    if (showCalibrationDialog) {
        OverlayCalibrationDialog(
            settings = settings,
            onDismiss = { showCalibrationDialog = false },
            onChange = onChange,
        )
    }

    if (showAccessibilityPromptDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityPromptDialog = false },
            title = { Text("Accessibility service required") },
            text = {
                Text(
                    "Expressive Clock uses Android's AccessibilityService API only to display " +
                        "active timer countdowns and stopwatch controls over other apps and system bars. " +
                        "The service does not read screen content, collect personal or sensitive user data, " +
                        "or share data. If you choose Continue, Android will open Accessibility settings " +
                        "where you can enable the service. This access is optional and is used for the " +
                        "Dynamic Island overlay feature.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAccessibilityPromptDialog = false
                        runCatching {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            Toast.makeText(context, "Tap Expressive Clock and turn on Accessibility.", Toast.LENGTH_LONG).show()
                        }
                    },
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAccessibilityPromptDialog = false
                        onChange { it.copy(theme = it.theme.copy(dynamicIsland = false)) }
                    },
                ) {
                    Text("Not now")
                }
            },
        )
    }
}
