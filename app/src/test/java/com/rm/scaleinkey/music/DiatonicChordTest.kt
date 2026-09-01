package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class DiatonicChordTest {

    private fun symbols(chords: List<DiatonicChord>) = chords.map { it.symbol() }
    private fun romans(chords: List<DiatonicChord>) = chords.map { it.romanNumeral }

    @Test
    fun `C major diatonic 7th chords`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.IONIAN))
        assertEquals(
            listOf("Cmaj7", "Dm7", "Em7", "Fmaj7", "G7", "Am7", "Bm7♭5"),
            symbols(chords)
        )
        assertEquals(
            listOf("Imaj7", "ii7", "iii7", "IVmaj7", "V7", "vi7", "viiø7"),
            romans(chords)
        )
    }

    @Test
    fun `A natural minor diatonic 7th chords`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.A, 0), ScaleType.AEOLIAN))
        assertEquals(
            listOf("Am7", "Bm7♭5", "Cmaj7", "Dm7", "Em7", "Fmaj7", "G7"),
            symbols(chords)
        )
    }

    @Test
    fun `C harmonic minor i is minor-major7, V is dominant, vii is fully diminished`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.HARMONIC_MINOR))
        assertEquals("Cm(maj7)", chords[0].symbol())
        assertEquals(ChordQuality.MINOR_MAJOR7, chords[0].quality)
        assertEquals("G7", chords[4].symbol())
        assertEquals(ChordQuality.DOMINANT7, chords[4].quality)
        assertEquals("Bdim7", chords[6].symbol())
        assertEquals(ChordQuality.DIMINISHED7, chords[6].quality)
    }

    @Test
    fun `C melodic minor i is minor-major7, bIII is augmented major7, IV is dominant`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.MELODIC_MINOR))
        assertEquals("Cm(maj7)", chords[0].symbol())
        assertEquals(ChordQuality.MINOR_MAJOR7, chords[0].quality)
        assertEquals("E♭maj7♯5", chords[2].symbol())
        assertEquals(ChordQuality.AUGMENTED_MAJOR7, chords[2].quality)
        assertEquals("F7", chords[3].symbol())
        assertEquals(ChordQuality.DOMINANT7, chords[3].quality)
    }

    @Test
    fun `C Phrygian dominant I is dominant, bii is major7, v is minor7`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.PHRYGIAN_DOMINANT))
        assertEquals("C7", chords[0].symbol())
        assertEquals(ChordQuality.DOMINANT7, chords[0].quality)
        assertEquals("D♭maj7", chords[1].symbol())
        assertEquals(ChordQuality.MAJOR7, chords[1].quality)
        assertEquals("B♭m7", chords[6].symbol())
        assertEquals(ChordQuality.MINOR7, chords[6].quality)
    }

    @Test
    fun `C Lydian augmented I is augmented major7, ii is dominant`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.LYDIAN_AUGMENTED))
        assertEquals("Cmaj7♯5", chords[0].symbol())
        assertEquals(ChordQuality.AUGMENTED_MAJOR7, chords[0].quality)
        assertEquals("D7", chords[1].symbol())
        assertEquals(ChordQuality.DOMINANT7, chords[1].quality)
    }

    @Test
    fun `C Locrian natural 2 i is half-diminished, IV is minor7`() {
        val chords = buildDiatonicChords(Scale(Note(Letter.C, 0), ScaleType.LOCRIAN_NATURAL_2))
        assertEquals("Cm7♭5", chords[0].symbol())
        assertEquals(ChordQuality.HALF_DIMINISHED7, chords[0].quality)
        assertEquals("Fm7", chords[3].symbol())
        assertEquals(ChordQuality.MINOR7, chords[3].quality)
    }
}
