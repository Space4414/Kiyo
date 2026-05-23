package com.space4414.kiyo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.ui.settings.LocalAppSettings

/**
 * Translucent "frosted" card backed by [KiyoGlassPanel].
 *
 * Glass layer stack (API 23+):
 *  1. Pre-blurred ambient [ImageBitmap] from [LegacyGlassEngine] / [LocalBlurredAmbient].
 *  2. #1AFFFFFF frost overlay.
 *  3. 1dp #33FFFFFF specular edge.
 *  4. [fillColor] tint (when non-Transparent) — drawn above the frost, below content.
 *
 * When blur is disabled or no [AmbientBackdrop] ancestor exists,
 * [KiyoGlassPanel] automatically falls back to its dark-charcoal base.
 * [fillColor] still applies in that case (e.g. active-track teal highlight).
 *
 * @param fillColor    Additional colour tint above the frost layer.
 *                     Defaults to [Color.Transparent] (no tint).
 * @param cornerRadius Clip / border corner radius.
 */
@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    fillColor: Color = Color.Transparent,
    outlineColor: Color = Color.Unspecified,   // kept for API compat; glass edge is #33FFFFFF
    outlineWidth: Dp = 1.dp,                   // kept for API compat
    content: @Composable BoxScope.() -> Unit,
) {
    val settings = LocalAppSettings.current

    val effectiveBitmap: ImageBitmap? =
        if (settings.blurEnabled && !settings.performanceModeEnabled)
            LocalBlurredAmbient.current
        else
            null

    KiyoGlassPanel(
        modifier      = modifier,
        cornerRadius  = cornerRadius,
        blurredBitmap = effectiveBitmap,
    ) {
        // Layer 4: optional tint overlay sits above frost but below caller content
        if (fillColor != Color.Transparent) {
            Box(modifier = Modifier.matchParentSize().background(fillColor))
        }
        content()
    }
}
