package com.dev.expressiveclock.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Clean white canvas for the glass treatment. Colour comes from subtle blue light, not a gradient page. */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.background)) { content() }
}

@Composable
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = Color.White.copy(alpha = .88f),
    borderAlpha: Float = .82f,
    accent: Color? = null,
    shadowElevation: Dp = 14.dp,
): Modifier {
    val isDark = tint.luminance() < .5f
    val resolvedAccent = accent ?: MaterialTheme.colorScheme.primary
    val shadow = if (shadowElevation > 0.dp) {
        Modifier.shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = .50f) else resolvedAccent.copy(alpha = .12f),
            spotColor = if (isDark) Color.Black.copy(alpha = .70f) else resolvedAccent.copy(alpha = .22f),
        )
    } else {
        Modifier
    }
    return this
    .then(shadow)
    .background(
        Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    tint.copy(alpha = .94f),
                    tint.copy(alpha = .88f),
                    tint.copy(alpha = .84f),
                )
            } else {
                listOf(
                    tint.copy(alpha = .96f),
                    tint.copy(alpha = .88f),
                    tint.copy(alpha = .82f),
                )
            },
            start = Offset(0f, 0f),
            end = Offset(600f, 600f),
        ),
        shape,
    )
    .border(1.25.dp, if (isDark) Color.White.copy(alpha = .14f) else Color.White.copy(alpha = borderAlpha), shape)
}

@Composable
fun FloatingCircle(
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    fill: Color,
    border: Color = Color.White.copy(alpha = .82f),
    shadowColor: Color = Color(0xFF1687D0),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.size(size + 72.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f + 12.dp.toPx()
            val r = this.size.width * .48f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        shadowColor.copy(alpha = .30f),
                        shadowColor.copy(alpha = .14f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = r + 28.dp.toPx(),
                ),
                radius = r + 28.dp.toPx(),
                center = Offset(cx, cy),
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .background(fill, androidx.compose.foundation.shape.CircleShape)
                .border(1.25.dp, border, androidx.compose.foundation.shape.CircleShape),
            content = content,
        )
    }
}

/** A small theme-coloured drop shadow beneath floating controls. */
@Composable
fun Modifier.blueGlow(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(32.dp),
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
): Modifier = this.shadow(
    elevation = 34.dp,
    shape = shape,
    clip = false,
    ambientColor = color.copy(alpha = .24f),
    spotColor = color.copy(alpha = .40f),
)

