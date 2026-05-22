package com.space4414.kiyo.data.parser

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Poweramp-style multi-artist parser.
 *
 * Splits a raw artist tag into individual artist names using configurable split tokens,
 * while honouring an exception whitelist of band names that contain delimiter-like
 * characters but must NEVER be split (e.g. "AC/DC", "Earth, Wind & Fire").
 *
 * Split algorithm:
 *  1. Trim the input. If blank → empty list.
 *  2. If the entire string is an exact (case-insensitive) match in [unsplitExceptions] → return as-is.
 *  3. Iteratively split on each token in [splitTokens] (longest-first to prevent partial matches),
 *     but skip splitting any individual segment that is itself a whitelisted exception.
 *  4. Strip blank segments, trim whitespace, deduplicate.
 */
@Singleton
class ArtistParser @Inject constructor() {

    /** User-configurable split delimiters. Always sorted longest-first internally. */
    var splitTokens: List<String> = DEFAULT_TOKENS
        set(value) {
            field = value.sortedByDescending { it.length }
        }

    /** Exact-match bypass list. Any segment equal to one of these is never split further. */
    var unsplitExceptions: Set<String> = DEFAULT_EXCEPTIONS

    /**
     * Parse [rawArtist] into a deduplicated list of individual artist names.
     */
    fun parse(rawArtist: String): List<String> {
        val trimmed = rawArtist.trim()
        if (trimmed.isBlank()) return emptyList()

        // Rule 1: full-string exception bypass
        if (unsplitExceptions.any { it.equals(trimmed, ignoreCase = true) }) {
            return listOf(trimmed)
        }

        // Rule 2: iterative token splitting with per-segment exception guard
        var segments = listOf(trimmed)
        for (token in splitTokens) {
            segments = segments.flatMap { seg ->
                val segTrimmed = seg.trim()
                if (unsplitExceptions.any { it.equals(segTrimmed, ignoreCase = true) }) {
                    listOf(seg) // protected — do not split
                } else {
                    seg.split(token, ignoreCase = true)
                }
            }
        }

        return segments
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    companion object {
        /**
         * Default split tokens. Sorted longest-first to prevent "feat." matching inside "feat. ".
         * Extend this list via [splitTokens] setter in app settings.
         */
        val DEFAULT_TOKENS: List<String> = listOf(
            " // ", "//",
            " \\\\ ", "\\\\",
            " feat. ", " feat ", "feat.",
            " ft. ", " ft ", "ft.",
            " & ",
            " vs. ", " vs ",
            " x ", " X ",
            " × ",
            " with ",
            ";",
        ).sortedByDescending { it.length }

        /**
         * Default exception whitelist. Band names that contain split-token characters
         * but must be treated as single indivisible artist entities.
         *
         * Future agents: add new entries here — do NOT change the split logic.
         */
        val DEFAULT_EXCEPTIONS: Set<String> = setOf(
            "AC/DC",
            "Crosby, Stills & Nash",
            "Crosby, Stills, Nash & Young",
            "Earth, Wind & Fire",
            "Love, Warmth & Affection",
            "Guns N' Roses",
            "Tyler, the Creator",
            "A\$AP Rocky",
            "Of Monsters and Men",
            "The War on Drugs",
            "Run the Jewels",
            "Rage Against the Machine",
            "Simon & Garfunkel",
            "Hall & Oates",
            "Daryl Hall & John Oates",
            "Salt-N-Pepa",
            "Hootie & the Blowfish",
        )
    }
}
