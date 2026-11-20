package app.expressiveclock.ui.screens.alarms

import android.graphics.Paint
import android.util.Log
import app.expressiveclock.ui.WorldMapData
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
import app.expressiveclock.core.Alarm
import app.expressiveclock.core.WorldCity
import app.expressiveclock.ui.glass
import app.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import app.expressiveclock.ui.responsive.TwoPaneLayout
import app.expressiveclock.ui.sheets.AlarmPickerSheetContent
import app.expressiveclock.ui.sheets.SILENT
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import app.expressiveclock.data.WeekStart
import app.expressiveclock.data.order
import app.expressiveclock.ui.sheets.systemFirstDay
import app.expressiveclock.ui.theme.CapText
import app.expressiveclock.ui.theme.ClockFace
import app.expressiveclock.ui.theme.Numerals
import app.expressiveclock.ui.theme.inkBounds
import app.expressiveclock.ui.theme.rememberInkBounds
import app.expressiveclock.ui.theme.StretchedCaps
import java.time.DayOfWeek
import kotlin.math.sin

/**
 * The alarm grid.
 *
 * Proportions are measured off the research-concept renders in `clock-mockups/reference/concept/`.
 * At 412 dp wide the render gives a tile of 184 × 250 dp on a 15.7 dp margin with a 12 dp gutter,
 * a 23.9 dp corner radius, and an hour numeral whose cap height is 153 dp: **0.62 of the tile**.
 * That figure is the design; it is what makes the grid read as a wall of numerals rather than a
 * list of cards with big text on them.
 *
 * Armed and disarmed differ by **weight, not by fill**: `wght` 260 against `wght` 120 on the same
 * ultra-condensed cut, solved from the render's measured stem-over-cap of 0.0639 and 0.0306. The
 * off state is a light numeral, not an outlined one.
 *
 * Every element of the time (hour, minutes, meridiem) carries the *same* tracking. See
 * [ClockFace.CONDENSED_TRACKING] for why that is one constant rather than solved per element.
 */
/**
 * The inset every edge of the tile shares: numeral ink, day letters, switch track alike.
 *
 * The render measured this twice and got the same answer both times: numeral ink 17.9 dp down a
 * 250 dp tile, and the switch's visible track 18.2 dp up from its bottom. It was a *fraction* of
 * the tile height while the tile had a fixed aspect; now the height is derived from the content it
 * has to be an absolute, or the two would chase each other.
 */
private const val CONTENT_INSET_DP = 18.1f

/**
 * From the top of the day row's box to the bottom of the switch you can *see*.
 *
 * `Switch` is 48 dp of touch target around a 32 dp track, centred, so the visible track starts 8 dp
 * down. The column's bottom padding gives the other 8 dp back, which is what lands the track on
 * [CONTENT_INSET_DP].
 */
private const val SWITCH_ROW_VISIBLE_DP = 40f
/** Measured: the minutes are three-quarters of the hour's cap height, cap-tops aligned. */
private const val MINUTE_CAP_FRACTION = 0.743f
/** Measured: the meridiem's cap is 0.145 of the hour's. */
private const val MERIDIEM_FRACTION = 0.145f
/** Derived, so the column closes on the hour's baseline: 1 − 0.743 − 0.145. */
private const val MERIDIEM_GAP_FRACTION = 1f - MINUTE_CAP_FRACTION - MERIDIEM_FRACTION
/**
 * The gap between the hour's and the minutes' boxes, as a fraction of the hour's cap.
 *
 * A fraction rather than the 8.3 dp it was measured at, because the cap is now solved per tile: a
 * fixed gap would be a different proportion of a 140 dp numeral than of a 170 dp one, and the fit
 * would drift between tiles.
 */
/**
 * What one step of [ClockFace.TRACKING_DELTA] is worth, measured in hour-caps.
 *
 * Tracking is in em and this gap is in caps, so the two only trade through the digit's own
 * ink-to-em ratio: a font size of `cap / DIGIT_EM` means `delta` em is `delta / DIGIT_EM` of a cap.
 * There are exactly two internal digit gaps in a time, one inside the hour and one inside the
 * minutes, so twice this comes out of the gap between them and the block's width is unchanged.
 */
private const val TRACKING_DELTA_AS_CAP = ClockFace.TRACKING_DELTA / ClockFace.DIGIT_EM
private const val GAP_FRACTION = 8.3f / 155f - 2f * TRACKING_DELTA_AS_CAP
/**
 * `Switch` reserves a 48 dp touch target around a 32 dp track, so its visible top is 8 dp below the
 * row's. Every vertical measurement here is of the switch you can *see*, so the row is placed 8 dp
 * higher than the number says.
 */
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
    // Track how many cards are revealed during first-launch post-onboarding stagger
    var visibleCardsCount by remember(staggerEntranceToken) {
        mutableIntStateOf(if (staggerEntranceToken > 0) 0 else Int.MAX_VALUE)
    }

    LaunchedEffect(staggerEntranceToken, alarms.size) {
        if (staggerEntranceToken > 0) {
            // Brief 120ms settling pause after page slide transition completes
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
    // The setting reaches all the way into the tile, not only into the editor. An app that starts
    // the week on Monday in the picker and on Sunday in the grid is worse than one with no setting.
    val order = remember(weekStart) { weekStart.order(systemFirstDay()) }

    // Keep a visual copy of the list so a deleted alarm can remain in the grid long enough for
    // its exit animation to finish. Removing an item directly from the LazyGrid data source makes
    // it disappear from composition immediately, which means there is nothing left to animate.
    // The source list remains authoritative; this is only a short-lived presentation layer.
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
            // Normal updates, including an edited alarm changing its sort position, should mirror
            // the source list exactly.
            displayedAlarms = alarms
        } else {
            // During deletion, preserve the exiting card in its original slot while updating every
            // surviving card by ID. This prevents a key from accidentally receiving another alarm's
            // content when two alarms swap positions.
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
            // Slightly longer than the visual exit so the final frame is not clipped by the list
            // mutation. The neighbouring cards then get their own spring placement animation.
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
            AnimatedContent(
                targetState = displayedAlarms.isEmpty(),
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.78f)) +
                            slideInVertically(animationSpec = spring(stiffness = 380f, dampingRatio = 0.78f)) { it / 16 }
                        ) togetherWith (
                            fadeOut(tween(200, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.95f, animationSpec = tween(200))
                        )
                    } else {
                        (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.78f))
                        ) togetherWith (
                            fadeOut(tween(200, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.92f, animationSpec = tween(200)) +
                            slideOutVertically(animationSpec = tween(200)) { it / 16 }
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
