package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordFingeringTest {

    @Test
    fun `chord shapes are playable for every chord-supporting scale, root, degree, voicing, and fretted instrument`() {
        val chordSupportingTypes = ScaleType.entries.filter { it.supportsChords }
        val tunings = listOf(InstrumentTunings.GUITAR, InstrumentTunings.UKULELE)

        for (type in chordSupportingTypes) {
            for (root in CANONICAL_ROOTS) {
                val chords = buildDiatonicChords(Scale(root, type))
                for (chord in chords) {
                    for (voicing in ChordVoicing.entries) {
                        val tones = chord.display(voicing).orderedTones
                        val tonePcs = tones.map { it.pitchClass }.toSet()
                        for (tuning in tunings) {
                            val shape = findChordShape(tuning, tones, chord.root.pitchClass)
                            val context = "$type ${root.displayName()} degree=${chord.degree} $voicing ${tuning.label}"

                            val frettedNonOpen = shape.marks.filter { it.fret != null && it.fret != 0 }
                            if (frettedNonOpen.isNotEmpty()) {
                                val span = frettedNonOpen.maxOf { it.fret!! } - frettedNonOpen.minOf { it.fret!! }
                                assertTrue("$context: fret span $span > 3", span <= 3)
                            }
                            shape.marks.forEach { mark ->
                                mark.finger?.let { finger ->
                                    assertTrue("$context: finger $finger out of range", finger in 1..4)
                                }
                                mark.pitchClass?.let { pc ->
                                    assertTrue("$context: sounded pitch class $pc not a chord tone", pc in tonePcs)
                                }
                            }
                            val soundedPcs = shape.marks.mapNotNull { it.pitchClass }.toSet()
                            val coverage = soundedPcs.count { it in tonePcs }
                            val requiredCoverage = minOf(3, tonePcs.size)
                            assertTrue(
                                "$context: coverage $coverage below required $requiredCoverage " +
                                    "(tones=$tonePcs, shape=${shape.marks})",
                                coverage >= requiredCoverage,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `findChordShape is deterministic`() {
        val root = CANONICAL_ROOTS[0]
        val chord = buildDiatonicChords(Scale(root, ScaleType.HARMONIC_MINOR))[0]
        val tones = chord.display(ChordVoicing.SEVENTH).orderedTones
        val first = findChordShape(InstrumentTunings.GUITAR, tones, chord.root.pitchClass)
        val second = findChordShape(InstrumentTunings.GUITAR, tones, chord.root.pitchClass)
        assertEquals(first, second)
    }

    @Test
    fun `findBassRootPosition always sounds the requested root, for every root`() {
        for (root in CANONICAL_ROOTS) {
            val shape = findBassRootPosition(InstrumentTunings.BASS, root.pitchClass)
            val sounded = shape.marks.filter { it.fret != null }
            assertEquals("root=${root.displayName()}: expected exactly one sounded string", 1, sounded.size)
            val mark = sounded.single()
            assertEquals(root.pitchClass, mark.pitchClass)
            assertTrue(mark.isRoot)
        }
    }
}
