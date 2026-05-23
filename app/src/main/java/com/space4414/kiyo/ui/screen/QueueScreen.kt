package com.space4414.kiyo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.space4414.kiyo.R
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.ui.component.AlbumArtBox
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.theme.KiyoCharcoalCard
import com.space4414.kiyo.ui.theme.KiyoTeal
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
import com.space4414.kiyo.util.toDisplayArtist

@Composable
fun QueueScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            QueueTopBar(count = uiState.queue.size, onClose = onBack)

            if (uiState.queue.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_kiyo_graphic_eq),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Queue is empty", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(uiState.queue, key = { i, t -> "${i}_${t.id}" }) { index, track ->
                        QueueRow(
                            track = track,
                            isActive = track.id == uiState.currentTrack?.id,
                            onClick = { viewModel.playAll(uiState.queue, index) },
                        )
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KiyoCharcoalCard,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) { Text("+ Add New Queue", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueTopBar(count: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.KeyboardArrowDown, "Close",
                tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Active Queues", style = MaterialTheme.typography.titleLarge)
            Text("$count tracks", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun QueueRow(track: TrackEntity, isActive: Boolean, onClick: () -> Unit) {
    FrostedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        fillColor = if (isActive) KiyoTeal.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        cornerRadius = 16.dp,
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AlbumArtBox(
                albumId = track.albumId,
                fallbackLabel = track.album.ifBlank { track.title },
                modifier = Modifier.size(48.dp),
                cornerRadius = 8.dp, iconSize = 20.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (isActive) Text("Currently playing", style = MaterialTheme.typography.labelMedium,
                    color = KiyoTeal, fontSize = 10.sp)
                Text(
                    track.title, style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) KiyoTeal else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(track.rawArtist.toDisplayArtist(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isActive) {
                Icon(
                    painter = painterResource(R.drawable.ic_kiyo_graphic_eq),
                    contentDescription = null,
                    tint = KiyoTeal, modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
