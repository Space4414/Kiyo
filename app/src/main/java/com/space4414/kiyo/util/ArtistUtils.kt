package com.space4414.kiyo.util

/**
 * Display-friendly artist name formatting — Poweramp style.
 *
 * Replaces common multi-artist separators found in file tags with the clean
 * mid-dot " · " separator used throughout the Kiyo UI.
 *
 * Handles (longest patterns first to avoid partial matches):
 *   " feat. "  " Feat. "  " FEAT. "
 *   " ft. "    " Ft. "
 *   " x "      " X "      (collab shorthand)
 *   " & "
 *   " and "    (case-insensitive)
 *   "; "
 *   "// "
 *   ", "       (last — avoids splitting "Last, First" style)
 *
 * The raw string is preserved in the database; this is purely a display
 * transform applied at render time.
 */
fun String.toDisplayArtist(): String {
    if (isBlank()) return this

    val patterns = listOf(
        Regex(" feat\\. ", RegexOption.IGNORE_CASE),
        Regex(" ft\\. ",   RegexOption.IGNORE_CASE),
        Regex(" x ",       RegexOption.IGNORE_CASE),
        Regex(" & "),
        Regex(" and ",     RegexOption.IGNORE_CASE),
        Regex("; "),
        Regex("// "),
        Regex(", "),
    )

    var result = this.trim()
    for (pattern in patterns) {
        result = pattern.replace(result, " · ")
    }
    return result
}

/**
 * Returns a compact "Artist · Album" line for track rows.
 * Null-safe: falls back gracefully when either field is blank.
 */
fun artistAlbumLine(rawArtist: String, album: String): String {
    val artist = rawArtist.toDisplayArtist()
    return when {
        artist.isBlank() && album.isBlank() -> ""
        artist.isBlank() -> album
        album.isBlank()  -> artist
        else             -> "$artist · $album"
    }
}
