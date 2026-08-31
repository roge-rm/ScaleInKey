package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewScaleTypesTest {

    private fun names(type: ScaleType, letter: Letter = Letter.C, accidental: Int = 0) =
        computeScaleNotes(Note(letter, accidental), type).map { it.displayName() }

    @Test
    fun `pentatonic and blues scales spell with the correct skipped letters`() {
        assertEquals(listOf("C", "D", "E", "G", "A"), names(ScaleType.MAJOR_PENTATONIC))
        assertEquals(listOf("C", "E♭", "F", "G", "B♭"), names(ScaleType.MINOR_PENTATONIC))
        assertEquals(listOf("C", "D", "E♭", "E", "G", "A"), names(ScaleType.MAJOR_BLUES))
        assertEquals(listOf("C", "E♭", "F", "G♭", "G", "B♭"), names(ScaleType.MINOR_BLUES))
    }

    @Test
    fun `Japanese pentatonic scales spell correctly`() {
        assertEquals(listOf("C", "D", "E♭", "G", "A♭"), names(ScaleType.HIRAJOSHI))
        assertEquals(listOf("C", "D♭", "F", "G", "B♭"), names(ScaleType.IN_SEN))
        assertEquals(listOf("C", "D♭", "F", "G♭", "B♭"), names(ScaleType.IWATO))
    }

    @Test
    fun `world exotic 7-note scales spell correctly`() {
        assertEquals(listOf("C", "D", "E♭", "F♯", "G", "A♭", "B"), names(ScaleType.HUNGARIAN_MINOR))
        assertEquals(listOf("C", "D♭", "E", "F", "G", "A♭", "B"), names(ScaleType.BYZANTINE))
        assertEquals(listOf("C", "D♭", "E", "F", "G♭", "A♭", "B"), names(ScaleType.PERSIAN))
        assertEquals(listOf("C", "D♭", "E", "F♯", "G♯", "A♯", "B"), names(ScaleType.ENIGMATIC))
    }

    @Test
    fun `jazz melodic-minor modes spell correctly`() {
        // Altered is famously spelled with an "Fb" (it's the 7th mode of Db melodic minor).
        assertEquals(listOf("C", "D♭", "E♭", "F♭", "G♭", "A♭", "B♭"), names(ScaleType.ALTERED))
        assertEquals(listOf("C", "D", "E", "F♯", "G", "A", "B♭"), names(ScaleType.LYDIAN_DOMINANT))
    }

    @Test
    fun `pentatonic, blues, and most world scales are marked as not supporting chords`() {
        val expectedNoChords = setOf(
            ScaleType.MAJOR_PENTATONIC, ScaleType.MINOR_PENTATONIC,
            ScaleType.MAJOR_BLUES, ScaleType.MINOR_BLUES,
            ScaleType.HUNGARIAN_MINOR, ScaleType.BYZANTINE, ScaleType.PERSIAN,
            ScaleType.HIRAJOSHI, ScaleType.IN_SEN, ScaleType.IWATO, ScaleType.ENIGMATIC,
        )
        for (type in expectedNoChords) {
            assertTrue("$type should not support chords", !type.supportsChords)
        }
        assertTrue(ScaleType.ALTERED.supportsChords)
        assertTrue(ScaleType.LYDIAN_DOMINANT.supportsChords)
    }

    @Test
    fun `every chord-supporting scale type builds 7 valid diatonic chords for every root`() {
        val chordSupportingTypes = ScaleType.entries.filter { it.supportsChords }
        for (type in chordSupportingTypes) {
            for (root in CANONICAL_ROOTS) {
                val chords = buildDiatonicChords(Scale(root, type))
                assertEquals("$type from ${root.displayName()}", 7, chords.size)
                // Building each chord's symbol()/triadSymbol() must not throw for any degree —
                // this is the real assertion: ChordQuality/TriadQuality classification succeeds.
                chords.forEach {
                    it.symbol()
                    it.triadSymbol()
                }
            }
        }
    }
}
