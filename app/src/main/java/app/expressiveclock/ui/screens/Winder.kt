package app.expressiveclock.ui.screens

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun Winder(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 76.dp,
    stepRadians: Float = 0.080f,
    majorEvery: Int = 5,
) {
    val snapSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val scope = rememberCoroutineScope()
    val accent = MaterialTheme.colorScheme.primary
    val ridge = MaterialTheme.colorScheme.onSurfaceVariant
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val onChange by rememberUpdatedState(onValueChange)

    var pos by remember { mutableFloatStateOf(value.toFloat()) }
    var emitted by remember { mutableIntStateOf(value) }
    if (value != emitted && abs(pos - value) > 0.5f) {
        pos = value.toFloat()
        emitted = value
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                contentDescription = "Winder"
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = value.toFloat(),
                    range = range.first.toFloat()..range.last.toFloat(),
                    steps = range.last - range.first,
                )
            }
            .pointerInput(range) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val target = pos.roundToInt()
                            .coerceIn(range.first, range.last)
                            .toFloat()
                        scope.launch {
                            animate(initialValue = pos, targetValue = target, animationSpec = snapSpec) { v, _ ->
                                pos = v
                            }
                        }
                        if (pos.roundToInt() != emitted) {
                            emitted = pos.roundToInt()
                            onChange(emitted)
                        }
                    },
                ) { change, dx ->
                    change.consume()
                    val delta = -(dx / (size.width / 2f)) / stepRadians
                    pos = (pos + delta).coerceIn(range.first.toFloat(), range.last.toFloat())
                    val rounded = pos.roundToInt()
                    if (rounded != emitted) {
                        emitted = rounded
                        onChange(rounded)
                    }
                }
            },
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f
        val trackH = size.height
        val corner = 18.dp.toPx()
        val ridgeLen = trackH * 0.68f

        drawRoundRect(
            color = track,
            topLeft = Offset(0f, cy - trackH / 2f),
            size = Size(size.width, trackH),
            cornerRadius = CornerRadius(corner),
        )
        val halfTurn = (PI / 2 / stepRadians).toInt()
        val selectRange = stepRadians * 1.6f

        for (i in -halfTurn..halfTurn) {
            val unit = pos.roundToInt() + i
            val theta = (unit - pos) * stepRadians
            val depth = cos(theta)
            if (depth <= 0.02f) continue

            val x = cx + r * sin(theta)
            val inRange = unit >= range.first && unit <= range.last
            val major = unit % majorEvery == 0 && inRange

            val t = (1f - (abs(theta) / selectRange)).coerceIn(0f, 1f)
            val sel = t * t * (3f - 2f * t)

            val baseAlpha = ((if (major) 0.34f else 0.14f) + 0.66f * depth * depth) *
                (if (inRange) 1f else 0.22f)
            val h = ridgeLen * (1f + 0.22f * sel)
            val w = (6.4f * (0.26f + 0.74f * depth)) * (1f + 1.05f * sel)
            val color = lerp(
                (if (major) accent else ridge).copy(alpha = baseAlpha.coerceIn(0f, 1f)),
                accent,
                sel,
            )

            drawLine(
                color = color,
                start = Offset(x, cy - h / 2f),
                end = Offset(x, cy + h / 2f),
                strokeWidth = w,
                cap = StrokeCap.Round,
            )
        }
    }
}
