package app.expressiveclock.ui.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.expressiveclock.data.ThemeSettings
import app.expressiveclock.ui.screens.ThemePresetPicker

/**
 * Theme presets, as a bottom sheet rather than a block in the settings list.
 *
 * Sixteen preview cards would be the tallest thing on the screen by a wide margin and would sit
 * above it permanently to change something that is chosen a handful of times in the app's life.
 * As a sheet the settings list keeps one row for it, and opening it puts the grid at a comfortable
 * thumb height with the current settings still visible above the sheet's edge.
 *
 * Tapping a card applies immediately and closes — there is no confirm step — and dismissal
 * without a choice (scrim or swipe) leaves the theme untouched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePresetSheet(
    theme: ThemeSettings,
    isDark: Boolean,
    onThemeChange: (ThemeSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 32.dp),
        ) {
            SheetTitle("Theme presets")
            Text(
                "Select a card to apply its colors to the app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )
            ThemePresetPicker(
                theme = theme,
                isDark = isDark,
                onThemeChange = { next ->
                    onThemeChange(next)
                    onDismiss()
                },
            )
        }
    }
}
