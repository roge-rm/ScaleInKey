package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class InstrumentTuningTest {

    @Test
    fun `guitar standard tuning pitch classes`() {
        assertEquals(listOf(4, 9, 2, 7, 11, 4), InstrumentTunings.GUITAR.openStringPitchClasses)
    }

    @Test
    fun `ukulele standard tuning pitch classes`() {
        assertEquals(listOf(7, 0, 4, 9), InstrumentTunings.UKULELE.openStringPitchClasses)
    }

    @Test
    fun `bass standard tuning pitch classes`() {
        assertEquals(listOf(4, 9, 2, 7), InstrumentTunings.BASS.openStringPitchClasses)
    }

    @Test
    fun `fretPitchClass wraps around the octave`() {
        // Low E string, 3rd fret -> G
        assertEquals(7, fretPitchClass(InstrumentTunings.GUITAR, stringIndex = 0, fret = 3))
        // A string, 2nd fret -> B
        assertEquals(11, fretPitchClass(InstrumentTunings.GUITAR, stringIndex = 1, fret = 2))
        // High E string, 12th fret -> back to E
        assertEquals(4, fretPitchClass(InstrumentTunings.GUITAR, stringIndex = 5, fret = 12))
    }

    @Test
    fun `fretMidiNote gives real absolute pitch, not just pitch class`() {
        // Low E string (E2=40), 3rd fret -> G2, not just pitch class 7
        assertEquals(43, fretMidiNote(InstrumentTunings.GUITAR, stringIndex = 0, fret = 3))
        // High E string (E4=64), 12th fret -> E5
        assertEquals(76, fretMidiNote(InstrumentTunings.GUITAR, stringIndex = 5, fret = 12))
    }

    @Test
    fun `assignAscendingMidiNotes voices a C major triad ascending from middle C`() {
        // C, E, G pitch classes starting at MIDI 60 (C4) -> C4 E4 G4
        assertEquals(listOf(60, 64, 67), assignAscendingMidiNotes(listOf(0, 4, 7), baseMidiNote = 60))
    }

    @Test
    fun `assignAscendingMidiNotes wraps to the next octave when a pitch class is lower`() {
        // Starting on A (9) then C (0): C must jump up an octave to stay ascending.
        assertEquals(listOf(60, 69, 72), assignAscendingMidiNotes(listOf(0, 9, 0), baseMidiNote = 60))
    }
}
