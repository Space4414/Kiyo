package com.space4414.kiyo.ui.screen

  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.LazyRow
  import androidx.compose.foundation.lazy.items
  import androidx.compose.foundation.lazy.itemsIndexed
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Settings
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextOverflow
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.space4414.kiyo.data.db.entity.TrackEntity
  import com.space4414.kiyo.ui.component.AlbumArtBox
  import com.space4414.kiyo.ui.theme.KiyoTeal
  import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
  import com.space4414.kiyo.util.toDisplayArtist

  @Composable
  fun HomeScreen(
      viewModel: PlayerViewModel,
      onOpenPlayer: () -> Unit,
      onOpenSettings: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      val allTracks by viewModel.allTracks.collectAsState()
      val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
      val uiState by viewModel.uiState.collectAsState()

      LazyColumn(
          modifier = modifier
              .fillMaxSize()
              .background(Color.Black)
              .statusBarsPadding(),
      ) {
          item {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                  Text("Kiyo", fontWeight = FontWeight.ExtraBold, fontSize = 34.sp,
                      color = Color.White, letterSpacing = (-1).sp)
                  IconButton(onClick = onOpenSettings) {
                      Icon(Icons.Default.Settings, "Settings",
                          modifier = Modifier.size(24.dp), tint = Color(0xFF8A9098))
                  }
              }
          }

          if (recentlyPlayed.isNotEmpty()) {
              item {
                  Text("Recently Played", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
              }
              item {
                  LazyRow(
                      contentPadding = PaddingValues(horizontal = 14.dp),
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                      items(recentlyPlayed, key = { it.id }) { track ->
                          Column(
                              modifier = Modifier
                                  .width(108.dp)
                                  .clip(RoundedCornerShape(10.dp))
                                  .background(Color(0xFF1C1C1E))
                                  .clickable {
                                      val idx = allTracks.indexOf(track).takeIf { it >= 0 } ?: 0
                                      viewModel.playAll(allTracks, idx)
                                      onOpenPlayer()
                                  }
                                  .padding(8.dp),
                          ) {
                              AlbumArtBox(
                                  albumId = track.albumId,
                                  fallbackLabel = track.album.ifBlank { track.title },
                                  modifier = Modifier.fillMaxWidth().height(90.dp),
                                  cornerRadius = 6.dp, iconSize = 26.dp,
                              )
                              Spacer(Modifier.height(6.dp))
                              Text(track.title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                  color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                              Text(track.rawArtist.toDisplayArtist(), fontSize = 11.sp,
                                  color = Color(0xFF8A9098), maxLines = 1, overflow = TextOverflow.Ellipsis)
                          }
                      }
                  }
                  Spacer(Modifier.height(18.dp))
              }
          }

          item {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                  Text("Library", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                  Spacer(Modifier.weight(1f))
                  if (allTracks.isNotEmpty()) {
                      Text(allTracks.size.toString() + " tracks", fontSize = 12.sp, color = Color(0xFF6E6E73))
                  }
              }
          }

          item { HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp) }

          itemsIndexed(allTracks, key = { _, t -> t.id }) { index, track ->
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .background(if (track.id == uiState.currentTrack?.id) Color(0xFF1A2C2A) else Color.Black)
                      .clickable {
                          viewModel.playAll(allTracks, index)
                          onOpenPlayer()
                      }
                      .padding(horizontal = 14.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                  AlbumArtBox(
                      albumId = track.albumId,
                      fallbackLabel = track.album.ifBlank { track.title },
                      modifier = Modifier.size(50.dp),
                      cornerRadius = 6.dp, iconSize = 22.dp,
                  )
                  Spacer(Modifier.width(14.dp))
                  Column(modifier = Modifier.weight(1f)) {
                      Text(track.title, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                          color = if (track.id == uiState.currentTrack?.id) KiyoTeal else Color.White,
                          maxLines = 1, overflow = TextOverflow.Ellipsis)
                      val subtitle = buildString {
                          append(track.rawArtist.toDisplayArtist())
                          if (track.album.isNotBlank()) append(" — ").append(track.album)
                      }
                      Text(subtitle, fontSize = 12.sp, color = Color(0xFF8A9098),
                          maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                  Text(formatDuration(track.durationMs), fontSize = 11.sp, color = Color(0xFF6E6E73))
              }
              HorizontalDivider(modifier = Modifier.padding(start = 78.dp),
                  color = Color(0xFF2C2C2E), thickness = 0.5.dp)
          }

          item { Spacer(Modifier.height(160.dp)) }
      }
  }
  