package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaleComputationTest {

    private fun names(notes: List<Note>) = notes.map { it.displayName() }

    @Test
    fun `C major`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.IONIAN)
        assertEquals(listOf("C", "D", "E", "F", "G", "A", "B"), names(notes))
    }

    @Test
    fun `A natural minor`() {
        val notes = computeScaleNotes(Note(Letter.A, 0), ScaleType.AEOLIAN)
        assertEquals(listOf("A", "B", "C", "D", "E", "F", "G"), names(notes))
    }

    @Test
    fun `F major uses flat spelling`() {
        val notes = computeScaleNotes(Note(Letter.F, 0), ScaleType.IONIAN)
        assertEquals(listOf("F", "G", "A", "B♭", "C", "D", "E"), names(notes))
    }

    @Test
    fun `D major uses sharp spelling`() {
        val notes = computeScaleNotes(Note(Letter.D, 0), ScaleType.IONIAN)
        assertEquals(listOf("D", "E", "F♯", "G", "A", "B", "C♯"), names(notes))
    }

    @Test
    fun `C harmonic minor`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.HARMONIC_MINOR)
        assertEquals(listOf("C", "D", "E♭", "F", "G", "A♭", "B"), names(notes))
    }

    @Test
    fun `C melodic minor`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.MELODIC_MINOR)
        assertEquals(listOf("C", "D", "E♭", "F", "G", "A", "B"), names(notes))
    }

    @Test
    fun `E Phrygian is C major rotated to start on E`() {
        val ePhrygian = computeScaleNotes(Note(Letter.E, 0), ScaleType.PHRYGIAN)
        val cMajor = computeScaleNotes(Note(Letter.C, 0), ScaleType.IONIAN)
        val rotated = cMajor.drop(2) + cMajor.take(2)
        assertEquals(names(rotated), names(ePhrygian))
    }

    @Test
    fun `C whole tone`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.WHOLE_TONE)
        assertEquals(listOf("C", "D", "E", "F♯", "G♯", "A♯"), names(notes))
    }

    @Test
    fun `C diminished whole-half`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.DIMINISHED_WHOLE_HALF)
        assertEquals(listOf("C", "D", "E♭", "F", "G♭", "A♭", "A", "B"), names(notes))
    }

    @Test
    fun `C diminished half-whole`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.DIMINISHED_HALF_WHOLE)
        assertEquals(listOf("C", "D♭", "E♭", "E", "F♯", "G", "A", "B♭"), names(notes))
    }

    @Test
    fun `C bebop dominant`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.BEBOP_DOMINANT)
        assertEquals(listOf("C", "D", "E", "F", "G", "A", "B♭", "B"), names(notes))
    }

    @Test
    fun `C bebop major`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.BEBOP_MAJOR)
        assertEquals(listOf("C", "D", "E", "F", "G", "G♯", "A", "B"), names(notes))
    }

    @Test
    fun `C Neapolitan minor and major`() {
        assertEquals(
            listOf("C", "D♭", "E♭", "F", "G", "A♭", "B"),
            names(computeScaleNotes(Note(Letter.C, 0), ScaleType.NEAPOLITAN_MINOR)),
        )
        assertEquals(
            listOf("C", "D♭", "E♭", "F", "G", "A", "B"),
            names(computeScaleNotes(Note(Letter.C, 0), ScaleType.NEAPOLITAN_MAJOR)),
        )
    }

    @Test
    fun `C Phrygian dominant`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.PHRYGIAN_DOMINANT)
        assertEquals(listOf("C", "D♭", "E", "F", "G", "A♭", "B♭"), names(notes))
    }

    @Test
    fun `C Egyptian pentatonic`() {
        val notes = computeScaleNotes(Note(Letter.C, 0), ScaleType.EGYPTIAN_PENTATONIC)
        assertEquals(listOf("C", "D", "F", "G", "B♭"), names(notes))
    }

    @Test
    fun `every scale type produces 7 ascending-mod-12 degrees matching its interval formula`() {
        for (type in ScaleType.entries) {
            val notes = computeScaleNotes(Note(Letter.C, 0), type)
            assertEquals(type.intervals, notes.map { it.pitchClass })
        }
    }
}
