package com.rm.scaleinkey.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rm.scaleinkey.audio.SoundEngine
import com.rm.scaleinkey.ui.components.ChordRow
import com.rm.scaleinkey.ui.components.InstrumentPager
import com.rm.scaleinkey.ui.components.ScaleInfoHeader
import com.rm.scaleinkey.ui.components.ScaleSelectorRow
import kotlinx.coroutines.launch

@Composable
fun ScaleExplorerScreen(modifier: Modifier = Modifier) {
    val state = rememberScaleExplorerState()
    val context = LocalContext.current
    val soundEngine = remember { SoundEngine(context.applicationContext) }

    CompositionLocalProvider(LocalSoundEngine provides soundEngine) {
        Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HeroBand()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 640.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            ScaleSelectorRow(
                                rootIndex = state.rootIndex,
                                scaleType = state.scaleType,
                                onRootSelected = state::onRootSelected,
                                onScaleTypeSelected = state::onScaleTypeSelected,
                                modifier = Modifier.padding(top = 20.dp),
                            )
                            ScaleInfoHeader(
                                root = state.root,
                                scaleDisplayName = state.scaleType.displayName,
                                notes = state.scale.notes,
                                instrument = state.instrument,
                            )
                            if (state.scaleType.supportsChords) {
                                ChordRow(
                                    chords = state.chords,
                                    selectedDegree = state.selectedDegree,
                                    voicingFor = state::voicingFor,
                                    instrument = state.instrument,
                                    onChordTapped = state::onChordTapped,
                                    onSeventhToggled = state::onSeventhToggled,
                                )
                            }
                            InstrumentPager(
                                instrument = state.instrument,
                                onInstrumentSelected = { state.instrument = it },
                                rootPitchClass = state.root.pitchClass,
                                highlightedNotes = state.highlightedNotes,
                                isChordSelection = state.selectedDegree != null,
                                modifier = Modifier.padding(bottom = 96.dp),
                            )
                        }
                    }
                }
                SoundControls(
                    soundEngine = soundEngine,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SoundControls(soundEngine: SoundEngine, modifier: Modifier = Modifier) {
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
            SmallFloatingActionButton(onClick = { menuExpanded = true }) {
                Text("🎼")
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

@Composable
private fun HeroBand(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            )
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ScaleInKey",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
