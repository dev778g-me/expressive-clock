package com.dev.expressiveclock.ui.screens.alarms

import android.graphics.Paint
import android.util.Log
import com.dev.expressiveclock.ui.WorldMapData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dev.expressiveclock.core.Alarm
import com.dev.expressiveclock.core.WorldCity
import com.dev.expressiveclock.ui.glass
import com.dev.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import com.dev.expressiveclock.ui.responsive.TwoPaneLayout
import com.dev.expressiveclock.ui.sheets.AlarmPickerSheetContent
import com.dev.expressiveclock.ui.sheets.SILENT
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import com.dev.expressiveclock.data.WeekStart
import com.dev.expressiveclock.data.order
import com.dev.expressiveclock.ui.sheets.systemFirstDay
import com.dev.expressiveclock.ui.theme.CapText
import com.dev.expressiveclock.ui.theme.ClockFace
import com.dev.expressiveclock.ui.theme.Numerals
import com.dev.expressiveclock.ui.theme.inkBounds
import com.dev.expressiveclock.ui.theme.rememberInkBounds
import com.dev.expressiveclock.ui.theme.StretchedCaps
import java.time.DayOfWeek
import kotlin.math.sin

private const val CONTENT_INSET_DP = 18.1f

private const val SWITCH_ROW_VISIBLE_DP = 40f
/** Measured: the minutes are three-quarters of the hour's cap height, cap-tops aligned. */
private const val MINUTE_CAP_FRACTION = 0.743f
/** Measured: the meridiem's cap is 0.145 of the hour's. */
private const val MERIDIEM_FRACTION = 0.145f
/** Derived, so the column closes on the hour's baseline: 1 − 0.743 − 0.145. */
private const val MERIDIEM_GAP_FRACTION = 1f - MINUTE_CAP_FRACTION - MERIDIEM_FRACTION
private const val TRACKING_DELTA_AS_CAP = ClockFace.TRACKING_DELTA / ClockFace.DIGIT_EM
private const val GAP_FRACTION = 8.3f / 155f - 2f * TRACKING_DELTA_AS_CAP
private const val SWITCH_TOUCH_INSET_DP = 8f
/** A little shorter than the switch's 32 dp track, because level with it reads as shouting. */
private const val DAY_CAP_DP = 23f
/** Fixed, so the block is one width whatever the repeat pattern and the switch never shifts. */
private const val DAY_BLOCK_DP = 91f
/** The constant slice of space between letters. The scale is derived from it, not tuned. */
private const val DAY_TRACKING_DP = 2.2f

@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    weekStart: WeekStart,
    staggerEntranceToken: Int = 0,
    onStaggerConsumed: () -> Unit = {},
    onToggle: (Long) -> Unit,
    onEdit: (Alarm) -> Unit,
    contentPadding: PaddingValues,
    onAddAlarm: (() -> Unit)? = null,
    onDeleteAlarms: ((Set<Long>) -> Unit)? = null,
    onSelectionCountChange: ((Int) -> Unit)? = null,
    clearSelectionSignal: Int = 0,
    deleteClickSignal: Int = 0,
    editingAlarm: Alarm? = null,
    showNewAlarmPicker: Boolean = false,
    onSaveAlarm: ((Alarm) -> Unit)? = null,
    onDismissInline: (() -> Unit)? = null,
    cities: List<WorldCity> = emptyList(),
    selectedCityZoneId: String? = null,
    onSelectCity: ((WorldCity) -> Unit)? = null,
    onAddCity: (() -> Unit)? = null,
    cityPickerContent: (@Composable () -> Unit)? = null,
    showCityPicker: Boolean = false,
    onShowLocalTime: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var visibleCardsCount by remember(staggerEntranceToken) {
        mutableIntStateOf(if (staggerEntranceToken > 0) 0 else Int.MAX_VALUE)
    }

    LaunchedEffect(staggerEntranceToken, alarms.size) {
        if (staggerEntranceToken > 0) {
            delay(120L)
            Log.d("DynamicIslandOverlay", "SETUP INITIAL ALARM STAGGER START count=${alarms.size}")
            for (i in 1..alarms.size) {
                visibleCardsCount = i
                Log.d("DynamicIslandOverlay", "SETUP INITIAL ALARM ITEM index=${i - 1}")
                if (i < alarms.size) {
                    delay(70L)
                }
            }
            onStaggerConsumed()
        }
    }
    val order = remember(weekStart) { weekStart.order(systemFirstDay()) }

    var displayedAlarms by remember { mutableStateOf(alarms) }
    var exitingAlarmIds by remember { mutableStateOf(emptySet<Long>()) }

    var selectedIds by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    val inSelectionMode = selectedIds.isNotEmpty()
    var showDeleteConfirmSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIds.size) {
        onSelectionCountChange?.invoke(selectedIds.size)
    }

    LaunchedEffect(clearSelectionSignal) {
        if (clearSelectionSignal > 0) {
            selectedIds = emptySet()
        }
    }

    LaunchedEffect(deleteClickSignal) {
        if (deleteClickSignal > 0 && selectedIds.isNotEmpty()) {
            showDeleteConfirmSheet = true
        }
    }

    LaunchedEffect(displayedAlarms) {
        val existingIds = displayedAlarms.map { it.id }.toSet()
        val validSelected = selectedIds.filter { it in existingIds }.toSet()
        if (validSelected != selectedIds) {
            selectedIds = validSelected
        }
    }

    LaunchedEffect(alarms) {
        val sourceIds = alarms.asSequence().map { it.id }.toSet()
        val oldDisplayed = displayedAlarms
        val removed = oldDisplayed.filter { it.id !in sourceIds }
        val removedIds = removed.map { it.id }.toSet()

        if (removed.isNotEmpty()) {
            exitingAlarmIds = exitingAlarmIds + removed.map { it.id }
        }

        if (removed.isEmpty()) {
            displayedAlarms = alarms
        } else {
            val sourceById = alarms.associateBy { it.id }
            val oldIds = oldDisplayed.map { it.id }.toSet()
            displayedAlarms = buildList {
                oldDisplayed.forEach { old ->
                    add(sourceById[old.id] ?: old)
                }
                alarms.forEach { alarm ->
                    if (alarm.id !in oldIds) add(alarm)
                }
            }
        }

        if (removed.isNotEmpty()) {
            kotlinx.coroutines.delay(360)
            displayedAlarms = displayedAlarms.filter { it.id in sourceIds }
            exitingAlarmIds = exitingAlarmIds - removedIds
        }
    }

    val windowSizeInfo = LocalAppWindowSizeInfo.current

    if (windowSizeInfo.isExpanded) {
        ExpandedAlarmsScreen(
            alarms = alarms,
            weekStart = weekStart,
            order = order,
            displayedAlarms = displayedAlarms,
            exitingAlarmIds = exitingAlarmIds,
            selectedIds = selectedIds,
            inSelectionMode = inSelectionMode,
            visibleCardsCount = visibleCardsCount,
            editingAlarm = editingAlarm,
            showNewAlarmPicker = showNewAlarmPicker,
            onToggle = onToggle,
            onEdit = onEdit,
            onToggleSelect = { id ->
                selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
            },
            onStartSelection = { id ->
                selectedIds = selectedIds + id
            },
            onAddAlarm = { onAddAlarm?.invoke() },
            onSaveAlarm = { alarm: Alarm -> onSaveAlarm?.invoke(alarm) },
            onDismissInline = { onDismissInline?.invoke() },
            cities = cities,
            cityPickerContent = cityPickerContent,
            showCityPicker = showCityPicker,
            onShowLocalTime = onShowLocalTime,
            selectedCityZoneId = selectedCityZoneId,
            onSelectCity = { city -> onSelectCity?.invoke(city) },
            onAddCity = { onAddCity?.invoke() },
            onDeleteAlarms = onDeleteAlarms,
            onSelectionCountChange = onSelectionCountChange,
            contentPadding = contentPadding,
            modifier = modifier,
        )
    } else {
        FixedEdgeBlurContainer(
            topInset = contentPadding.calculateTopPadding(),
            bottomInset = contentPadding.calculateBottomPadding(),
            modifier = modifier,
        ) {
            val motionScheme = MaterialTheme.motionScheme
            AnimatedContent(
                targetState = displayedAlarms.isEmpty(),
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(motionScheme.defaultEffectsSpec()) +
                            scaleIn(initialScale = 0.92f, animationSpec = motionScheme.defaultSpatialSpec()) +
                            slideInVertically(animationSpec = motionScheme.defaultSpatialSpec()) { it / 16 }
                        ) togetherWith (
                            fadeOut(motionScheme.defaultEffectsSpec()) +
                            scaleOut(targetScale = 0.95f, animationSpec = motionScheme.fastEffectsSpec())
                        )
                    } else {
                        (fadeIn(motionScheme.defaultEffectsSpec()) +
                            scaleIn(initialScale = 0.95f, animationSpec = motionScheme.defaultSpatialSpec())
                        ) togetherWith (
                            fadeOut(motionScheme.defaultEffectsSpec()) +
                            scaleOut(targetScale = 0.92f, animationSpec = motionScheme.fastEffectsSpec()) +
                            slideOutVertically(animationSpec = motionScheme.fastEffectsSpec()) { it / 16 }
                        )
                    }
                },
                label = "alarmsEmptyTransition",
                modifier = Modifier.fillMaxSize(),
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyAlarmState(
                        onAddAlarm = { onAddAlarm?.invoke() },
                        contentPadding = contentPadding,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(displayedAlarms, key = { _, alarm -> alarm.id }) { index, alarm ->
                            val isCardVisible = alarm.id !in exitingAlarmIds && (index < visibleCardsCount)
                            AnimatedVisibility(
                                modifier = Modifier.animateItem(
                                    placementSpec = spring(
                                        stiffness = 380f,
                                        dampingRatio = 0.68f,
                                    ),
                                ),
                                visible = isCardVisible,
                                enter = fadeIn(tween(180)) +
                                    scaleIn(
                                        initialScale = 0.86f,
                                        animationSpec = spring(
                                            dampingRatio = 0.62f,
                                            stiffness = 400f,
                                        ),
                                    ) +
                                    slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = 0.62f,
                                            stiffness = 400f,
                                        ),
                                    ) { it / 10 },
                                exit = fadeOut(tween(260)) +
                                    scaleOut(
                                        targetScale = 0.90f,
                                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 500f),
                                    ),
                            ) {
                                AlarmTile(
                                    alarm = alarm,
                                    order = order,
                                    selected = alarm.id in selectedIds,
                                    inSelectionMode = inSelectionMode,
                                    onToggle = { onToggle(alarm.id) },
                                    onEdit = { onEdit(alarm) },
                                    onToggleSelect = {
                                        selectedIds = if (alarm.id in selectedIds) {
                                            selectedIds - alarm.id
                                        } else {
                                            selectedIds + alarm.id
                                        }
                                    },
                                    onStartSelection = {
                                        selectedIds = selectedIds + alarm.id
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmSheet) {
        DeleteAlarmsConfirmationSheet(
            count = selectedIds.size,
            onDismiss = { showDeleteConfirmSheet = false },
            onConfirmDelete = {
                val idsToDelete = selectedIds
                selectedIds = emptySet()
                showDeleteConfirmSheet = false
                onDeleteAlarms?.invoke(idsToDelete)
            },
        )
    }
}
