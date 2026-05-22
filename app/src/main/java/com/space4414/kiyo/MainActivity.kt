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

    private val playerViewModel: PlayerViewModel by viewModels()

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        playerViewModel.updateStoragePermission(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync initial permission state into the ViewModel before Compose renders
        val alreadyGranted = hasStoragePermission()
        playerViewModel.updateStoragePermission(alreadyGranted)

        setContent {
            KiyoTheme {
                KiyoNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    onRequestStoragePermission = ::requestStoragePermission,
                )
            }
        }

        if (!alreadyGranted) {
            storagePermissionLauncher.launch(storagePermission())
        }
    }

    private fun hasStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(this, storagePermission()) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        if (!hasStoragePermission()) {
            storagePermissionLauncher.launch(storagePermission())
        }
    }

    companion object {
        fun storagePermission(): String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
