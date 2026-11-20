package app.expressiveclock.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.expressiveclock.R

object ClockFace {

    /** Ultra-condensed, for the alarm grid. */
    const val CONDENSED = 25f

    /** Normal width, for body text and labels. */
    const val NORMAL = 100f

    const val TIMER_WIDTH = 95f
    const val TIMER_WEIGHT = 490

    const val WEIGHT_ON = 260

    const val WEIGHT_OFF = 120

    const val DIGIT_EM = 0.728f

    const val INK_TOP_EM = 0.966f - DIGIT_EM

    fun fontSizeFor(capHeight: Dp): Dp = capHeight / DIGIT_EM

    private const val CAPITAL_EM = 0.716f

    /** Distance from a text box's top down to a capital's top, in em. */
    const val CAPITAL_INK_TOP_EM = 0.966f - CAPITAL_EM

    fun capitalSizeFor(capHeight: Dp): Dp = capHeight / CAPITAL_EM

    private val cache = HashMap<Int, FontFamily>()

    /** Google Sans Flex pinned to one point in axis space. */
    fun family(
        opticalSize: Float,
        width: Float = NORMAL,
        weight: Int = 400,
        grade: Float = 0f,
        roundness: Float = 0f,
    ): FontFamily {
        val key = (opticalSize.toInt() * 31 + width.toInt()) * 31 * 31 +
            weight * 31 + grade.toInt() * 7 + roundness.toInt()
        return cache.getOrPut(key) {
            FontFamily(
                Font(
                    resId = R.font.google_sans_flex,
                    weight = FontWeight(weight.coerceIn(1, 1000)),
                    variationSettings = FontVariation.Settings(
                        FontVariation.Setting("opsz", opticalSize.coerceIn(6f, 144f)),
                        FontVariation.Setting("wdth", width.coerceIn(25f, 151f)),
                        FontVariation.Setting("wght", weight.coerceIn(1, 1000).toFloat()),
                        FontVariation.Setting("GRAD", grade.coerceIn(0f, 100f)),
                        FontVariation.Setting("ROND", roundness.coerceIn(0f, 100f)),
                    ),
                ),
            )
        }
    }

    /** The meridiem's style, exposed so a caller can measure it before deciding its tracking. */
    fun capitals(capHeight: Dp, width: Float = 75f, weight: Int = 500, tracking: Float = 0f): TextStyle {
        val size = capitalSizeFor(capHeight)
        return TextStyle(
            fontFamily = family(opticalSize = size.value, width = width, weight = weight),
            fontSize = size.value.sp,
            letterSpacing = tracking.em,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        )
    }

    const val ROUNDED = 100f

    fun numerals(
        capHeight: Dp,
        width: Float,
        weight: Int,
        tabular: Boolean = true,
        tracking: Float = 0f,
        slashedZero: Boolean = false,
        roundness: Float = 0f,
    ): TextStyle = TextStyle(
        fontFamily = family(
            opticalSize = fontSizeFor(capHeight).value,
            width = width,
            weight = weight,
            roundness = roundness,
        ),
        fontSize = fontSizeFor(capHeight).value.sp,
        letterSpacing = tracking.em,
        fontFeatureSettings = buildString {
            append(if (tabular) "tnum, lnum" else "pnum, lnum")
            if (slashedZero) append(", zero")
        },
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    const val TRACKING_DELTA = 0.004f
    const val CONDENSED_TRACKING = -0.014f + TRACKING_DELTA

}

class InkBounds(val left: Float, val width: Float, val advance: Float) {
    /** Distance from the last ink to the end of the reserved box. */
    val right: Float get() = advance - left - width
}

@Composable
fun rememberInkBounds(text: String, style: TextStyle): InkBounds {
    val resolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current
    return remember(text, style, density) { inkBounds(resolver, density, text, style) }
}

fun inkBounds(
    resolver: FontFamily.Resolver,
    density: Density,
    text: String,
    style: TextStyle,
): InkBounds {
    return run {
        val typeface = resolver.resolve(
            fontFamily = style.fontFamily,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            fontSynthesis = FontSynthesis.All,
        ).value as android.graphics.Typeface
        val paint = android.graphics.Paint().apply {
            this.typeface = typeface
            textSize = with(density) { style.fontSize.toPx() }
            letterSpacing = style.letterSpacing.value
            fontFeatureSettings = style.fontFeatureSettings
            isAntiAlias = true
        }
        val r = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, r)
        InkBounds(r.left.toFloat(), r.width().toFloat(), paint.measureText(text))
    }
}

@Composable
fun CapText(
    text: String,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    width: Float = 75f,
    weight: Int = 500,
    tracking: Float = 0f,
) {
    val style = ClockFace.capitals(capHeight, width, weight, tracking).copy(color = color)
    InkTight(text, style, capHeight, modifier)
}

@Composable
fun StretchedCaps(
    text: String,
    capHeight: Dp,
    color: Color,
    scaleX: Float,
    modifier: Modifier = Modifier,
    width: Float = ClockFace.CONDENSED,
    weight: Int = 600,
) {
    val style = ClockFace.capitals(capHeight, width, weight).copy(color = color)
    StretchedCaps(AnnotatedString(text), style, capHeight, scaleX, modifier)
}

@Composable
fun StretchedCaps(
    text: AnnotatedString,
    style: TextStyle,
    capHeight: Dp,
    scaleX: Float,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val layout = remember(text, style) { measurer.measure(text, style) }
    Layout(
        content = {
            BasicText(
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.graphicsLayer {
                    this.scaleX = scaleX
                    transformOrigin = TransformOrigin(0f, 0.5f)
                },
            )
        },
        modifier = modifier.padding(end = 12.dp),
    ) { measurables, _ ->
        val placeable = measurables.first().measure(
            Constraints(maxWidth = Constraints.Infinity, maxHeight = Constraints.Infinity),
        )
        val inkTop = (layout.firstBaseline - capHeight.toPx()).toInt()
        layout((placeable.width * scaleX).toInt(), capHeight.roundToPx()) {
            placeable.place(0, -inkTop)
        }
    }
}

@Composable
fun Numerals(
    text: String,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    width: Float = ClockFace.NORMAL,
    weight: Int = ClockFace.WEIGHT_ON,
    tabular: Boolean = true,
    tracking: Float = 0f,
    slashedZero: Boolean = false,
    roundness: Float = 0f,
) {
    val style = ClockFace.numerals(capHeight, width, weight, tabular, tracking, slashedZero, roundness)
        .copy(color = color)
    InkTight(text, style, capHeight, modifier)
}

@Composable
private fun InkTight(text: String, style: TextStyle, capHeight: Dp, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    val layout = remember(text, style) { measurer.measure(text, style) }
    Layout(
        content = { BasicText(text = text, style = style, maxLines = 1, softWrap = false) },
        modifier = modifier,
    ) { measurables, _ ->
        val placeable = measurables.first().measure(
            Constraints(maxWidth = Constraints.Infinity, maxHeight = Constraints.Infinity),
        )
        val inkTop = (layout.firstBaseline - capHeight.toPx()).toInt()
        layout(placeable.width, capHeight.roundToPx()) { placeable.place(0, -inkTop) }
    }
}
