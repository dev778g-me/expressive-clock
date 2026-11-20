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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePresetSheet(
    theme: ThemeSettings,
    isDark: Boolean,
    onThemeChange: (ThemeSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        contentWindowInsets = { WindowInsets(bottom = 0.dp) },
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
