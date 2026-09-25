package com.rm.scaleinkey.audio

import com.rm.scaleinkey.music.InstrumentType

/**
 * Plays short note/chord previews from a SoundFont: the bundled default or one the user loaded.
 * Android renders through the native Oboe/TinySoundFont engine, the browser through Web Audio.
 * Construct once and share: the UI reaches it through LocalSoundEngine.
 */
interface SoundEngine {
    val enabled: Boolean
    fun setEnabled(value: Boolean)
    fun playNote(instrument: InstrumentType, midiNote: Int)
    fun playChord(instrument: InstrumentType, midiNotes: List<Int>, durationMs: Long = CHORD_PREVIEW_MS)
    fun resetToDefault(onResult: (Boolean) -> Unit)

    companion object {
        const val NOTE_PREVIEW_MS = 700L
        const val CHORD_PREVIEW_MS = 1000L
    }
}
