package com.space4414.kiyo.ui.screen

  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.itemsIndexed
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.KeyboardArrowDown
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.res.painterResource
  import androidx.compose.ui.text.font.FontWeight
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
  fun QueueScreen(
      viewModel: PlayerViewModel,
      onBack: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      val uiState by viewModel.uiState.collectAsState()

      Column(
          modifier = modifier
              .fillMaxSize()
              .background(Color.Black)
              .statusBarsPadding(),
      ) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
              IconButton(onClick = onBack) {
                  Icon(Icons.Default.KeyboardArrowDown, "Close", tint = Color.White)
              }
              Spacer(Modifier.width(8.dp))
              Column {
                  Text("Up Next", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White)
                  Text(uiState.queue.size.toString() + " tracks", fontSize = 12.sp, color = Color(0xFF6E6E73))
              }
          }
          HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)

          if (uiState.queue.isEmpty()) {
              Box(Modifier.fillMaxSize(), Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(painterResource(R.drawable.ic_kiyo_graphic_eq), null,
                          modifier = Modifier.size(52.dp), tint = Color(0xFF3A3A3C))
                      Spacer(Modifier.height(12.dp))
                      Text("Queue is empty", fontSize = 14.sp, color = Color(0xFF6E6E73))
                  }
              }
          } else {
              LazyColumn {
                  itemsIndexed(uiState.queue, key = { i, t -> i.toString() + "_" + t.id.toString() }) { index, track ->
                      QueueRow(
                          track = track,
                          isActive = track.id == uiState.currentTrack?.id,
                          onClick = { viewModel.playAll(uiState.queue, index) },
                      )
                      HorizontalDivider(modifier = Modifier.padding(start = 78.dp),
                          color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                  }
              }
          }
      }
  }

  @Composable
  private fun QueueRow(track: TrackEntity, isActive: Boolean, onClick: () -> Unit) {
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
              modifier = Modifier.size(50.dp),
              cornerRadius = 6.dp, iconSize = 22.dp,
          )
          Spacer(Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
              if (isActive) Text("Now playing", fontSize = 10.sp, color = KiyoTeal,
                  fontWeight = FontWeight.Medium)
              Text(track.title, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                  color = if (isActive) KiyoTeal else Color.White,
                  maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(track.rawArtist.toDisplayArtist(), fontSize = 12.sp,
                  color = Color(0xFF8A9098), maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
          if (isActive) {
              Icon(painterResource(R.drawable.ic_kiyo_graphic_eq), null,
                  tint = KiyoTeal, modifier = Modifier.size(18.dp))
          }
      }
  }
  