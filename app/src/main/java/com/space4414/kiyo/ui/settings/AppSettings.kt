package com.space4414.kiyo.ui.settings

/**
 * All user-configurable app settings.
 *
 * Defaults represent the "high-quality, full-feature" experience.
 * Low-end device mode flips several of these automatically, and the user
 * can fine-tune each sub-setting individually.
 */
data class AppSettings(
    // ── Performance / Low-end ──────────────────────────────────────────────
    /** Master toggle: enables all sub-settings for low-end optimization. */
    val performanceModeEnabled: Boolean = false,

    // ── Blur ──────────────────────────────────────────────────────────────
    /** Gaussian blur on the ambient backdrop. Off in performance mode. */
    val blurEnabled: Boolean = true,
    /** Blur radius in dp applied to the ambient gradient layer. */
    val blurRadiusDp: Int = 20,
    /** Quality tier — controls final radius scaling. */
    val blurQuality: BlurQuality = BlurQuality.MEDIUM,

    // ── Animations ────────────────────────────────────────────────────────
    /** Play enter/exit transition animations. Off in performance mode. */
    val animationsEnabled: Boolean = true,
    /** Use shorter durations (half) for all animations. */
    val reducedMotion: Boolean = false,

    // ── Background ────────────────────────────────────────────────────────
    /** Draw coloured radial gradient blobs behind cards. Off in perf mode. */
    val ambientGradientsEnabled: Boolean = true,
    /** Use a plain solid colour instead of the layered gradient backdrop. */
    val solidBackgroundEnabled: Boolean = false,
) {
    /** Effective blur radius after quality scaling, or 0 when disabled. */
    val effectiveBlurRadius: Int get() {
        if (!blurEnabled || performanceModeEnabled) return 0
        val scale = when (blurQuality) {
            BlurQuality.HIGH   -> 1.5f
            BlurQuality.MEDIUM -> 1.0f
            BlurQuality.LOW    -> 0.5f
        }
        return (blurRadiusDp * scale).toInt().coerceAtLeast(0)
    }

    /** True when any animation shortening is requested. */
    val isMotionReduced: Boolean get() = reducedMotion || performanceModeEnabled

    /** True when gradient backdrop should be drawn. */
    val showGradients: Boolean get() = ambientGradientsEnabled && !performanceModeEnabled && !solidBackgroundEnabled
}

enum class BlurQuality { LOW, MEDIUM, HIGH }
