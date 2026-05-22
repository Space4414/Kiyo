package com.space4414.kiyo.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that makes [AppSettings] available to every composable in the tree
 * without explicit parameter threading.
 *
 * Provided once at the root in [KiyoNavGraph] via [CompositionLocalProvider].
 */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }
