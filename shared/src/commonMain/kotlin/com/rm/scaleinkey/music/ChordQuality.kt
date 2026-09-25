package com.rm.scaleinkey.music

enum class ChordQuality(val symbolSuffix: String, val romanSuffix: String, val upperCaseRoman: Boolean) {
    MAJOR7("maj7", "maj7", true),
    DOMINANT7("7", "7", true),
    MINOR7("m7", "7", false),
    HALF_DIMINISHED7("m7♭5", "ø7", false),
    DIMINISHED7("dim7", "°7", false),
    MINOR_MAJOR7("m(maj7)", "(maj7)", false),
    AUGMENTED_MAJOR7("maj7♯5", "maj7♯5", true);

    companion object {
        /**
         * Classifies a stacked-third 7th chord from the semitone offsets of its
         * third, fifth, and seventh above the root. Scale-type-agnostic, so it works
         * for any mode or minor variant without special-casing.
         */
        fun fromIntervals(third: Int, fifth: Int, seventh: Int): ChordQuality = when {
            third == 4 && fifth == 7 && seventh == 11 -> MAJOR7
            third == 4 && fifth == 7 && seventh == 10 -> DOMINANT7
            third == 3 && fifth == 7 && seventh == 10 -> MINOR7
            third == 3 && fifth == 6 && seventh == 10 -> HALF_DIMINISHED7
            third == 3 && fifth == 6 && seventh == 9 -> DIMINISHED7
            third == 3 && fifth == 7 && seventh == 11 -> MINOR_MAJOR7
            third == 4 && fifth == 8 && seventh == 11 -> AUGMENTED_MAJOR7
            else -> throw IllegalArgumentException(
                "Unsupported chord tone combination: third=$third fifth=$fifth seventh=$seventh"
            )
        }
    }
}
