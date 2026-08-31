package com.rm.scaleinkey.music

data class DiatonicChord(
    val degree: Int,
    val root: Note,
    val quality: ChordQuality,
    val tones: List<Note>,
    val romanNumeral: String,
) {
    fun symbol(): String = "${root.displayName()}${quality.symbolSuffix}"

    val triadQuality: TriadQuality by lazy {
        val third = Math.floorMod(tones[1].pitchClass - tones[0].pitchClass, 12)
        val fifth = Math.floorMod(tones[2].pitchClass - tones[0].pitchClass, 12)
        TriadQuality.fromIntervals(third, fifth)
    }

    fun triadSymbol(): String = "${root.displayName()}${triadQuality.symbolSuffix}"

    val triadRomanNumeral: String by lazy {
        val base = ROMAN_BASE[degree].let { if (triadQuality.upperCaseRoman) it else it.lowercase() }
        "$base${triadQuality.romanSuffix}"
    }
}

private val ROMAN_BASE = listOf("I", "II", "III", "IV", "V", "VI", "VII")

fun buildDiatonicChords(scale: Scale): List<DiatonicChord> {
    val notes = scale.notes
    return (0 until 7).map { degree ->
        val root = notes[degree]
        val third = notes[(degree + 2) % 7]
        val fifth = notes[(degree + 4) % 7]
        val seventh = notes[(degree + 6) % 7]

        val thirdInterval = Math.floorMod(third.pitchClass - root.pitchClass, 12)
        val fifthInterval = Math.floorMod(fifth.pitchClass - root.pitchClass, 12)
        val seventhInterval = Math.floorMod(seventh.pitchClass - root.pitchClass, 12)

        val quality = ChordQuality.fromIntervals(thirdInterval, fifthInterval, seventhInterval)
        val base = ROMAN_BASE[degree].let { if (quality.upperCaseRoman) it else it.lowercase() }

        DiatonicChord(
            degree = degree,
            root = root,
            quality = quality,
            tones = listOf(root, third, fifth, seventh),
            romanNumeral = "$base${quality.romanSuffix}",
        )
    }
}
