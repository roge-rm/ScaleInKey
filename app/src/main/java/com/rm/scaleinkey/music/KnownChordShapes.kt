package com.rm.scaleinkey.music

// Real, standard open-position chord shapes, keyed by root pitch class. One entry per string in
// tuning order (low to high); null = muted string. Scoped deliberately to the "vanilla" qualities
// players actually recognize (major/minor triads, dominant7/major7/minor7) and to the roots that
// have a genuine open-position shape — everything else (other roots, and the rarer diatonic
// qualities from harmonic/melodic minor etc.) falls back to the algorithmic shape-finder in
// ChordFingering.kt, which has no such gaps.
private typealias Frets = List<Int?>

private val GUITAR_MAJOR: Map<Int, Frets> = mapOf(
    0 to listOf(null, 3, 2, 0, 1, 0), // C
    2 to listOf(null, null, 0, 2, 3, 2), // D
    4 to listOf(0, 2, 2, 1, 0, 0), // E
    7 to listOf(3, 2, 0, 0, 0, 3), // G
    9 to listOf(null, 0, 2, 2, 2, 0), // A
)

private val GUITAR_MINOR: Map<Int, Frets> = mapOf(
    2 to listOf(null, null, 0, 2, 3, 1), // Dm
    4 to listOf(0, 2, 2, 0, 0, 0), // Em
    9 to listOf(null, 0, 2, 2, 1, 0), // Am
)

private val GUITAR_DOMINANT7: Map<Int, Frets> = mapOf(
    0 to listOf(null, 3, 2, 3, 1, 0), // C7
    2 to listOf(null, null, 0, 2, 1, 2), // D7
    4 to listOf(0, 2, 0, 1, 0, 0), // E7
    7 to listOf(3, 2, 0, 0, 0, 1), // G7
    9 to listOf(null, 0, 2, 0, 2, 0), // A7
    11 to listOf(null, 2, 1, 2, 0, 2), // B7
)

private val GUITAR_MAJOR7: Map<Int, Frets> = mapOf(
    0 to listOf(null, 3, 2, 0, 0, 0), // Cmaj7
    2 to listOf(null, null, 0, 2, 2, 2), // Dmaj7
    7 to listOf(3, 2, 0, 0, 0, 2), // Gmaj7
    9 to listOf(null, 0, 2, 1, 2, 0), // Amaj7
)

private val GUITAR_MINOR7: Map<Int, Frets> = mapOf(
    2 to listOf(null, null, 0, 2, 1, 1), // Dm7
    4 to listOf(0, 2, 0, 0, 0, 0), // Em7
    9 to listOf(null, 0, 2, 0, 1, 0), // Am7
)

private val UKULELE_MAJOR: Map<Int, Frets> = mapOf(
    0 to listOf(0, 0, 0, 3), // C
    2 to listOf(2, 2, 2, 0), // D
    5 to listOf(2, 0, 1, 0), // F
    7 to listOf(0, 2, 3, 2), // G
    9 to listOf(2, 1, 0, 0), // A
)

private val UKULELE_MINOR: Map<Int, Frets> = mapOf(
    2 to listOf(2, 2, 1, 0), // Dm
    4 to listOf(0, 4, 3, 2), // Em
    9 to listOf(2, 0, 0, 0), // Am
)

private val UKULELE_DOMINANT7: Map<Int, Frets> = mapOf(
    0 to listOf(0, 0, 0, 1), // C7
    2 to listOf(2, 2, 2, 3), // D7
    7 to listOf(0, 2, 1, 2), // G7
    9 to listOf(0, 1, 0, 0), // A7
)

private val UKULELE_MAJOR7: Map<Int, Frets> = mapOf(
    0 to listOf(0, 0, 0, 2), // Cmaj7
)

private data class KnownShapeTable(
    val triad: Map<TriadQuality, Map<Int, Frets>>,
    val seventh: Map<ChordQuality, Map<Int, Frets>>,
)

private val GUITAR_KNOWN_SHAPES = KnownShapeTable(
    triad = mapOf(TriadQuality.MAJOR to GUITAR_MAJOR, TriadQuality.MINOR to GUITAR_MINOR),
    seventh = mapOf(
        ChordQuality.DOMINANT7 to GUITAR_DOMINANT7,
        ChordQuality.MAJOR7 to GUITAR_MAJOR7,
        ChordQuality.MINOR7 to GUITAR_MINOR7,
    ),
)

private val UKULELE_KNOWN_SHAPES = KnownShapeTable(
    triad = mapOf(TriadQuality.MAJOR to UKULELE_MAJOR, TriadQuality.MINOR to UKULELE_MINOR),
    seventh = mapOf(
        ChordQuality.DOMINANT7 to UKULELE_DOMINANT7,
        ChordQuality.MAJOR7 to UKULELE_MAJOR7,
    ),
)

private fun knownShapeTableFor(tuning: StringInstrumentTuning): KnownShapeTable? = when (tuning) {
    InstrumentTunings.GUITAR -> GUITAR_KNOWN_SHAPES
    InstrumentTunings.UKULELE -> UKULELE_KNOWN_SHAPES
    else -> null
}

/**
 * A known, real-world fret pattern for [chordTones] on [tuning], if one exists — null falls
 * through to the algorithmic search. Quality is re-derived from [chordTones]' own intervals
 * (mirroring how [DiatonicChord.triadQuality]/[DiatonicChord.quality] are computed), so this
 * needs no extra parameter beyond what [findChordShape] already receives.
 */
internal fun findKnownShapeFrets(tuning: StringInstrumentTuning, chordTones: List<Note>, rootPitchClass: Int): Frets? {
    val table = knownShapeTableFor(tuning) ?: return null
    if (chordTones.isEmpty()) return null
    val third = Math.floorMod(chordTones[1].pitchClass - chordTones[0].pitchClass, 12)
    val fifth = Math.floorMod(chordTones[2].pitchClass - chordTones[0].pitchClass, 12)
    return when (chordTones.size) {
        3 -> table.triad[TriadQuality.fromIntervals(third, fifth)]?.get(rootPitchClass)
        4 -> {
            val seventh = Math.floorMod(chordTones[3].pitchClass - chordTones[0].pitchClass, 12)
            table.seventh[ChordQuality.fromIntervals(third, fifth, seventh)]?.get(rootPitchClass)
        }
        else -> null
    }
}
