package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.music.InstrumentTunings
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.fretMidiNote
import com.rm.scaleinkey.ui.LocalSoundEngine
import com.rm.scaleinkey.ui.components.diagrams.FrettedInstrumentDiagram
import com.rm.scaleinkey.ui.components.diagrams.PianoDiagram
import com.rm.scaleinkey.ui.theme.scaleColors
import kotlinx.coroutines.launch

private val TABS = InstrumentType.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentPager(
    instrument: InstrumentType,
    onInstrumentSelected: (InstrumentType) -> Unit,
    rootPitchClass: Int,
    highlightedNotes: List<Note>,
    isChordSelection: Boolean,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = instrument.ordinal,
        pageCount = { TABS.size },
    )
    val coroutineScope = rememberCoroutineScope()
    val soundEngine = LocalSoundEngine.current

    LaunchedEffect(instrument) {
        if (pagerState.currentPage != instrument.ordinal) {
            pagerState.animateScrollToPage(instrument.ordinal)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        onInstrumentSelected(TABS[pagerState.currentPage])
    }

    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Column {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                TABS.forEachIndexed { index, type ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) { page ->
                when (TABS[page]) {
                    InstrumentType.PIANO -> PianoDiagram(
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        onKeyTapped = { key -> soundEngine.playNote(InstrumentType.PIANO, key.midiNote) },
                    )
                    InstrumentType.GUITAR -> FrettedInstrumentDiagram(
                        tuning = InstrumentTunings.GUITAR,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        onFretTapped = { stringIndex, fret ->
                            soundEngine.playNote(InstrumentType.GUITAR, fretMidiNote(InstrumentTunings.GUITAR, stringIndex, fret))
                        },
                    )
                    InstrumentType.UKULELE -> FrettedInstrumentDiagram(
                        tuning = InstrumentTunings.UKULELE,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        onFretTapped = { stringIndex, fret ->
                            soundEngine.playNote(InstrumentType.UKULELE, fretMidiNote(InstrumentTunings.UKULELE, stringIndex, fret))
                        },
                    )
                    InstrumentType.BASS -> FrettedInstrumentDiagram(
                        tuning = InstrumentTunings.BASS,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        onFretTapped = { stringIndex, fret ->
                            soundEngine.playNote(InstrumentType.BASS, fretMidiNote(InstrumentTunings.BASS, stringIndex, fret))
                        },
                    )
                }
            }
            HighlightLegend(
                isChordSelection = isChordSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun HighlightLegend(isChordSelection: Boolean, modifier: Modifier = Modifier) {
    val palette = MaterialTheme.scaleColors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterHorizontally),
    ) {
        LegendSwatch(color = palette.root, label = "Root")
        LegendSwatch(
            color = if (isChordSelection) palette.chordTone else palette.scaleTone,
            label = if (isChordSelection) "Chord tone" else "Scale tone",
        )
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
