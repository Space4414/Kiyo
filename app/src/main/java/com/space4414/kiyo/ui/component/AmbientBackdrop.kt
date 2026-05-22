package com.space4414.kiyo.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Optionally accepts dynamic dominant colors extracted from the album art
 * (via Palette API) so the backdrop subtly shifts to match the current track.
 *
 * Implementation deliberately avoids [RenderEffect] / backdrop blur which requires
 * API 31+ and causes lag on Android 5/6 devices.
 * All rendering is pure Canvas paint ops — zero OS-level blur dependency.
 *
 * @param tealColor    Teal accent light — defaults to Kiyo palette teal.
 * @param purpleColor  Purple accent light.
 * @param amberColor   Amber accent light.
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    tealColor: Color = KiyoTeal.copy(alpha = 0.20f),
    purpleColor: Color = KiyoPurple.copy(alpha = 0.16f),
    amberColor: Color = KiyoAmber.copy(alpha = 0.11f),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(color = KiyoCharcoal)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tealColor, Color.Transparent),
                center = Offset(x = w * 0.12f, y = h * 0.08f),
                radius = w * 0.70f,
            ),
            radius = w * 0.70f,
            center = Offset(x = w * 0.12f, y = h * 0.08f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(purpleColor, Color.Transparent),
                center = Offset(x = w * 0.88f, y = h * 0.06f),
                radius = w * 0.58f,
            ),
            radius = w * 0.58f,
            center = Offset(x = w * 0.88f, y = h * 0.06f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(amberColor, Color.Transparent),
                center = Offset(x = w * 0.50f, y = h * 0.94f),
                radius = w * 0.48f,
            ),
            radius = w * 0.48f,
            center = Offset(x = w * 0.50f, y = h * 0.94f),
        )
    }
}
