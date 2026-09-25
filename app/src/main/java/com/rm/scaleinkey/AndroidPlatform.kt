package com.rm.scaleinkey

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.rm.scaleinkey.audio.AndroidSoundEngine
import com.rm.scaleinkey.audio.SoundEngine

class AndroidPlatform(private val appContext: Context) : ScaleInKeyPlatform {
    @Composable
    override fun rememberSoundEngine(): SoundEngine = remember { AndroidSoundEngine(appContext) }

    @Composable
    override fun rememberSoundFontPicker(soundEngine: SoundEngine, onResult: (Boolean) -> Unit): () -> Unit {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) (soundEngine as AndroidSoundEngine).loadFromUri(uri, onResult)
        }
        return { launcher.launch(arrayOf("*/*")) }
    }

    override fun showMessage(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}
