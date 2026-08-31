package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class ChordVoicingTest {

    private fun names(notes: List<Note>) = notes.map { it.displayName() }

    @Test
    fun `C major I chord as triad and seventh`() {
        val chord = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.IONIAN))[0]

        val triad = chord.display(ChordVoicing.TRIAD)
        assertEquals("I", triad.romanNumeral)
        assertEquals("C", triad.symbol)
        assertEquals(listOf("C", "E", "G"), names(triad.orderedTones))

        val seventh = chord.display(ChordVoicing.SEVENTH)
        assertEquals("Imaj7", seventh.romanNumeral)
        assertEquals("Cmaj7", seventh.symbol)
        assertEquals(listOf("C", "E", "G", "B"), names(seventh.orderedTones))
    }

    @Test
    fun `C major vii is a diminished triad with lowercase roman and degree symbol`() {
        val chord = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.IONIAN))[6]

        assertEquals(TriadQuality.DIMINISHED, chord.triadQuality)
        assertEquals("Bdim", chord.triadSymbol())
        assertEquals("vii°", chord.triadRomanNumeral)
    }

    @Test
    fun `C melodic minor bIII is an augmented triad`() {
        val chord = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.MELODIC_MINOR))[2]

        assertEquals(TriadQuality.AUGMENTED, chord.triadQuality)
        assertEquals("E♭aug", chord.triadSymbol())
        assertEquals("III+", chord.triadRomanNumeral)
    }
}
