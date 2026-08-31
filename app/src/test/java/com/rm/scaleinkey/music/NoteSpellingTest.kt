package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteSpellingTest {

    @Test
    fun `canonical roots cover all 12 pitch classes with expected spellings`() {
        val expected = listOf(
            "C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"
        )
        val actual = CANONICAL_ROOTS.map { it.displayName().replace("♭", "b").replace("♯", "#") }
        assertEquals(expected, actual)
        assertEquals((0..11).toList(), CANONICAL_ROOTS.map { it.pitchClass })
    }

    @Test
    fun `pitch class wraps accidentals correctly`() {
        assertEquals(11, Note(Letter.C, -1).pitchClass) // Cb == B
        assertEquals(0, Note(Letter.B, 1).pitchClass) // B# == C
    }
}
