package com.rm.scaleinkey.ui

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun newState(): ChordProgressionState = ChordProgressionState(
    bpmState = mutableIntStateOf(100),
    beatsPerChordState = mutableStateOf(BeatsPerChord.FOUR),
    loopState = mutableStateOf(false),
    isPlayingState = mutableStateOf(false),
    currentStepIndexState = mutableIntStateOf(-1),
    slotsState = mutableStateListOf(),
    nextIdState = mutableIntStateOf(0),
)

class ChordProgressionStateTest {

    @Test
    fun `append assigns distinct ids even for repeated degrees`() {
        val state = newState()
        state.append(0)
        state.append(0)
        state.append(3)

        assertEquals(listOf(0, 0, 3), state.slots.map { it.degree })
        assertEquals(3, state.slots.map { it.id }.distinct().size)
    }

    @Test
    fun `move reorders adjacent and non-adjacent slots`() {
        val state = newState()
        listOf(0, 1, 2, 3).forEach { state.append(it) }
        val ids = state.slots.map { it.id }

        state.move(1, 2) // adjacent swap: degrees 0,1,2,3 -> 0,2,1,3
        assertEquals(listOf(0, 2, 1, 3), state.slots.map { it.degree })

        state.move(0, 3) // non-adjacent: move first slot to the end
        assertEquals(listOf(2, 1, 3, 0), state.slots.map { it.degree })

        // ids travel with their slots, not with position
        assertEquals(ids[0], state.slots.last().id)
    }

    @Test
    fun `move is a no-op for out-of-range indices`() {
        val state = newState()
        listOf(0, 1).forEach { state.append(it) }

        state.move(-1, 1)
        state.move(0, 5)
        state.move(0, 0)

        assertEquals(listOf(0, 1), state.slots.map { it.degree })
    }

    @Test
    fun `remove deletes exactly the matching id, even with duplicate degrees`() {
        val state = newState()
        state.append(2)
        state.append(2)
        val secondId = state.slots[1].id

        state.remove(secondId)

        assertEquals(1, state.slots.size)
        assertEquals(2, state.slots.single().degree)
    }

    @Test
    fun `remove stops playback once the sequence becomes empty`() {
        val state = newState()
        state.append(0)
        state.play()
        assertTrue(state.isPlaying)

        state.remove(state.slots.single().id)

        assertFalse(state.isPlaying)
    }

    @Test
    fun `play is a no-op on an empty sequence`() {
        val state = newState()
        state.play()
        assertFalse(state.isPlaying)
    }

    @Test
    fun `stepDurationMs follows bpm and beats-per-chord`() {
        val state = newState()
        state.bpm = 120
        state.beatsPerChord = BeatsPerChord.FOUR
        assertEquals(2000L, state.stepDurationMs)

        state.bpm = 60
        state.beatsPerChord = BeatsPerChord.ONE
        assertEquals(1000L, state.stepDurationMs)

        state.bpm = 100
        state.beatsPerChord = BeatsPerChord.TWO
        assertEquals(1200L, state.stepDurationMs)
    }
}
