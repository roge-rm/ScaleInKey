package com.rm.scaleinkey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.rm.scaleinkey.audio.SoundEngine

/** What differs between the Android app and the browser build. */
interface ScaleInKeyPlatform {
    /** The one [SoundEngine] for the whole app; called once, from [ScaleInKeyApp]. */
    @Composable
    fun rememberSoundEngine(): SoundEngine

    /** Returns a function that asks the user for a .sf2 file and loads it into [soundEngine],
     *  reporting whether it worked. */
    @Composable
    fun rememberSoundFontPicker(soundEngine: SoundEngine, onResult: (Boolean) -> Unit): () -> Unit

    /** A brief message, e.g. a Toast. */
    fun showMessage(message: String)
}

val LocalPlatform = staticCompositionLocalOf<ScaleInKeyPlatform> {
    error("LocalPlatform not provided — wrap content in ScaleInKeyApp")
}
