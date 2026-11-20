package app.expressiveclock.ui.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.TimeScroll
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.VibrantTimePickerDialog
import androidx.compose.material3.isInputValid
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.expressiveclock.ui.screens.settings.joinedBlockShape
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale
import java.time.format.TextStyle as DayTextStyle

/** Special sound URI value used when an alarm/timer is explicitly silent. */
internal const val SILENT = "silent"

/** Returns the locale/system-defined first day of the week. */
internal fun systemFirstDay(): DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

internal val AlarmSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
private val AlarmCardShape = RoundedCornerShape(22.dp)
private val AlarmControlShape = RoundedCornerShape(18.dp)

/** Shared Material-card presentation used by both the create and edit alarm sheets. */
@Composable
fun AlarmPickerSheetContent(
    title: String,
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    pm: Boolean,
    editingHour: Boolean,
    label: String,
    days: Set<DayOfWeek>,
    weekOrder: List<DayOfWeek>,
    vibrate: Boolean,
    soundLabel: String,
    showDelete: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPmChange: (Boolean) -> Unit,
    onEditingHourChange: (Boolean) -> Unit,
    onLabelChange: (String) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onSoundClick: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = if (is24Hour) hour else (hour % 12) + if (pm) 12 else 0,
        initialMinute = minute,
        is24Hour = is24Hour,
    )

    LaunchedEffect(pickerState, is24Hour) {
        snapshotFlow { Triple(pickerState.hour, pickerState.minute, pickerState.selection) }
            .distinctUntilChanged()
            .collect { (h, m, selection) ->
                onHourChange(if (is24Hour) h else hour12(h))
                onMinuteChange(m)
                if (!is24Hour) onPmChange(h >= 12)
                onEditingHourChange(selection == TimePickerSelectionMode.Hour)
            }
    }

    LaunchedEffect(pickerState, hour, minute, pm, is24Hour) {
        val expected = if (is24Hour) hour else (hour % 12) + if (pm) 12 else 0
        if (pickerState.hour != expected) pickerState.hour = expected
        if (pickerState.minute != minute) pickerState.minute = minute
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .clip(AlarmSheetShape)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SheetHeaderButton(
                text = "Cancel",
                primary = false,
                onClick = onCancel,
            )
            Text(
                title,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SheetHeaderButton(
                text = "Save",
                primary = true,
                onClick = onSave,
                icon = Icons.Rounded.Check,
            )
        }



        Spacer(Modifier.height(8.dp))


        var scrollMode by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = remember(pickerState.hour, pickerState.minute, days) {
                    nextRingSummary(pickerState.hour, pickerState.minute, days)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
            FilledTonalIconButton(
                onClick = { scrollMode = !scrollMode },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = if (scrollMode) Icons.Filled.Keyboard else Icons.Filled.SwipeVertical,
                    contentDescription = if (scrollMode) "Switch to typing" else "Switch to scrolling",
                )
            }
        }

        Spacer(Modifier.height(8.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val scale = (maxWidth.value / 280f).coerceIn(0.8f, 1.4f)

                if (scrollMode) {TimeScroll(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                        },
                    state = pickerState,
                    shapes = TimePickerDefaults.shapes(),
                )} else {
                    TimeInput(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                            },
                        state = pickerState,
                        shapes = TimePickerDefaults.shapes(),
                    )
                }
            }


        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            weekOrder.forEach { day ->
                ToggleButton(
                    modifier = Modifier.weight(1f),
                    checked = day in days,
                    colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onCheckedChange = { onToggleDay(day) },
                ) {
                    Text(day.getDisplayName(DayTextStyle.NARROW, LocalLocale.current.platformLocale), style = MaterialTheme.typography.titleMedium)
                }

            }
        }



        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            AlarmNameListItem(
                label = label,
                onLabelChange = onLabelChange,
                isFirst = true,
                isLast = false,
            )
            SoundListItem(
                soundLabel = soundLabel,
                onSoundClick = onSoundClick,
                isFirst = false,
                isLast = false,
            )
            VibrationListItem(
                vibrate = vibrate,
                onVibrateChange = onVibrateChange,
                isFirst = false,
                isLast = true,
            )
        }

        if (showDelete) {
            Spacer(Modifier.height(8.dp))
            DeleteAlarmCard(onDelete = onDelete)
        }

        Spacer(Modifier.height(2.dp))
    }
}

private fun hour12(hour: Int): Int = (hour % 12).let { if (it == 0) 12 else it }

@Composable
private fun DayChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shapeRadius by animateDpAsState(
        targetValue = if (selected) 13.dp else 20.dp,
        animationSpec = spring(dampingRatio = .78f, stiffness = 520f),
        label = "dayChipRadius",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(180),
        label = "dayChipFill",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(160),
        label = "dayChipText",
    )

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(shapeRadius))
            .background(fill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = textColor,
        )
    }
}

@Composable
private fun AlarmNameListItem(
    label: String,
    onLabelChange: (String) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    var isDefaultUntouched by remember(label) { mutableStateOf(label == "Alarm") }
    var value by remember {
        mutableStateOf(TextFieldValue(text = label, selection = TextRange(label.length)))
    }

    LaunchedEffect(label) {
        if (value.text != label) {
            value = TextFieldValue(text = label, selection = TextRange(label.length))
            isDefaultUntouched = (label == "Alarm")
        }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .padding(vertical = 1.dp),
        shapes = ListItemDefaults.shapes(
            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Icon(
                Icons.Outlined.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        },
        supportingContent = {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (isDefaultUntouched && value.text == "Alarm" && newValue.text != "Alarm") {
                        isDefaultUntouched = false
                    }
                    value = newValue
                    onLabelChange(value.text)
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && isDefaultUntouched && value.text == "Alarm") {
                            isDefaultUntouched = false
                            value = TextFieldValue("")
                            onLabelChange("")
                        }
                    },
            )
        },
    ) {
        Text(
            "Name",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SoundListItem(
    soundLabel: String,
    onSoundClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .clickable(onClick = onSoundClick)
            .padding(vertical = 1.dp),
        shapes = ListItemDefaults.shapes(
            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Icon(
                Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    soundLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Text(
            "Sound",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun VibrationListItem(
    vibrate: Boolean,
    onVibrateChange: (Boolean) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(joinedBlockShape(isFirst = isFirst, isLast = isLast))
            .clickable(onClick = { onVibrateChange(!vibrate) })
            .padding(vertical = 1.dp),
        shapes = ListItemDefaults.shapes(
            shape = joinedBlockShape(isFirst = isFirst, isLast = isLast),
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Icon(
                Icons.Outlined.Vibration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        },
        trailingContent = {
            Switch(
                checked = vibrate,
                onCheckedChange = onVibrateChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            )
        },
    ) {
        Text(
            "Vibration",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DeleteAlarmCard(onDelete: () -> Unit) {

    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete alarm", tint = MaterialTheme.colorScheme.onErrorContainer)

        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        onClick = onDelete
    ) {
        Text("Delete Alarm", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer)

    }

}

@Composable
internal fun SheetHeaderButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {


   if (primary){
       Button(
           onClick = onClick,
           shapes = ButtonDefaults.shapes()
       ) {
           if (icon != null) {
               Icon(icon, contentDescription = icon.name)
               Spacer(Modifier.width(4.dp))
           }
           Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
       }
   }else {
       FilledTonalButton(onClick = onClick, shapes = ButtonDefaults.shapes()) {
           Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))

       }
   }

}
private fun nextRingSummary(hour24: Int, minute: Int, days: Set<DayOfWeek>): String {
    val now = LocalDateTime.now()
    val next = (0..7).asSequence()
        .map { now.toLocalDate().plusDays(it.toLong()).atTime(hour24, minute) }
        .firstOrNull { it.isAfter(now) && (days.isEmpty() || it.dayOfWeek in days) }
        ?: return "Pick at least one day"

    val total = Duration.between(now, next).toMinutes()
    val d = total / 1440
    val h = (total % 1440) / 60
    val m = total % 60
    val parts = buildList {
        if (d > 0) add("$d d")
        if (h > 0) add("$h h")
        if (m > 0 || isEmpty()) add("$m min")
    }
    return "Rings in ${parts.joinToString(" ")}"
}