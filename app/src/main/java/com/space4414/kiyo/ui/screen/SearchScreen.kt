package com.space4414.kiyo.ui.screen

  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.itemsIndexed
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.foundation.text.KeyboardActions
  import androidx.compose.foundation.text.KeyboardOptions
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Close
  import androidx.compose.material.icons.filled.Search
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.platform.LocalFocusManager
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.input.ImeAction
  import androidx.compose.ui.text.style.TextOverflow
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.space4414.kiyo.ui.component.AlbumArtBox
  import com.space4414.kiyo.ui.theme.KiyoTeal
  import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
  import com.space4414.kiyo.util.artistAlbumLine

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun SearchScreen(
      viewModel: PlayerViewModel,
      onOpenPlayer: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      val allTracks by viewModel.allTracks.collectAsState()
      var query by remember { mutableStateOf("") }
      val focusManager = LocalFocusManager.current

      val results by remember(query, allTracks) {
          derivedStateOf {
              if (query.isBlank()) emptyList()
              else allTracks.filter { t ->
                  t.title.contains(query, ignoreCase = true) ||
                          t.rawArtist.contains(query, ignoreCase = true) ||
                          t.album.contains(query, ignoreCase = true)
              }
          }
      }

      Column(
          modifier = modifier
              .fillMaxSize()
              .background(Color.Black)
              .statusBarsPadding(),
      ) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp)
                  .clip(RoundedCornerShape(28.dp))
                  .background(Color(0xFF1C1C1E)),
              verticalAlignment = Alignment.CenterVertically,
          ) {
              Icon(Icons.Default.Search, null, tint = Color(0xFF8A9098),
                  modifier = Modifier.padding(start = 14.dp).size(20.dp))
              TextField(
                  value = query,
                  onValueChange = { query = it },
                  modifier = Modifier.weight(1f),
                  placeholder = {
                      Text("Search songs, artists, albums", fontSize = 14.sp, color = Color(0xFF6E6E73))
                  },
                  colors = TextFieldDefaults.colors(
                      focusedContainerColor = Color.Transparent,
                      unfocusedContainerColor = Color.Transparent,
                      focusedIndicatorColor = Color.Transparent,
                      unfocusedIndicatorColor = Color.Transparent,
                      cursorColor = KiyoTeal,
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White,
                  ),
                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                  keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                  singleLine = true,
              )
              if (query.isNotEmpty()) {
                  IconButton(onClick = { query = "" }) {
                      Icon(Icons.Default.Close, "Clear", tint = Color(0xFF8A9098),
                          modifier = Modifier.size(18.dp))
                  }
              }
          }

          if (query.isNotEmpty()) {
              Text(results.size.toString() + if (results.size == 1) " result" else " results",
                  fontSize = 12.sp, color = Color(0xFF6E6E73),
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
          }

          HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)

          when {
              query.isBlank() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(Icons.Default.Search, null, modifier = Modifier.size(52.dp),
                          tint = Color(0xFF3A3A3C))
                      Spacer(Modifier.height(12.dp))
                      Text("Type to search your library", fontSize = 14.sp, color = Color(0xFF6E6E73))
                  }
              }
              results.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                  Text("No results for: " + query, fontSize = 14.sp, color = Color(0xFF6E6E73))
              }
              else -> LazyColumn {
                  itemsIndexed(results, key = { _, t -> t.id }) { index, track ->
                      Row(
                          modifier = Modifier
                              .fillMaxWidth()
                              .background(Color.Black)
                              .clickable {
                                  viewModel.playAll(results, index)
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
                                  color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                              Text(artistAlbumLine(track.rawArtist, track.album),
                                  fontSize = 12.sp, color = Color(0xFF8A9098),
                                  maxLines = 1, overflow = TextOverflow.Ellipsis)
                          }
                          Text(formatDuration(track.durationMs), fontSize = 11.sp, color = Color(0xFF6E6E73))
                      }
                      HorizontalDivider(modifier = Modifier.padding(start = 78.dp),
                          color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                  }
              }
          }
      }
  }
  