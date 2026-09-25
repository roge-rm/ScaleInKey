package com.rm.scaleinkey.music

data class Note(val letter: Letter, val accidental: Int) {
    val pitchClass: Int
        get() = (letter.naturalPitchClass + accidental).mod(12)

    fun displayName(): String {
        val symbol = when (accidental) {
            -2 -> "♭♭"
            -1 -> "♭"
            0 -> ""
            1 -> "♯"
            2 -> "x"
            else -> if (accidental < 0) "♭".repeat(-accidental) else "♯".repeat(accidental)
        }
        return "${letter.name}$symbol"
    }
}
