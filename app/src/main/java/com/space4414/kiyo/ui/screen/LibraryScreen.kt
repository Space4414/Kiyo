package com.space4414.kiyo.ui.screen

  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.itemsIndexed
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.res.painterResource
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.text.style.TextOverflow
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.space4414.kiyo.R
  import com.space4414.kiyo.data.db.entity.TrackEntity
  import com.space4414.kiyo.ui.component.AlbumArtBox
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

      Column(
          modifier = modifier
              .fillMaxSize()
              .background(Color.Black)
              .statusBarsPadding(),
      ) {
          when {
              !hasStoragePermission -> PermissionEmptyState(
                  onRequestPermission = onRequestStoragePermission,
                  modifier = Modifier.fillMaxSize(),
              )
              tracks.isEmpty() -> EmptyLibrary(modifier = Modifier.fillMaxSize())
              else -> {
                  LibrarySectionHeader(
                      iconRes = R.drawable.ic_kiyo_library_music_filled,
                      title = "All Songs",
                      count = tracks.size,
                  )
                  LibraryActionRow(
                      onShuffle = {
                          viewModel.playAll(tracks.shuffled())
                          onOpenPlayer()
                      },
                      onPlayAll = {
                          viewModel.playAll(tracks)
                          onOpenPlayer()
                      },
                  )
                  HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                  TrackList(
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
  private fun LibrarySectionHeader(iconRes: Int, title: String, count: Int) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
          Box(
              modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF2C2F8C)),
              contentAlignment = Alignment.Center,
          ) {
              Icon(
                  painter = painterResource(iconRes),
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(28.dp),
              )
          }
          Column {
              Text(title, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.White)
              Text("$count tracks", fontSize = 12.sp, color = Color(0xFF6E6E73))
          }
      }
  }

  @Composable
  private fun LibraryActionRow(onShuffle: () -> Unit, onPlayAll: () -> Unit) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
          ActionIconPill(iconRes = R.drawable.ic_kiyo_shuffle, onClick = onShuffle)
          ActionIconPill(iconRes = R.drawable.ic_kiyo_graphic_eq, onClick = onPlayAll)
      }
  }

  @Composable
  private fun ActionIconPill(iconRes: Int, onClick: () -> Unit) {
      Box(
          modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color(0xFF2C2C2E))
              .clickable(onClick = onClick),
          contentAlignment = Alignment.Center,
      ) {
          Icon(
              painter = painterResource(iconRes),
              contentDescription = null,
              tint = Color(0xFFAEAEB2),
              modifier = Modifier.size(22.dp),
          )
      }
  }

  @Composable
  private fun TrackList(
      tracks: List<TrackEntity>,
      currentTrackId: Long?,
      modifier: Modifier = Modifier,
      onTrackClick: (Int) -> Unit,
  ) {
      LazyColumn(modifier = modifier) {
          itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
              TrackRow(
                  track = track,
                  isActive = track.id == currentTrackId,
                  onClick = { onTrackClick(index) },
              )
              HorizontalDivider(
                  modifier = Modifier.padding(start = 84.dp),
                  color = Color(0xFF2C2C2E),
                  thickness = 0.5.dp,
              )
          }
      }
  }

  @Composable
  private fun TrackRow(track: TrackEntity, isActive: Boolean, onClick: () -> Unit) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .background(if (isActive) Color(0xFF1A2C2A) else Color.Black)
              .clickable(onClick = onClick)
              .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
          AlbumArtBox(
              albumId = track.albumId,
              fallbackLabel = track.album.ifBlank { track.title },
              modifier = Modifier.size(56.dp),
              cornerRadius = 6.dp,
              iconSize = 24.dp,
          )
          Spacer(Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
              Text(
                  text = track.title,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp,
                  color = if (isActive) KiyoTeal else Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
              Text(
                  text = buildString {
                      append(track.rawArtist.toDisplayArtist())
                      if (track.album.isNotBlank()) append(" — ").append(track.album)
                  },
                  fontSize = 12.sp,
                  color = Color(0xFF8A9098),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
              Text(
                  text = formatDuration(track.durationMs),
                  fontSize = 11.sp,
                  color = Color(0xFF6E6E73),
              )
          }
      }
  }

  @Composable
  private fun PermissionEmptyState(
      onRequestPermission: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
              Icon(painterResource(R.drawable.ic_kiyo_folder_lock), null,
                  modifier = Modifier.size(56.dp), tint = Color(0xFF6E6E73))
              Text("Music Library Access", fontWeight = FontWeight.Bold,
                  fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
              Text("Kiyo needs permission to read your music files.",
                  fontSize = 14.sp, color = Color(0xFF8A9098), textAlign = TextAlign.Center)
              Button(
                  onClick = onRequestPermission,
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = KiyoTeal),
              ) { Text("Grant Access", color = Color.Black) }
          }
      }
  }

  @Composable
  private fun EmptyLibrary(modifier: Modifier = Modifier) {
      Box(modifier = modifier, contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(painterResource(R.drawable.ic_kiyo_library_music_outline), null,
                  modifier = Modifier.size(64.dp), tint = Color(0xFF3A3A3C))
              Text("No music found", fontWeight = FontWeight.SemiBold,
                  fontSize = 16.sp, color = Color.White)
              Text("Add audio files to your device", fontSize = 13.sp, color = Color(0xFF8A9098))
          }
      }
  }
  