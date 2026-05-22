package com.space4414.kiyo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.space4414.kiyo.ui.navigation.KiyoNavGraph
import com.space4414.kiyo.ui.theme.KiyoTheme
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Obtain the ViewModel at Activity scope so we can trigger a library refresh
     * after the user grants storage permission. Using viewModels() here and
     * hiltViewModel() in Compose both resolve to the same instance via the
     * Activity's ViewModelStore.
     */
    private val playerViewModel: PlayerViewModel by viewModels()

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            playerViewModel.refreshLibrary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiyoTheme {
                KiyoNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
        // Request storage permission after setContent so the UI is visible
        // while the dialog is shown. The ViewModel's init-block scan is
        // protected by a try-catch and will simply return empty on first run
        // without permission; refreshLibrary() re-triggers after grant.
        checkAndRequestStoragePermission()
    }

    private fun checkAndRequestStoragePermission() {
        val permission = storagePermission()
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            storagePermissionLauncher.launch(permission)
        }
        // If already granted: PlayerViewModel.init already triggered syncLibrary().
    }

    companion object {
        /** The correct storage permission for the running API level. */
        fun storagePermission(): String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
