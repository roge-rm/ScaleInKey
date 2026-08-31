package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PianoKeysTest {

    @Test
    fun `two octaves produce 24 keys with 14 white and 10 black`() {
        val keys = buildPianoKeys(octaves = 2, startMidiNote = 60)
        assertEquals(24, keys.size)
        assertEquals(14, keys.count { !it.isBlack })
        assertEquals(10, keys.count { it.isBlack })
    }

    @Test
    fun `white-black pattern matches a real keyboard starting on C`() {
        val keys = buildPianoKeys(octaves = 1, startMidiNote = 60)
        val expectedBlack = listOf(false, true, false, true, false, false, true, false, true, false, true, false)
        assertEquals(expectedBlack, keys.map { it.isBlack })
    }

    @Test
    fun `pitch class wraps across octaves`() {
        val keys = buildPianoKeys(octaves = 1, startMidiNote = 70)
        assertEquals(10, keys.first().pitchClass) // Bb, black key
        assertTrue(keys.first().isBlack)
        assertEquals(9, keys.last().pitchClass) // A, white key
        assertFalse(keys.last().isBlack)
    }

    @Test
    fun `midi note increments one per key from startMidiNote`() {
        val keys = buildPianoKeys(octaves = 2, startMidiNote = 60)
        assertEquals((60..83).toList(), keys.map { it.midiNote })
    }
}
