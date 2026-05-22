package com.space4414.kiyo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.space4414.kiyo.ui.navigation.KiyoNavGraph
import com.space4414.kiyo.ui.theme.KiyoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiyoTheme {
                KiyoNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
