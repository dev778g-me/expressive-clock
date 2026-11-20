package com.dev.expressiveclock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dev.expressiveclock.R

/** Weights we instantiate per optical size. Enough that a call-site `fontWeight` override picks a
 *  real cut instead of triggering synthetic bold. */
private val INSTANTIATED_WEIGHTS = intArrayOf(400, 500, 600, 700)

private val familyCache = HashMap<Int, FontFamily>()

private fun googleSansFlex(opticalSize: Float): FontFamily = familyCache.getOrPut(opticalSize.toInt()) {
    val opsz = opticalSize.coerceIn(6f, 144f)
    FontFamily(
        INSTANTIATED_WEIGHTS.map { w ->
            Font(
                resId = R.font.google_sans_flex,
                weight = FontWeight(w),
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(w),
                    FontVariation.Setting("opsz", opsz),
                ),
            )
        },
    )
}

/** Builds one style with its optical size locked to its point size, per the spec's tokens. */
private fun style(
    size: Int,
    lineHeight: Int,
    weight: Int,
    tracking: Double = 0.0,
): TextStyle = TextStyle(
    fontFamily = googleSansFlex(size.toFloat()),
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

private val REGULAR = 400
private val MEDIUM = 500
private val BOLD = 700

fun clockTypography(): Typography = Typography().copy(
    displayLarge = style(57, 64, REGULAR),
    displayMedium = style(45, 52, REGULAR),
    displaySmall = style(36, 44, REGULAR),
    headlineLarge = style(32, 40, REGULAR),
    headlineMedium = style(28, 36, REGULAR),
    headlineSmall = style(24, 32, REGULAR),
    titleLarge = style(22, 28, REGULAR),

    titleMedium = style(16, 24, MEDIUM),
    titleSmall = style(14, 20, MEDIUM),
    bodyLarge = style(16, 24, REGULAR),
    bodyMedium = style(14, 20, REGULAR),
    bodySmall = style(12, 16, REGULAR, tracking = 0.1),
    labelLarge = style(14, 20, MEDIUM),
    labelMedium = style(12, 16, MEDIUM, tracking = 0.1),
    labelSmall = style(11, 16, MEDIUM, tracking = 0.1),

    displayLargeEmphasized = style(57, 64, MEDIUM),
    displayMediumEmphasized = style(45, 52, MEDIUM),
    displaySmallEmphasized = style(36, 44, MEDIUM),
    headlineLargeEmphasized = style(32, 40, MEDIUM),
    headlineMediumEmphasized = style(28, 36, MEDIUM),
    headlineSmallEmphasized = style(24, 32, MEDIUM),
    titleLargeEmphasized = style(22, 28, MEDIUM),
    titleMediumEmphasized = style(16, 24, BOLD),
    titleSmallEmphasized = style(14, 20, BOLD),
    bodyLargeEmphasized = style(16, 24, MEDIUM),
    bodyMediumEmphasized = style(14, 20, MEDIUM),
    bodySmallEmphasized = style(12, 16, MEDIUM, tracking = 0.1),
    labelLargeEmphasized = style(14, 20, BOLD),
    labelMediumEmphasized = style(12, 16, BOLD, tracking = 0.1),
    labelSmallEmphasized = style(11, 16, BOLD, tracking = 0.1),
)

