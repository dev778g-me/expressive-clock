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
import app.expressiveclock.ui.screens.WorldClockPanel
import app.expressiveclock.ui.sheets.systemFirstDay
import app.expressiveclock.ui.theme.CapText
import app.expressiveclock.ui.theme.ClockFace
import app.expressiveclock.ui.theme.Numerals
import app.expressiveclock.ui.theme.inkBounds
import app.expressiveclock.ui.theme.rememberInkBounds
import app.expressiveclock.ui.theme.StretchedCaps
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
internal fun ExpandedAlarmsScreen(
    alarms: List<Alarm>,
    weekStart: WeekStart,
    order: List<DayOfWeek>,
    displayedAlarms: List<Alarm>,
    exitingAlarmIds: Set<Long>,
    selectedIds: Set<Long>,
    inSelectionMode: Boolean,
    visibleCardsCount: Int,
    editingAlarm: Alarm?,
    showNewAlarmPicker: Boolean,
    onToggle: (Long) -> Unit,
    onEdit: (Alarm) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onStartSelection: (Long) -> Unit,
    onAddAlarm: () -> Unit,
    onSaveAlarm: (Alarm) -> Unit,
    onDismissInline: () -> Unit,
    cities: List<WorldCity>,
    cityPickerContent: (@Composable () -> Unit)?,
    showCityPicker: Boolean,
    onShowLocalTime: () -> Unit,
    selectedCityZoneId: String?,
    onSelectCity: (WorldCity) -> Unit,
    onAddCity: () -> Unit,
    onDeleteAlarms: ((Set<Long>) -> Unit)?,
    onSelectionCountChange: ((Int) -> Unit)?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val topPadding = contentPadding.calculateTopPadding() + 8.dp
    val bottomPadding = contentPadding.calculateBottomPadding()

    TwoPaneLayout(
        leftPane = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topPadding, bottom = bottomPadding),
            ) {
                if (displayedAlarms.isEmpty()) {
                    EmptyAlarmState(
                        onAddAlarm = onAddAlarm,
                        contentPadding = PaddingValues(0.dp),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
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
                                    ),
                                exit = fadeOut(tween(260)) +
                                    scaleOut(
                                        targetScale = 0.90f,
                                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 500f),
                                    ),
                            ) {
                                AlarmTile(
                                    alarm = alarm,
                                    order = order,
                                    selected = editingAlarm?.id == alarm.id || alarm.id in selectedIds,
                                    inSelectionMode = inSelectionMode,
                                    onToggle = { onToggle(alarm.id) },
                                    onEdit = { onEdit(alarm) },
                                    onToggleSelect = { onToggleSelect(alarm.id) },
                                    onStartSelection = { onStartSelection(alarm.id) },
                                )
                            }
                        }
                    }
                }
            }
        },
        rightPane = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, end = 16.dp, top = topPadding, bottom = bottomPadding),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    targetState = if (showCityPicker) 2 else if (showNewAlarmPicker || editingAlarm != null) 1 else 0,
                    transitionSpec = {
                        if (targetState != 0) {
                            (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f),
                                ) +
                                slideInVertically(
                                    animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f),
                                    initialOffsetY = { it / 16 },
                                )) togetherWith (
                                fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.97f, animationSpec = tween(180))
                            )
                        } else {
                            (fadeIn(tween(220, delayMillis = if (initialState == 2) 180 else 0, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.97f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f))
                            ) togetherWith (
                                fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.94f, animationSpec = tween(180)) +
                                    slideOutVertically(animationSpec = tween(180)) { it / 16 }
                            )
                        }
                    },
                    label = "rightPaneEditorTransition",
                ) { pane ->
                    if (pane == 2 && cityPickerContent != null) {
                        cityPickerContent()
                    } else if (pane == 1) {
                        InlineAlarmEditorPanel(
                            editingAlarm = if (showNewAlarmPicker) null else editingAlarm,
                            weekStart = weekStart,
                            order = order,
                            onSave = onSaveAlarm,
                            onCancel = onDismissInline,
                            onDelete = { id ->
                                onDeleteAlarms?.invoke(setOf(id))
                                onDismissInline()
                            },
                        )
                    } else {
                        WorldClockPanel(
                            cities = cities,
                            selectedZoneId = selectedCityZoneId,
                            onSelectCity = onSelectCity,
                            onAddLocation = onAddCity,
                            onShowLocalTime = onShowLocalTime,
                        )
                    }
                }
            }
        },
        leftWeight = 0.46f,
        rightWeight = 0.54f,
        paneSpacing = 20.dp,
        contentPaddingDp = 16.dp,
        modifier = modifier,
    )
}

@Composable
internal fun InlineAlarmEditorPanel(
    editingAlarm: Alarm?,
    weekStart: WeekStart,
    order: List<DayOfWeek>,
    onSave: (Alarm) -> Unit,
    onCancel: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    val initialTime = editingAlarm?.time ?: LocalTime.of(8, 0)
    var hour by remember(editingAlarm?.id) { mutableIntStateOf(initialTime.hour) }
    var minute by remember(editingAlarm?.id) { mutableIntStateOf(initialTime.minute) }
    var pm by remember(editingAlarm?.id) { mutableStateOf(initialTime.hour >= 12) }
    var editingHour by remember { mutableStateOf(true) }
    var days by remember(editingAlarm?.id) { mutableStateOf(editingAlarm?.days ?: emptySet()) }
    var vibrate by remember(editingAlarm?.id) { mutableStateOf(editingAlarm?.vibrate ?: true) }
    var soundUri by remember(editingAlarm?.id) { mutableStateOf(editingAlarm?.soundUri) }
    var label by remember(editingAlarm?.id) { mutableStateOf(editingAlarm?.label ?: "") }

    val soundLabel = remember(soundUri) {
        when {
            soundUri == null -> "Default"
            soundUri == SILENT -> "Silent"
            else -> runCatching { RingtoneManager.getRingtone(context, Uri.parse(soundUri))?.getTitle(context) }
                .getOrNull() ?: "Default"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glass(
                shape = RoundedCornerShape(28.dp),
                tint = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
                borderAlpha = 0.82f,
                shadowElevation = 14.dp,
            )
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AlarmPickerSheetContent(
            title = if (editingAlarm == null || editingAlarm.id == 0L) "New Alarm" else "Edit Alarm",
            hour = hour,
            minute = minute,
            is24Hour = is24Hour,
            pm = pm,
            editingHour = editingHour,
            label = if (label.isBlank()) "Alarm" else label,
            days = days,
            weekOrder = order,
            vibrate = vibrate,
            soundLabel = soundLabel,
            showDelete = editingAlarm != null && editingAlarm.id != 0L,
            onHourChange = { hour = it },
            onMinuteChange = { minute = it },
            onPmChange = { pm = it },
            onEditingHourChange = { editingHour = it },
            onLabelChange = { label = it },
            onToggleDay = { day ->
                days = if (day in days) days - day else days + day
            },
            onVibrateChange = { vibrate = it },
            onSoundClick = { /* sound action */ },
            onCancel = onCancel,
            onSave = {
                val base = editingAlarm ?: Alarm(0L, LocalTime.of(hour, minute))
                val alarmToSave = base.copy(
                    time = LocalTime.of(hour, minute),
                    days = days,
                    vibrate = vibrate,
                    soundUri = soundUri,
                    label = label,
                    enabled = true,
                )
                onSave(alarmToSave)
            },
            onDelete = {
                if (editingAlarm != null && editingAlarm.id != 0L) {
                    onDelete(editingAlarm.id)
                }
            },
        )
    }
}

@Composable
internal fun DefaultRightPaneWorldClockPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glass(
                shape = RoundedCornerShape(28.dp),
                tint = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
                borderAlpha = 0.82f,
                shadowElevation = 14.dp,
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            WorldMapIllustration(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .height(230.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.52f),
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .glass(
                        shape = RoundedCornerShape(24.dp),
                        tint = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
                        borderAlpha = 0.82f,
                        shadowElevation = 4.dp,
                    )
                    .padding(22.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Newcastle",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Local time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "06:50 AM",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = ClockFace.family(opticalSize = 30f, width = ClockFace.NORMAL, weight = 700),
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Tue, 28 Jan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WorldMapIllustration(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val dotRadius = 1.25f

        WorldMapData.DOT_OFFSETS.forEach { offset ->
            val cx = offset.x * w
            val cy = offset.y * h
            drawCircle(
                color = tint,
                radius = dotRadius,
                center = Offset(cx, cy),
            )
        }
    }
}

@Composable
fun FixedEdgeBlurContainer(
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()

                val width = size.width
                val height = size.height
                val topInsetPx = topInset.toPx()
                val bottomInsetPx = bottomInset.toPx()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas

                        val tileMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Shader.TileMode.DECAL
                        } else {
                            Shader.TileMode.CLAMP
                        }

                        val blurPaint = Paint()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setPaintBlurEffect(blurPaint, 32f, tileMode)
                        }

                        if (topInsetPx > 0f) {
                            val saveCount = nativeCanvas.saveLayer(
                                0f, 0f, width, topInsetPx, blurPaint
                            )
                            drawContent()

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black,
                                        Color.Black.copy(alpha = 0.70f),
                                        Color.Transparent,
                                    ),
                                    startY = 0f,
                                    endY = topInsetPx,
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                            nativeCanvas.restoreToCount(saveCount)
                        }

                        val bottomStart = height - bottomInsetPx
                        if (bottomInsetPx > 0f && bottomStart < height) {
                            val saveCount = nativeCanvas.saveLayer(
                                0f, bottomStart, width, height, blurPaint
                            )
                            drawContent()

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.70f),
                                        Color.Black,
                                    ),
                                    startY = bottomStart,
                                    endY = height,
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                            nativeCanvas.restoreToCount(saveCount)
                        }
                    }
                }

                if (topInsetPx > 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black,
                            ),
                            startY = 0f,
                            endY = topInsetPx,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }

                val bottomStart = height - bottomInsetPx
                if (bottomInsetPx > 0f && bottomStart < height) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent,
                            ),
                            startY = bottomStart,
                            endY = height,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
            },
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun setPaintBlurEffect(paint: Paint, radius: Float, tileMode: Shader.TileMode) {
    runCatching {
        val method = paint.javaClass.getMethod("setRenderEffect", RenderEffect::class.java)
        method.invoke(paint, RenderEffect.createBlurEffect(radius, radius, tileMode))
    }
}

internal class TimeFit(val cap: Dp, val padStart: Dp, val padEnd: Dp)

@Composable
internal fun fittedTime(meridiem: String, tileWidth: Dp, target: Dp): TimeFit {
    val density = LocalDensity.current
    val ref = REFERENCE_CAP_DP.dp
    val hourStyle = ClockFace.numerals(
        ref, ClockFace.CONDENSED, ClockFace.WEIGHT_ON,
        tabular = true, tracking = ClockFace.CONDENSED_TRACKING,
    )
    val minuteStyle = ClockFace.numerals(
        ref * MINUTE_CAP_FRACTION, ClockFace.CONDENSED, ClockFace.WEIGHT_ON,
        tabular = true, tracking = ClockFace.CONDENSED_TRACKING,
    )
    val meridiemStyle = ClockFace.capitals(
        ref * MERIDIEM_FRACTION, ClockFace.CONDENSED, tracking = ClockFace.CONDENSED_TRACKING,
    )
    val h = rememberInkBounds("00", hourStyle)
    val m = rememberInkBounds("00", minuteStyle)
    val a = rememberInkBounds(meridiem, meridiemStyle)

    return remember(h, m, a, tileWidth, target, density) {
        with(density) {
            val trailing = if (m.advance >= a.advance) m else a

            val k = (h.advance + maxOf(m.advance, a.advance)).toDp() / ref + GAP_FRACTION
            val lf = h.left.toDp() / ref
            val rf = trailing.right.toDp() / ref

            val cap = (tileWidth - target * 2f) / (k - lf - rf)

            val slack = tileWidth - cap * k
            val padStart = (slack + cap * (rf - lf)) / 2f
            TimeFit(cap = cap, padStart = padStart, padEnd = slack - padStart)
        }
    }
}

/** The `wdth` that makes the meridiem as wide as the minutes, and the shift that squares them up. */
internal class MeridiemFit(val width: Float, val offsetX: Dp)

@Composable
internal fun rememberMeridiemFit(
    meridiem: String,
    minutes: String,
    hourCap: Dp,
    weight: Int,
): MeridiemFit {
    val resolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current
    return remember(meridiem, minutes, hourCap, weight, density) {
        val minuteInk = inkBounds(
            resolver, density, minutes,
            ClockFace.numerals(
                hourCap * MINUTE_CAP_FRACTION, ClockFace.CONDENSED, weight,
                tracking = ClockFace.CONDENSED_TRACKING,
            ),
        )
        val cap = hourCap * MERIDIEM_FRACTION
        fun measure(w: Float) = inkBounds(
            resolver, density, meridiem,
            ClockFace.capitals(cap, w, tracking = ClockFace.CONDENSED_TRACKING),
        )

        var lo = ClockFace.CONDENSED
        var hi = 151f
        repeat(16) {
            val mid = (lo + hi) / 2f
            if (measure(mid).width < minuteInk.width) lo = mid else hi = mid
        }
        val width = (lo + hi) / 2f
        val fitted = measure(width)
        val boxOffset = (fitted.advance - minuteInk.advance) / 2f
        val minuteMid = minuteInk.left + minuteInk.width / 2f
        val meridiemMid = fitted.left + fitted.width / 2f
        MeridiemFit(width, with(density) { (boxOffset + minuteMid - meridiemMid).toDp() })
    }
}

/** Any size would do; widths are linear in the cap, so this only sets the measurement's precision. */
private const val REFERENCE_CAP_DP = 120f

@Composable
internal fun AlarmTile(
    alarm: Alarm,
    order: List<DayOfWeek>,
    selected: Boolean = false,
    inSelectionMode: Boolean = false,
    itemModifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onToggleSelect: () -> Unit = {},
    onStartSelection: () -> Unit = {},
) {
    val view = LocalView.current
    val windowExpanded = LocalAppWindowSizeInfo.current.isExpanded
    val armed = alarm.enabled
    val weightTarget = if (armed) ClockFace.WEIGHT_ON else ClockFace.WEIGHT_OFF
    val animatedWeight by androidx.compose.animation.core.animateIntAsState(
        targetValue = weightTarget,
        animationSpec = tween(durationMillis = 220),
        label = "alarmTimeWeight",
    )
    val animatedContainer by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            armed -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(220),
        label = "alarmContainer",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = .26f),
        animationSpec = tween(220),
        label = "alarmBorder",
    )
    val animatedInk by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimaryContainer
            armed -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "alarmInk",
    )
    val cardScale = remember { Animatable(1f) }
    var hasComposed by remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(armed) {
        if (!hasComposed) {
            hasComposed = true
            return@LaunchedEffect
        }
        cardScale.snapTo(if (armed) .985f else 1.015f)
        cardScale.animateTo(
            if (armed) 1.018f else .982f,
            animationSpec = spring(dampingRatio = .72f, stiffness = 520f),
        )
        cardScale.animateTo(1f, spring(dampingRatio = .82f, stiffness = 430f))
    }

    val hour12 = (alarm.time.hour % 12).takeIf { it != 0 } ?: 12
    val meridiem = if (alarm.time.hour < 12) "AM" else "PM"
    val minutesText = "%02d".format(alarm.time.minute)

    BoxWithConstraints(Modifier.fillMaxWidth()) {
    val tileWidth = maxWidth
    val inset = CONTENT_INSET_DP.dp
    val fit = fittedTime(meridiem = meridiem, tileWidth = tileWidth, target = inset)
    val hourCap = fit.cap
    val meridiemFit = rememberMeridiemFit(meridiem, minutesText, hourCap, ClockFace.WEIGHT_ON)
    val timeToDays = hourCap * MERIDIEM_GAP_FRACTION - SWITCH_TOUCH_INSET_DP.dp - ALARM_DIGIT_BOTTOM_BLEED_DP.dp
    val tileHeight = inset * 2 + hourCap + timeToDays + SWITCH_ROW_VISIBLE_DP.dp

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        border = if (selected) BorderStroke(width = 2.dp, animatedBorderColor) else null,
        shadowElevation = 0.dp,
        modifier = itemModifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .graphicsLayer {
                val scale = cardScale.value
                scaleX = scale
                scaleY = scale
            }
            .background(animatedContainer, RoundedCornerShape(24.dp))
            .height(tileHeight)
            .combinedClickable(
                onClick = {
                    if (inSelectionMode) {
                        onToggleSelect()
                    } else {
                        onEdit()
                    }
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    if (inSelectionMode) {
                        onToggleSelect()
                    } else {
                        onStartSelection()
                    }
                },
            )
            .clearAndSetSemantics {
                contentDescription = buildString {
                    if (selected) append("Selected, ")
                    append("%d:%02d %s".format(hour12, alarm.time.minute, meridiem))
                    if (alarm.label.isNotBlank()) append(", ${alarm.label}")
                    append(", ${alarm.repeatLabel()}")
                    append(if (armed) ", on" else ", off")
                    append(", double tap to edit")
                }
            },
    ) {
            Box(Modifier.fillMaxSize()) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, top = 12.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = inset, bottom = inset - SWITCH_TOUCH_INSET_DP.dp),
                    verticalArrangement = Arrangement.spacedBy(timeToDays),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = fit.padStart, end = fit.padEnd),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        AnimatedAlarmNumber(
                            value = hour12,
                            capHeight = hourCap,
                            color = animatedInk,
                            weight = animatedWeight,
                        )
                        Spacer(Modifier.width(hourCap * GAP_FRACTION))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedAlarmNumber(
                                value = alarm.time.minute,
                                capHeight = hourCap * MINUTE_CAP_FRACTION,
                                color = animatedInk,
                                weight = animatedWeight,
                            )
                            Spacer(Modifier.height(hourCap * MERIDIEM_GAP_FRACTION))
                            CapText(
                                text = meridiem,
                                capHeight = hourCap * MERIDIEM_FRACTION,
                                color = animatedInk,
                                width = meridiemFit.width,
                                tracking = ClockFace.CONDENSED_TRACKING,
                                modifier = Modifier.offset(x = meridiemFit.offsetX),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = inset),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DayLetters(alarm, order, animatedInk)
                        Switch(
                            checked = armed,
                            modifier = Modifier.graphicsLayer {
                                if (windowExpanded) {
                                    scaleX = .88f
                                    scaleY = .88f
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, .5f)
                                }
                            },
                            onCheckedChange = {
                                if (inSelectionMode) {
                                    onToggleSelect()
                                } else {
                                    onToggle()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = animatedContainer,
                                checkedTrackColor = animatedInk,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = animatedInk,
                                uncheckedTrackColor = Color.Transparent,
                                uncheckedBorderColor = animatedInk,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionToolbar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
            .border(1.25.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f), CircleShape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onClearSelection) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Clear selection",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Text(
            "$selectedCount selected",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete selected alarms",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeleteAlarmsConfirmationSheet(
    count: Int,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.60f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Delete $count alarm${if (count > 1) "s" else ""}?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "This can't be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onConfirmDelete()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Delete",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
                    .clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

private const val ALARM_DIGIT_BOTTOM_BLEED_DP = 2

@Composable
internal fun AnimatedAlarmNumber(
    value: Int,
    capHeight: Dp,
    color: Color,
    weight: Int,
) {
    val digits = "%02d".format(value)

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .height(capHeight + ALARM_DIGIT_BOTTOM_BLEED_DP.dp)
            .clipToBounds(),
    ) {
        digits.forEachIndexed { index, digit ->
            AnimatedAlarmDigit(
                digit = digit.digitToInt(),
                capHeight = capHeight,
                color = color,
                weight = weight,
                key = index,
            )
        }
    }
}

@Composable
internal fun AnimatedAlarmDigit(
    digit: Int,
    capHeight: Dp,
    color: Color,
    weight: Int,
    key: Int,
) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            val forward = (targetState - initialState + 10) % 10
            val increasing = forward in 1..5
            val enterOffset: (Int) -> Int = if (increasing) { { it } } else { { -it } }
            val exitOffset: (Int) -> Int = if (increasing) { { -it } } else { { it } }

            slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 430f,
                ),
                initialOffsetY = enterOffset,
            ) + androidx.compose.animation.scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(
                    dampingRatio = 0.62f,
                    stiffness = 430f,
                ),
            ) + fadeIn(tween(85)) togetherWith
                slideOutVertically(
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 520f,
                    ),
                    targetOffsetY = exitOffset,
                ) + androidx.compose.animation.scaleOut(
                    targetScale = 0.96f,
                    animationSpec = tween(120),
                ) + fadeOut(tween(75))
        },
        label = "alarmDigitChange_$key",
        contentAlignment = Alignment.Center,
    ) { target ->
        Box(
            modifier = Modifier
                .height(capHeight + ALARM_DIGIT_BOTTOM_BLEED_DP.dp)
                .clipToBounds(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Numerals(
                text = target.toString(),
                capHeight = capHeight,
                color = color,
                width = ClockFace.CONDENSED,
                weight = weight,
                tracking = ClockFace.CONDENSED_TRACKING,
            )
        }
    }
}

@Composable
internal fun DayLetters(alarm: Alarm, order: List<DayOfWeek>, ink: Color) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val bold = ClockFace.capitals(DAY_CAP_DP.dp, ClockFace.CONDENSED, weight = DAY_WEIGHT_ON)
    val light = ClockFace.capitals(DAY_CAP_DP.dp, ClockFace.CONDENSED, weight = DAY_WEIGHT_OFF)

    val text = remember(alarm.days, alarm.isOneShot, order, ink, bold, light) {
        buildAnnotatedString {
            order.forEach { day ->
                val on = !alarm.isOneShot && day in alarm.days
                withStyle(
                    SpanStyle(

                        fontFamily = (if (on) bold else light).fontFamily,
                        color = if (on) ink else ink.copy(alpha = 0.30f),
                    )
                ) {
                    append(day.name.take(1))
                }
            }
        }
    }

    val style = bold.copy(
        letterSpacing = with(density) { (DAY_TRACKING_DP.dp / bold.fontSize.toDp()).em },
    )
    val scaleX = with(density) {
        DAY_BLOCK_DP.dp.toPx() / measurer.measure(text, style).size.width
    }

    StretchedCaps(text = text, style = style, capHeight = DAY_CAP_DP.dp, scaleX = scaleX)
}

/** Weight is the state, the same way it is on the numerals above. Not a second colour ramp. */
private const val DAY_WEIGHT_ON = 700
private const val DAY_WEIGHT_OFF = 400

@Composable
internal fun EmptyAlarmState(
    onAddAlarm: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SleepingAlarmClockIcon(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            bellColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
            modifier = Modifier.size(136.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "No alarms yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Get started by adding your first alarm.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onAddAlarm)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Alarm",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
internal fun SleepingAlarmClockIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
    bellColor: Color = color.copy(alpha = 0.50f),
) {
    val clockRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6500)
            val angles = listOf(-5.5f, 5.5f, -5f, 5f, -3.5f, 3.5f, -2f, 2f, 0f)
            for (angle in angles) {
                clockRotation.animateTo(
                    targetValue = angle,
                    animationSpec = tween(durationMillis = 65, easing = LinearEasing),
                )
            }
        }
    }

    val zTransition = rememberInfiniteTransition(label = "zLanesTransition")

    val z0Progress by zTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "z0",
    )

    val z1Progress by zTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "z1",
    )

    val z2Progress by zTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "z2",
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 3.5.dp.toPx()
        val cx = size.width * 0.42f
        val cy = size.height * 0.70f
        val r = size.width * 0.24f

        rotate(degrees = clockRotation.value, pivot = Offset(cx, cy)) {
            drawCircle(
                color = color,
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth),
            )

            val footLength = r * 0.38f
            val leftFootStart = Offset(cx - r * 0.707f, cy + r * 0.707f)
            val leftFootEnd = Offset(
                leftFootStart.x - footLength * 0.55f,
                leftFootStart.y + footLength * 0.85f,
            )
            drawLine(
                color = color,
                start = leftFootStart,
                end = leftFootEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            val rightFootStart = Offset(cx + r * 0.707f, cy + r * 0.707f)
            val rightFootEnd = Offset(
                rightFootStart.x + footLength * 0.55f,
                rightFootStart.y + footLength * 0.85f,
            )
            drawLine(
                color = color,
                start = rightFootStart,
                end = rightFootEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            val bellRadius = r * 0.32f

            val leftBellCenter = Offset(cx - r * 0.76f, cy - r * 0.82f)
            drawArc(
                color = bellColor,
                startAngle = 135f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(leftBellCenter.x - bellRadius, leftBellCenter.y - bellRadius),
                size = Size(bellRadius * 2f, bellRadius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            val rightBellCenter = Offset(cx + r * 0.76f, cy - r * 0.82f)
            drawArc(
                color = bellColor,
                startAngle = 225f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(rightBellCenter.x - bellRadius, rightBellCenter.y - bellRadius),
                size = Size(bellRadius * 2f, bellRadius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            drawCircle(
                color = color,
                radius = strokeWidth * 0.75f,
                center = Offset(cx, cy),
            )

            drawLine(
                color = color,
                start = Offset(cx, cy),
                end = Offset(cx, cy - r * 0.62f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            drawLine(
                color = color,
                start = Offset(cx, cy),
                end = Offset(cx - r * 0.36f, cy - r * 0.20f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        fun drawZMarkInLane(
            baseX: Float,
            baseY: Float,
            driftDistanceDp: Dp,
            sizePx: Float,
            swPx: Float,
            progress: Float,
            maxRotation: Float,
        ) {
            val p = progress.coerceIn(0f, 1f)
            val driftY = -driftDistanceDp.toPx() * p
            val driftX = (driftDistanceDp.toPx() * 0.55f) * p

            val alpha = when {
                p < 0.20f -> p / 0.20f
                p > 0.75f -> (1f - p) / 0.25f
                else -> 1f
            }

            val x = baseX + driftX
            val y = baseY + driftY
            val zColor = color.copy(alpha = color.alpha * alpha)
            val rotationAngle = sin(p * Math.PI.toFloat() * 2f) * maxRotation

            rotate(degrees = rotationAngle, pivot = Offset(x + sizePx / 2f, y + sizePx * 0.55f)) {
                val zPath = Path().apply {
                    moveTo(x, y)
                    lineTo(x + sizePx, y)
                    lineTo(x, y + sizePx * 1.1f)
                    lineTo(x + sizePx, y + sizePx * 1.1f)
                }
                drawPath(
                    path = zPath,
                    color = zColor,
                    style = Stroke(width = swPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }

        drawZMarkInLane(
            baseX = cx + r * 0.55f,
            baseY = cy - r * 1.55f,
            driftDistanceDp = 6.dp,
            sizePx = 9.dp.toPx(),
            swPx = 2.2.dp.toPx(),
            progress = z0Progress,
            maxRotation = -4.0f,
        )

        drawZMarkInLane(
            baseX = cx + r * 1.05f,
            baseY = cy - r * 2.50f,
            driftDistanceDp = 8.dp,
            sizePx = 13.dp.toPx(),
            swPx = 2.8.dp.toPx(),
            progress = z1Progress,
            maxRotation = 4.5f,
        )

        drawZMarkInLane(
            baseX = cx + r * 1.55f,
            baseY = cy - r * 3.65f,
            driftDistanceDp = 10.dp,
            sizePx = 18.dp.toPx(),
            swPx = 3.5.dp.toPx(),
            progress = z2Progress,
            maxRotation = -4.0f,
        )
    }
}
