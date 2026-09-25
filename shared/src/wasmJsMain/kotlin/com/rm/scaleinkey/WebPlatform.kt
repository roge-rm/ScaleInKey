package com.rm.scaleinkey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.rm.scaleinkey.audio.SoundEngine
import com.rm.scaleinkey.audio.WebSoundEngine

object WebPlatform : ScaleInKeyPlatform {
    @Composable
    override fun rememberSoundEngine(): SoundEngine = remember { WebSoundEngine() }

    @Composable
    override fun rememberSoundFontPicker(soundEngine: SoundEngine, onResult: (Boolean) -> Unit): () -> Unit =
        { pickSoundFont(onResult) }

    override fun showMessage(message: String) = showToast(message)
}

/** sikSound lives in index.html. */
private fun pickSoundFont(onResult: (Boolean) -> Unit): Unit = js("sikSound.pickFile(onResult)")

private fun showToast(message: String): Unit = js("sikSound.toast(message)")
