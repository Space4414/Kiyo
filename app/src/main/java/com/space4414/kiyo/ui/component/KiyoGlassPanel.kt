package com.space4414.kiyo.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Composition local that carries the pre-blurred ambient [ImageBitmap] produced
 * by [LegacyGlassEngine] inside [AmbientBackdrop] down to every [KiyoGlassPanel]
 * without explicit prop-drilling.
 *
 * Provides null by default — [KiyoGlassPanel] falls back to a charcoal base.
 */
val LocalBlurredAmbient = compositionLocalOf<ImageBitmap?> { null }

/**
 * Three-layer frosted-glass panel — API 23 compatible.
 *
 * **Layer 1 — Base**: the [LegacyGlassEngine] blurred bitmap ([blurredBitmap])
 * stretched to fill the panel surface.  Falls back to deep charcoal when null.
 *
 * **Layer 2 — Frost overlay**: `#1AFFFFFF` (10% translucent white), simulating
 * frosted-glass light scattering.
 *
 * **Layer 3 — Specular edge**: razor-thin 1dp border in `#33FFFFFF` (15% white),
 * mimicking glass-pane edge reflection.
 *
 * Layers 1 and 2 are rendered via [drawWithCache] so that the [IntSize]
 * computation and [Color] allocations are performed once per layout-size change
 * and cached across redraws — keeping the render pipe at 60 FPS on API 23+.
 *
 * @param blurredBitmap  Pre-blurred [ImageBitmap].  Defaults to [LocalBlurredAmbient];
 *                       pass an album-art bitmap for per-card art-glass effects.
 * @param cornerRadius   Clip + border corner radius.
 * @param content        Composable content placed above the three glass layers.
 */
@Composable
fun KiyoGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    blurredBitmap: ImageBitmap? = LocalBlurredAmbient.current,
    content: @Composable BoxScope.() -> Unit,
) {
    // Allocate colour/shape constants once per composition — not per frame.
    val frostColor    = remember { Color(0x1AFFFFFF) }
    val edgeColor     = remember { Color(0x33FFFFFF) }
    val fallbackColor = remember { Color(0xFF1A1E24.toInt()) }
    val shape         = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    Box(
        modifier = modifier
            .clip(shape)

            // ── Layers 1 + 2: cached behind content via drawWithCache ────────
            // drawWithCache re-evaluates its block when `size` changes or when
            // any Compose state captured inside it changes.  `blurredBitmap` is
            // captured here so an updated bitmap triggers a redraw automatically.
            .drawWithCache {
                val dstSize = IntSize(size.width.toInt(), size.height.toInt())
                val bmp     = blurredBitmap          // snapshot for this cache pass

                onDrawBehind {
                    // Layer 1: blurred base (or charcoal fallback)
                    if (bmp != null) {
                        drawImage(
                            image         = bmp,
                            srcOffset     = IntOffset.Zero,
                            srcSize       = IntSize(bmp.width, bmp.height),
                            dstOffset     = IntOffset.Zero,
                            dstSize       = dstSize,
                            filterQuality = FilterQuality.Low,
                        )
                    } else {
                        drawRect(fallbackColor)
                    }

                    // Layer 2: #1AFFFFFF frost overlay
                    drawRect(frostColor)
                }
            }

            // ── Layer 3: #33FFFFFF specular glass edge (1dp) ─────────────────
            .border(width = 1.dp, color = edgeColor, shape = shape),

        content = content,
    )
}
