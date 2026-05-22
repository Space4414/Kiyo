package com.space4414.kiyo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.space4414.kiyo.ui.screen.LibraryScreen
import com.space4414.kiyo.ui.screen.PlayerScreen
import com.space4414.kiyo.ui.screen.QueueScreen
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel

object Routes {
    const val LIBRARY = "library"
    const val PLAYER  = "player"
    const val QUEUE   = "queue"
}

@Composable
fun KiyoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onRequestStoragePermission: () -> Unit = {},
) {
    val viewModel: PlayerViewModel = hiltViewModel()
    val storagePermissionGranted by viewModel.storagePermissionGranted
        .collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Routes.LIBRARY,
        ) {
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    viewModel = viewModel,
                    hasStoragePermission = storagePermissionGranted,
                    onRequestStoragePermission = onRequestStoragePermission,
                    onOpenPlayer = { navController.navigate(Routes.PLAYER) },
                )
            }
            composable(Routes.PLAYER) {
                PlayerScreen(
                    viewModel = viewModel,
                    onOpenQueue = { navController.navigate(Routes.QUEUE) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.QUEUE) {
                QueueScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
