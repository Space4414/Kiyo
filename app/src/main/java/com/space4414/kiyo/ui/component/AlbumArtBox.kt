package com.space4414.kiyo.ui.component

import android.net.Uri
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.space4414.kiyo.ui.theme.KiyoCharcoalLight

@Composable
fun AlbumArtBox(
    albumId: Long,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    iconSize: Dp = 48.dp,
) {
    val artUri: Uri? = if (albumId > 0L)
        Uri.parse("content://media/external/audio/albumart/$albumId")
    else null

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(KiyoCharcoalLight),
        contentAlignment = Alignment.Center,
    ) {
        if (artUri != null) {
            SubcomposeAsyncImage(
                model = artUri,
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
private fun AlbumArtFallback(iconSize: Dp = 48.dp) {
    Icon(
        Icons.Default.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    )
}
