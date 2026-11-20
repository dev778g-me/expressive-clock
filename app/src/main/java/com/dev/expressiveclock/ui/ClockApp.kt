package com.dev.expressiveclock.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.expressiveclock.alarm.Notifications
import com.dev.expressiveclock.core.Alarm
import com.dev.expressiveclock.core.WorldCity
import com.dev.expressiveclock.data.DefaultStartupPage
import com.dev.expressiveclock.ui.screens.alarms.AlarmsScreen
import com.dev.expressiveclock.ui.screens.alarms.SelectionToolbar
import com.dev.expressiveclock.ui.screens.OnboardingScreen
import com.dev.expressiveclock.ui.screens.stopwatch.StopwatchScreen
import com.dev.expressiveclock.ui.screens.settings.SettingsScreen
import com.dev.expressiveclock.ui.screens.timers.TimersScreen
import com.dev.expressiveclock.ui.sheets.AlarmEditSheet
import com.dev.expressiveclock.ui.sheets.NewAlarmPickerSheet
import com.dev.expressiveclock.ui.sheets.AddCitySheet
import com.dev.expressiveclock.ui.theme.ClockTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dev.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import com.dev.expressiveclock.ui.responsive.rememberAppWindowSizeInfo

/** Breathing room between the dock and the navigation bar. */
private val DOCK_CLEARANCE = 20.dp

private val DOCK_RESERVE = DOCK_HEIGHT + DOCK_CLEARANCE + 12.dp

enum class Tab(val label: String, val icon: ImageVector, val key: String) {
    ALARMS("Alarms", Icons.Outlined.Alarm, Notifications.TAB_ALARMS),
    TIMERS("Timers", Icons.Outlined.HourglassEmpty, Notifications.TAB_TIMERS),
    STOPWATCH("Stopwatch", Icons.Outlined.Timer, Notifications.TAB_STOPWATCH),
}

@Composable
fun ClockApp(startTab: String? = null, vm: ClockViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val isSettingsLoaded by vm.isSettingsLoaded.collectAsStateWithLifecycle()
    val cities by vm.cities.collectAsStateWithLifecycle()
    val windowSizeInfo = rememberAppWindowSizeInfo()

    ClockTheme(settings.theme) {
        CompositionLocalProvider(
            LocalAppWindowSizeInfo provides windowSizeInfo,
        ) {
            val defaultTab = when (settings.defaultStartupPage) {
                DefaultStartupPage.ALARMS -> Tab.ALARMS
                DefaultStartupPage.TIMER -> Tab.TIMERS
                DefaultStartupPage.STOPWATCH -> Tab.STOPWATCH
            }

        var tab by rememberSaveable { mutableStateOf<Tab?>(null) }

        LaunchedEffect(isSettingsLoaded, startTab) {
            if (isSettingsLoaded && tab == null) {
                tab = startTab?.let { k -> Tab.entries.firstOrNull { it.key == k } } ?: defaultTab
            } else if (startTab != null) {
                startTab.let { k -> Tab.entries.firstOrNull { it.key == k } }?.let { tab = it }
            }
        }

        val activeTab = tab ?: startTab?.let { k -> Tab.entries.firstOrNull { it.key == k } } ?: defaultTab

        var editing by remember { mutableStateOf<Alarm?>(null) }
        var showNewAlarmPicker by remember { mutableStateOf(false) }
        var showAddCitySheet by rememberSaveable { mutableStateOf(false) }
        var selectedWorldCityZoneId by rememberSaveable { mutableStateOf<String?>(null) }
        var alarmSelectionCount by remember { mutableIntStateOf(0) }
        var clearSelectionSignal by remember { mutableIntStateOf(0) }
        var deleteClickSignal by remember { mutableIntStateOf(0) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        val settingsProgress = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        fun openSettings() {
            showSettings = true
            scope.launch {
                settingsProgress.snapTo(0f)
                settingsProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                )
            }
        }

        fun closeSettings() {
            scope.launch {
                settingsProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                )
                showSettings = false
            }
        }

        PredictiveBackHandler(enabled = showSettings) { backEvents ->
            try {
                backEvents.collect { event ->
                    settingsProgress.snapTo(1f - event.progress)
                }
                settingsProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                )
                showSettings = false
            } catch (_: CancellationException) {
                settingsProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                )
            }
        }
        val snackbar = remember { SnackbarHostState() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }

        val showOnboarding = !settings.hasCompletedInitialSetup
        var isPostOnboardingStaggerToken by rememberSaveable { mutableIntStateOf(0) }

        val onboardingTransition = updateTransition(targetState = showOnboarding, label = "onboardingTransition")
        val motionScheme = MaterialTheme.motionScheme

        onboardingTransition.AnimatedContent(
            transitionSpec = {
                if (!targetState) {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { width -> width },
                    ) + fadeIn(motionScheme.defaultEffectsSpec()) togetherWith
                        slideOutHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            targetOffsetX = { width -> -width },
                        ) + fadeOut(motionScheme.fastEffectsSpec())
                } else {
                    fadeIn(motionScheme.fastEffectsSpec()) togetherWith fadeOut(motionScheme.fastEffectsSpec())
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { isSetup ->
            if (isSetup) {
                OnboardingScreen(
                    onComplete = {
                        isPostOnboardingStaggerToken = 1
                        vm.updateSettings { it.copy(hasCompletedInitialSetup = true) }
                    },
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val p = settingsProgress.value
                                translationX = -size.width * 0.12f * p
                                alpha = 1f - 0.06f * p
                            },
                    ) {
                Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                if (activeTab == Tab.ALARMS && alarmSelectionCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .height(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        SelectionToolbar(
                            selectedCount = alarmSelectionCount,
                            onClearSelection = { clearSelectionSignal++ },
                            onDeleteClick = { deleteClickSignal++ },
                        )
                    }
                } else {
                    TopAppBar(
                        title = {},
                        actions = {
                            IconButton(onClick = ::openSettings) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    )
                }
            },
        ) { padding ->
            val ld = LocalLayoutDirection.current
            val underDock = padding.calculateBottomPadding() + DOCK_RESERVE
            val body = PaddingValues(
                start = padding.calculateStartPadding(ld) + 16.dp,
                end = padding.calculateEndPadding(ld) + 16.dp,
                top = padding.calculateTopPadding(),
                bottom = underDock,
            )
            val edgeToEdge = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = underDock,
            )

          GlassBackdrop(Modifier.fillMaxSize()) {
            val motionScheme = MaterialTheme.motionScheme
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { width -> if (forward) width else -width },
                    ) + fadeIn(motionScheme.defaultEffectsSpec()) togetherWith
                        slideOutHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            targetOffsetX = { width -> if (forward) -width else width },
                        ) + fadeOut(motionScheme.defaultEffectsSpec())
                },
                label = "tab",
            ) { current ->
                when (current) {
                    Tab.ALARMS -> {
                        val alarms by vm.alarms.collectAsStateWithLifecycle()
                        AlarmsScreen(
                            alarms = alarms,
                            weekStart = settings.alarms.weekStart,
                            staggerEntranceToken = isPostOnboardingStaggerToken,
                            onStaggerConsumed = { isPostOnboardingStaggerToken = 0 },
                            onToggle = vm::toggleAlarm,
                            onEdit = { editing = it },
                            onAddAlarm = { showNewAlarmPicker = true },
                            onDeleteAlarms = { vm.deleteAlarms(it) },
                            onSelectionCountChange = { count ->
                                alarmSelectionCount = count
                            },
                            clearSelectionSignal = clearSelectionSignal,
                            deleteClickSignal = deleteClickSignal,
                            editingAlarm = editing,
                            showNewAlarmPicker = showNewAlarmPicker,
                            onSaveAlarm = { alarm ->
                                vm.saveAlarm(alarm)
                                editing = null
                                showNewAlarmPicker = false
                            },
                            onDismissInline = {
                                editing = null
                                showNewAlarmPicker = false
                            },
                            cities = cities,
                            selectedCityZoneId = selectedWorldCityZoneId,
                            onSelectCity = { city -> selectedWorldCityZoneId = city.zone.id },
                            onAddCity = { showAddCitySheet = true },
                            onShowLocalTime = { selectedWorldCityZoneId = null },
                            showCityPicker = showAddCitySheet,
                            cityPickerContent = {
                                    val now by rememberWallTicker()
                                    AddCitySheet(
                                        existing = cities,
                                        nowUtcMillis = now,
                                        inline = true,
                                        onAdd = { city ->
                                            vm.addCity(city)
                                            selectedWorldCityZoneId = city.zone.id
                                            showAddCitySheet = false
                                        },
                                        onDismiss = { showAddCitySheet = false },
                                    )
                            },
                            contentPadding = body,
                        )
                    }

                    Tab.TIMERS -> {
                        val timer by vm.timer.collectAsStateWithLifecycle()
                        val now by rememberElapsedTicker(active = timer != null)
                        TimersScreen(
                            timer = timer,
                            draft = vm.draftDuration,
                            nowElapsedMillis = now,
                            presets = settings.timers.presets,
                            onDigit = vm::pressDigit,
                            onBackspace = vm::backspace,
                            onResetDraft = vm::resetTimerDraft,
                            onPreset = vm::setTimerPreset,
                            onStart = { vm.startTimer() },
                            onPauseResume = { vm.pauseOrResumeTimer() },
                            onAddTen = { vm.addTenSeconds() },
                            onCancel = { vm.cancelTimer() },
                            contentPadding = edgeToEdge,
                        )
                    }

                    Tab.STOPWATCH -> {
                        val sw by vm.stopwatch.collectAsStateWithLifecycle()
                        val now by rememberElapsedTicker(active = sw.running)
                        StopwatchScreen(
                            stopwatch = sw,
                            nowElapsedMillis = now,
                            onToggle = { vm.toggleStopwatch() },
                            onLap = { vm.lap() },
                            onReset = { vm.resetStopwatch() },
                            contentPadding = edgeToEdge,
                        )
                    }
                }
            }

            val showAdd = activeTab == Tab.ALARMS
            val addAlpha by animateFloatAsState(
                targetValue = if (showAdd) 1f else 0f,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "toolbarAddAlpha",
            )
            val addScale by animateFloatAsState(
                targetValue = if (showAdd) 1f else .78f,
                animationSpec = motionScheme.fastSpatialSpec(),
                label = "toolbarAddScale",
            )
            val fabWidth by animateDpAsState(
                targetValue = if (showAdd) 56.dp else 0.dp,
                animationSpec = motionScheme.defaultSpatialSpec(),
                label = "toolbarFabWidth",
            )

            val toolbarColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
                toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            val navContent: @Composable RowScope.() -> Unit = {
                listOf(Tab.ALARMS, Tab.TIMERS, Tab.STOPWATCH).forEach { item ->
                    FloatingToolbarNavItem(
                        tab = item,
                        selected = activeTab == item,
                        onClick = { tab = item },
                    )
                }
            }

            LookaheadScope {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = DOCK_CLEARANCE),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = toolbarColors,
                        content = navContent,
                    )
                    Box(
                        modifier = Modifier
                            .width(fabWidth)
                            .size(56.dp)
                            .graphicsLayer {
                                alpha = addAlpha
                                scaleX = addScale
                                scaleY = addScale
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = { if (showAdd) showNewAlarmPicker = true },
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add alarm")
                        }
                    }
                }
            }
          }
            }
            }

            if (showSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = settingsProgress.value
                            translationX = size.width * (1f - p)
                            alpha = 0.92f + 0.08f * p
                        },
                ) {
                    SettingsScreen(
                        settings = settings,
                        onChange = vm::updateSettings,
                        onBack = ::closeSettings,
                        onReopenOnboarding = {
                            closeSettings()
                            vm.updateSettings { it.copy(hasCompletedInitialSetup = false) }
                        },
                    )
                }
            }
        }
    }
}

        if (!windowSizeInfo.isExpanded) {
            editing?.let { draft ->
                AlarmEditSheet(
                    initial = draft,
                    weekStart = settings.alarms.weekStart,
                    onDismiss = { editing = null },
                    onSave = { vm.saveAlarm(it); editing = null },
                    onDelete = if (draft.id == 0L) null else ({ id -> vm.deleteAlarm(id) }),
                )
            }

            if (showNewAlarmPicker) {
                NewAlarmPickerSheet(
                    initial = vm.blankAlarm(),
                    weekStart = settings.alarms.weekStart,
                    onDismiss = { showNewAlarmPicker = false },
                    onSave = { alarm -> vm.saveAlarm(alarm); showNewAlarmPicker = false },
                )
            }
        }

        if (showAddCitySheet && !windowSizeInfo.isExpanded) {
            AddCitySheet(
                existing = cities,
                nowUtcMillis = System.currentTimeMillis(),
                onAdd = { city: WorldCity ->
                    vm.addCity(city)
                    selectedWorldCityZoneId = city.zone.id
                    showAddCitySheet = false
                },
                onDismiss = { showAddCitySheet = false },
            )
        }
        }
    }
}

@Composable
private fun RowScope.FloatingToolbarNavItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    var tapCount by remember { mutableIntStateOf(0) }
    val rotation = remember { Animatable(0f) }
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1f else .94f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "floatingToolbarScale",
    )

    LaunchedEffect(tapCount) {
        if (tapCount == 0) return@LaunchedEffect
        rotation.snapTo(0f)
        when (tab) {
            Tab.ALARMS -> {
                rotation.animateTo(-5f, tween(65))
                rotation.animateTo(5f, tween(85))
                rotation.animateTo(-3f, tween(65))
                rotation.animateTo(0f, tween(65))
            }
            Tab.TIMERS -> rotation.animateTo(180f, tween(320))
            Tab.STOPWATCH -> rotation.animateTo(360f, tween(420))
        }
        rotation.snapTo(0f)
    }

    ToggleButton(
        checked = selected,
        shapes = ToggleButtonShapes(
            shape = CircleShape,
            pressedShape = CircleShape,
            checkedShape = CircleShape,
        ),
        onCheckedChange = {
            tapCount++
            onClick()
        },
        colors = ToggleButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.height(56.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Crossfade(
                targetState = selected,
                label = "navigationIcon",
            ) { isSelected ->
                Icon(
                    imageVector = if (selected) when (tab) {
                        Tab.ALARMS -> Icons.Filled.AlarmAdd
                        Tab.TIMERS -> Icons.Filled.HourglassBottom
                        Tab.STOPWATCH -> Icons.Filled.Timer
                    } else tab.icon,
                    contentDescription = tab.label,
                    modifier = Modifier.graphicsLayer {
                        scaleX = selectedScale
                        scaleY = selectedScale
                        rotationZ = rotation.value
                        cameraDistance = 18f * density
                    },
                )
            }

            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                ) + fadeIn(),
                exit = shrinkHorizontally(
                    animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                ) + fadeOut(),
            ) {
                Text(
                    text = tab.label,
                    maxLines = 1,
                    fontWeight = FontWeight.ExtraBold,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                )
            }
        }
    }
}
