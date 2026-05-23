package com.space4414.kiyo.ui.component

  import androidx.annotation.DrawableRes
  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.material3.Icon
  import androidx.compose.material3.IconButton
  import androidx.compose.material3.Text
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.res.painterResource
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextOverflow
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.space4414.kiyo.R
  import com.space4414.kiyo.data.db.entity.TrackEntity
  import com.space4414.kiyo.util.toDisplayArtist

  private val MiniPlayerBg   = Color(0xFF1C1C1E)
  private val ProgressTrack  = Color(0xFF3A3A3C)
  private val ProgressFill   = Color(0xFF6E6E73)

  /**
   * Poweramp-style mini player bar.
   * Flat dark surface, album art left, track info centre, play/pause right.
   * Thin rounded progress bar below the content row.
   */
  @Composable
  fun MiniPlayer(
      track: TrackEntity,
      isPlaying: Boolean,
      positionMs: Long,
      durationMs: Long,
      onToggle: () -> Unit,
      onExpand: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
      @DrawableRes val icon: Int = if (isPlaying) R.drawable.ic_kiyo_pause else R.drawable.ic_kiyo_graphic_eq

      Column(
          modifier = modifier
              .fillMaxWidth()
              .background(MiniPlayerBg)
              .clickable(onClick = onExpand),
      ) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
              AlbumArtBox(
                  albumId = track.albumId,
                  fallbackLabel = track.album.ifBlank { track.title },
                  modifier = Modifier.size(44.dp),
                  cornerRadius = 6.dp,
                  iconSize = 20.dp,
              )
              Spacer(Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                  Text(
                      text = track.title,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 14.sp,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                  )
                  val subtitle = buildString {
                      append(track.rawArtist.toDisplayArtist())
                      if (track.album.isNotBlank()) append(" — ").append(track.album)
                  }
                  Text(
                      text = subtitle,
                      fontSize = 12.sp,
                      color = Color(0xFF8A9098),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                  )
              }
              IconButton(onClick = onToggle, modifier = Modifier.size(44.dp)) {
                  Icon(
                      painter = painterResource(icon),
                      contentDescription = if (isPlaying) "Pause" else "Play",
                      tint = Color.White,
                      modifier = Modifier.size(24.dp),
                  )
              }
          }
          // Progress pill
          Box(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp)
                  .padding(bottom = 6.dp),
          ) {
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(3.dp)
                      .clip(RoundedCornerShape(50))
                      .background(ProgressTrack),
              )
              if (progress > 0f) {
                  Box(
                      modifier = Modifier
                          .fillMaxWidth(progress)
                          .height(3.dp)
                          .clip(RoundedCornerShape(50))
                          .background(ProgressFill),
                  )
              }
          }
      }
  }
  