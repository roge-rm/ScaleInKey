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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.audio.SoundEngine
import com.rm.scaleinkey.ui.components.ChordRow
import com.rm.scaleinkey.ui.components.HeroBand
import com.rm.scaleinkey.ui.components.InstrumentPager
import com.rm.scaleinkey.ui.components.ScaleInfoHeader
import com.rm.scaleinkey.ui.components.ScaleSelectorRow
import com.rm.scaleinkey.ui.components.SoundControls

@Composable
fun ScaleExplorerScreen(
    state: ScaleExplorerState,
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
                    switchIcon = "🎹",
                    switchContentDescription = "Open chord progression sequencer",
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
                            chartViewEnabled = state.chartViewEnabled,
                            onChartViewChanged = { state.chartViewEnabled = it },
                            guitarChordShape = state.guitarChordShape,
                            ukuleleChordShape = state.ukuleleChordShape,
                            bassTuning = state.bassTuning,
                            bassRootShape = state.bassRootShape,
                            bassFiveStringEnabled = state.bassFiveStringEnabled,
                            onBassFiveStringChanged = { state.bassFiveStringEnabled = it },
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
