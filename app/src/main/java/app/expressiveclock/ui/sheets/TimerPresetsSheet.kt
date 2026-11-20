package app.expressiveclock.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberScrollFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.expressiveclock.data.DEFAULT_TIMER_PRESETS
import app.expressiveclock.data.formatPresetLabel
import app.expressiveclock.ui.screens.settings.joinedBlockShape
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimerPresetsSheet(
    presets: List<Int>,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var currentPresets by remember(presets) {
        mutableStateOf(presets.take(4).let { if (it.size == 4) it else DEFAULT_TIMER_PRESETS })
    }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AlarmSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SheetHeaderButton(text = "Cancel", primary = false, onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                })
                Text("Timer presets", style = MaterialTheme.typography.titleLargeEmphasized, color = MaterialTheme.colorScheme.onSurface)
                SheetHeaderButton(text = "Save", primary = true, onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onSave(currentPresets) }
                }, )
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                currentPresets.forEachIndexed { index, seconds ->
                    val isFirst = index == 0
                    val originallyLast = index == currentPresets.lastIndex
                    val isLast = originallyLast && (editingIndex != index)
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
                            .clickable { editingIndex = if (editingIndex == index) null else index }
                            .padding(vertical = 1.dp),
                        shapes = ListItemDefaults.shapes(
                            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
                            pressedShape = CircleShape
                        ),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    formatPresetLabel(seconds),
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        },
                    ) {
                        Text("Preset ${index + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    AnimatedVisibility(visible = editingIndex == index) {
                        PresetDurationPicker(
                            initialSeconds = currentPresets.getOrElse(index) { 0 },
                            isLast = originallyLast,
                            onSet = { total ->
                                val updated = currentPresets.toMutableList()
                                updated[index] = total.coerceIn(1, 86399)
                                currentPresets = updated
                                editingIndex = null
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun PresetDurationPicker(
    initialSeconds: Int,
    isLast: Boolean = false,
    onSet: (Int) -> Unit,
) {
    var editMin by remember { mutableStateOf(initialSeconds / 60) }
    var editSec by remember { mutableStateOf(initialSeconds % 60) }
    val minState = rememberScrollFieldState(itemCount = 60, index = editMin)
    val secState = rememberScrollFieldState(itemCount = 60, index = editSec)
    LaunchedEffect(minState.selectedOption) { editMin = minState.selectedOption }
    LaunchedEffect(secState.selectedOption) { editSec = secState.selectedOption }
    LaunchedEffect(editMin) { if (minState.selectedOption != editMin) minState.scrollToOption(editMin) }
    LaunchedEffect(editSec) { if (secState.selectedOption != editSec) secState.scrollToOption(editSec) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(joinedBlockShape(isFirst = false, isLast = isLast))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScrollField(
                state = minState,
                contentDescription = "Minutes",
                modifier = Modifier.weight(1f).height(120.dp),
            )
            Text(":", style = MaterialTheme.typography.titleLarge)
            ScrollField(
                state = secState,
                contentDescription = "Seconds",
                modifier = Modifier.weight(1f).height(120.dp),
            )
        }
        FilledTonalButton (
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonDefaults.shapes(
                shape = RoundedCornerShape(12.dp),
                pressedShape = CircleShape
            ),
            onClick = { onSet(editMin * 60 + editSec) }) {
            Text("Set")
        }
    }
}