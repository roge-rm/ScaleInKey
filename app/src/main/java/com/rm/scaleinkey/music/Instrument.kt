package com.rm.scaleinkey.music

enum class InstrumentType { PIANO, GUITAR, UKULELE, BASS }

data class StringInstrumentTuning(
    val label: String,
    val openStringMidiNotes: List<Int>,
    val fretCount: Int = 12,
) {
    val openStringPitchClasses: List<Int> get() = openStringMidiNotes.map { Math.floorMod(it, 12) }
}

object InstrumentTunings {
    val GUITAR = StringInstrumentTuning(
        label = "Guitar",
        // Low E2, A2, D3, G3, B3, high E4
        openStringMidiNotes = listOf(40, 45, 50, 55, 59, 64),
    )
    val UKULELE = StringInstrumentTuning(
        label = "Ukulele",
        // Standard reentrant tuning G4, C4, E4, A4
        openStringMidiNotes = listOf(67, 60, 64, 69),
    )
    val BASS = StringInstrumentTuning(
        label = "Bass",
        // Low E1, A1, D2, G2
        openStringMidiNotes = listOf(28, 33, 38, 43),
    )
}

fun fretMidiNote(tuning: StringInstrumentTuning, stringIndex: Int, fret: Int): Int =
    tuning.openStringMidiNotes[stringIndex] + fret

fun fretPitchClass(tuning: StringInstrumentTuning, stringIndex: Int, fret: Int): Int =
    Math.floorMod(fretMidiNote(tuning, stringIndex, fret), 12)

data class PianoKey(
    val pitchClass: Int,
    val isBlack: Boolean,
    val keyIndex: Int,
    val midiNote: Int,
)

private val BLACK_KEY_PITCH_CLASSES = setOf(1, 3, 6, 8, 10)

fun buildPianoKeys(octaves: Int = 2, startMidiNote: Int = 60): List<PianoKey> {
    val totalKeys = octaves * 12
    return (0 until totalKeys).map { keyIndex ->
        val midiNote = startMidiNote + keyIndex
        val pitchClass = Math.floorMod(midiNote, 12)
        PianoKey(
            pitchClass = pitchClass,
            isBlack = pitchClass in BLACK_KEY_PITCH_CLASSES,
            keyIndex = keyIndex,
            midiNote = midiNote,
        )
    }
}

/**
 * Assigns each pitch class the lowest MIDI note at or above the previous one, producing a
 * strictly ascending voicing (e.g. for a chord or scale where only pitch classes are known).
 */
fun assignAscendingMidiNotes(pitchClasses: List<Int>, baseMidiNote: Int = 60): List<Int> {
    var previous = baseMidiNote - 1
    return pitchClasses.map { pitchClass ->
        val candidate = previous + 1 + Math.floorMod(pitchClass - (previous + 1), 12)
        previous = candidate
        candidate
    }
}
