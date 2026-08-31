package com.rm.scaleinkey.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/** One chord in a built progression. [id] is a stable identity independent of [degree], since the
 * same diatonic degree can appear more than once in a sequence and both drag-reorder and
 * tap-to-remove need to target one specific occurrence, not "the slot with this degree". */
data class ProgressionSlot(val id: Long, val degree: Int)

enum class BeatsPerChord(val beats: Int, val label: String) {
    ONE(1, "1"),
    TWO(2, "2"),
    FOUR(4, "4"),
}

/**
 * Holds a built chord progression and its playback settings. Mirrors [ScaleExplorerState]'s
 * pattern: a plain class over externally-owned [androidx.compose.runtime.MutableState]s, built by
 * [rememberChordProgressionState]. Deliberately holds no reference to [ScaleExplorerState] itself
 * — a slot only stores a scale degree (0-6), resolved against whatever the current scale's
 * diatonic chords are at read time, by the caller.
 */
class ChordProgressionState(
    bpmState: MutableIntState,
    beatsPerChordState: MutableState<BeatsPerChord>,
    loopState: MutableState<Boolean>,
    isPlayingState: MutableState<Boolean>,
    currentStepIndexState: MutableIntState,
    private val slotsState: SnapshotStateList<ProgressionSlot>,
    private val nextIdState: MutableIntState,
) {
    var bpm by bpmState
    var beatsPerChord by beatsPerChordState
    var loop by loopState
    var isPlaying by isPlayingState
    var currentStepIndex by currentStepIndexState

    val slots: List<ProgressionSlot> get() = slotsState

    val stepDurationMs: Long
        get() = (60_000L / bpm.coerceAtLeast(1)) * beatsPerChord.beats

    fun append(degree: Int) {
        slotsState.add(ProgressionSlot(id = nextIdState.intValue++.toLong(), degree = degree))
    }

    fun remove(id: Long) {
        slotsState.removeAll { it.id == id }
        if (slotsState.isEmpty()) isPlaying = false
    }

    /** Moves the slot currently at [fromIndex] to [toIndex], shifting the rest. No-op if either index is out of range. */
    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in slotsState.indices || toIndex !in slotsState.indices) return
        val item = slotsState.removeAt(fromIndex)
        slotsState.add(toIndex, item)
    }

    fun play() {
        if (slotsState.isEmpty()) return
        currentStepIndex = 0
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
    }
}

@Composable
fun rememberChordProgressionState(): ChordProgressionState {
    val bpmState = rememberSaveable { mutableIntStateOf(100) }
    val beatsPerChordState = rememberSaveable { mutableStateOf(BeatsPerChord.FOUR) }
    val loopState = rememberSaveable { mutableStateOf(false) }
    // NOT rememberSaveable, matching ScaleExplorerState's voicingByDegree precedent: this is
    // transient session state (a built-but-unsaved sequence, and whether it's mid-playback right
    // now), not a setting worth restoring after process death — and the slot list is a plain data
    // class list with no natural Saver without writing a custom one, for state that's cheap to
    // rebuild by tapping a few chords again.
    val isPlayingState = remember { mutableStateOf(false) }
    val currentStepIndexState = remember { mutableIntStateOf(-1) }
    val slotsState = remember { mutableStateListOf<ProgressionSlot>() }
    val nextIdState = remember { mutableIntStateOf(0) }

    return remember {
        ChordProgressionState(
            bpmState,
            beatsPerChordState,
            loopState,
            isPlayingState,
            currentStepIndexState,
            slotsState,
            nextIdState,
        )
    }
}
