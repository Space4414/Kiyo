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
    val performanceModeEnabled: Boolean = false,

    // ── Blur ──────────────────────────────────────────────────────────────
    val blurEnabled: Boolean = true,
    val blurRadiusDp: Int = 20,
    val blurQuality: BlurQuality = BlurQuality.MEDIUM,

    // ── Animations ────────────────────────────────────────────────────────
    val animationsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,

    // ── Background ────────────────────────────────────────────────────────
    val ambientGradientsEnabled: Boolean = true,
    val solidBackgroundEnabled: Boolean = false,

    // ── Audio Crossfade ────────────────────────────────────────────────────
    /** Enable 300 ms linear fade-in on track transitions. */
    val crossfadeEnabled: Boolean = true,

    // ── Multi-artist parser ────────────────────────────────────────────────
    /**
     * Comma-separated list of custom delimiter tokens added by the user,
     * beyond the built-in defaults (";", "//", "feat.", "ft.", " & ", etc.).
     * Example: "vs,prod. by,presents"
     */
    val customDelimiters: String = "",

    /**
     * Comma-separated list of exact artist names that must never be split,
     * added by the user beyond the built-in whitelist (AC/DC, etc.).
     */
    val customUnsplitExceptions: String = "",

    // ── Last.fm ────────────────────────────────────────────────────────────
    val lastFmApiKey: String = "",
    val lastFmApiSecret: String = "",
    val lastFmUsername: String = "",
    val lastFmSessionKey: String = "",

    // ── Discord RPC ────────────────────────────────────────────────────────
    /** Enable Discord Rich Presence tracking via webhook. */
    val discordRpcEnabled: Boolean = false,
    /** Discord webhook or bridge endpoint URL. */
    val discordWebhookUrl: String = "",
) {
    val effectiveBlurRadius: Int get() {
        if (!blurEnabled || performanceModeEnabled) return 0
        val scale = when (blurQuality) {
            BlurQuality.HIGH   -> 1.5f
            BlurQuality.MEDIUM -> 1.0f
            BlurQuality.LOW    -> 0.5f
        }
        return (blurRadiusDp * scale).toInt().coerceAtLeast(0)
    }

    val isMotionReduced: Boolean get() = reducedMotion || performanceModeEnabled
    val showGradients: Boolean get() = ambientGradientsEnabled && !performanceModeEnabled && !solidBackgroundEnabled

    /** Parsed list of custom delimiter tokens (non-blank, trimmed). */
    val parsedCustomDelimiters: List<String> get() =
        customDelimiters.split(",").map { it.trim() }.filter { it.isNotBlank() }

    /** Parsed list of custom unsplit exceptions (non-blank, trimmed). */
    val parsedCustomExceptions: Set<String> get() =
        customUnsplitExceptions.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
}

enum class BlurQuality { LOW, MEDIUM, HIGH }
