package com.space4414.kiyo.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.space4414.kiyo.R
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.settings.AppSettings
import com.space4414.kiyo.ui.settings.BlurQuality
import com.space4414.kiyo.ui.settings.LocalAppSettings
import com.space4414.kiyo.ui.settings.SettingsViewModel
import com.space4414.kiyo.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val focusManager = LocalFocusManager.current

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
                item { CrossfadeSection(settings = settings, vm = viewModel) }
                item { DelimiterSection(settings = settings, vm = viewModel, focusManager = focusManager) }
                item { LastFmSection(settings = settings, vm = viewModel, focusManager = focusManager) }
                item { DiscordSection(settings = settings, vm = viewModel, focusManager = focusManager) }
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
                painter = painterResource(R.drawable.ic_kiyo_arrow_back),
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

// ─── Sections ────────────────────────────────────────────────────────────────

@Composable
private fun PerformanceSection(settings: AppSettings, vm: SettingsViewModel) {
    DrawableSettingsSection(
        icon = R.drawable.ic_kiyo_speed,
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
    DrawableSettingsSection(
        icon = R.drawable.ic_kiyo_blur_on,
        iconTint = KiyoTeal,
        title = "Gaussian Blur",
        description = "Applies a Gaussian blur to the ambient backdrop. Hardware-accelerated on API 31+.",
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
            SubSettingRow(label = "Blur quality", description = "Higher quality uses more GPU.") {
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
    DrawableSettingsSection(
        icon = R.drawable.ic_kiyo_movie,
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
    DrawableSettingsSection(
        icon = R.drawable.ic_kiyo_palette,
        iconTint = KiyoAmber,
        title = "Ambient Gradients",
        description = "Draw coloured radial gradient blobs behind the UI for depth.",
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
private fun CrossfadeSection(settings: AppSettings, vm: SettingsViewModel) {
    DrawableSettingsSection(
        icon = R.drawable.ic_kiyo_graphic_eq,
        iconTint = KiyoTeal,
        title = "Audio Crossfade",
        description = "Smooth 300 ms fade-in transition between tracks instead of an abrupt cut.",
        checked = settings.crossfadeEnabled,
        onToggle = vm::setCrossfadeEnabled,
    )
}

@Composable
private fun DelimiterSection(
    settings: AppSettings,
    vm: SettingsViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    var delimiterInput by remember { mutableStateOf(settings.customDelimiters) }
    var exceptionInput by remember { mutableStateOf(settings.customUnsplitExceptions) }

    LaunchedEffect(settings.customDelimiters) { delimiterInput = settings.customDelimiters }
    LaunchedEffect(settings.customUnsplitExceptions) { exceptionInput = settings.customUnsplitExceptions }

    FrostedCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(KiyoPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_kiyo_memory),
                        contentDescription = null, tint = KiyoPurple,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text("Artist Parser", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Configure custom delimiter tokens and whitelist entries for the multi-artist parser. " +
                "Separates artist tags containing tokens like \"feat.\", \"//\", \";\" etc.",
                style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted,
            )
            Spacer(Modifier.height(12.dp))

            Text("Custom split tokens (comma-separated):", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Text("e.g. vs,prod. by,presents", style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
            Spacer(Modifier.height(4.dp))
            SettingsTextField(
                value = delimiterInput,
                onValueChange = { delimiterInput = it },
                placeholder = "vs, prod. by, presents",
                onDone = { vm.setCustomDelimiters(delimiterInput.trim()); focusManager.clearFocus() },
            )

            Spacer(Modifier.height(12.dp))
            Text("Always-keep exceptions (comma-separated):", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Artist names that must never be split (e.g. AC/DC).",
                style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
            Spacer(Modifier.height(4.dp))
            SettingsTextField(
                value = exceptionInput,
                onValueChange = { exceptionInput = it },
                placeholder = "AC/DC, Earth Wind & Fire",
                onDone = { vm.setCustomUnsplitExceptions(exceptionInput.trim()); focusManager.clearFocus() },
            )
        }
    }
}

@Composable
private fun LastFmSection(
    settings: AppSettings,
    vm: SettingsViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    var apiKey by remember { mutableStateOf(settings.lastFmApiKey) }
    var apiSecret by remember { mutableStateOf(settings.lastFmApiSecret) }
    var username by remember { mutableStateOf(settings.lastFmUsername) }
    var sessionKey by remember { mutableStateOf(settings.lastFmSessionKey) }

    LaunchedEffect(settings) {
        apiKey = settings.lastFmApiKey
        apiSecret = settings.lastFmApiSecret
        username = settings.lastFmUsername
        sessionKey = settings.lastFmSessionKey
    }

    FrostedCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(KiyoError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_kiyo_lastfm),
                        contentDescription = null, tint = KiyoError,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Last.fm Scrobbling", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Enter your Last.fm API credentials to enable scrobbling and Now Playing updates. " +
                "Get your API key at last.fm/api/account/create",
                style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted,
            )
            Spacer(Modifier.height(12.dp))

            CredentialField("API Key", apiKey, { apiKey = it }, false,
                { vm.setLastFmApiKey(apiKey.trim()); focusManager.clearFocus() })
            Spacer(Modifier.height(8.dp))
            CredentialField("API Secret", apiSecret, { apiSecret = it }, true,
                { vm.setLastFmApiSecret(apiSecret.trim()); focusManager.clearFocus() })
            Spacer(Modifier.height(8.dp))
            CredentialField("Username", username, { username = it }, false,
                { vm.setLastFmUsername(username.trim()); focusManager.clearFocus() })
            Spacer(Modifier.height(8.dp))
            CredentialField("Session Key", sessionKey, { sessionKey = it }, true,
                { vm.setLastFmSessionKey(sessionKey.trim()); focusManager.clearFocus() })

            if (settings.lastFmApiKey.isNotBlank() && settings.lastFmSessionKey.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                InfoRow("Last.fm credentials configured — scrobbling active")
            }
        }
    }
}

@Composable
private fun DiscordSection(
    settings: AppSettings,
    vm: SettingsViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    var webhookUrl by remember { mutableStateOf(settings.discordWebhookUrl) }
    LaunchedEffect(settings.discordWebhookUrl) { webhookUrl = settings.discordWebhookUrl }

    FrostedCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(Color(0xFF5865F2).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_kiyo_discord),
                            contentDescription = null, tint = Color(0xFF5865F2),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Discord Rich Presence", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(
                    checked = settings.discordRpcEnabled,
                    onCheckedChange = vm::setDiscordRpcEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF5865F2)),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Broadcast currently playing track to Discord via a webhook or local bridge. " +
                "Requires a running Discord RPC bridge on your network.",
                style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted,
            )

            AnimatedVisibility(visible = settings.discordRpcEnabled,
                enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Webhook / Bridge URL:", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    SettingsTextField(
                        value = webhookUrl,
                        onValueChange = { webhookUrl = it },
                        placeholder = "https://discord.com/api/webhooks/...",
                        onDone = { vm.setDiscordWebhookUrl(webhookUrl.trim()); focusManager.clearFocus() },
                    )
                }
            }
        }
    }
}

// ─── Reusable setting components ─────────────────────────────────────────────

@Composable
private fun DrawableSettingsSection(
    @DrawableRes icon: Int,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    forceDisabled: Boolean = false,
    subContent: @Composable () -> Unit = {},
) {
    FrostedCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null, tint = iconTint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (forceDisabled) KiyoOnSurfaceMuted else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Switch(
                    checked = checked, onCheckedChange = onToggle, enabled = !forceDisabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White, checkedTrackColor = KiyoTeal),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted)
            AnimatedVisibility(visible = checked, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) { subContent() }
            }
        }
    }
}

@Composable
private fun SubSettingToggle(
    label: String, description: String, checked: Boolean,
    onToggle: (Boolean) -> Unit, forceDisabled: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
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
        Switch(checked = checked, onCheckedChange = onToggle, enabled = !forceDisabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = KiyoTeal))
    }
}

@Composable
private fun SubSettingSlider(
    label: String, value: Float,
    valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = value, onValueChange = onValueChange, valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = KiyoTeal, activeTrackColor = KiyoTeal,
                inactiveTrackColor = KiyoTeal.copy(alpha = 0.25f)),
        )
    }
}

@Composable
private fun SubSettingRow(label: String, description: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
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
        modifier = Modifier.clip(RoundedCornerShape(50.dp))
            .background(if (selected) KiyoTeal else KiyoTeal.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else KiyoTeal,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTextField(
    value: String, onValueChange: (String) -> Unit,
    placeholder: String, onDone: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            focusedIndicatorColor = KiyoTeal,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = KiyoTeal,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CredentialField(
    label: String, value: String, onValueChange: (String) -> Unit,
    isSecret: Boolean, onDone: () -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = KiyoOnSurfaceMuted)
        Spacer(Modifier.height(2.dp))
        TextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedIndicatorColor = KiyoTeal,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = KiyoTeal,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = if (isSecret) KeyboardType.Password else KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            singleLine = true,
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KiyoSuccess))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = KiyoOnSurfaceMuted)
    }
}
