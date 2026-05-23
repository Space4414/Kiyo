package com.space4414.kiyo.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.space4414.kiyo.ui.settings.LocalAppSettings
import com.space4414.kiyo.ui.theme.KiyoAmber
import com.space4414.kiyo.ui.theme.KiyoCharcoal
import com.space4414.kiyo.ui.theme.KiyoPurple
import com.space4414.kiyo.ui.theme.KiyoTeal

/**
 * Hardware-safe ambient gradient backdrop — API 23 compatible.
 *
 * Replaces [androidx.compose.ui.draw.blur] (API 31+) with
 * [LegacyGlassEngine.renderAndBlurAmbient], which runs a StackBlur pass over
 * a 10%-resolution off-screen bitmap.  Cost approaches 0 ms on legacy hardware.
 *
 * Rendering branches:
 *  1. Blur enabled  → [LegacyGlassEngine] produces a blurred ambient bitmap,
 *     displayed via [Image] and surfaced to [KiyoGlassPanel] via [LocalBlurredAmbient].
 *  2. Gradients on, blur off  → Compose Canvas draws the three radial blobs
 *     directly (no blur modifier, works on API 23+).
 *  3. All off  → plain [KiyoCharcoal] rectangle, zero GPU overhead.
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    tealColor:   Color = KiyoTeal.copy(alpha   = 0.22f),
    purpleColor: Color = KiyoPurple.copy(alpha = 0.17f),
    amberColor:  Color = KiyoAmber.copy(alpha  = 0.12f),
) {
    val settings    = LocalAppSettings.current
    val blurActive  = settings.showGradients &&
                      settings.blurEnabled   &&
                      !settings.performanceModeEnabled

    // Render + blur only once per settings/colour change.
    // At 200 × 400 px the operation completes in < 5 ms — safe on main thread.
    val blurredAmbient = remember(blurActive, tealColor, purpleColor, amberColor) {
        if (blurActive) {
            LegacyGlassEngine.renderAndBlurAmbient(
                width      = 200,
                height     = 400,
                tealArgb   = tealColor.toArgb(),
                purpleArgb = purpleColor.toArgb(),
                amberArgb  = amberColor.toArgb(),
            ).asImageBitmap()
        } else null
    }

    // Surface blurred bitmap to every KiyoGlassPanel in this subtree.
    CompositionLocalProvider(LocalBlurredAmbient provides blurredAmbient) {
        Box(modifier = modifier) {

            // ── 1. Charcoal base — always drawn ─────────────────────────────
            Box(Modifier.fillMaxSize().background(KiyoCharcoal))

            when {
                // ── 2a. Blur enabled: pre-blurred ambient bitmap (API 23+) ──
                blurredAmbient != null -> {
                    Image(
                        bitmap             = blurredAmbient,
                        contentDescription = null,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.FillBounds,
                    )
                }

                // ── 2b. Gradients on, blur off: plain radial blobs via Canvas
                settings.showGradients -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width; val h = size.height

                        drawCircle(
                            brush  = Brush.radialGradient(
                                colors = listOf(tealColor, Color.Transparent),
                                center = Offset(w * 0.12f, h * 0.08f),
                                radius = w * 0.78f,
                            ),
                            radius = w * 0.78f,
                            center = Offset(w * 0.12f, h * 0.08f),
                        )
                        drawCircle(
                            brush  = Brush.radialGradient(
                                colors = listOf(purpleColor, Color.Transparent),
                                center = Offset(w * 0.88f, h * 0.06f),
                                radius = w * 0.64f,
                            ),
                            radius = w * 0.64f,
                            center = Offset(w * 0.88f, h * 0.06f),
                        )
                        drawCircle(
                            brush  = Brush.radialGradient(
                                colors = listOf(amberColor, Color.Transparent),
                                center = Offset(w * 0.50f, h * 0.94f),
                                radius = w * 0.52f,
                            ),
                            radius = w * 0.52f,
                            center = Offset(w * 0.50f, h * 0.94f),
                        )
                    }
                }
                // ── 2c. Both off → charcoal only (already drawn above) ──────
            }
        }
    }
}
