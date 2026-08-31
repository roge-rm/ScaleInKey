package com.rm.scaleinkey.music

enum class ChordVoicing { TRIAD, SEVENTH }

data class ChordVoicingDisplay(
    val romanNumeral: String,
    val symbol: String,
    val orderedTones: List<Note>,
)

fun DiatonicChord.display(voicing: ChordVoicing): ChordVoicingDisplay = when (voicing) {
    ChordVoicing.TRIAD -> ChordVoicingDisplay(
        romanNumeral = triadRomanNumeral,
        symbol = triadSymbol(),
        orderedTones = tones.take(3),
    )
    ChordVoicing.SEVENTH -> ChordVoicingDisplay(
        romanNumeral = romanNumeral,
        symbol = symbol(),
        orderedTones = tones,
    )
}
