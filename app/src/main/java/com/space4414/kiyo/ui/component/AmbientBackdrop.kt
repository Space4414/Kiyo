package com.space4414.kiyo.ui.component

  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.Box
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.Color

  /**
   * Background surface simplified to pure black.
   * Poweramp-style: no blur, no gradients — single opaque rectangle.
   */
  @Composable
  fun AmbientBackdrop(modifier: Modifier = Modifier) {
      Box(modifier = modifier.background(Color.Black))
  }
  