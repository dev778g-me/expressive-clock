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
import com.dev.expressiveclock.ui.theme.ClockFace
import com.dev.expressiveclock.ui.theme.Numerals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimersScreen(
    timer: ClockTimer?,
    draft: Duration,
    nowElapsedMillis: Long,
    presets: List<Int> = DEFAULT_TIMER_PRESETS,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onResetDraft: () -> Unit,
    onPreset: (Int) -> Unit,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val active = timer != null
    val remaining = timer?.remaining(nowElapsedMillis) ?: Duration.ZERO
    val isFinished = timer?.state == TimerState.FINISHED || (timer?.state == TimerState.RUNNING && remaining.isZero)
    val running = timer?.state == TimerState.RUNNING && !isFinished
    val windowSizeInfo = LocalAppWindowSizeInfo.current

    if (windowSizeInfo.isExpanded) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                modifier = Modifier.graphicsLayer { clip = false },
                targetState = active,
                transitionSpec = {
                    if (targetState) {
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = 420f,
                            ),
                            initialOffsetY = { it / 5 },
                        ) + fadeIn(tween(220)) togetherWith
                            (slideOutVertically(
                                animationSpec = tween(160),
                                targetOffsetY = { -it / 10 },
                            ) + fadeOut(tween(140))) using SizeTransform(clip = false)
                    } else {
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = 420f,
                            ),
                            initialOffsetY = { -it / 10 },
                        ) + fadeIn(tween(220)) togetherWith
                            (slideOutVertically(
                                animationSpec = tween(160),
                                targetOffsetY = { it / 5 },
                            ) + fadeOut(tween(140))) using SizeTransform(clip = false)
                    }
                },
                label = "timerBodyExpanded",
            ) { showingActive ->
                if (showingActive && timer != null) {
                    ExpandedRunningTimerBody(
                        timer = timer,
                        nowElapsedMillis = nowElapsedMillis,
                        isFinished = isFinished,
                        onPauseResume = onPauseResume,
                        onAddTen = onAddTen,
                        onCancel = onCancel,
                    )
                } else {
                    ExpandedSetTimerBody(
                        draft = draft,
                        presets = presets,
                        onDigit = onDigit,
                        onBackspace = onBackspace,
                        onResetDraft = onResetDraft,
                        onPreset = onPreset,
                        onStart = onStart,
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.fillMaxWidth().weight(1f).then(
                    if (windowSizeInfo.isHeightCompact) Modifier.verticalScroll(rememberScrollState())
                    else Modifier,
                ),
                contentAlignment = Alignment.TopCenter,
            ) {
                AnimatedContent(
                    modifier = Modifier.graphicsLayer { clip = false },
                    targetState = active,
                    transitionSpec = {
                        if (targetState) {
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = 420f,
                                ),
                                initialOffsetY = { it / 5 },
                            ) + fadeIn(tween(220)) togetherWith
                                (slideOutVertically(
                                    animationSpec = tween(160),
                                    targetOffsetY = { -it / 10 },
                                ) + fadeOut(tween(140))) using SizeTransform(clip = false)
                        } else {
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = 420f,
                                ),
                                initialOffsetY = { -it / 10 },
                            ) + fadeIn(tween(220)) togetherWith
                                (slideOutVertically(
                                    animationSpec = tween(160),
                                    targetOffsetY = { it / 5 },
                                ) + fadeOut(tween(140))) using SizeTransform(clip = false)
                        }
                    },
                    label = "timerBody",
                ) { showingActive ->
                    if (showingActive && timer != null) {
                        RunningTimerBody(timer, nowElapsedMillis, isFinished)
                    } else {
                        SetTimerBody(
                            draft = draft,
                            presets = presets,
                            onDigit = onDigit,
                            onBackspace = onBackspace,
                            onResetDraft = onResetDraft,
                            onPreset = onPreset,
                        )
                    }
                }
            }

            TimerControls(
                hasTimer = active,
                running = running,
                isFinished = isFinished,
                onStart = onStart,
                onPauseResume = onPauseResume,
                onAddTen = onAddTen,
                onCancel = onCancel,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}
