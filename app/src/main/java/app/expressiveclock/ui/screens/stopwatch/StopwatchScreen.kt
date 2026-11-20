package app.expressiveclock.ui.screens.stopwatch

import app.expressiveclock.ui.screens.stopwatch.ExpandedStopwatch
import app.expressiveclock.ui.screens.stopwatch.ExpandedStopwatchControls
import app.expressiveclock.ui.screens.stopwatch.StopwatchFace
import app.expressiveclock.ui.screens.alarms.FixedEdgeBlurContainer
import app.expressiveclock.ui.screens.stopwatch.StopwatchControls
import app.expressiveclock.ui.screens.stopwatch.LapRow
import app.expressiveclock.ui.screens.stopwatch.ROW_HEIGHT
import app.expressiveclock.ui.screens.stopwatch.LAP_ROW_HEIGHT
import app.expressiveclock.ui.screens.stopwatch.LAP_GAP
import app.expressiveclock.ui.screens.stopwatch.STOPWATCH_FACE_SIZE

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
import app.expressiveclock.core.Lap
import app.expressiveclock.ui.glass
import app.expressiveclock.ui.theme.ClockFace
import app.expressiveclock.core.Stopwatch
import app.expressiveclock.core.stopwatchParts
import app.expressiveclock.ui.responsive.LocalAppWindowSizeInfo
import app.expressiveclock.ui.responsive.TwoPaneLayout

/**
 * The stopwatch.
 *
 * Extrapolated: the concept set contains no stopwatch at all, so this reuses the timer's own
 * vocabulary (the stacked hollow digits, the wide pills) rather than importing a different look.
 * The bottom pair of digits is hundredths, which is why the whole screen is driven off a
 * frame-paced ticker instead of a one-second one.
 *
 * The fastest and slowest laps are marked in tertiary and error. That is not in the renders; it is
 * the one addition, on the grounds that a lap list nobody can compare is just a receipt.
 */
@Composable
fun StopwatchScreen(
    stopwatch: Stopwatch,
    nowElapsedMillis: Long,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val elapsed = stopwatch.elapsed(nowElapsedMillis)
    val fastest = stopwatch.fastest
    val slowest = stopwatch.slowest
    val listState = rememberLazyListState()
    val view = LocalView.current
    var previousRunning by remember { mutableStateOf<Boolean?>(null) }

    val faceScale = remember { Animatable(1f) }
    var previousRunningForScale by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(stopwatch.running) {
        val prevScale = previousRunningForScale
        previousRunningForScale = stopwatch.running
        if (prevScale != null && prevScale != stopwatch.running) {
            faceScale.snapTo(0.95f)
            faceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 380f,
                ),
            )
        }
    }

    LaunchedEffect(stopwatch.running) {
        val prev = previousRunning
        previousRunning = stopwatch.running
        if (prev != null && prev != stopwatch.running) {
            if (stopwatch.running) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }
        }
    }

    val handleReset = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        onReset()
    }

    if (LocalAppWindowSizeInfo.current.isExpanded) {
        ExpandedStopwatch(
            stopwatch = stopwatch,
            elapsedMillis = elapsed.toMillis(),
            faceScale = faceScale.value,
            onToggle = onToggle,
            onLap = onLap,
            onReset = handleReset,
            contentPadding = contentPadding,
            modifier = modifier,
        )
        return
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var displayedLaps by remember { mutableStateOf(stopwatch.laps) }
    val lapsAlpha = remember { Animatable(1f) }

    LaunchedEffect(stopwatch.laps) {
        val currentLaps = stopwatch.laps
        if (currentLaps.size > displayedLaps.size) {
            // Restore the original spring glide and overshoot on lap insertion.
            lapsAlpha.snapTo(1f)
            displayedLaps = currentLaps
            val lapRowHeightPx = with(density) { (LAP_ROW_HEIGHT + 14.dp).toPx() }
            listState.animateScrollBy(
                value = lapRowHeightPx,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = 320f,
                ),
            )
        } else if (currentLaps.isEmpty() && displayedLaps.isNotEmpty()) {
            // Reset: fade out lap bars while gliding scroll back up to top (item 0)
            val fadeJob = scope.launch {
                lapsAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            listState.animateScrollToItem(index = 0, scrollOffset = 0)
            fadeJob.join()
            displayedLaps = emptyList()
            lapsAlpha.snapTo(1f)
        } else {
            lapsAlpha.snapTo(1f)
            displayedLaps = currentLaps
        }
    }

    val effectiveBottomPadding = if (displayedLaps.isNotEmpty()) {
        contentPadding.calculateBottomPadding() + 140.dp
    } else {
        contentPadding.calculateBottomPadding()
    }

    FixedEdgeBlurContainer(
        topInset = contentPadding.calculateTopPadding(),
        bottomInset = contentPadding.calculateBottomPadding(),
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = displayedLaps.isNotEmpty() || LocalAppWindowSizeInfo.current.isHeightCompact,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = effectiveBottomPadding,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "face") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(540.dp)
                        .graphicsLayer {
                            scaleX = faceScale.value
                            scaleY = faceScale.value
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    StopwatchFace(
                        elapsedMillis = elapsed.toMillis(),
                        running = stopwatch.running,
                        onToggle = onToggle,
                    )
                }
            }
            item(key = "controls") {
                StopwatchControls(
                    idle = !stopwatch.running && elapsed.isZero,
                    running = stopwatch.running,
                    onToggle = onToggle,
                    onLap = onLap,
                    onReset = handleReset,
                )
            }
            if (displayedLaps.isNotEmpty()) {
                items(displayedLaps, key = { it.index }) { lap ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .graphicsLayer { alpha = lapsAlpha.value },
                    ) {
                        LapRow(lap, fastest == lap.index, slowest == lap.index)
                    }
                }
            }
        }
    }
}
