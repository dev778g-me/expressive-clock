package app.expressiveclock.ui.screens

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
import app.expressiveclock.ui.sheets.AlarmSheetShape
import app.expressiveclock.ui.sheets.TimerPresetsSheet
import app.expressiveclock.ui.sheets.SheetHeaderButton
import app.expressiveclock.ui.sheets.ThemePresetSheet
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
                        // With the wallpaper theme on, everything below collapses and this
                        // becomes the last row in the card — so its bottom corners round.
                        isLast = settings.theme.dynamicColor,
                        onCheckedChange = { value ->
                            onChange { it.copy(theme = it.theme.copy(dynamicColor = value)) }
                        },
                    )
                    // Wallpaper colour off? The preset row is the only thing left, so it carries
                    // the bottom corners of the card. Wallpaper on? It leaves with a vertical
                    // collapse rather than popping out.
                    AnimatedVisibility(
                        visible = !settings.theme.dynamicColor,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
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

            if (category == null || category == SettingsCategory.ALARMS) SettingsCard(title = "Alarms") {
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

            if (category == null || category == SettingsCategory.TIMERS) SettingsCard(title = "Timers") {
                val timerSoundSubtitle = remember(settings.timers.soundUri) {
                    when (val u = settings.timers.soundUri) {
                        null -> "Default alarm sound"
                        SILENT -> "Silent"
                        else -> runCatching {
                            RingtoneManager.getRingtone(context, Uri.parse(u))?.getTitle(context)
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
                    enter = expandVertically(animationSpec = spring(stiffness = 450f, dampingRatio = 0.8f)) + fadeIn(animationSpec = tween(180)),
                    exit = shrinkVertically(animationSpec = spring(stiffness = 450f, dampingRatio = 0.8f)) + fadeOut(animationSpec = tween(150)),
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

            if (category == null || category == SettingsCategory.MISCELLANEOUS) SettingsCard(title = "MISCELLANEOUS") {
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

            if (category == null || category == SettingsCategory.ABOUT) SettingsCard(title = "About") {
                SettingsNavigateRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    subtitleContent = {
                        AnimatedContent(
                            targetState = if (isCheckingUpdates) "Checking for updates" else "Keep Expressive Clock up to date",
                            transitionSpec = {
                                (slideInVertically(
                                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                    initialOffsetY = { height -> height / 2 },
                                ) + fadeIn(tween(durationMillis = 200))) togetherWith
                                    (slideOutVertically(
                                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                        targetOffsetY = { height -> -height / 2 },
                                    ) + fadeOut(tween(durationMillis = 180)))
                            },
                            label = "updateSubtitleTransition",
                        ) { targetText ->
                            Text(
                                text = targetText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    },
                    isFirst = true,
                    onClick = {
                        if (!isCheckingUpdates) {
                            checkForUpdates()
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
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy policy",
                    subtitle = "Read how Expressive Clock handles your data",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://samuelbeckman.github.io/expressive-clock/privacy-policy.html"),
                        )
                        runCatching { context.startActivity(intent) }
                            .onFailure {
                                Toast.makeText(context, "Couldn't open the privacy policy.", Toast.LENGTH_SHORT).show()
                            }
                    },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChoicePickerSheet(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    icon: ImageVector? = null,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AlarmSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                options.forEachIndexed { index, option ->
                    val isFirst = index == 0
                    val isLast = index == options.lastIndex
                    val  isSelected = option == selected
                    ListItem(
                        selected = isSelected,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(option) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
                            .padding(vertical = 1.dp),

                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTrailingContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shapes = ListItemDefaults.shapes(
                            pressedShape = CircleShape
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        trailingContent = {
                            if (isSelected){
                                Icon(imageVector = Icons.Rounded.Check, "Checked")
                            }
                        }
                    ) {
                        Text(label(option))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private enum class SettingsCategory(val title: String, val subtitle: String, val icon: ImageVector) {
    APPEARANCE("Appearance", "Theme, colours and display", Icons.Outlined.Palette),
    ALARMS("Alarms", "Silence, snooze and calendar", Icons.Outlined.NotificationsNone),
    TIMERS("Timers", "Sounds, presets and vibration", Icons.Outlined.HourglassEmpty),
    MISCELLANEOUS("Miscellaneous", "Startup page and setup", Icons.Outlined.Home),
    ABOUT("About", "Updates and privacy", Icons.Outlined.Info),
}

// Only the expanded presentation changes; the same controls and callbacks serve both layouts.
private val LocalSettingsCategory = staticCompositionLocalOf<SettingsCategory?> { null }



@Composable
private fun Modifier.expandedSettingsTile(): Modifier = if (LocalSettingsCategory.current != null) {
    clip(RoundedCornerShape(22.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerLow)
        .border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = .25f),
            RoundedCornerShape(22.dp)
        )
} else this

@Composable
private fun CalibrationControlRow(
    label: String,
    valueDp: Int,
    minValue: Float,
    maxValue: Float,
    leftIcon: ImageVector,
    rightIcon: ImageVector,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$valueDp dp", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = {
                    val newValue = (valueDp - 1).toFloat().coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = "Decrease $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            Slider(
                value = valueDp.toFloat(),
                onValueChange = { onValueChange(it) },
                valueRange = minValue..maxValue,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = {
                    val newValue = (valueDp + 1).toFloat().coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = "Increase $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OverlayCalibrationDialog(
    settings: ClockSettings,
    onDismiss: () -> Unit,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
) {
    val context = LocalContext.current
    var offsetX by remember { mutableFloatStateOf(settings.theme.overlayOffsetX.toFloat()) }
    var offsetY by remember { mutableFloatStateOf(settings.theme.overlayOffsetY.toFloat()) }
    var widthOffset by remember { mutableFloatStateOf(settings.theme.overlayWidthOffset.toFloat()) }
    var heightOffset by remember { mutableFloatStateOf(settings.theme.overlayHeightOffset.toFloat()) }

    LaunchedEffect(Unit) {
        OverlayManager.showCalibrationPreview(context, settings)
    }

    DisposableEffect(Unit) {
        onDispose {
            OverlayManager.hideCalibrationPreview()
        }
    }

    val updateSettingsAndPreview = { newX: Int, newY: Int, newW: Int, newH: Int ->
        val updated = settings.copy(
            theme = settings.theme.copy(
                overlayOffsetX = newX,
                overlayOffsetY = newY,
                overlayWidthOffset = newW,
                overlayHeightOffset = newH,
            )
        )
        onChange { updated }
        OverlayManager.updateCalibrationPreview(updated)
    }

    val relX = (offsetX - FACTORY_OVERLAY_OFFSET_X).roundToInt()
    val relY = (offsetY - FACTORY_OVERLAY_OFFSET_Y).roundToInt()
    val relW = (widthOffset - FACTORY_OVERLAY_WIDTH_OFFSET).roundToInt()
    val relH = (heightOffset - FACTORY_OVERLAY_HEIGHT_OFFSET).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Overlay position & size calibration") },
        confirmButton = {
            TextButton(
                onClick = {
                    updateSettingsAndPreview(
                        offsetX.roundToInt(),
                        offsetY.roundToInt(),
                        widthOffset.roundToInt(),
                        heightOffset.roundToInt(),
                    )
                    onDismiss()
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    offsetX = FACTORY_OVERLAY_OFFSET_X.toFloat()
                    offsetY = FACTORY_OVERLAY_OFFSET_Y.toFloat()
                    widthOffset = FACTORY_OVERLAY_WIDTH_OFFSET.toFloat()
                    heightOffset = FACTORY_OVERLAY_HEIGHT_OFFSET.toFloat()
                    updateSettingsAndPreview(
                        FACTORY_OVERLAY_OFFSET_X,
                        FACTORY_OVERLAY_OFFSET_Y,
                        FACTORY_OVERLAY_WIDTH_OFFSET,
                        FACTORY_OVERLAY_HEIGHT_OFFSET,
                    )
                },
            ) {
                Text("Reset")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Fine-tune position and size if the capsule does not line up with your camera cutout.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                // 1. Horizontal offset (X Position)
                CalibrationControlRow(
                    label = "Horizontal offset",
                    valueDp = relX,
                    minValue = -50f,
                    maxValue = 50f,
                    leftIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                    rightIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onValueChange = { rel ->
                        offsetX = FACTORY_OVERLAY_OFFSET_X + rel
                        updateSettingsAndPreview(offsetX.roundToInt(), offsetY.roundToInt(), widthOffset.roundToInt(), heightOffset.roundToInt())
                    },
                )

                // 2. Vertical offset (Y Position)
                CalibrationControlRow(
                    label = "Vertical offset",
                    valueDp = relY,
                    minValue = -30f,
                    maxValue = 30f,
                    leftIcon = Icons.Rounded.ArrowUpward,
                    rightIcon = Icons.Rounded.ArrowDownward,
                    onValueChange = { rel ->
                        offsetY = FACTORY_OVERLAY_OFFSET_Y + rel
                        updateSettingsAndPreview(offsetX.roundToInt(), offsetY.roundToInt(), widthOffset.roundToInt(), heightOffset.roundToInt())
                    },
                )

                // 3. Capsule width
                CalibrationControlRow(
                    label = "Capsule width",
                    valueDp = relW,
                    minValue = -60f,
                    maxValue = 60f,
                    leftIcon = Icons.Rounded.Compress,
                    rightIcon = Icons.Rounded.Expand,
                    onValueChange = { rel ->
                        widthOffset = FACTORY_OVERLAY_WIDTH_OFFSET + rel
                        updateSettingsAndPreview(offsetX.roundToInt(), offsetY.roundToInt(), widthOffset.roundToInt(), heightOffset.roundToInt())
                    },
                )

                // 4. Capsule height
                CalibrationControlRow(
                    label = "Capsule height",
                    valueDp = relH,
                    minValue = -20f,
                    maxValue = 20f,
                    leftIcon = Icons.Rounded.Compress,
                    rightIcon = Icons.Rounded.Expand,
                    onValueChange = { rel ->
                        heightOffset = FACTORY_OVERLAY_HEIGHT_OFFSET + rel
                        updateSettingsAndPreview(offsetX.roundToInt(), offsetY.roundToInt(), widthOffset.roundToInt(), heightOffset.roundToInt())
                    },
                )
            }
        },
    )
}

private enum class SettingsPicker { THEME, SILENCE, SNOOZE, WEEK_START, PRIVACY, PRESETS, STARTUP_PAGE }

@Composable
private fun DefaultStartupPageDialog(
    current: DefaultStartupPage,
    onDismiss: () -> Unit,
    onSelect: (DefaultStartupPage) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default startup page") },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column {
                DefaultStartupPage.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (option == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        RadioButton(
                            selected = option == current,
                            onClick = { onSelect(option) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    val category = LocalSettingsCategory.current
    if (category != null) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(category.icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Text(category.title, style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            content()
        }
        return
    }
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding( top = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsNavigateRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    subtitleContent: (@Composable () -> Unit)? = null,
    trailing: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .expandedSettingsTile()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp),
        shapes = ListItemDefaults.shapes(
            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            SettingIcon(icon)
        },
        trailingContent = {
            if (trailing != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        trailing,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            } else {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        supportingContent = {
            if (subtitleContent != null) {
                subtitleContent()
            } else if (subtitle != null) {
                Text(subtitle)
            }
        },
    ) {
        Text(title)
    }
}

internal fun joinedBlockShape(isFirst: Boolean, isLast: Boolean): AbsoluteRoundedCornerShape =
    AbsoluteRoundedCornerShape(
        topLeft = if (isFirst) 16.dp else 4.dp,
        topRight = if (isFirst) 16.dp else 4.dp,
        bottomLeft = if (isLast) 16.dp else 4.dp,
        bottomRight = if (isLast) 16.dp else 4.dp
    )
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
) {

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .expandedSettingsTile()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .clickable(onClick = { onCheckedChange(!checked) })
            .padding(vertical = 1.dp),
        shapes = ListItemDefaults.shapes(
            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            SettingIcon(icon)
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        supportingContent = {
            Text(subtitle)
        },
    ) {
        Text(
            title,
        )
    }

}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp),
    )
}


@Composable
private fun SettingsDivider() {
    if (LocalSettingsCategory.current != null) return
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 76.dp, end = 20.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    )
}

@Composable
private fun <T> ChoicePickerDialog(
    title: String,
    value: T,
    options: List<T>,
    label: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = { onSelect(option) })
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == value, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(label(option), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}

@Composable
private fun ThemePickerDialog(
    value: DarkMode,
    onDismiss: () -> Unit,
    onSelect: (DarkMode) -> Unit,
) {
    ChoicePickerDialog(
        title = "Theme",
        value = value,
        options = DarkMode.entries,
        label = { it.label },
        onDismiss = onDismiss,
        onSelect = onSelect,
    )
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        text = {
            Text(
                "No accounts, analytics, crash reporting or advertising ID. The app has no " +
                    "INTERNET permission, so it cannot make network connections. Your alarms, " +
                    "timers, world clocks and preferences remain on your device.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}

private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}



@Composable
private fun SinglePresetEditDialog(
    title: String,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var minText by remember { mutableStateOf((initialSeconds / 60).toString()) }
    var secText by remember { mutableStateOf((initialSeconds % 60).toString()) }

    val quickOptions = remember {
        listOf(
            15 to "15s",
            30 to "30s",
            60 to "1 min",
            120 to "2 min",
            180 to "3 min",
            300 to "5 min",
            600 to "10 min",
            900 to "15 min",
            1200 to "20 min",
            1800 to "30 min",
            2700 to "45 min",
            3600 to "1 hour",
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                onClick = {
                    val m = minText.toIntOrNull() ?: 0
                    val s = secText.toIntOrNull() ?: 0
                    val total = (m * 60 + s).coerceIn(1, 86399)
                    onConfirm(total)
                },
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = secText,
                        onValueChange = { secText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Seconds") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    "Quick options",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                HorizontalEdgeBlurContainer(fadeLength = 16.dp) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(quickOptions) { (secs, label) ->
                            val currentSecs = (minText.toIntOrNull() ?: 0) * 60 + (secText.toIntOrNull() ?: 0)
                            val isSelected = currentSecs == secs
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                    .clickable {
                                        minText = (secs / 60).toString()
                                        secText = (secs % 60).toString()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun HorizontalEdgeBlurContainer(
    modifier: Modifier = Modifier,
    fadeLength: Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                // Step 1: Draw base scrolling horizontal content
                drawContent()

                val width = size.width
                val height = size.height
                val fadePx = fadeLength.toPx()

                if (width <= 0f || height <= 0f || fadePx <= 0f) return@drawWithContent

                // Step 2: On Android 12+, render genuine progressive hardware blur overlays at left and right edges
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas

                        val blurPaint = Paint().apply {
                            isAntiAlias = true
                            setPaintBlurEffect(this, 24f, Shader.TileMode.DECAL)
                        }

                        // Left edge hardware blur overlay
                        val leftSave = nativeCanvas.saveLayer(
                            0f, 0f, fadePx, height, blurPaint
                        )
                        drawContent()

                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color.Black.copy(alpha = 0.60f),
                                    Color.Transparent,
                                ),
                                startX = 0f,
                                endX = fadePx,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                        nativeCanvas.restoreToCount(leftSave)

                        // Right edge hardware blur overlay
                        val rightStart = width - fadePx
                        if (rightStart > 0f) {
                            val rightSave = nativeCanvas.saveLayer(
                                rightStart, 0f, width, height, blurPaint
                            )
                            drawContent()

                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.60f),
                                        Color.Black,
                                    ),
                                    startX = rightStart,
                                    endX = width,
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                            nativeCanvas.restoreToCount(rightSave)
                        }
                    }
                }

                // Step 3: Draw smooth alpha gradient falloff at left & right edges
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black,
                        ),
                        startX = 0f,
                        endX = fadePx,
                    ),
                    blendMode = BlendMode.DstIn,
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Transparent,
                        ),
                        startX = width - fadePx,
                        endX = width,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun setPaintBlurEffect(paint: Paint, radius: Float, tileMode: Shader.TileMode) {
    runCatching {
        val method = paint.javaClass.getMethod("setRenderEffect", RenderEffect::class.java)
        method.invoke(paint, RenderEffect.createBlurEffect(radius, radius, tileMode))
    }
}
