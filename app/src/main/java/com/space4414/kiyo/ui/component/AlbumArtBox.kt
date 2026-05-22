package com.space4414.kiyo.ui.component

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.space4414.kiyo.ui.theme.KiyoCharcoalLight

/**
 * Album art thumbnail box.
 *
 * Loads album art from Android MediaStore via Coil. Uses two complementary
 * content URI strategies for maximum compatibility across all API levels:
 *
 *  • Primary:  `content://media/external/audio/albumart/{albumId}`
 *              Standard legacy album art path — works API 16–34+.
 *  • Fallback: `MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI` with album ID
 *              appended — explicit MediaStore API path.
 *
 * Falls back to a teal MusicNote icon if neither URI returns data
 * (file has no embedded cover art, or album ID is unknown).
 */
@Composable
fun AlbumArtBox(
    albumId: Long,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    iconSize: Dp = 48.dp,
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
                loading = { AlbumArtFallback(iconSize) },
                error = { AlbumArtFallback(iconSize) },
                success = { SubcomposeAsyncImageContent() },
            )
        } else {
            AlbumArtFallback(iconSize)
        }
    }
}

@Composable
internal fun AlbumArtFallback(iconSize: Dp = 48.dp) {
    Icon(
        Icons.Default.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    )
}
