package com.space4414.kiyo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.space4414.kiyo.ui.component.BottomNavBar
import com.space4414.kiyo.ui.component.MiniPlayer
import com.space4414.kiyo.ui.screen.DspScreen
import com.space4414.kiyo.ui.screen.HomeScreen
import com.space4414.kiyo.ui.screen.LibraryScreen
import com.space4414.kiyo.ui.screen.PlayerScreen
import com.space4414.kiyo.ui.screen.QueueScreen
import com.space4414.kiyo.ui.screen.SearchScreen
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val DSP = "dsp"
    const val PLAYER = "player"
    const val QUEUE = "queue"
}

private val tabRoutes = setOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.DSP)

@Composable
fun KiyoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onRequestStoragePermission: () -> Unit = {},
) {
    val viewModel: PlayerViewModel = hiltViewModel()
    val storagePermissionGranted by viewModel.storagePermissionGranted.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
    val isTabScreen = currentRoute in tabRoutes

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenPlayer = { navController.navigate(Routes.PLAYER) },
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    viewModel = viewModel,
                    onOpenPlayer = { navController.navigate(Routes.PLAYER) },
                )
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    viewModel = viewModel,
                    hasStoragePermission = storagePermissionGranted,
                    onRequestStoragePermission = onRequestStoragePermission,
                    onOpenPlayer = { navController.navigate(Routes.PLAYER) },
                )
            }
            composable(Routes.DSP) {
                DspScreen(viewModel = viewModel)
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

        if (isTabScreen) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                if (uiState.currentTrack != null) {
                    MiniPlayer(
                        track = uiState.currentTrack!!,
                        isPlaying = uiState.isPlaying,
                        positionMs = uiState.positionMs,
                        durationMs = uiState.durationMs,
                        onToggle = viewModel::togglePlayPause,
                        onExpand = { navController.navigate(Routes.PLAYER) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavItemClick = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        }
    }
}
