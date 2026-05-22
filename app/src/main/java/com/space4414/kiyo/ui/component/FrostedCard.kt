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
import com.space4414.kiyo.ui.theme.KiyoCharcoalCard
import com.space4414.kiyo.ui.theme.KiyoOutline

/**
 * Hardware-safe translucent "frosted" card.
 *
 * Simulates frosted glass without OS-level blur (which requires API 31+ RenderEffect
 * and causes jank on Android 5/6 devices). Instead:
 *  - [KiyoCharcoalCard] = #1AFFFFFF (10% white alpha) fill painted directly in Canvas.
 *  - [KiyoOutline]      = #26FFFFFF (15% white alpha) 1dp border.
 *
 * The result is a crisp, lightweight translucent panel with zero blur overhead
 * that looks polished at all API levels.
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
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind { drawRect(fillColor) }
            .border(width = outlineWidth, color = outlineColor, shape = shape),
        content = content,
    )
}
