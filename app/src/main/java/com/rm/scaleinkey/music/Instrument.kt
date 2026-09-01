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
    val BASS_5 = StringInstrumentTuning(
        label = "Bass (5-string)",
        // Low B0, then the same E1, A1, D2, G2 as 4-string BASS — a fifth-lower B string added
        // below, not a re-tuning of the existing four.
        openStringMidiNotes = listOf(23, 28, 33, 38, 43),
    )
}

fun fretMidiNote(tuning: StringInstrumentTuning, stringIndex: Int, fret: Int): Int =
    tuning.openStringMidiNotes[stringIndex] + fret

fun fretPitchClass(tuning: StringInstrumentTuning, stringIndex: Int, fret: Int): Int =
    Math.floorMod(fretMidiNote(tuning, stringIndex, fret), 12)

private const val SCALE_BOX_FRET_COUNT = 4

/**
 * Narrows a tuning to the open-position scale-box window (frets 0-4) for fingering-chart mode.
 * v1 simplification: always the open position, not a CAGED-style multi-position finder.
 */
fun StringInstrumentTuning.scaleBoxWindow(): StringInstrumentTuning = copy(fretCount = SCALE_BOX_FRET_COUNT)

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

// Two octaves below the default preview register (60/C4) — lands Bass previews around C2,
// close to the real Bass tuning's own open strings (E1..G2, MIDI 28-43), so a tap-to-preview
// actually sounds bassy instead of sharing the same middle-C-ish register as every other
// instrument's preview.
private const val BASS_PREVIEW_BASE_MIDI_NOTE = 36

/**
 * MIDI notes for a tap-to-preview playback, tailored per instrument. A bassist doesn't strum a
 * full chord, so Bass previews collapse to just the root pitch class ([pitchClasses]' first
 * entry — always the chord/scale root by convention at every call site) transposed down into a
 * real bass register; every other instrument gets the normal ascending voicing near middle C.
 */
fun previewMidiNotes(instrument: InstrumentType, pitchClasses: List<Int>): List<Int> {
    if (pitchClasses.isEmpty()) return emptyList()
    return if (instrument == InstrumentType.BASS) {
        assignAscendingMidiNotes(listOf(pitchClasses.first()), baseMidiNote = BASS_PREVIEW_BASE_MIDI_NOTE)
    } else {
        assignAscendingMidiNotes(pitchClasses)
    }
}
