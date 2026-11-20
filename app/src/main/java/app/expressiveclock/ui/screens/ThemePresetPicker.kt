package app.expressiveclock.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.expressiveclock.data.ThemeSettings
import app.expressiveclock.ui.theme.toColorSpec
import app.expressiveclock.ui.theme.toContrastLevel
import app.expressiveclock.ui.theme.toPaletteStyle
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.toDynamicScheme
import com.materialkolor.material3.toColorScheme

/**
 * One named look: a seed colour plus the three material-kolor knobs that shape it.
 *
 * A preset is the only way to customise the theme. Selecting one writes all four stored fields in
 * one go, so what the settings show and what the app renders can never drift apart — there is no
 * second "custom" state to reconcile with the scheme actually on screen.
 */
@Immutable
data class ThemePreset(
    val name: String,
    val seedColor: Color,
    val paletteStyle: PaletteStyle,
    val colorSpec: ColorSpec.SpecVersion,
    val contrast: Contrast,
)

private val themePresets = listOf(
    ThemePreset("Calm", Color(0xFF6750A4), PaletteStyle.TonalSpot, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Ocean", Color(0xFF0277BD), PaletteStyle.Vibrant, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Lagoon", Color(0xFF00897B), PaletteStyle.TonalSpot, ColorSpec.SpecVersion.SPEC_2021, Contrast.Default),
    ThemePreset("Reef", Color(0xFF00ACC1), PaletteStyle.Expressive, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Forest", Color(0xFF2E7D32), PaletteStyle.Fidelity, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Meadow", Color(0xFF43A047), PaletteStyle.TonalSpot, ColorSpec.SpecVersion.SPEC_2021, Contrast.Default),
    ThemePreset("Sunshine", Color(0xFFF9A825), PaletteStyle.Expressive, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Sunset", Color(0xFFF4511E), PaletteStyle.FruitSalad, ColorSpec.SpecVersion.SPEC_2021, Contrast.Default),
    ThemePreset("Ember", Color(0xFFF57C00), PaletteStyle.Vibrant, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Cherry", Color(0xFFC62828), PaletteStyle.TonalSpot, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Rose", Color(0xFFD81B60), PaletteStyle.Expressive, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Orchid", Color(0xFF8E24AA), PaletteStyle.Vibrant, ColorSpec.SpecVersion.SPEC_2025, Contrast.Default),
    ThemePreset("Sand", Color(0xFF8D6E63), PaletteStyle.Neutral, ColorSpec.SpecVersion.SPEC_2025, Contrast.Medium),
    ThemePreset("Slate", Color(0xFF546E7A), PaletteStyle.Neutral, ColorSpec.SpecVersion.SPEC_2021, Contrast.Default),
    ThemePreset("Graphite", Color(0xFF424242), PaletteStyle.Monochrome, ColorSpec.SpecVersion.SPEC_2021, Contrast.Medium),
)

/**
 * The settings row's subtitle: the preset the user is on, or a short description when the stored
 * values came from an older build's custom mix and match nothing in [themePresets].
 */
internal fun themePresetLabel(theme: ThemeSettings): String =
    themePresets.firstOrNull { it.matches(theme) }?.name
        ?: "${theme.paletteStyle.toPaletteStyle().name} · " +
            "${theme.colorSpec.toColorSpec().name.removePrefix("SPEC_")} · " +
            "${theme.contrast.toContrastLevel().name} contrast"

private fun ThemePreset.matches(theme: ThemeSettings): Boolean =
    !theme.dynamicColor &&
        theme.seedColor == seedColor &&
        theme.paletteStyle == paletteStyle.name &&
        theme.colorSpec == colorSpec.name &&
        theme.contrast == contrast.name

/** The scheme a given configuration would render, used for every preview. */
private fun previewScheme(
    seedColor: Color,
    paletteStyle: String,
    colorSpec: String,
    contrast: String,
    isDark: Boolean,
): ColorScheme = seedColor.toDynamicScheme(
    isDark,
    paletteStyle.toPaletteStyle(),
    contrast.toContrastLevel().value,
    colorSpec.toColorSpec(),
    com.materialkolor.dynamiccolor.DynamicScheme.Platform.PHONE,
).toColorScheme()

/**
 * The preset grid — the whole of the app's theme customisation.
 *
 * It is only ever composed while the dynamic (wallpaper) theme is off: the caller wraps it in an
 * animated visibility, so nothing here knows about that mode and the joined-block corners of the
 * settings card are decided by whichever item is genuinely last on screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemePresetPicker(
    theme: ThemeSettings,
    isDark: Boolean,
    onThemeChange: (ThemeSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            themePresets.forEach { preset ->
                PresetCard(
                    modifier = Modifier.weight(1f),
                    preset = preset,
                    isDark = isDark,
                    isSelected = preset.matches(theme),
                    onClick = {
                        onThemeChange(
                            theme.copy(
                                dynamicColor = false,
                                seedColor = preset.seedColor,
                                paletteStyle = preset.paletteStyle.name,
                                colorSpec = preset.colorSpec.name,
                                contrast = preset.contrast.name,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: ThemePreset,
    isDark: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = remember(preset, isDark) {
        previewScheme(
            seedColor = preset.seedColor,
            paletteStyle = preset.paletteStyle.name,
            colorSpec = preset.colorSpec.name,
            contrast = preset.contrast.name,
            isDark = isDark,
        )
    }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.6f),
        label = "presetBorder",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "presetBorderWidth",
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .width(104.dp)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            },
        border = BorderStroke(borderWidth, borderColor),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.inversePrimary),
            ) {
                Row(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        Modifier
                            .width(16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(scheme.primary),
                    )
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scheme.secondary),
                    )
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scheme.tertiary),
                    )
                }
                if (isSelected) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(scheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}
