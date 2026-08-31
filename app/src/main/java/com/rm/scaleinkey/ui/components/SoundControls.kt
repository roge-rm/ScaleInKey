package com.rm.scaleinkey.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rm.scaleinkey.audio.SoundEngine
import kotlinx.coroutines.launch

/** Bottom-corner FAB stack (soundfont load/reset, mute toggle) shared by every top-level screen. */
@Composable
fun SoundControls(soundEngine: SoundEngine, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var soundEnabled by remember { mutableStateOf(soundEngine.enabled) }
    var menuExpanded by remember { mutableStateOf(false) }

    val pickSoundFontLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        soundEngine.loadFromUri(uri) { ok ->
            coroutineScope.launch {
                Toast.makeText(
                    context,
                    if (ok) "Soundfont loaded" else "Couldn't load that file as a soundfont",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box {
            Surface(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🎼", fontSize = 14.sp)
                }
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Load soundfont…") },
                    onClick = {
                        menuExpanded = false
                        pickSoundFontLauncher.launch(arrayOf("*/*"))
                    },
                )
                DropdownMenuItem(
                    text = { Text("Reset to default") },
                    onClick = {
                        menuExpanded = false
                        soundEngine.resetToDefault { ok ->
                            coroutineScope.launch {
                                Toast.makeText(
                                    context,
                                    if (ok) "Reset to default soundfont" else "Couldn't reset soundfont",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                )
            }
        }
        Surface(
            onClick = {
                soundEnabled = !soundEnabled
                soundEngine.setEnabled(soundEnabled)
            },
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(if (soundEnabled) "🔊" else "🔇", fontSize = 14.sp)
            }
        }
    }
}
