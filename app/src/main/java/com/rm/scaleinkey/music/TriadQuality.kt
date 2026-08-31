package com.rm.scaleinkey.music

enum class TriadQuality(val symbolSuffix: String, val romanSuffix: String, val upperCaseRoman: Boolean) {
    MAJOR("", "", true),
    MINOR("m", "", false),
    DIMINISHED("dim", "°", false),
    AUGMENTED("aug", "+", true);

    companion object {
        /** Classifies a triad from the semitone offsets of its third and fifth above the root. */
        fun fromIntervals(third: Int, fifth: Int): TriadQuality = when {
            third == 4 && fifth == 7 -> MAJOR
            third == 3 && fifth == 7 -> MINOR
            third == 3 && fifth == 6 -> DIMINISHED
            third == 4 && fifth == 8 -> AUGMENTED
            else -> throw IllegalArgumentException("Unsupported triad interval combination: third=$third fifth=$fifth")
        }
    }
}
