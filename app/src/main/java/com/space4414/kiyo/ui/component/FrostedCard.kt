package com.space4414.kiyo.ui.component

  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.layout.BoxScope
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.unit.Dp
  import androidx.compose.ui.unit.dp

  private val FlatSurface = Color(0xFF1C1C1E)

  /**
   * Flat dark card replacing the frosted-glass panel.
   * Zero blur, zero transparency compositing — single opaque rectangle.
   */
  @Composable
  fun FrostedCard(
      modifier: Modifier = Modifier,
      cornerRadius: Dp = 16.dp,
      fillColor: Color = Color.Transparent,
      outlineColor: Color = Color.Unspecified,
      outlineWidth: Dp = 1.dp,
      content: @Composable BoxScope.() -> Unit,
  ) {
      val bg = if (fillColor != Color.Transparent) fillColor else FlatSurface
      Box(
          modifier = modifier
              .clip(RoundedCornerShape(cornerRadius))
              .background(bg),
          content = content,
      )
  }
  