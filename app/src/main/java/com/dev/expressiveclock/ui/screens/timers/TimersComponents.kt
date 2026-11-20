package com.dev.expressiveclock.ui.screens.timers

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import com.dev.expressiveclock.core.ClockTimer
import com.dev.expressiveclock.core.TimerState
import com.dev.expressiveclock.data.DEFAULT_TIMER_PRESETS
import com.dev.expressiveclock.data.formatPresetLabel
import com.dev.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import com.dev.expressiveclock.ui.responsive.TwoPaneLayout
import com.dev.expressiveclock.ui.screens.DigitRow
import com.dev.expressiveclock.ui.screens.PILL_HEIGHT_DP
import com.dev.expressiveclock.ui.theme.ClockFace
import com.dev.expressiveclock.ui.theme.Numerals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

internal fun formatTimer(duration: Duration): String {
    val total = duration.seconds.coerceAtLeast(0)
    return "%02d:%02d:%02d".format(total / 3600, (total % 3600) / 60, total % 60)
}

internal enum class TimerRollDirection { UP, DOWN }

internal fun Modifier.featheredVerticalViewport(
    active: Boolean,
    fadeHeightFraction: Float = 0.18f,
): Modifier = if (!active) this else this
    .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        val fadePx = size.height * fadeHeightFraction
        val topFadeFraction = (fadePx / size.height).coerceIn(0f, 0.45f)
        val bottomFadeFraction = 1f - topFadeFraction

        val maskBrush = Brush.verticalGradient(
            0.0f to Color.Transparent,
            topFadeFraction to Color.Black,
            bottomFadeFraction to Color.Black,
            1.0f to Color.Transparent,
        )

        drawRect(
            brush = maskBrush,
            blendMode = BlendMode.DstIn,
        )
    }

@Composable
internal fun UpwardSetupDigitCell(
    char: Char,
    width: Dp,
    capHeight: Dp,
    color: Color,
    numeralWidth: Float,
    staggerDelayMs: Int = 0,
) {
    var oldChar by remember { mutableStateOf<Char?>(null) }
    var newChar by remember { mutableStateOf(char) }
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(char) {
        if (char != newChar) {
            if (staggerDelayMs > 0) {
                delay(staggerDelayMs.toLong())
            }
            oldChar = newChar
            newChar = char
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            )
        }
    }

    val progress = animProgress.value
    val density = LocalDensity.current
    val totalBoxHeight = capHeight + 52.dp
    val heightPx = with(density) { totalBoxHeight.toPx() }
    val isAnimating = progress < 1f && oldChar != null && oldChar != newChar

    Box(
        modifier = Modifier
            .width(width)
            .height(totalBoxHeight)
            .featheredVerticalViewport(active = isAnimating, fadeHeightFraction = 0.08f),
        contentAlignment = Alignment.Center,
    ) {
        if (char == ':') {
            Text(
                ":",
                fontFamily = ClockFace.family(opticalSize = 36f, width = 100f, weight = 550),
                fontSize = (capHeight.value * 0.65f).sp,
                color = color,
            )
        } else {
            if (progress < 1f && oldChar != null && oldChar != newChar && oldChar != ':') {
                val outgoingY = -progress * heightPx
                val outgoingAlpha = (1f - progress * 1.2f).coerceIn(0f, 1f)
                val outgoingBlurDp = (progress * 3.5f).dp

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(outgoingBlurDp)
                        .graphicsLayer {
                            translationY = outgoingY
                            alpha = outgoingAlpha
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Numerals(
                        text = oldChar.toString(),
                        capHeight = capHeight,
                        color = color,
                        width = numeralWidth,
                        weight = 600,
                        tracking = 0.004f,
                        slashedZero = false,
                        roundness = ClockFace.ROUNDED,
                    )
                }
            }

            val overshootPx = with(density) { 18.dp.toPx() } // Extended vertical overshoot (18dp)
            val incomingY = if (oldChar != null && oldChar != newChar) {
                if (progress < 0.42f) {
                    val p1 = progress / 0.42f
                    heightPx - p1 * (heightPx + overshootPx)
                } else {
                    val p2 = (progress - 0.42f) / 0.58f
                    -overshootPx * (1f + sin((p2 + 0.5f) * Math.PI.toFloat())) / 2f
                }
            } else {
                0f
            }

            val incomingAlpha = (progress * 1.4f).coerceIn(0f, 1f)
            val incomingBlurDp = ((1f - progress) * 3.5f).dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(incomingBlurDp)
                    .graphicsLayer {
                        translationY = incomingY
                        alpha = incomingAlpha
                        scaleX = 1f
                        scaleY = 1f
                    },
                contentAlignment = Alignment.Center,
            ) {
                Numerals(
                    text = newChar.toString(),
                    capHeight = capHeight,
                    color = color,
                    width = numeralWidth,
                    weight = 600,
                    tracking = 0.008f,
                    slashedZero = false,
                    roundness = ClockFace.ROUNDED,
                )
            }
        }
    }
}

@Composable
internal fun TickerTimerReadout(
    text: String,
    capHeight: Dp = 72.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    scale: Float = 1f,
    spacing: androidx.compose.ui.unit.Dp = 3.dp,
    digitWidth: androidx.compose.ui.unit.Dp = 56.dp,
    colonWidth: androidx.compose.ui.unit.Dp = 15.dp,
    numeralWidth: Float = ClockFace.TIMER_WIDTH,
    autoSize: Boolean = false,
    rollDirection: TimerRollDirection = TimerRollDirection.UP,
    enableStagger: Boolean = false,
) {
    BoxWithConstraintsCompat(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        if (autoSize) {
            val availableWidth = maxWidth
            val numDigits = text.count { it != ':' }
            val numColons = text.count { it == ':' }
            val numGaps = (text.length - 1).coerceAtLeast(0)

            val digitWeight = 1.0f
            val colonWeight = 0.28f
            val spacingWeight = 0.08f
            val totalUnits = (numDigits * digitWeight) + (numColons * colonWeight) + (numGaps * spacingWeight)

            val unit = if (totalUnits > 0f) availableWidth / totalUnits else availableWidth
            val computedDigitWidth = unit * digitWeight
            val computedColonWidth = unit * colonWeight
            val computedSpacing = unit * spacingWeight
            val computedCapHeight = (computedDigitWidth.value * 1.32f).dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { contentDescription = text },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(computedSpacing),
            ) {
                text.forEachIndexed { index, char ->
                    val staggerIndex = if (rollDirection == TimerRollDirection.UP) index else (text.length - 1 - index).coerceAtLeast(0)
                    val staggerDelayMs = if (enableStagger) (staggerIndex * 38) else 0

                    if (rollDirection == TimerRollDirection.UP) {
                        UpwardSetupDigitCell(
                            char = char,
                            width = if (char == ':') computedColonWidth else computedDigitWidth,
                            capHeight = computedCapHeight,
                            color = color,
                            numeralWidth = numeralWidth,
                            staggerDelayMs = staggerDelayMs,
                        )
                    } else {
                        TickerCell(
                            char = char,
                            width = if (char == ':') computedColonWidth else computedDigitWidth,
                            capHeight = computedCapHeight,
                            key = index,
                            color = color,
                            numeralWidth = numeralWidth,
                            rollDirection = rollDirection,
                            staggerDelayMs = staggerDelayMs,
                        )
                    }
                }
            }
        } else {
            val density = LocalDensity.current
            val desiredWidth = with(density) {
                (text.count { it != ':' } * digitWidth.toPx()) +
                    (text.count { it == ':' } * colonWidth.toPx()) +
                    ((text.length - 1).coerceAtLeast(0) * spacing.toPx())
            }
            val availableWidth = with(density) { maxWidth.toPx() }
            val fitScale = if (desiredWidth > 0f) (availableWidth / desiredWidth).coerceAtMost(1f) else 1f

            Row(
                modifier = Modifier
                    .graphicsLayer {
                        val effective = fitScale * scale
                        scaleX = effective
                        scaleY = effective
                    }
                    .clearAndSetSemantics { contentDescription = text },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                text.forEachIndexed { index, char ->
                    val staggerIndex = if (rollDirection == TimerRollDirection.UP) index else (text.length - 1 - index).coerceAtLeast(0)
                    val staggerDelayMs = if (enableStagger) (staggerIndex * 38) else 0

                    if (rollDirection == TimerRollDirection.UP) {
                        UpwardSetupDigitCell(
                            char = char,
                            width = if (char == ':') colonWidth else digitWidth,
                            capHeight = capHeight,
                            color = color,
                            numeralWidth = numeralWidth,
                            staggerDelayMs = staggerDelayMs,
                        )
                    } else {
                        TickerCell(
                            char = char,
                            width = if (char == ':') colonWidth else digitWidth,
                            capHeight = capHeight,
                            key = index,
                            color = color,
                            numeralWidth = numeralWidth,
                            rollDirection = rollDirection,
                            staggerDelayMs = staggerDelayMs,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxWithConstraintsCompat(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment,
    content: @Composable androidx.compose.foundation.layout.BoxWithConstraintsScope.() -> Unit,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
internal fun TickerCell(
    char: Char,
    width: androidx.compose.ui.unit.Dp,
    capHeight: androidx.compose.ui.unit.Dp,
    key: Int,
    color: Color,
    numeralWidth: Float,
    rollDirection: TimerRollDirection,
    staggerDelayMs: Int = 0,
) {
    androidx.compose.animation.AnimatedContent(
        modifier = Modifier
            .width(width)
            .height(capHeight + 52.dp),
        targetState = char,
        transitionSpec = {
            val oldDigit = initialState.digitToIntOrNull()
            val newDigit = targetState.digitToIntOrNull()
            if (oldDigit == null || newDigit == null) {
                fadeIn(tween(100)) togetherWith fadeOut(tween(70)) using SizeTransform(clip = false)
            } else {
                val incomingDirection = if (rollDirection == TimerRollDirection.UP) 1 else -1
                val outgoingDirection = -incomingDirection

                val springSpec = if (rollDirection == TimerRollDirection.UP) {
                    spring<IntOffset>(
                        stiffness = 420f,
                        dampingRatio = 1.0f,
                    )
                } else {
                    spring<IntOffset>(
                        stiffness = 380f,
                        dampingRatio = 0.68f, // Active timer ticker detent overshoot
                    )
                }

                val incoming = slideInVertically(
                    animationSpec = springSpec,
                    initialOffsetY = { it * incomingDirection },
                )

                val outgoing = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = if (rollDirection == TimerRollDirection.UP) 160 else 180,
                        delayMillis = staggerDelayMs,
                        easing = FastOutSlowInEasing,
                    ),
                    targetOffsetY = { it * outgoingDirection },
                )

                incoming + fadeIn(tween(90, delayMillis = staggerDelayMs)) togetherWith
                    (outgoing + fadeOut(tween(70, delayMillis = staggerDelayMs))) using SizeTransform(clip = false)
            }
        },
        label = "timerTicker-$key",
    ) { value ->
        val cellBlurProgress by transition.animateFloat(
            label = "cellBlurProgress",
            transitionSpec = {
                tween(durationMillis = 180, delayMillis = staggerDelayMs, easing = FastOutSlowInEasing)
            },
        ) { targetState ->
            if (targetState == EnterExitState.Visible) 0f else 1f
        }
        val currentBlurDp = (cellBlurProgress * 3.5f).dp
        val isCellAnimating = cellBlurProgress > 0.01f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(currentBlurDp)
                .featheredVerticalViewport(active = isCellAnimating, fadeHeightFraction = 0.08f),
            contentAlignment = Alignment.Center,
        ) {
            if (value == ':') {
                Text(
                    ":",
                    fontFamily = ClockFace.family(opticalSize = 36f, width = 100f, weight = if (rollDirection == TimerRollDirection.DOWN) 750 else 500, roundness = ClockFace.ROUNDED),
                    fontSize = (capHeight.value * 0.65f).sp,
                    color = color,
                )
            } else {
                Numerals(
                    text = value.toString(),
                    capHeight = capHeight,
                    color = color,
                    width = numeralWidth,
                    weight = if (rollDirection == TimerRollDirection.DOWN) 750 else ClockFace.TIMER_WEIGHT,
                    tracking = 0.008f,
                    slashedZero = false,
                    roundness = ClockFace.ROUNDED,
                )
            }
        }
    }
}

@Composable
internal fun ExpandedSetTimerBody(
    draft: Duration,
    presets: List<Int>,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onResetDraft: () -> Unit,
    onPreset: (Int) -> Unit,
    onStart: () -> Unit,
) {
    val view = LocalView.current
    var isLongPressReset by remember { mutableStateOf(false) }

    LaunchedEffect(draft) {
        if (!draft.isZero) {
            isLongPressReset = false
        }
    }

    TwoPaneLayout(
        leftPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Keypad(
                    onDigit = { char ->
                        isLongPressReset = false
                        onDigit(char)
                    },
                    onBackspace = {
                        isLongPressReset = false
                        onBackspace()
                    },
                    onResetDraft = {
                        isLongPressReset = true
                        onResetDraft()
                    },
                )
                Spacer(Modifier.height(14.dp))
                Presets(
                    presets = presets,
                    onPreset = { seconds ->
                        isLongPressReset = false
                        onPreset(seconds)
                    },
                )
            }
        },
        rightPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TickerTimerReadout(
                    text = formatTimer(draft),
                    autoSize = true,
                    numeralWidth = 82f,
                    rollDirection = TimerRollDirection.UP,
                    enableStagger = isLongPressReset,
                )
                Spacer(Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(56.dp),
                ) {
                    TimerButton(
                        text = "Start",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onStart() },
                        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .72f),
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        height = 56.dp,
                        compact = true,
                        fillWidth = true,
                        glow = false,
                    )
                }
            }
        },
        leftWeight = 1.1f,
        rightWeight = 0.9f,
        paneSpacing = 24.dp,
        contentPaddingDp = 12.dp,
    )
}

@Composable
internal fun ExpandedRunningTimerBody(
    timer: ClockTimer,
    nowElapsedMillis: Long,
    isFinished: Boolean = false,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
) {
    val view = LocalView.current
    val remaining = if (isFinished) Duration.ZERO else timer.remaining(nowElapsedMillis)
    val progress = if (isFinished) 1f else timer.fractionLeft(nowElapsedMillis).coerceIn(0f, 1f)
    val primary = MaterialTheme.colorScheme.primary
    val running = timer.state == TimerState.RUNNING && !isFinished

    TwoPaneLayout(
        leftPane = {
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val circleSize = minOf(maxWidth, maxHeight, 480.dp).coerceAtLeast(1.dp)
                val surface = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .96f)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .widthIn(max = circleSize)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    ExpressiveTimerRing(
                        progress = progress,
                        color = primary,
                        running = running || isFinished,
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    ) {
                        TickerTimerReadout(
                            text = formatTimer(remaining),
                            capHeight = 40.dp,
                            scale = 0.96f,
                            spacing = 0.5.dp,
                            digitWidth = 38.dp,
                            colonWidth = 10.dp,
                            rollDirection = TimerRollDirection.DOWN,
                            enableStagger = true,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isFinished) "TIME'S UP" else when (timer.state) {
                                TimerState.PAUSED -> "PAUSED"
                                TimerState.RUNNING -> "TIMER"
                                else -> "READY"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFinished) primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f),
                            letterSpacing = 1.8.sp,
                        )
                    }
                }
            }
        },
        rightPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isFinished) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        TimerButton(
                            text = "Stop",
                            icon = Icons.Rounded.Close,
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onCancel() },                            container = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f),
                            content = MaterialTheme.colorScheme.onTertiaryContainer,
                            height = 56.dp,
                            compact = true,
                            fillWidth = true,
                            glow = false,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            TimerButton(
                                text = if (running) "Pause" else "Resume",
                                icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onPauseResume() },                                height = 56.dp,
                                compact = true,
                                fillWidth = true,
                                glow = false,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            TimerButton(
                                text = "+10s",
                                icon = Icons.Rounded.Add,
                                onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onAddTen() },                                height = 56.dp,
                                compact = true,
                                fillWidth = true,
                                glow = false,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            TimerButton(
                                text = "Cancel",
                                icon = Icons.Rounded.Close,
                                onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onCancel() },                                height = 56.dp,
                                outlined = true,
                                content = MaterialTheme.colorScheme.onSurface,
                                compact = true,
                                fillWidth = true,
                                glow = false,
                            )
                        }
                    }
                }
            }
        },
        leftWeight = 1.1f,
        rightWeight = 0.9f,
        paneSpacing = 24.dp,
        contentPaddingDp = 12.dp,
    )
}

@Composable
internal fun SetTimerBody(
    draft: Duration,
    presets: List<Int>,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onResetDraft: () -> Unit,
    onPreset: (Int) -> Unit,
) {
    var isLongPressReset by remember { mutableStateOf(false) }

    LaunchedEffect(draft) {
        if (!draft.isZero) {
            isLongPressReset = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TickerTimerReadout(
            text = formatTimer(draft),
            autoSize = true,
            numeralWidth = 82f,
            rollDirection = TimerRollDirection.UP,
            enableStagger = isLongPressReset,
        )
        Spacer(Modifier.height(4.dp))
        Keypad(
            onDigit = { char ->
                isLongPressReset = false
                onDigit(char)
            },
            onBackspace = {
                isLongPressReset = false
                onBackspace()
            },
            onResetDraft = {
                isLongPressReset = true
                onResetDraft()
            },
        )
        Spacer(Modifier.height(4.dp))
        Presets(
            presets = presets,
            onPreset = { seconds ->
                isLongPressReset = false
                onPreset(seconds)
            },
        )
    }
}

@Composable
internal fun Presets(
    presets: List<Int>,
    onPreset: (Int) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            "Presets",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            presets.forEach { seconds ->
                Box(Modifier.weight(1f)) {
                    PresetButton(
                        seconds = seconds,
                        onClick = { onPreset(seconds) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TimerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: ImageVector? = null,
    outlined: Boolean = false,
    height: Dp = PILL_HEIGHT_DP.dp,
    compact: Boolean = false,
    fillWidth: Boolean = true,
    contentPaddingH: Dp = if (compact) 4.dp else 24.dp,
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
                    modifier = Modifier.size(if (compact) 24.dp else 34.dp),
                )
                if (text.isNotEmpty()) Spacer(Modifier.width(if (compact) 8.dp else 16.dp))
            }
            if (text.isNotEmpty()) {
                Text(
                    text,
                    style = if (compact) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.headlineSmallEmphasized,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
internal fun PresetButton(seconds: Int, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shapes = ButtonDefaults.shapes(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Text(
            formatPresetLabel(seconds),
            style = MaterialTheme.typography.labelLargeEmphasized,
        )
    }
}

@Composable
internal fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onResetDraft: () -> Unit) {
    val view = LocalView.current
    val rows = listOf("123", "456", "789")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c -> DigitKey(c, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.weight(1f))
            DigitKey('0', onDigit, Modifier.weight(1f))
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = {
                    onBackspace()
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                },
                onLongClick = {
                    onResetDraft()
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                },
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "Delete",
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
internal fun DigitKey(digit: Char, onDigit: (Char) -> Unit, modifier: Modifier = Modifier) {
    val view = LocalView.current
    KeyBox(modifier = modifier, onClick = {
        onDigit(digit)
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }, label = digit.toString()) {
        Text(
            digit.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = ClockFace.family(opticalSize = 30f, width = ClockFace.NORMAL, weight = 500),
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun KeyBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    label: String,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var longPressTriggered by remember { mutableStateOf(false) }
    var longPressJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Button(
        onClick = {
            if (!longPressTriggered) onClick()
            longPressTriggered = false
        },
        modifier = modifier
            .height(68.dp)
            .then(
                if (onLongClick != null) {
                    Modifier.pointerInput(onLongClick) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull() ?: continue
                                if (change.pressed) {
                                    if (longPressJob == null && !longPressTriggered) {
                                        longPressJob = scope.launch {
                                            delay(450.milliseconds)
                                            longPressTriggered = true
                                            onLongClick.invoke()
                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        }
                                    }
                                } else {
                                    longPressJob?.cancel()
                                    longPressJob = null
                                    longPressTriggered = false
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                },
            )
            .semantics { contentDescription = label },
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = container.copy(alpha = .88f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        content()
    }
}

@Composable
internal fun RunningTimerBody(
    timer: ClockTimer,
    nowElapsedMillis: Long,
    isFinished: Boolean = false,
) {
    val remaining = if (isFinished) Duration.ZERO else timer.remaining(nowElapsedMillis)
    val progress = if (isFinished) 1f else timer.fractionLeft(nowElapsedMillis).coerceIn(0f, 1f)
    val size = 420.dp
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints {
            val circleSize = minOf(maxWidth, maxHeight, size).coerceAtLeast(1.dp)
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                ExpressiveTimerRing(progress = progress, color = primary, running = timer.state == TimerState.RUNNING || isFinished)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    TickerTimerReadout(
                        text = formatTimer(remaining),
                        capHeight = 45.dp,
                        scale = 0.96f,
                        spacing = 0.5.dp,
                        digitWidth = 43.dp,
                        colonWidth = 12.dp,
                        rollDirection = TimerRollDirection.DOWN,
                        enableStagger = true,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isFinished) "Time's Up" else when (timer.state) {
                            TimerState.PAUSED -> "Paused"
                            TimerState.RUNNING -> "Timer"
                            else -> "Ready"
                        },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFinished) primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f),
                        letterSpacing = 1.8.sp,
                    )
                }
            }
        }
    }
}

/** Material Expressive-inspired organic progress ring. */
@Composable
internal fun ExpressiveTimerRing(progress: Float, color: Color, running: Boolean) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val strokeWidthPx = with(LocalDensity.current) { 16.dp.toPx() }
    val stroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }

    val waveSpeed by animateDpAsState(
        targetValue = if (running) 16.dp else 1.dp,
        label = "waveFraction",
    )

    CircularWavyProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxSize(),
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = stroke,
        wavelength = 60.dp,
        waveSpeed = waveSpeed,
        gapSize = 8.dp,
    )
}
internal fun buildSquigglePath(
    center: Offset,
    radius: Float,
    startDegrees: Float,
    sweepDegrees: Float,
    amplitude: Float,
    phase: Float,
): Path {
    val path = Path()
    val steps = maxOf(96, (kotlin.math.abs(sweepDegrees) / 1.25f).toInt())

    fun point(degrees: Float): Offset {
        val angle = Math.toRadians((degrees - 90f).toDouble())
        val primary = kotlin.math.sin(angle * 16.0 + phase)
        val secondary = kotlin.math.sin(angle * 11.0 - phase * 0.68) * 0.34
        val tertiary = kotlin.math.sin(angle * 23.0 + phase * 1.30) * 0.13
        val wave = amplitude * (primary + secondary + tertiary)
        val r = radius + wave.toFloat()
        return Offset(
            center.x + kotlin.math.cos(angle).toFloat() * r,
            center.y + kotlin.math.sin(angle).toFloat() * r,
        )
    }

    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val degrees = startDegrees + sweepDegrees * t
        val p = point(degrees)
        if (i == 0) {
            path.moveTo(p.x, p.y)
        } else {
            val previousDegrees = startDegrees + sweepDegrees * ((i - 1) / steps.toFloat())
            val previous = point(previousDegrees)
            val midpoint = Offset((previous.x + p.x) / 2f, (previous.y + p.y) / 2f)
            path.quadraticBezierTo(previous.x, previous.y, midpoint.x, midpoint.y)
            if (i == steps) path.lineTo(p.x, p.y)
        }
    }
    if (sweepDegrees >= 359f) path.close()
    return path
}

@Composable
internal fun TimerControls(
    hasTimer: Boolean,
    running: Boolean,
    isFinished: Boolean = false,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
) {
    val view = LocalView.current
    val cancelInteraction = remember { MutableInteractionSource() }
    val resumeInteraction = remember { MutableInteractionSource() }
    val secondsInteraction = remember { MutableInteractionSource() }
    if (isFinished) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TIMER_ROW_HEIGHT)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            TimerButton(
                text = "Stop",
                icon = Icons.Rounded.Close,
                onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onCancel() },                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                height = TIMER_ROW_HEIGHT,
                compact = true,
                fillWidth = true,
                glow = false,
            )
        }
        return
    }
    val expanded = hasTimer
    val expand by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "timerControlsExpand",
    )
    val label by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "timerControlsLabel",
    )
    val split by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = TIMER_SPLIT_MS,
            delayMillis = if (expanded) TIMER_EXPAND_LEAD_MS else 0,
        ),
        label = "timerControlsSplit",
    )
    val e = expand.coerceIn(0f, 1f)

    SubcomposeLayout(
        Modifier.fillMaxWidth().height(TIMER_ROW_HEIGHT).padding(horizontal = 16.dp).padding(bottom = 34.dp),
    ) { constraints ->
        val full = constraints.maxWidth
        val h = TIMER_ROW_HEIGHT.roundToPx()

        val rowWidth = subcompose("timerRowProbe") {
            DigitRow(
                part = "00",
                capHeight = 66.dp,
                color = MaterialTheme.colorScheme.primary,
                separator = true,
            )
        }.first().measure(Constraints()).width

        val natural = subcompose("timerStartProbe") {
            TimerButton(
                text = "Start",
                icon = Icons.Rounded.PlayArrow,
                onClick = {},
                height = TIMER_ROW_HEIGHT,
                compact = true,
                fillWidth = false,
                contentPaddingH = 28.dp,
                glow = false,
            )
        }.first().measure(Constraints(minHeight = h, maxHeight = h)).width

        val collapsedW = maxOf(rowWidth - TIMER_ROW_INSET.roundToPx(), natural).coerceAtMost(full)
        val collapsedX = ((full - collapsedW) / 2).coerceAtLeast(0)
        val w = lerp(collapsedW, full, e)
        val x = lerp(collapsedX, 0, e)

        val single = if (split < 1f) {
            subcompose("timerSingle") {
                Box(Modifier.graphicsLayer { alpha = 1f - split }) {
                    TimerButton(
                        text = "Start",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onStart() },
                        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .72f),
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        height = TIMER_ROW_HEIGHT,
                        compact = true,
                        contentPaddingH = 28.dp,
                        contentAlpha = label,
                        glow = false,
                    )
                }
            }.first().measure(Constraints.fixed(w, h))
        } else null

        val trio = if (split > 0f) {
            subcompose("timerTrio") {
                Box(Modifier.graphicsLayer {
                    alpha = split
                    scaleX = 0.85f + 0.15f * split
                    scaleY = 0.85f + 0.15f * split
                }) {
                    ButtonGroup(
                        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
                        modifier = Modifier.fillMaxWidth().height(TIMER_ROW_HEIGHT),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {

                        customItem(
                            buttonGroupContent = {
                                Button(
                                    onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onPauseResume() },                                    modifier = Modifier
                                        .weight(0.7f)
                                        .animateWidth(resumeInteraction),
                                    interactionSource = resumeInteraction,
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                                    ),
                                ) {
                                    AnimatedContent(targetState = running) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (it) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                            )
                                            Spacer(modifier = Modifier.width( 8.dp))
                                            Text(if (it)"Pause" else "Resume",style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                        }

                                    }
                                }
                            },
                            menuContent = {}
                        )

                        customItem(
                            buttonGroupContent = {
                                FilledTonalButton (
                                    onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onAddTen() },                                    modifier = Modifier
                                        .weight(0.6f)
                                        .animateWidth(secondsInteraction),
                                    interactionSource = secondsInteraction,
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = RoundedCornerShape(8.dp),
                                        pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width( 8.dp))
                                    Text( "+10s",style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)

                                }
                            },
                            menuContent = {}
                        )
                        customItem(
                            buttonGroupContent = {
                                OutlinedButton(
                                    onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onCancel() },                                    modifier = Modifier
                                        .weight(0.25f)
                                        .animateWidth(cancelInteraction),
                                    shapes = androidx.compose.material3.ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    interactionSource = cancelInteraction,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            menuContent = { _ ->
                            },
                        )
                    }
                }
            }.first().measure(Constraints.fixed(full, h))
        } else null

        layout(full, h) {
            single?.place(x, 0)
            trio?.place(0, 0)
        }
    }
}

internal val TIMER_GAP = 6.dp
internal val TIMER_ROW_HEIGHT = 64.dp
internal val TIMER_ROW_INSET = 10.dp
internal const val TIMER_EXPAND_LEAD_MS = 95
internal const val TIMER_SPLIT_MS = 150
internal const val TIMER_LABEL_FADE_MS = 70

