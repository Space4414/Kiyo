package com.space4414.kiyo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.ui.component.AlbumArtBox
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FilterPill
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.theme.KiyoTeal
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
import com.space4414.kiyo.util.toDisplayArtist

private enum class LibraryFilter { ARTISTS, ALBUMS, SONGS }

@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    var activeFilter by remember { mutableStateOf(LibraryFilter.SONGS) }
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            HomeHeader(onSettings = onOpenSettings)
            Spacer(Modifier.height(8.dp))

            if (recentlyPlayed.isNotEmpty()) {
                SectionHeader("Recently Played")
                RecentlyPlayedRow(
                    tracks = recentlyPlayed,
                    onTrackClick = { track ->
                        val index = allTracks.indexOf(track).takeIf { it >= 0 } ?: 0
                        viewModel.playAll(allTracks, index)
                        onOpenPlayer()
                    },
                )
                Spacer(Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LibraryFilter.values().forEach { filter ->
                    FilterPill(
                        label = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionHeader(
                when (activeFilter) {
                    LibraryFilter.ARTISTS -> "Artists"
                    LibraryFilter.ALBUMS  -> "Albums"
                    LibraryFilter.SONGS   -> "Library"
                },
            )

            when (activeFilter) {
                LibraryFilter.SONGS -> LibraryGrid(
                    tracks = allTracks,
                    onTrackClick = { index ->
                        viewModel.playAll(allTracks, index)
                        onOpenPlayer()
                    },
                )
                LibraryFilter.ALBUMS  -> AlbumGrid(tracks = allTracks)
                LibraryFilter.ARTISTS -> ArtistGrid(tracks = allTracks)
            }

            Spacer(Modifier.height(200.dp))
        }
    }
}

@Composable
private fun HomeHeader(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kiyo",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-1).sp,
        )
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Icon(
            Icons.Default.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun RecentlyPlayedRow(tracks: List<TrackEntity>, onTrackClick: (TrackEntity) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tracks, key = { it.id }) { track ->
            FrostedCard(
                modifier = Modifier.width(140.dp).clickable { onTrackClick(track) },
                cornerRadius = 16.dp,
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    AlbumArtBox(
                        albumId = track.albumId,
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        cornerRadius = 10.dp, iconSize = 32.dp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        track.title, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.rawArtist.toDisplayArtist(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryGrid(tracks: List<TrackEntity>, onTrackClick: (Int) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tracks.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { track ->
                    FrostedCard(
                        modifier = Modifier.weight(1f).clickable { onTrackClick(tracks.indexOf(track)) },
                        cornerRadius = 14.dp,
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            AlbumArtBox(
                                albumId = track.albumId,
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                cornerRadius = 8.dp, iconSize = 28.dp,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                track.title, style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track.rawArtist.toDisplayArtist(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AlbumGrid(tracks: List<TrackEntity>) {
    val albums = tracks.distinctBy { it.albumId }.sortedBy { it.album }
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        albums.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { track ->
                    FrostedCard(modifier = Modifier.weight(1f), cornerRadius = 14.dp) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            AlbumArtBox(
                                albumId = track.albumId,
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                cornerRadius = 8.dp, iconSize = 28.dp,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                track.album, style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track.rawArtist.toDisplayArtist(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ArtistGrid(tracks: List<TrackEntity>) {
    val artists = tracks.flatMap { it.rawArtist.split(";", "//", " feat. ", " ft. ", " & ", " x ") }
        .map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        artists.forEach { artist ->
            FrostedCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(50)).let {
                            it
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            artist.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium, color = KiyoTeal,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        artist, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
