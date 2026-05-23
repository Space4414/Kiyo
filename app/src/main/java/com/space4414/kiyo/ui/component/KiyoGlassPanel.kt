package com.space4414.kiyo.ui.component

  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.layout.BoxScope
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.compositionLocalOf
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.graphics.ImageBitmap
  import androidx.compose.ui.unit.Dp
  import androidx.compose.ui.unit.dp

  /** Kept for binary compatibility; always null in flat-dark build. */
  val LocalBlurredAmbient = compositionLocalOf<ImageBitmap?> { null }

  private val FlatSurface = Color(0xFF1C1C1E)

  /**
   * Flat dark panel. Blur/frost logic removed for low-end device performance.
   */
  @Composable
  fun KiyoGlassPanel(
      modifier: Modifier = Modifier,
      cornerRadius: Dp = 16.dp,
      blurredBitmap: ImageBitmap? = null,
      content: @Composable BoxScope.() -> Unit,
  ) {
      Box(
          modifier = modifier
              .clip(RoundedCornerShape(cornerRadius))
              .background(FlatSurface),
          content = content,
      )
  }
  