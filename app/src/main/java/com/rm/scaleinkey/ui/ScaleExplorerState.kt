package com.rm.scaleinkey.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.rm.scaleinkey.music.CANONICAL_ROOTS
import com.rm.scaleinkey.music.ChordVoicing
import com.rm.scaleinkey.music.DiatonicChord
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.Scale
import com.rm.scaleinkey.music.ScaleType
import com.rm.scaleinkey.music.buildDiatonicChords
import com.rm.scaleinkey.music.display

class ScaleExplorerState(
    rootIndexState: MutableIntState,
    scaleTypeState: MutableState<ScaleType>,
    selectedDegreeState: MutableState<Int?>,
    instrumentState: MutableState<InstrumentType>,
    private val voicingByDegree: SnapshotStateMap<Int, ChordVoicing>,
) {
    var rootIndex by rootIndexState
    var scaleType by scaleTypeState
    var selectedDegree by selectedDegreeState
    var instrument by instrumentState

    val root: Note get() = CANONICAL_ROOTS[rootIndex]
    val scale: Scale get() = Scale(root, scaleType)
    val chords: List<DiatonicChord>
        get() = if (scaleType.supportsChords) buildDiatonicChords(scale) else emptyList()

    val selectedChord: DiatonicChord?
        get() = selectedDegree?.let { degree -> chords.getOrNull(degree) }

    fun voicingFor(degree: Int): ChordVoicing = voicingByDegree[degree] ?: ChordVoicing.TRIAD

    val highlightedNotes: List<Note>
        get() = selectedChord?.let { chord -> chord.display(voicingFor(chord.degree)).orderedTones }
            ?: scale.notes

    fun onRootSelected(index: Int) {
        rootIndex = index
        selectedDegree = null
        voicingByDegree.clear()
    }

    fun onScaleTypeSelected(type: ScaleType) {
        scaleType = type
        selectedDegree = null
        voicingByDegree.clear()
    }

    fun onChordTapped(degree: Int) {
        selectedDegree = if (selectedDegree == degree) null else degree
    }

    fun onSeventhToggled(degree: Int) {
        if (voicingByDegree[degree] == ChordVoicing.SEVENTH) {
            voicingByDegree.remove(degree)
        } else {
            voicingByDegree[degree] = ChordVoicing.SEVENTH
        }
        selectedDegree = degree
    }
}

@Composable
fun rememberScaleExplorerState(): ScaleExplorerState {
    val rootIndexState = rememberSaveable { mutableIntStateOf(0) }
    val scaleTypeState = rememberSaveable { mutableStateOf(ScaleType.IONIAN) }
    val selectedDegreeState = rememberSaveable { mutableStateOf<Int?>(null) }
    val instrumentState = rememberSaveable { mutableStateOf(InstrumentType.PIANO) }
    val voicingByDegree = remember { mutableStateMapOf<Int, ChordVoicing>() }

    return remember {
        ScaleExplorerState(rootIndexState, scaleTypeState, selectedDegreeState, instrumentState, voicingByDegree)
    }
}
