package com.dev.expressiveclock.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val TWO_PANE_MIN_WIDTH = 600.dp
private val TWO_PANE_MIN_HEIGHT = 600.dp

enum class AppWindowWidthClass {
    COMPACT,
    EXPANDED,
}

/**
 * Window size information wrapper.
 */
data class AppWindowSizeInfo(
    val widthClass: AppWindowWidthClass,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
) {
    /** Short windows need scrolling even when there are no laps or other list items yet. */
    val isHeightCompact: Boolean
        get() = screenHeightDp < TWO_PANE_MIN_HEIGHT

    val isExpanded: Boolean
        get() = widthClass == AppWindowWidthClass.EXPANDED

    val isCompact: Boolean
        get() = widthClass == AppWindowWidthClass.COMPACT
}

@Composable
fun rememberAppWindowSizeInfo(): AppWindowSizeInfo {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val widthClass = if (widthDp >= TWO_PANE_MIN_WIDTH && heightDp >= TWO_PANE_MIN_HEIGHT) {
        AppWindowWidthClass.EXPANDED
    } else {
        AppWindowWidthClass.COMPACT
    }

    return AppWindowSizeInfo(
        widthClass = widthClass,
        screenWidthDp = widthDp,
        screenHeightDp = heightDp,
    )
}

/**
 * CompositionLocal providing [AppWindowSizeInfo] throughout the UI hierarchy.
 */
val LocalAppWindowSizeInfo = staticCompositionLocalOf {
    AppWindowSizeInfo(
        widthClass = AppWindowWidthClass.COMPACT,
        screenWidthDp = 360.dp,
        screenHeightDp = 800.dp,
    )
}
