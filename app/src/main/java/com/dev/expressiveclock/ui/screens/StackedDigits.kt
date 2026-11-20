package com.dev.expressiveclock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.expressiveclock.ui.theme.ClockFace
import com.dev.expressiveclock.ui.blueGlow
import com.dev.expressiveclock.ui.glass
import com.dev.expressiveclock.ui.theme.Numerals

@Composable
fun StackedDigits(
    parts: List<String>,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    label: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (label != null) {
                    Modifier.clearAndSetSemantics { contentDescription = label }
                } else {
                    Modifier
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(35.dp),
    ) {
        parts.forEachIndexed { i, part ->
            DigitRow(
                part = part,
                capHeight = capHeight,
                color = color,
                leading = leading.takeIf { i == 0 },
                separator = i > 0,
            )
        }
    }
}

@Composable
fun DigitRow(
    part: String,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    separator: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(SEPARATOR_CELL_DP.dp), contentAlignment = Alignment.Center) {
            when {
                leading != null -> androidx.compose.material3.Icon(
                    leading,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(46.dp),
                )
                separator -> ColonDots(color)
            }
        }
        Numerals(
            text = part,
            capHeight = capHeight,
            color = color,
            width = ClockFace.TIMER_WIDTH,
            weight = ClockFace.TIMER_WEIGHT,
            slashedZero = true,
        )
    }
}

/** Measured: 18 dp dots with their centres 40.8 dp apart, so 22.8 dp of clear space between. */
private const val SEPARATOR_CELL_DP = 67.2f
private val DOT_DIAMETER = 18.dp

val ROW_INK_INSET: Dp = (SEPARATOR_CELL_DP.dp - DOT_DIAMETER) / 2

@Composable
private fun ColonDots(color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(22.8.dp)) {
        repeat(2) {
            Surface(color = color, shape = CircleShape, modifier = Modifier.size(DOT_DIAMETER)) {}
        }
    }
}

/** Measured off the render's Pause button. */
const val PILL_HEIGHT_DP = 136f

@Composable
fun WidePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: ImageVector? = null,
    outlined: Boolean = false,
    height: Dp = PILL_HEIGHT_DP.dp,
    compact: Boolean = false,
    fillWidth: Boolean = true,
    contentPaddingH: Dp = if (compact) 10.dp else 24.dp,
    contentAlpha: Float = 1f,
    glow: Boolean = true,
    shape: Shape = CircleShape,
) {
    Box(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(height)
            .clickable(onClick = onClick)
            .clip(shape)
            .glass(
                shape,
                if (outlined) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .94f)
                else container.copy(alpha = .40f),
                .86f,
                shadowElevation = if (glow) 18.dp else 0.dp,
            )
            .padding(horizontal = contentPaddingH),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 22.dp else 30.dp),
                    tint = content,
                )
                if (text.isNotEmpty()) Spacer(Modifier.width(if (compact) 8.dp else 14.dp))
            }
            if (text.isNotEmpty()) {
                Text(
                    text,
                    style = if (compact) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.headlineSmallEmphasized,
                    color = content,
                    maxLines = 1,
                )
            }
        }
    }
}
