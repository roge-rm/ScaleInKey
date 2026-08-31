package com.rm.scaleinkey.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.rm.scaleinkey.audio.SoundEngine

val LocalSoundEngine = staticCompositionLocalOf<SoundEngine> {
    error("LocalSoundEngine not provided — wrap content in ScaleExplorerScreen")
}
