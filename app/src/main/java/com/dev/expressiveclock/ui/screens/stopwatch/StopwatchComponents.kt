package com.dev.expressiveclock.ui.screens.stopwatch

import com.dev.expressiveclock.ui.screens.DigitRow
import com.dev.expressiveclock.ui.screens.ROW_INK_INSET
import com.dev.expressiveclock.ui.screens.progressiveListEdgeMask

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.dev.expressiveclock.core.Lap
import com.dev.expressiveclock.ui.glass
import com.dev.expressiveclock.ui.theme.ClockFace
import com.dev.expressiveclock.core.Stopwatch
import com.dev.expressiveclock.core.stopwatchParts
import com.dev.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import com.dev.expressiveclock.ui.responsive.TwoPaneLayout

/** The dial and controls stay fixed; only the right-hand lap viewport scrolls. */
@Composable
internal fun ExpandedStopwatch(
    stopwatch: Stopwatch,
    elapsedMillis: Long,
    faceScale: Float,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val idle = !stopwatch.running && elapsedMillis == 0L && stopwatch.laps.isEmpty()
    val lapList = rememberLazyListState()
    val firstLapEntrance = remember { Animatable(if (stopwatch.laps.isEmpty()) 0f else 1f) }
    LaunchedEffect(stopwatch.laps.isEmpty()) {
        if (stopwatch.laps.isEmpty()) {
            firstLapEntrance.snapTo(0f)
        } else {
            firstLapEntrance.animateTo(1f, spring(dampingRatio = .68f, stiffness = 320f))
        }
    }
    val hasLapsBelow by remember(stopwatch.laps.size) {
        derivedStateOf {
            val layout = lapList.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()
            lastVisible != null && (
                lastVisible.index < stopwatch.laps.lastIndex ||
                    lastVisible.offset + lastVisible.size > layout.viewportEndOffset
            )
        }
    }
    val bottomFalloff by animateFloatAsState(
        targetValue = if (hasLapsBelow) 1f else 0f,
        label = "unfoldedLapBottomFalloff",
    )
    LaunchedEffect(stopwatch.laps.firstOrNull()?.index) {
        if (stopwatch.laps.isNotEmpty()) lapList.animateScrollToItem(0)
    }
    val controlsExpansion by animateFloatAsState(
        targetValue = if (idle) 0f else 1f,
        animationSpec = spring(dampingRatio = .62f, stiffness = 260f),
        label = "unfoldedStopwatchControls",
    )
    TwoPaneLayout(
        modifier = modifier.padding(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        leftWeight = .58f,
        rightWeight = .42f,
        paneSpacing = 20.dp,
        contentPaddingDp = 16.dp,
        leftPane = {
            BoxWithConstraints(
                Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val diameter = minOf(maxWidth, maxHeight).coerceAtLeast(1.dp)
                val scale = diameter / STOPWATCH_FACE_SIZE
                Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.requiredSize(STOPWATCH_FACE_SIZE).graphicsLayer {
                            scaleX = scale * faceScale
                            scaleY = scale * faceScale
                        },
                    ) {
                        StopwatchFace(elapsedMillis, stopwatch.running, onToggle)
                    }
                }
            }
        },
        rightPane = {
            BoxWithConstraints(
                Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val buttonHeight = ((maxHeight - 48.dp) / 3 - 8.dp).coerceIn(28.dp, 56.dp)
                if (!idle || controlsExpansion > .001f) {
                    Column(
                        Modifier.fillMaxSize()
                            .padding(top = buttonHeight * 3 + 26.dp)
                            .graphicsLayer { alpha = controlsExpansion.coerceIn(0f, 1f) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (stopwatch.laps.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))
                        }
                        LazyColumn(
                            state = lapList,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                                .clipToBounds()
                                .progressiveListEdgeMask(topStrength = 0f, bottomStrength = bottomFalloff),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 48.dp),
                        ) {
                            items(stopwatch.laps, key = { it.index }) { lap ->
                                val placement = if (lap.index == 1) {
                                    Modifier.animateItem(
                                        fadeInSpec = null,
                                        placementSpec = spring(dampingRatio = .68f, stiffness = 320f),
                                    )
                                } else {
                                    Modifier.animateItem(
                                        placementSpec = spring(dampingRatio = .68f, stiffness = 320f),
                                    )
                                }
                                Box(placement) {
                                    Box(if (lap.index == 1) Modifier.graphicsLayer {
                                        translationY = -LAP_ROW_HEIGHT.toPx() * (1f - firstLapEntrance.value)
                                        alpha = firstLapEntrance.value.coerceIn(0f, 1f)
                                    } else Modifier) {
                                        LapRow(
                                            lap, stopwatch.fastest == lap.index, stopwatch.slowest == lap.index,
                                            compact = true,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                ExpandedStopwatchControls(
                    idle = idle,
                    running = stopwatch.running,
                    expansion = controlsExpansion,
                    buttonHeight = buttonHeight,
                    onToggle = onToggle,
                    onLap = onLap,
                    onReset = onReset,
                )
            }
        },
    )
}

/** A single reversible spatial spring connects the centred pill to the three-button stack. */
@Composable
internal fun ExpandedStopwatchControls(
    idle: Boolean,
    running: Boolean,
    expansion: Float,
    buttonHeight: androidx.compose.ui.unit.Dp,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    val blend = expansion.coerceIn(0f, 1f)
    SubcomposeLayout(Modifier.fillMaxSize()) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val restingHeight = minOf(64.dp.roundToPx(), height)
        val activeHeight = buttonHeight.roundToPx()
        val animatedHeight = lerp(restingHeight, activeHeight, blend).coerceAtLeast(1)
        val animatedWidth = (width * (.86f + .04f * blend)).roundToInt().coerceIn(0, width)
        val restingY = (height - restingHeight) / 2f
        val gap = 8.dp.toPx()
        val buttonConstraints = Constraints.fixed(animatedWidth, animatedHeight)
        val main = subcompose("startPause") {
            StopwatchButton(
                text = if (idle) "Start" else if (running) "Pause" else "Resume",
                icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                onClick = onToggle,
                height = animatedHeight.toDp(),
                compact = true,
                glow = false,
            )
        }.first().measure(buttonConstraints)
        val satellites = if (!idle || expansion > .001f) {
            listOf("lap", "reset").map { slot ->
                subcompose(slot) {
                    Box(Modifier.graphicsLayer {
                        alpha = ((blend - .15f) / .85f).coerceIn(0f, 1f)
                    }) {
                        StopwatchButton(
                            text = if (slot == "lap") "Lap" else "Reset",
                            icon = if (slot == "reset") Icons.Rounded.Replay else null,
                            onClick = { if (!idle) { if (slot == "lap") onLap() else onReset() } },
                            container = if (slot == "lap") MaterialTheme.colorScheme.surfaceContainerHighest
                                else MaterialTheme.colorScheme.tertiaryContainer,
                            content = if (slot == "lap") MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onTertiaryContainer,
                            height = animatedHeight.toDp(), compact = true, glow = false,
                        )
                    }
                }.first().measure(buttonConstraints)
            }
        } else emptyList()
        layout(width, height) {
            val x = (width - animatedWidth) / 2
            satellites.forEachIndexed { index, button ->
                val targetY = (index + 1) * (activeHeight + gap)
                button.placeRelative(x, (restingY + (targetY - restingY) * expansion).roundToInt())
            }
            main.placeRelative(x, (restingY * (1f - expansion)).roundToInt())
        }
    }
}

@Composable
internal fun StopwatchFace(elapsedMillis: Long, running: Boolean, onToggle: () -> Unit) {
    val safeMillis = elapsedMillis.coerceAtLeast(0L)
    val hundredths = ((safeMillis % 1000L) / 10L).toInt()
    val minutes = (safeMillis / 60000L).toInt()
    val seconds = ((safeMillis / 1000L) % 60L).toInt()
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val currentTurnFraction = ((safeMillis % 60000L) / 60000f)
    val handTurnAnim = remember { Animatable(currentTurnFraction) }

    LaunchedEffect(safeMillis, running) {
        if (running || safeMillis > 0L) {
            handTurnAnim.snapTo(currentTurnFraction)
        } else {
            val start = (handTurnAnim.value % 1f + 1f) % 1f
            if (start > 0.001f && start < 0.999f) {
                val (overshootTarget, finalTarget) = if (start <= 0.5f) {
                    Pair(-0.038f, 0.0f) // counter-clockwise return past 12 o'clock
                } else {
                    Pair(1.038f, 1.0f) // clockwise return past 12 o'clock
                }
                handTurnAnim.snapTo(start)

                handTurnAnim.animateTo(
                    targetValue = overshootTarget,
                    animationSpec = tween(
                        durationMillis = 340,
                        easing = FastOutSlowInEasing,
                    ),
                )

                handTurnAnim.animateTo(
                    targetValue = finalTarget,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            handTurnAnim.snapTo(0f)
        }
    }

    val positiveTurns = (handTurnAnim.value % 1f + 1f) % 1f
    val secondAngle = positiveTurns * (2f * Math.PI.toFloat())

    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val faceSize = minOf(maxWidth, maxHeight, 480.dp).coerceAtLeast(1.dp)
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .widthIn(max = faceSize)
                .clip(CircleShape)
                .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .border(1.25.dp, MaterialTheme.colorScheme.outline.copy(0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            for (i in 0 until 60) {
                val angle = i / 60f * (2f * Math.PI.toFloat()) - Math.PI.toFloat() / 2f
                val outer = radius - if (i % 5 == 0) 5.dp.toPx() else 2.dp.toPx()
                val inner = radius - if (i % 5 == 0) 15.dp.toPx() else 8.dp.toPx()
                val p1 = Offset(center.x + kotlin.math.cos(angle) * inner, center.y + kotlin.math.sin(angle) * inner)
                val p2 = Offset(center.x + kotlin.math.cos(angle) * outer, center.y + kotlin.math.sin(angle) * outer)
                drawLine(
                    color = if (i % 5 == 0) primary.copy(alpha = .65f) else onSurface.copy(alpha = .16f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            val handLength = radius - 28.dp.toPx()
            val handAngle = secondAngle - Math.PI.toFloat() / 2f
            val tip = Offset(center.x + kotlin.math.cos(handAngle) * handLength, center.y + kotlin.math.sin(handAngle) * handLength)

            val innerCutoff = 72.dp.toPx()
            val fadeRange = 20.dp.toPx()
            val handBrush = Brush.radialGradient(
                0.0f to Color.Transparent,
                (innerCutoff / handLength).coerceIn(0f, 1f) to Color.Transparent,
                ((innerCutoff + fadeRange) / handLength).coerceIn(0f, 1f) to primary,
                1.0f to primary,
                center = center,
                radius = handLength,
            )

            drawLine(
                brush = handBrush,
                start = center,
                end = tip,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(primary, 5.dp.toPx(), tip)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    "%02d:".format(minutes),
                    fontFamily = ClockFace.family(opticalSize = 34f, width = 100f, weight = 500, roundness = ClockFace.ROUNDED),
                    fontSize = 42.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "%02d.%02d".format(seconds, hundredths),
                    fontFamily = ClockFace.family(opticalSize = 34f, width = 100f, weight = 500, roundness = ClockFace.ROUNDED),
                    fontSize = 42.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                if (running) "RUNNING" else "STOPWATCH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f),
                letterSpacing = 1.5.sp,
            )
        }
        }
    }
}

internal val STOPWATCH_FACE_SIZE = 410.dp

@Composable
internal fun StopwatchButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    icon: ImageVector? = null,
    outlined: Boolean = false,
    height: Dp = ROW_HEIGHT,
    compact: Boolean = false,
    fillWidth: Boolean = true,
    contentPaddingH: Dp = if (compact) 8.dp else 24.dp,
    contentAlpha: Float = 1f,
    glow: Boolean = true,
) {
    val buttonModifier = modifier
        .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
        .height(height)
        .graphicsLayer { alpha = contentAlpha }

    val contentRow: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
                if (text.isNotEmpty()) Spacer(Modifier.width(if (compact) 8.dp else 16.dp))
            }
            if (text.isNotEmpty()) {
                Text(
                    text,
                    style = if (compact) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.headlineSmallEmphasized,
                    maxLines = 1,
                )
            }
        }
    }

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            shapes = ButtonDefaults.shapes(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = content),
            contentPadding = PaddingValues(horizontal = contentPaddingH, vertical = 0.dp),
        ) {
            contentRow()
        }
    } else {
        Button (
            onClick = onClick,
            modifier = buttonModifier,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = contentPaddingH, vertical = 0.dp),
            elevation = if (glow) ButtonDefaults.buttonElevation() else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            contentRow()
        }
    }
}

@Composable
internal fun StopwatchControls(
    idle: Boolean,
    running: Boolean,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    val startStopInteraction = remember { MutableInteractionSource() }
    val lapInteraction = remember { MutableInteractionSource() }
    val resetInteraction = remember { MutableInteractionSource() }
    val expanded = !idle
    val expand by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expand",
    )
    val label by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "label",
    )
    val split by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = SPLIT_MS,
            delayMillis = if (expanded) EXPAND_LEAD_MS else 0,
        ),
        label = "split",
    )
    val e = expand.coerceIn(0f, 1f)

    SubcomposeLayout(
        Modifier.fillMaxWidth().height(ROW_HEIGHT).padding(horizontal = 20.dp),
    ) { constraints ->
        val full = constraints.maxWidth
        val h = ROW_HEIGHT.roundToPx()

        val inset = ROW_INK_INSET.roundToPx()
        val rowWidth = subcompose("rowProbe") {
            DigitRow(
                part = "00",
                capHeight = READOUT_CAP,
                color = MaterialTheme.colorScheme.primary,
                separator = true,
            )
        }.first().measure(Constraints()).width

        val natural = subcompose("probe") {
            StopwatchButton(
                text = "Start",
                icon = Icons.Rounded.PlayArrow,
                onClick = {},
                height = ROW_HEIGHT,
                compact = true,
                fillWidth = false,
                contentPaddingH = IDLE_PADDING_H,
                glow = false,
            )
        }.first().measure(Constraints(minHeight = h, maxHeight = h)).width

        val collapsedW = maxOf(rowWidth - inset, natural).coerceAtMost(full)
        val collapsedX = ((full - collapsedW) / 2).coerceAtLeast(0)

        val w = lerp(collapsedW, full, e)
        val x = lerp(collapsedX, 0, e)

        val single = if (split < 1f) {
            subcompose("single") {
                Box(Modifier.graphicsLayer { alpha = 1f - split }) {
                    StopwatchButton(
                        text = "Start",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = onToggle,
                        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        height = ROW_HEIGHT,
                        compact = true,
                        contentPaddingH = IDLE_PADDING_H,
                        contentAlpha = label,
                        glow = false,
                    )
                }
            }.first().measure(Constraints.fixed(w, h))
        } else {
            null
        }

        val trio = if (split > 0f) {
            subcompose("trio") {
                Box(Modifier.graphicsLayer {
                    alpha = split
                    scaleX = 0.85f + 0.15f * split
                    scaleY = 0.85f + 0.15f * split
                }) {
                    ButtonGroup(
                        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
                        modifier = Modifier.fillMaxWidth().height(ROW_HEIGHT),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        customItem(
                            buttonGroupContent = {
                                Button(
                                    onClick = onLap,
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(lapInteraction),
                                    interactionSource = lapInteraction,
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                                    ),
                                ) {
                                    Text("Lap", maxLines = 1, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                }
                            },
                            menuContent = { _ -> },
                        )
                        val startStopInteraction = startStopInteraction
                        customItem(
                            buttonGroupContent = {
                                Button(
                                    onClick = onToggle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(startStopInteraction),
                                    interactionSource = startStopInteraction,
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = RoundedCornerShape(8.dp),
                                        pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (running) MaterialTheme.colorScheme.tertiaryContainer
                                        else MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = if (running) MaterialTheme.colorScheme.onTertiaryContainer
                                        else MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (running) "Stop" else "Start", maxLines = 1, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                }
                            },
                            menuContent = { _ -> },
                        )
                        customItem(
                            buttonGroupContent = {
                                OutlinedButton(
                                    onClick = onReset,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .animateWidth(resetInteraction),
                                    interactionSource = resetInteraction,
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Replay,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            menuContent = { _ -> },
                        )
                    }
                }
            }.first().measure(Constraints.fixed(full, h))
        } else {
            null
        }

        layout(full, h) {
            single?.place(x, 0)
            trio?.place(0, 0)
        }
    }
}

/** How long the pill has to finish growing before the row is allowed to divide. */
internal const val EXPAND_LEAD_MS = 95
internal const val SPLIT_MS = 150
internal const val LABEL_FADE_MS = 70

/** Roomier than the three-abreast pills: this one is not fighting two neighbours for width. */
internal val IDLE_PADDING_H = 28.dp

/** `StackedDigits` sets its rows 35 dp apart; the button sits on the same rhythm. */
internal val STACK_GAP = 35.dp

/** Shared so the Start button can be measured against a row drawn at the same size. */
internal val READOUT_CAP = 66.dp

internal val GAP = 6.dp
internal val ROW_HEIGHT = 64.dp

internal val LAP_ROW_HEIGHT = 48.dp
internal val LAP_GAP = 4.dp

@Composable
internal fun LapRow(lap: Lap, isFastest: Boolean, isSlowest: Boolean, compact: Boolean = false) {
    val badge = when {
        isFastest -> MaterialTheme.colorScheme.tertiaryContainer
        isSlowest -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val onBadge = when {
        isFastest -> MaterialTheme.colorScheme.onTertiaryContainer
        isSlowest -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val (m, s, cs) = lap.split.stopwatchParts()
    val (tm, ts, tcs) = lap.total.stopwatchParts()

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(percent = 50),
        border = null,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(LAP_ROW_HEIGHT)
            .glass(
                shape = RoundedCornerShape(percent = 50),
                tint = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .76f),
                borderAlpha = .72f,
                shadowElevation = 0.dp,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Surface(color = badge, shape = CircleShape, modifier = Modifier.size(if (compact) 24.dp else 28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        lap.index.toString(),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = onBadge,
                    )
                }
            }
            Text(
                "$m:$s.$cs",
                style = if (compact) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                "$tm:$ts.$tcs",
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
