package com.rm.scaleinkey.music

data class Scale(val root: Note, val type: ScaleType) {
    val notes: List<Note> by lazy { computeScaleNotes(root, type) }
}

/**
 * Spells each scale degree using a letter-cycle + accidental-diff approach so results
 * match conventional key spellings (e.g. F major -> Bb not A#, D major -> F# not Gb).
 */
fun computeScaleNotes(root: Note, type: ScaleType): List<Note> {
    val letters = Letter.entries
    val letterOffsets = type.letterOffsets ?: type.intervals.indices.toList()
    return type.intervals.mapIndexed { i, interval ->
        val letter = letters[(root.letter.ordinal + letterOffsets[i]) % 7]
        val targetPitchClass = Math.floorMod(root.pitchClass + interval, 12)
        val accidental = normalizeAccidental(targetPitchClass - letter.naturalPitchClass)
        Note(letter, accidental)
    }
}

private fun normalizeAccidental(rawDiff: Int): Int {
    var d = Math.floorMod(rawDiff, 12)
    if (d > 6) d -= 12
    return d
}
