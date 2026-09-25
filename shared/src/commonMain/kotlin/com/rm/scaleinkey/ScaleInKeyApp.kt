package com.rm.scaleinkey

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rm.scaleinkey.ui.AppScreen
import com.rm.scaleinkey.ui.ChordProgressionScreen
import com.rm.scaleinkey.ui.LocalSoundEngine
import com.rm.scaleinkey.ui.ScaleExplorerScreen
import com.rm.scaleinkey.ui.rememberChordProgressionState
import com.rm.scaleinkey.ui.rememberScaleExplorerState
import com.rm.scaleinkey.ui.theme.ScaleInKeyTheme

@Composable
fun ScaleInKeyApp(platform: ScaleInKeyPlatform) {
    ScaleInKeyTheme {
        // Hoisted above both screens: a single SoundEngine (on Android it owns the native
        // engine/soundfont lifetime, so a second instance per screen would double-load the
        // soundfont and fight over the native singleton) and a single ScaleExplorerState, so the
        // progression screen reads the exact same root/scale Explore shows rather than an
        // independent copy.
        val soundEngine = platform.rememberSoundEngine()
        val explorerState = rememberScaleExplorerState()
        val progressionState = rememberChordProgressionState()
        var screen by rememberSaveable { mutableStateOf(AppScreen.EXPLORE) }

        CompositionLocalProvider(LocalPlatform provides platform, LocalSoundEngine provides soundEngine) {
            when (screen) {
                AppScreen.EXPLORE -> ScaleExplorerScreen(
                    state = explorerState,
                    soundEngine = soundEngine,
                    onSwitchScreen = { screen = AppScreen.PROGRESSION },
                    modifier = Modifier.fillMaxSize(),
                )
                AppScreen.PROGRESSION -> ChordProgressionScreen(
                    explorerState = explorerState,
                    progressionState = progressionState,
                    soundEngine = soundEngine,
                    onSwitchScreen = { screen = AppScreen.EXPLORE },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
