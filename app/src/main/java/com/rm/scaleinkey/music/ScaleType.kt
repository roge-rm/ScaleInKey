package com.rm.scaleinkey.music

enum class ScaleGroup(val displayName: String) {
    DIATONIC_MODE("Modes"),
    MINOR_VARIANT("Minor Variants"),
    PENTATONIC_BLUES("Pentatonic & Blues"),
    WORLD("World & Exotic"),
    JAZZ("Jazz"),
}

enum class ScaleType(
    val displayName: String,
    val intervals: List<Int>,
    val group: ScaleGroup,
    /**
     * Which natural-letter step (0-6, from the root's letter) each interval gets spelled with.
     * Null means "consecutive" (0,1,2,...) — correct for any full 7-note scale, since each
     * degree simply gets the next letter in order. Scales that skip degrees of a 7-note parent
     * (pentatonic, blues, and the other 5/6-note scales here) must spell each kept note with
     * the *letter that degree would have had*, not the next unused letter — e.g. C major
     * pentatonic is "C D E G A", not "C D E F G" — so those provide this explicitly.
     */
    val letterOffsets: List<Int>? = null,
    /**
     * Diatonic 7th-chord harmonization (stacking thirds) only produces chord qualities from a
     * small, named set (maj7, m7, dominant7, m7♭5, °7, m(maj7), maj7♯5) for genuinely diatonic
     * 7-note scales. Several of the scales below aren't harmonized that way in practice and
     * mechanically stacking thirds on them yields chords with no standard name (e.g. a triad
     * with a major third and a ♭5, or a "third" only 2 semitones wide) — verified by brute-force
     * computation across all 12 roots before adding these, not guessed. Those scales set this
     * false so the UI shows notes/diagrams only, without a chord row.
     */
    val supportsChords: Boolean = true,
) {
    IONIAN("Ionian (Major)", listOf(0, 2, 4, 5, 7, 9, 11), ScaleGroup.DIATONIC_MODE),
    DORIAN("Dorian", listOf(0, 2, 3, 5, 7, 9, 10), ScaleGroup.DIATONIC_MODE),
    PHRYGIAN("Phrygian", listOf(0, 1, 3, 5, 7, 8, 10), ScaleGroup.DIATONIC_MODE),
    LYDIAN("Lydian", listOf(0, 2, 4, 6, 7, 9, 11), ScaleGroup.DIATONIC_MODE),
    MIXOLYDIAN("Mixolydian", listOf(0, 2, 4, 5, 7, 9, 10), ScaleGroup.DIATONIC_MODE),
    AEOLIAN("Aeolian (Natural Minor)", listOf(0, 2, 3, 5, 7, 8, 10), ScaleGroup.DIATONIC_MODE),
    LOCRIAN("Locrian", listOf(0, 1, 3, 5, 6, 8, 10), ScaleGroup.DIATONIC_MODE),

    HARMONIC_MINOR("Harmonic Minor", listOf(0, 2, 3, 5, 7, 8, 11), ScaleGroup.MINOR_VARIANT),
    MELODIC_MINOR("Melodic Minor", listOf(0, 2, 3, 5, 7, 9, 11), ScaleGroup.MINOR_VARIANT),

    MAJOR_PENTATONIC(
        "Major Pentatonic", listOf(0, 2, 4, 7, 9), ScaleGroup.PENTATONIC_BLUES,
        letterOffsets = listOf(0, 1, 2, 4, 5), supportsChords = false,
    ),
    MINOR_PENTATONIC(
        "Minor Pentatonic", listOf(0, 3, 5, 7, 10), ScaleGroup.PENTATONIC_BLUES,
        letterOffsets = listOf(0, 2, 3, 4, 6), supportsChords = false,
    ),
    MAJOR_BLUES(
        "Major Blues", listOf(0, 2, 3, 4, 7, 9), ScaleGroup.PENTATONIC_BLUES,
        letterOffsets = listOf(0, 1, 2, 2, 4, 5), supportsChords = false,
    ),
    MINOR_BLUES(
        "Minor Blues", listOf(0, 3, 5, 6, 7, 10), ScaleGroup.PENTATONIC_BLUES,
        letterOffsets = listOf(0, 2, 3, 4, 4, 6), supportsChords = false,
    ),

    HUNGARIAN_MINOR(
        "Hungarian Minor", listOf(0, 2, 3, 6, 7, 8, 11), ScaleGroup.WORLD,
        supportsChords = false,
    ),
    BYZANTINE(
        "Byzantine", listOf(0, 1, 4, 5, 7, 8, 11), ScaleGroup.WORLD,
        supportsChords = false,
    ),
    PERSIAN(
        "Persian", listOf(0, 1, 4, 5, 6, 8, 11), ScaleGroup.WORLD,
        supportsChords = false,
    ),
    HIRAJOSHI(
        "Hirajoshi", listOf(0, 2, 3, 7, 8), ScaleGroup.WORLD,
        letterOffsets = listOf(0, 1, 2, 4, 5), supportsChords = false,
    ),
    IN_SEN(
        "In Sen", listOf(0, 1, 5, 7, 10), ScaleGroup.WORLD,
        letterOffsets = listOf(0, 1, 3, 4, 6), supportsChords = false,
    ),
    IWATO(
        "Iwato", listOf(0, 1, 5, 6, 10), ScaleGroup.WORLD,
        letterOffsets = listOf(0, 1, 3, 4, 6), supportsChords = false,
    ),
    ENIGMATIC(
        "Enigmatic", listOf(0, 1, 4, 6, 8, 10, 11), ScaleGroup.WORLD,
        supportsChords = false,
    ),

    ALTERED("Altered", listOf(0, 1, 3, 4, 6, 8, 10), ScaleGroup.JAZZ),
    LYDIAN_DOMINANT("Lydian Dominant", listOf(0, 2, 4, 6, 7, 9, 10), ScaleGroup.JAZZ),
    ;

    // displayName with any parenthetical qualifier stripped, for compact UI (e.g. dropdown fields).
    val shortDisplayName: String get() = displayName.substringBefore(" (")
}
