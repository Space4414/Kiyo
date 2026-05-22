package com.space4414.kiyo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel

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

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                LibraryTopBar(
                    trackCount = tracks.size,
                    onPlayAll = {
                        if (tracks.isNotEmpty()) {
                            viewModel.playAll(tracks)
                            onOpenPlayer()
                        }
                    }
                )
            },
            bottomBar = {
                if (uiState.currentTrack != null) {
                    MiniPlayer(
                        track = uiState.currentTrack!!,
                        isPlaying = uiState.isPlaying,
                        onToggle = viewModel::togglePlayPause,
                        onExpand = onOpenPlayer,
                    )
                }
            }
        ) { padding ->
            when {
                // Permission denied — show a prominent banner with a "Grant" button
                !hasStoragePermission -> {
                    PermissionEmptyState(
                        onRequestPermission = onRequestStoragePermission,
                        modifier = Modifier.padding(padding),
                    )
                }
                // Permission granted but no tracks yet
                tracks.isEmpty() -> {
                    EmptyLibrary(modifier = Modifier.padding(padding))
                }
                // Normal track list
                else -> {
                    TrackList(
                        tracks = tracks,
                        currentTrackId = uiState.currentTrack?.id,
                        modifier = Modifier.padding(padding),
                        onTrackClick = { index ->
                            viewModel.playAll(tracks, index)
                            onOpenPlayer()
                        }
                    )
                }
            }
        }
    }
}

// ─── Permission empty state ────────────────────────────────────────────────────

@Composable
private fun PermissionEmptyState(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        FrostedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
                Text(
                    text = "Music Library Access",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Kiyo needs permission to read your music files from device storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant Access")
                }
                TextButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Open Settings if you already denied",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─── Empty library (permission granted, no tracks) ────────────────────────────

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("No music found", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add audio files to your device storage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Track list ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(trackCount: Int, onPlayAll: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("Kiyo", style = MaterialTheme.typography.titleLarge)
                Text(
                    "$trackCount tracks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            IconButton(onClick = onPlayAll) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play all")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
        ),
    )
}

@Composable
private fun TrackList(
    tracks: List<TrackEntity>,
    currentTrackId: Long?,
    modifier: Modifier = Modifier,
    onTrackClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        fillColor = if (isActive)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.rawArtist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Mini player ──────────────────────────────────────────────────────────────

@Composable
private fun MiniPlayer(
    track: TrackEntity,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onExpand: () -> Unit,
) {
    FrostedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onExpand),
        cornerRadius = 20.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track.rawArtist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (isPlaying) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
