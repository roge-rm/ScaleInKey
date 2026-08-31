package com.rm.scaleinkey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rm.scaleinkey.ui.ScaleExplorerScreen
import com.rm.scaleinkey.ui.theme.ScaleInKeyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScaleInKeyTheme {
                ScaleExplorerScreen()
            }
        }
    }
}
