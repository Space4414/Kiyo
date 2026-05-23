package com.space4414.kiyo.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ContentUris
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.space4414.kiyo.ui.theme.KiyoCharcoalLight
import com.space4414.kiyo.ui.theme.KiyoTeal

/**
 * Album art thumbnail box.
 *
 * Loads embedded album art from Android MediaStore via Coil.
 * Uses the canonical content://media/external/audio/albumart/{albumId} URI.
 *
 * Fallback hierarchy:
 *  1. Album art from MediaStore (via albumId)
 *  2. Glass-style letter placeholder showing the first character of [fallbackLabel]
 *  3. Teal question-mark text if both albumId <= 0 and fallbackLabel is blank
 *
 * The letter placeholder matches the frosted-glass aesthetic — dark background,
 * teal initial letter — without needing any bitmap asset.
 */
@Composable
fun AlbumArtBox(
    albumId: Long,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    iconSize: Dp = 48.dp,
    fallbackLabel: String = "",
) {
    val ctx = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    val artUri: Uri? = when {
        albumId <= 0L -> null
        else -> ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId,
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(KiyoCharcoalLight),
        contentAlignment = Alignment.Center,
    ) {
        if (artUri != null) {
            val request = ImageRequest.Builder(ctx)
                .data(artUri)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()

            SubcomposeAsyncImage(
                model = request,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { AlbumArtFallback(fallbackLabel, iconSize) },
                error = { AlbumArtFallback(fallbackLabel, iconSize) },
                success = { SubcomposeAsyncImageContent() },
            )
        } else {
            AlbumArtFallback(fallbackLabel, iconSize)
        }
    }
}

/**
 * Glass-style letter placeholder.
 * Shows the uppercase first letter of [label] in teal on the charcoal background.
 * Falls back to "?" if the label is blank.
 */
@Composable
internal fun AlbumArtFallback(label: String = "", iconSize: Dp = 48.dp) {
    val letter = label.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"
    val fontSize = (iconSize.value * 0.65f).coerceIn(10f, 64f)
    Text(
        text = letter,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = KiyoTeal.copy(alpha = 0.85f),
    )
}
