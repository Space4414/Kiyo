package com.space4414.kiyo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.R
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.ui.component.AlbumArtBox
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.theme.KiyoTeal
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
import com.space4414.kiyo.util.toDisplayArtist

@Composable
fun LibraryScreen(
    viewModel: PlayerViewModel,
    hasStoragePermission: Boolean,
    onRequestStoragePermission: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks by viewModel.allTracks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            LibraryHeader(
                trackCount = tracks.size,
                onShuffle = {
                    if (tracks.isNotEmpty()) {
                        viewModel.playAll(tracks.shuffled())
                        onOpenPlayer()
                    }
                },
            )

            when {
                !hasStoragePermission ->
                    PermissionEmptyState(
                        onRequestPermission = onRequestStoragePermission,
                        modifier = Modifier.fillMaxSize(),
                    )
                tracks.isEmpty() -> EmptyLibrary(modifier = Modifier.fillMaxSize())
                else -> TrackList(
                    tracks = tracks,
                    currentTrackId = uiState.currentTrack?.id,
                    modifier = Modifier.fillMaxSize(),
                    onTrackClick = { index ->
                        viewModel.playAll(tracks, index)
                        onOpenPlayer()
                    },
                )
            }
        }
    }
}

@Composable
private fun LibraryHeader(trackCount: Int, onShuffle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Library", style = MaterialTheme.typography.headlineMedium)
            if (trackCount > 0) {
                Text("$trackCount tracks", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onShuffle) {
            Icon(
                painter = painterResource(R.drawable.ic_kiyo_shuffle),
                contentDescription = "Shuffle all",
                tint = KiyoTeal, modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PermissionEmptyState(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        FrostedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_kiyo_folder_lock),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(0.8f),
                )
                Text("Music Library Access", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    "Kiyo needs permission to read your music files from device storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier)
                Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KiyoTeal)) {
                    Text("Grant Access")
                }
                TextButton(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Settings if you already denied",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_kiyo_music_note), null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
            Text("No music found", style = MaterialTheme.typography.titleMedium)
            Text("Add audio files to your device storage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<TrackEntity>, currentTrackId: Long?,
    modifier: Modifier = Modifier, onTrackClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
            TrackRow(
                track = track,
                isActive = track.id == currentTrackId,
                onClick = { onTrackClick(index) },
            )
        }
    }
}

@Composable
private fun TrackRow(track: TrackEntity, isActive: Boolean, onClick: () -> Unit) {
    FrostedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        fillColor = if (isActive) KiyoTeal.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AlbumArtBox(
                albumId = track.albumId,
                fallbackLabel = track.album.ifBlank { track.title },
                modifier = Modifier.size(48.dp),
                cornerRadius = 8.dp, iconSize = 20.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title, style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) KiyoTeal else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.rawArtist.toDisplayArtist(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
