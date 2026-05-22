package com.space4414.kiyo.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.ui.settings.LocalAppSettings
import com.space4414.kiyo.ui.theme.KiyoAmber
import com.space4414.kiyo.ui.theme.KiyoCharcoal
import com.space4414.kiyo.ui.theme.KiyoPurple
import com.space4414.kiyo.ui.theme.KiyoTeal

/**
 * Hardware-safe ambient gradient backdrop.
 *
 * Layers three soft radial "light leaks" (teal, purple, amber) over the deep
 * charcoal base. When Gaussian blur is enabled in [LocalAppSettings], the blob
 * layer is passed through [Modifier.blur] for a smooth, diffuse look that,
 * combined with [FrostedCard]'s semi-transparent fill, produces a convincing
 * frosted-glass atmosphere.
 *
 * Performance mode (or blur disabled): draws the plain charcoal base +
 * gradient blobs without any blur — zero GPU overhead, identical to the
 * original pre-blur behaviour.
 *
 * Solid background mode: skips all gradient blobs; renders a plain charcoal
 * rectangle only.
 *
 * @param tealColor    Teal accent blob colour.
 * @param purpleColor  Purple accent blob colour.
 * @param amberColor   Amber accent blob colour.
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    tealColor: Color = KiyoTeal.copy(alpha = 0.22f),
    purpleColor: Color = KiyoPurple.copy(alpha = 0.17f),
    amberColor: Color = KiyoAmber.copy(alpha = 0.12f),
) {
    val settings = LocalAppSettings.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KiyoCharcoal)
        )

        if (settings.showGradients) {
            val blurRadius = settings.effectiveBlurRadius.coerceAtMost(48).dp

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurRadius > 0.dp) Modifier.blur(blurRadius)
                        else Modifier
                    ),
            ) {
                val w = size.width
                val h = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tealColor, Color.Transparent),
                        center = Offset(x = w * 0.12f, y = h * 0.08f),
                        radius = w * 0.78f,
                    ),
                    radius = w * 0.78f,
                    center = Offset(x = w * 0.12f, y = h * 0.08f),
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(purpleColor, Color.Transparent),
                        center = Offset(x = w * 0.88f, y = h * 0.06f),
                        radius = w * 0.64f,
                    ),
                    radius = w * 0.64f,
                    center = Offset(x = w * 0.88f, y = h * 0.06f),
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(amberColor, Color.Transparent),
                        center = Offset(x = w * 0.50f, y = h * 0.94f),
                        radius = w * 0.52f,
                    ),
                    radius = w * 0.52f,
                    center = Offset(x = w * 0.50f, y = h * 0.94f),
                )
            }
        }
    }
}
