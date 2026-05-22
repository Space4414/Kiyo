package com.space4414.kiyo.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.space4414.kiyo.ui.theme.KiyoAmber
import com.space4414.kiyo.ui.theme.KiyoCharcoal
import com.space4414.kiyo.ui.theme.KiyoPurple
import com.space4414.kiyo.ui.theme.KiyoTeal

/**
 * Hardware-safe multi-point ambient gradient backdrop.
 *
 * Renders the deep-charcoal base with layered radial "light leaks" at three
 * anchored positions — teal (top-left), purple (top-right), amber (bottom-center).
 *
 * Implementation deliberately avoids [RenderEffect] / backdrop blur which requires
 * API 31+ hardware acceleration and causes LAG on Android 5/6 devices.
 * All rendering is pure Canvas paint ops — zero OS-level blur dependency.
 *
 * @param tealColor  Teal accent light. Default matches Kiyo palette.
 * @param purpleColor Purple accent light.
 * @param amberColor  Amber accent light.
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    tealColor: Color = KiyoTeal.copy(alpha = 0.18f),
    purpleColor: Color = KiyoPurple.copy(alpha = 0.15f),
    amberColor: Color = KiyoAmber.copy(alpha = 0.10f),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Base fill
        drawRect(color = KiyoCharcoal)

        // Teal — top-left anchor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tealColor, Color.Transparent),
                center = Offset(x = w * 0.15f, y = h * 0.10f),
                radius = w * 0.65f,
            ),
            radius = w * 0.65f,
            center = Offset(x = w * 0.15f, y = h * 0.10f),
            alpha = 1f,
        )

        // Purple — top-right anchor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(purpleColor, Color.Transparent),
                center = Offset(x = w * 0.85f, y = h * 0.08f),
                radius = w * 0.55f,
            ),
            radius = w * 0.55f,
            center = Offset(x = w * 0.85f, y = h * 0.08f),
            alpha = 1f,
        )

        // Amber — bottom-center anchor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(amberColor, Color.Transparent),
                center = Offset(x = w * 0.50f, y = h * 0.92f),
                radius = w * 0.45f,
            ),
            radius = w * 0.45f,
            center = Offset(x = w * 0.50f, y = h * 0.92f),
            alpha = 1f,
        )
    }
}
