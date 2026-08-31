package com.rm.scaleinkey.music

/**
 * One vertical string position within a [ChordShape]. `fret == null` means the string is muted;
 * `fret == 0` means open; a positive fret is an absolute fret number on the neck.
 */
data class StringMark(
    val stringIndex: Int,
    val fret: Int?,
    val pitchClass: Int?,
    val isRoot: Boolean,
    val finger: Int?,
)

/**
 * One mark per string, ordered by stringIndex (== the tuning's string order). [startFret] is the
 * fret immediately below the diagram's fretted range: 0 means an open-position shape (frets 0-4,
 * open strings allowed); any other value S means a movable shape spanning frets S+1..S+4 with no
 * open strings, conventionally labeled "(S+1)fr" alongside the diagram.
 */
data class ChordShape(val marks: List<StringMark>, val startFret: Int)

// Four fretted finger positions per window (span <= 3 among them, so they always map to fingers
// 1-4). startFret == 0 additionally allows the open string.
private const val WINDOW_FRETS = 4

private fun allowedFretsFor(startFret: Int): List<Int> =
    if (startFret == 0) listOf(0, 1, 2, 3, 4) else (startFret + 1..startFret + WINDOW_FRETS).toList()

private fun fingerFor(fret: Int, startFret: Int): Int? = if (fret == 0) null else fret - startFret

/**
 * Finds a playable near-the-nut voicing of [chordTones] on [tuning], picking one fret (or mute)
 * per string. Tries a real, standard shape first (see KnownChordShapes.kt — the common ~dozen
 * open chords players actually recognize, e.g. the classic open C = x32010); only chords with no
 * known shape (an unlisted root, or a rarer quality like an augmented-major7 from harmonic minor)
 * fall through to the algorithmic search below, which has no such gaps but doesn't always land on
 * a "textbook" fingering. Coverage requirements are relaxed and the search window moves up the
 * neck before ever falling back to [findBassRootPosition]'s single-note shape, so this always
 * returns something.
 */
fun findChordShape(tuning: StringInstrumentTuning, chordTones: List<Note>, rootPitchClass: Int): ChordShape {
    findKnownShapeFrets(tuning, chordTones, rootPitchClass)?.let { frets ->
        return buildShapeFromFrets(tuning, frets, rootPitchClass)
    }

    val tonePcs = chordTones.map { it.pitchClass }.toSet()
    if (tonePcs.isEmpty()) return findBassRootPosition(tuning, rootPitchClass)

    val coverageLevels = listOf(minOf(3, tonePcs.size), 2, 1).distinct().filter { it in 1..tonePcs.size }
    for (requiredCoverage in coverageLevels) {
        for (startFret in 0..(tuning.fretCount - WINDOW_FRETS)) {
            val shape = bestShapeInWindow(tuning, tonePcs, rootPitchClass, startFret, requiredCoverage)
            if (shape != null) return shape
        }
    }
    return findBassRootPosition(tuning, rootPitchClass)
}

/** Turns a literal fret pattern (as stored in KnownChordShapes.kt) into a [ChordShape]. */
private fun buildShapeFromFrets(tuning: StringInstrumentTuning, frets: List<Int?>, rootPitchClass: Int): ChordShape {
    val minFretted = frets.filterNotNull().filter { it > 0 }.minOrNull() ?: 0
    val startFret = if (minFretted <= WINDOW_FRETS) 0 else minFretted - 1
    return ChordShape(
        marks = frets.mapIndexed { s, fret ->
            if (fret == null) {
                StringMark(s, null, null, isRoot = false, finger = null)
            } else {
                val pitchClass = fretPitchClass(tuning, s, fret)
                StringMark(s, fret, pitchClass, isRoot = pitchClass == rootPitchClass, finger = fingerFor(fret, startFret))
            }
        },
        startFret = startFret,
    )
}

private fun bestShapeInWindow(
    tuning: StringInstrumentTuning,
    tonePcs: Set<Int>,
    rootPitchClass: Int,
    startFret: Int,
    requiredCoverage: Int,
): ChordShape? {
    val numStrings = tuning.openStringMidiNotes.size
    val allowedFrets = allowedFretsFor(startFret)

    val candidatesPerString: List<List<Int?>> = (0 until numStrings).map { s ->
        buildList {
            add(null) // mute is always an option
            for (fret in allowedFrets) {
                if (fretPitchClass(tuning, s, fret) in tonePcs) add(fret)
            }
        }
    }

    var best: List<Int?>? = null
    var bestScore = Int.MIN_VALUE

    // Exhaustive enumeration in a fixed order, keeping the first-seen max score on ties, so the
    // result is deterministic across runs.
    fun recurse(stringIdx: Int, combo: MutableList<Int?>) {
        if (stringIdx == numStrings) {
            val score = scoreCombo(tuning, combo, tonePcs, rootPitchClass, requiredCoverage) ?: return
            if (score > bestScore) {
                bestScore = score
                best = combo.toList()
            }
            return
        }
        for (candidate in candidatesPerString[stringIdx]) {
            combo.add(candidate)
            recurse(stringIdx + 1, combo)
            combo.removeAt(combo.lastIndex)
        }
    }
    recurse(0, mutableListOf())

    val chosen = best ?: return null
    return ChordShape(
        marks = (0 until numStrings).map { s ->
            val fret = chosen[s]
            if (fret == null) {
                StringMark(s, null, null, isRoot = false, finger = null)
            } else {
                val pitchClass = fretPitchClass(tuning, s, fret)
                StringMark(s, fret, pitchClass, isRoot = pitchClass == rootPitchClass, finger = fingerFor(fret, startFret))
            }
        },
        startFret = startFret,
    )
}

/** Null if this combination fails a hard filter; otherwise a score to maximize. */
private fun scoreCombo(
    tuning: StringInstrumentTuning,
    combo: List<Int?>,
    tonePcs: Set<Int>,
    rootPitchClass: Int,
    requiredCoverage: Int,
): Int? {
    val sounded = combo.withIndex().mapNotNull { (s, f) -> f?.let { s to it } }
    if (sounded.isEmpty()) return null

    val frettedNonOpen = sounded.filter { it.second != 0 }
    val span = if (frettedNonOpen.isEmpty()) 0 else frettedNonOpen.maxOf { it.second } - frettedNonOpen.minOf { it.second }
    if (span > 3) return null

    val soundedPcs = sounded.map { (s, f) -> fretPitchClass(tuning, s, f) }
    val coverage = soundedPcs.toSet().count { it in tonePcs }
    if (coverage < requiredCoverage) return null

    // Compare by absolute MIDI pitch, not string index: ukulele's tuning is reentrant
    // (openStringMidiNotes[0] is not the lowest-pitched string), so "the lowest sounded string"
    // can't be assumed from string order.
    val lowest = sounded.minBy { (s, f) -> fretMidiNote(tuning, s, f) }
    val rootIsLowest = fretPitchClass(tuning, lowest.first, lowest.second) == rootPitchClass
    val rootSoundedAnywhere = soundedPcs.any { it == rootPitchClass }
    val mutedCount = combo.size - sounded.size
    val openCount = sounded.count { (_, f) -> f == 0 }

    return coverage * 1000 - mutedCount * 50 - span * 10 +
        (if (rootIsLowest) 300 else 0) + (if (rootSoundedAnywhere) 100 else 0) + openCount * 5
}

/**
 * Nearest single fret sounding [rootPitchClass] on [tuning] — used for Bass's chord-mode display,
 * since bass isn't played as strummed chord shapes. Prefers the smallest fret (open strings win),
 * tie-broken by lowest absolute pitch.
 */
fun findBassRootPosition(tuning: StringInstrumentTuning, rootPitchClass: Int): ChordShape {
    val numStrings = tuning.openStringMidiNotes.size
    for (fret in 0..tuning.fretCount) {
        val matches = (0 until numStrings).filter { s -> fretPitchClass(tuning, s, fret) == rootPitchClass }
        if (matches.isEmpty()) continue

        val chosen = matches.minBy { s -> fretMidiNote(tuning, s, fret) }
        val startFret = if (fret <= WINDOW_FRETS) 0 else fret - 1
        return ChordShape(
            marks = (0 until numStrings).map { s ->
                if (s == chosen) {
                    StringMark(s, fret, rootPitchClass, isRoot = true, finger = fingerFor(fret, startFret))
                } else {
                    StringMark(s, null, null, isRoot = false, finger = null)
                }
            },
            startFret = startFret,
        )
    }
    error("unreachable: fretCount+1 consecutive semitones on one string always cover all 12 pitch classes")
}
