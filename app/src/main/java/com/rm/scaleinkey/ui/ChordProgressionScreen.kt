package com.rm.scaleinkey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.audio.SoundEngine
import com.rm.scaleinkey.music.display
import com.rm.scaleinkey.music.previewMidiNotes
import com.rm.scaleinkey.ui.components.ChordPalette
import com.rm.scaleinkey.ui.components.HeroBand
import com.rm.scaleinkey.ui.components.PlaybackControls
import com.rm.scaleinkey.ui.components.ProgressionSequenceRow
import com.rm.scaleinkey.ui.components.ScaleSelectorRow
import com.rm.scaleinkey.ui.components.SoundControls
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ChordProgressionScreen(
    explorerState: ScaleExplorerState,
    progressionState: ChordProgressionState,
    soundEngine: SoundEngine,
    onSwitchScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeroBand(
                    onSwitchScreen = onSwitchScreen,
                    switchIcon = "←",
                    switchContentDescription = "Back to Explore",
                )
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
                            rootIndex = explorerState.rootIndex,
                            scaleType = explorerState.scaleType,
                            onRootSelected = explorerState::onRootSelected,
                            onScaleTypeSelected = explorerState::onScaleTypeSelected,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                        if (explorerState.scaleType.supportsChords) {
                            ChordPalette(
                                chords = explorerState.chords,
                                voicingFor = explorerState::voicingFor,
                                instrument = explorerState.instrument,
                                onChordAppended = progressionState::append,
                                onSeventhToggled = explorerState::onSeventhToggled,
                            )
                            ProgressionSequenceRow(
                                slots = progressionState.slots,
                                chordFor = { degree -> explorerState.chords.getOrNull(degree) },
                                currentStepIndex = progressionState.currentStepIndex.takeIf { progressionState.isPlaying },
                                onRemove = progressionState::remove,
                                onMove = progressionState::move,
                            )
                            PlaybackControls(
                                bpm = progressionState.bpm,
                                onBpmChanged = { progressionState.bpm = it },
                                beatsPerChord = progressionState.beatsPerChord,
                                onBeatsPerChordChanged = { progressionState.beatsPerChord = it },
                                loop = progressionState.loop,
                                onLoopChanged = { progressionState.loop = it },
                                isPlaying = progressionState.isPlaying,
                                hasSlots = progressionState.slots.isNotEmpty(),
                                onPlayStop = {
                                    if (progressionState.isPlaying) progressionState.stop() else progressionState.play()
                                },
                                modifier = Modifier.padding(bottom = 96.dp),
                            )
                            SequencerPlaybackEffect(explorerState, progressionState, soundEngine)
                        } else {
                            Text(
                                text = "This scale doesn't have diatonic chords to sequence — pick a mode, minor variant, or jazz scale.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
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

/**
 * Drives the sequencer's playback clock. Only keyed on [ChordProgressionState.isPlaying]: setting
 * it to false (Stop, the natural end of a non-looping run, or the sequence emptying out from under
 * playback) changes the key, and Compose cancels the running coroutine for free — no manual `Job`
 * bookkeeping needed. The chord actually sounding is left to ring out its own already-scheduled
 * release (see SoundEngine.triggerNotes) rather than being hard-silenced on Stop.
 */
@Composable
private fun SequencerPlaybackEffect(
    explorerState: ScaleExplorerState,
    progressionState: ChordProgressionState,
    soundEngine: SoundEngine,
) {
    LaunchedEffect(progressionState.isPlaying) {
        if (!progressionState.isPlaying) return@LaunchedEffect
        if (progressionState.slots.isEmpty()) {
            progressionState.isPlaying = false
            return@LaunchedEffect
        }
        var index = 0
        while (isActive) {
            progressionState.currentStepIndex = index
            // Read live on every step (not captured once) so edits made while playing — removing
            // or reordering a slot mid-jam — are reflected on the next step rather than requiring
            // playback to be stopped and restarted.
            val slots = progressionState.slots
            val slot = slots.getOrNull(index)
            val chord = slot?.let { explorerState.chords.getOrNull(it.degree) }
            if (chord != null) {
                val display = chord.display(slot.voicing)
                val midiNotes = previewMidiNotes(explorerState.instrument, display.orderedTones.map { it.pitchClass })
                soundEngine.playChord(explorerState.instrument, midiNotes, durationMs = progressionState.stepDurationMs)
            }
            delay(progressionState.stepDurationMs)
            index++
            if (index >= progressionState.slots.size) {
                if (!progressionState.loop) {
                    progressionState.isPlaying = false
                    return@LaunchedEffect
                }
                index = 0
            }
        }
    }
}
