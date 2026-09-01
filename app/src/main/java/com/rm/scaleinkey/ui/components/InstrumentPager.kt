package com.rm.scaleinkey.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.sp
import com.rm.scaleinkey.music.ChordShape
import com.rm.scaleinkey.music.InstrumentTunings
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.StringInstrumentTuning
import com.rm.scaleinkey.music.fretMidiNote
import com.rm.scaleinkey.music.scaleBoxWindow
import com.rm.scaleinkey.ui.LocalSoundEngine
import com.rm.scaleinkey.ui.components.diagrams.CHART_MODE_SCALE
import com.rm.scaleinkey.ui.components.diagrams.ChordShapeDiagram
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
    chartViewEnabled: Boolean,
    onChartViewChanged: (Boolean) -> Unit,
    guitarChordShape: ChordShape?,
    ukuleleChordShape: ChordShape?,
    bassRootShape: ChordShape?,
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
                    InstrumentType.GUITAR -> FrettedOrChartDiagram(
                        instrument = InstrumentType.GUITAR,
                        tuning = InstrumentTunings.GUITAR,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        chartViewEnabled = chartViewEnabled,
                        chordShape = guitarChordShape,
                    )
                    InstrumentType.UKULELE -> FrettedOrChartDiagram(
                        instrument = InstrumentType.UKULELE,
                        tuning = InstrumentTunings.UKULELE,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        chartViewEnabled = chartViewEnabled,
                        chordShape = ukuleleChordShape,
                    )
                    InstrumentType.BASS -> FrettedOrChartDiagram(
                        instrument = InstrumentType.BASS,
                        tuning = InstrumentTunings.BASS,
                        rootPitchClass = rootPitchClass,
                        highlightedNotes = highlightedNotes,
                        isChordSelection = isChordSelection,
                        chartViewEnabled = chartViewEnabled,
                        chordShape = bassRootShape,
                    )
                }
            }
            // The toggle sits in the same row as the legend, off to the side, rather than
            // overlapping the diagram canvas above (which draws fret/string content edge-to-edge
            // in every mode, so any overlay on it ends up covering real content). defaultMinSize
            // keeps this row's height constant across tabs — including Piano, which has no
            // toggle — so switching tabs can't shift anything below it.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 32.dp)
                    .padding(bottom = 16.dp),
            ) {
                HighlightLegend(
                    isChordSelection = isChordSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                )
                if (TABS[pagerState.currentPage] != InstrumentType.PIANO) {
                    ChartViewToggleButton(
                        chartViewEnabled = chartViewEnabled,
                        onToggle = { onChartViewChanged(!chartViewEnabled) },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Picks between the three fretted-instrument display modes: a full chord shape / root marker
 * (chart mode + a chord selected + a shape available), a windowed scale-position box (chart mode,
 * otherwise), or the original full-neck highlighting (chart mode off).
 */
@Composable
private fun FrettedOrChartDiagram(
    instrument: InstrumentType,
    tuning: StringInstrumentTuning,
    rootPitchClass: Int,
    highlightedNotes: List<Note>,
    isChordSelection: Boolean,
    chartViewEnabled: Boolean,
    chordShape: ChordShape?,
) {
    val soundEngine = LocalSoundEngine.current
    // Each branch below now sizes its box to its own content (no more shared/forced aspect
    // ratio), so animateContentSize animates the box smoothly across chart-mode toggles, chord
    // select/deselect, and instrument switches instead of jumping between sizes.
    Box(modifier = Modifier.animateContentSize()) {
        when {
            chartViewEnabled && isChordSelection && chordShape != null -> ChordShapeDiagram(
                shape = chordShape,
                onFretTapped = { stringIndex, fret ->
                    soundEngine.playNote(instrument, fretMidiNote(tuning, stringIndex, fret))
                },
            )
            chartViewEnabled -> FrettedInstrumentDiagram(
                tuning = tuning.scaleBoxWindow(),
                rootPitchClass = rootPitchClass,
                highlightedNotes = highlightedNotes,
                isChordSelection = isChordSelection,
                contentScale = CHART_MODE_SCALE,
                onFretTapped = { stringIndex, fret ->
                    soundEngine.playNote(instrument, fretMidiNote(tuning, stringIndex, fret))
                },
            )
            else -> FrettedInstrumentDiagram(
                tuning = tuning,
                rootPitchClass = rootPitchClass,
                highlightedNotes = highlightedNotes,
                isChordSelection = isChordSelection,
                onFretTapped = { stringIndex, fret ->
                    soundEngine.playNote(instrument, fretMidiNote(tuning, stringIndex, fret))
                },
            )
        }
    }
}

/**
 * Small inline icon toggle placed beside the legend rather than overlaid on the diagram itself
 * (which draws content edge-to-edge in every mode, so any overlay on it covers real content) —
 * shows the *current* mode (🎸 neck, 📋 chart), matching the existing sound-on/off toggle's
 * convention in ScaleExplorerScreen.kt's SoundControls.
 */
@Composable
private fun ChartViewToggleButton(chartViewEnabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onToggle,
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(if (chartViewEnabled) "📋" else "🎸", fontSize = 14.sp)
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
