package com.space4414.kiyo.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.ui.settings.LocalAppSettings
import com.space4414.kiyo.ui.theme.KiyoCharcoalCard
import com.space4414.kiyo.ui.theme.KiyoOutline

/**
 * Hardware-safe translucent "frosted" card.
 *
 * The frosted-glass look comes from two layers working together:
 *  1. [AmbientBackdrop] applies [Modifier.blur] to its gradient blobs when blur
 *     is enabled — creating a soft, diffuse glowing background.
 *  2. This card sits on top of that blurry backdrop with a semi-transparent fill
 *     (`#1A–#33 FFFFFF`) that lets the coloured blobs show through, completing
 *     the frosted-glass illusion.
 *
 * Blur is intentionally NOT applied to the card's own Box — that would blur
 * the card's content (text, icons, art) rather than the background behind it.
 *
 * When blur is enabled in [LocalAppSettings], the fill alpha is raised slightly
 * to compensate for the brighter ambient glow and preserve legibility.
 */
@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    fillColor: Color = KiyoCharcoalCard,
    outlineColor: Color = KiyoOutline,
    outlineWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val settings = LocalAppSettings.current
    val shape = RoundedCornerShape(cornerRadius)

    val appliedFill = if (settings.blurEnabled && !settings.performanceModeEnabled)
        fillColor.copy(alpha = (fillColor.alpha + 0.08f).coerceAtMost(0.55f))
    else
        fillColor

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind { drawRect(appliedFill) }
            .border(width = outlineWidth, color = outlineColor, shape = shape),
        content = content,
    )
}
