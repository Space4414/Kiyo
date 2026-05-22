package com.space4414.kiyo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.settings.AppSettings
import com.space4414.kiyo.ui.settings.BlurQuality
import com.space4414.kiyo.ui.settings.LocalAppSettings
import com.space4414.kiyo.ui.settings.SettingsViewModel
import com.space4414.kiyo.ui.theme.KiyoAmber
import com.space4414.kiyo.ui.theme.KiyoError
import com.space4414.kiyo.ui.theme.KiyoOnSurfaceMuted
import com.space4414.kiyo.ui.theme.KiyoPurple
import com.space4414.kiyo.ui.theme.KiyoSuccess
import com.space4414.kiyo.ui.theme.KiyoTeal

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            SettingsTopBar(onBack = onBack)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { PerformanceSection(settings = settings, vm = viewModel) }
                item { BlurSection(settings = settings, vm = viewModel) }
                item { AnimationSection(settings = settings, vm = viewModel) }
                item { BackgroundSection(settings = settings, vm = viewModel) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PerformanceSection(settings: AppSettings, vm: SettingsViewModel) {
    SettingsSection(
        icon = Icons.Default.Speed,
        iconTint = KiyoError,
        title = "Low-end Device Mode",
        description = "Disables blur, gradients, and heavy animations for older or low-RAM devices.",
        checked = settings.performanceModeEnabled,
        onToggle = vm::setPerformanceMode,
    ) {
        if (settings.performanceModeEnabled) {
            InfoRow("Blur disabled automatically")
            InfoRow("Ambient gradients disabled automatically")
            InfoRow("Animations shortened automatically")
        }
    }
}

@Composable
private fun BlurSection(settings: AppSettings, vm: SettingsViewModel) {
    val blurForced = settings.performanceModeEnabled
    SettingsSection(
        icon = Icons.Default.BlurOn,
        iconTint = KiyoTeal,
        title = "Gaussian Blur",
        description = "Applies a Gaussian blur to the ambient backdrop for a frosted-glass look. Hardware-accelerated on API 31+.",
        checked = settings.blurEnabled && !blurForced,
        onToggle = { if (!blurForced) vm.setBlurEnabled(it) },
        forceDisabled = blurForced,
    ) {
        if (settings.blurEnabled && !blurForced) {
            SubSettingSlider(
                label = "Blur radius: ${settings.blurRadiusDp} dp",
                value = settings.blurRadiusDp.toFloat(),
                valueRange = 4f..40f,
                onValueChange = { vm.setBlurRadius(it.toInt()) },
            )
            SubSettingRow(
                label = "Blur quality",
                description = "Higher quality uses more GPU.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BlurQuality.values().forEach { q ->
                        QualityChip(
                            label = q.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = settings.blurQuality == q,
                            onClick = { vm.setBlurQuality(q) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimationSection(settings: AppSettings, vm: SettingsViewModel) {
    SettingsSection(
        icon = Icons.Default.Movie,
        iconTint = KiyoPurple,
        title = "Animations",
        description = "Enable enter/exit transitions and spring animations throughout the app.",
        checked = settings.animationsEnabled,
        onToggle = vm::setAnimationsEnabled,
    ) {
        if (settings.animationsEnabled) {
            SubSettingToggle(
                label = "Reduced motion",
                description = "Use shorter, simpler animations.",
                checked = settings.reducedMotion || settings.performanceModeEnabled,
                onToggle = { if (!settings.performanceModeEnabled) vm.setReducedMotion(it) },
                forceDisabled = settings.performanceModeEnabled,
            )
        }
    }
}

@Composable
private fun BackgroundSection(settings: AppSettings, vm: SettingsViewModel) {
    SettingsSection(
        icon = Icons.Default.Palette,
        iconTint = KiyoAmber,
        title = "Ambient Gradients",
        description = "Draw coloured radial gradient blobs behind the UI for depth. Disable for a solid dark background.",
        checked = settings.ambientGradientsEnabled && !settings.performanceModeEnabled,
        onToggle = { if (!settings.performanceModeEnabled) vm.setAmbientGradients(it) },
        forceDisabled = settings.performanceModeEnabled,
    ) {
        if (settings.ambientGradientsEnabled && !settings.performanceModeEnabled) {
            SubSettingToggle(
                label = "Solid background fallback",
                description = "Replace gradients with a plain dark background.",
                checked = settings.solidBackgroundEnabled,
                onToggle = vm::setSolidBackground,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    forceDisabled: Boolean = false,
    subContent: @Composable () -> Unit = {},
) {
    FrostedCard(
        cornerRadius = 18.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (forceDisabled) KiyoOnSurfaceMuted else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onToggle,
                    enabled = !forceDisabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = KiyoTeal,
                    ),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = KiyoOnSurfaceMuted,
            )
            AnimatedVisibility(
                visible = checked,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    subContent()
                }
            }
        }
    }
}

@Composable
private fun SubSettingToggle(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    forceDisabled: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            enabled = !forceDisabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KiyoTeal,
            ),
        )
    }
}

@Composable
private fun SubSettingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = KiyoTeal,
                activeTrackColor = KiyoTeal,
                inactiveTrackColor = KiyoTeal.copy(alpha = 0.25f),
            ),
        )
    }
}

@Composable
private fun SubSettingRow(
    label: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(description, style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun QualityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) KiyoTeal else KiyoTeal.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else KiyoTeal,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(KiyoSuccess),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
    }
}
