package com.space4414.kiyo.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.space4414.kiyo.ui.component.AlbumArtBox
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
import com.space4414.kiyo.util.artistAlbumLine

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOpenQueue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val track = uiState.currentTrack

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
        ) {
            PlayerTopBar(onBack = onBack)
            Spacer(Modifier.weight(0.3f))

            if (track != null) {
                AlbumArtBox(
                    albumId = track.albumId,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    cornerRadius = 24.dp, iconSize = 80.dp,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) { Text("Nothing playing", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            Spacer(Modifier.height(28.dp))

            if (track != null) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 32.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = artistAlbumLine(track.rawArtist, track.album),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(24.dp))
            PlayerSeekBar(
                positionMs = uiState.positionMs,
                durationMs = uiState.durationMs,
                onSeek = viewModel::seekTo,
            )
            Spacer(Modifier.height(20.dp))
            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPrev = viewModel::skipPrev,
                onToggle = viewModel::togglePlayPause,
                onNext = viewModel::skipNext,
            )
            Spacer(Modifier.weight(1f))
            PlayerBottomLinks(onLyrics = {}, onUpNext = onOpenQueue)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlayerTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back",
                modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Text("Now Playing", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerSeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    val progress = if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f
    Column {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { onSeek((it * durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
            interactionSource = remember { MutableInteractionSource() },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(positionMs), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.92f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "play_scale",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous",
                modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(28.dp))
        Box(
            modifier = Modifier.size(72.dp).scale(scale).clip(CircleShape)
                .background(Color.White).clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(40.dp), tint = Color(0xFF12161A),
            )
        }
        Spacer(Modifier.width(28.dp))
        IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next",
                modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PlayerBottomLinks(onLyrics: () -> Unit, onUpNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onLyrics) {
            Text("Lyrics", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onUpNext) {
            Text("Up Next  ↑", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

internal fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
