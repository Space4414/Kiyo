package com.space4414.kiyo.ui.theme

  import androidx.compose.material3.MaterialTheme
  import androidx.compose.material3.darkColorScheme
  import androidx.compose.runtime.Composable

  private val KiyoDarkColorScheme = darkColorScheme(
      primary          = KiyoTeal,
      onPrimary        = KiyoPureBlack,
      primaryContainer = KiyoTealDim,
      secondary        = KiyoPurple,
      onSecondary      = KiyoPureBlack,
      tertiary         = KiyoAmber,
      onTertiary       = KiyoPureBlack,
      background       = KiyoPureBlack,
      onBackground     = KiyoOnSurface,
      surface          = KiyoDarkSurface,
      onSurface        = KiyoOnSurface,
      surfaceVariant   = KiyoDarkCard,
      onSurfaceVariant = KiyoOnSurfaceMuted,
      outline          = KiyoOutline,
      error            = KiyoError,
  )

  @Composable
  fun KiyoTheme(content: @Composable () -> Unit) {
      MaterialTheme(
          colorScheme = KiyoDarkColorScheme,
          typography  = KiyoTypography,
          content     = content,
      )
  }
  