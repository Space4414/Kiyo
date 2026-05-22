package com.space4414.kiyo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KiyoDarkColorScheme = darkColorScheme(
    primary          = KiyoTeal,
    onPrimary        = KiyoCharcoal,
    primaryContainer = KiyoTealDim,
    secondary        = KiyoPurple,
    onSecondary      = KiyoCharcoal,
    tertiary         = KiyoAmber,
    onTertiary       = KiyoCharcoal,
    background       = KiyoCharcoal,
    onBackground     = KiyoOnSurface,
    surface          = KiyoSurface,
    onSurface        = KiyoOnSurface,
    surfaceVariant   = KiyoCharcoalLight,
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
