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
    fun `every scale type produces 7 ascending-mod-12 degrees matching its interval formula`() {
        for (type in ScaleType.entries) {
            val notes = computeScaleNotes(Note(Letter.C, 0), type)
            assertEquals(type.intervals, notes.map { it.pitchClass })
        }
    }
}
