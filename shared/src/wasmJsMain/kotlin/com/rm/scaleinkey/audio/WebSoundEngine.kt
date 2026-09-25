package com.rm.scaleinkey.audio

import com.rm.scaleinkey.music.InstrumentType

/**
 * The browser's [SoundEngine]: a thin shell over sikSound in index.html, which parses the SoundFont
 * and plays it through Web Audio. The on/off switch is kept in localStorage, like Android's
 * SharedPreferences; a loaded soundfont is kept by sikSound in IndexedDB.
 */
class WebSoundEngine : SoundEngine {
    override var enabled: Boolean = loadEnabled()
        private set

    override fun setEnabled(value: Boolean) {
        enabled = value
        saveEnabled(value)
        if (!value) stopAll()
    }

    override fun playNote(instrument: InstrumentType, midiNote: Int) {
        if (enabled) play(instrument.name, midiNote.toString(), 0.9f, SoundEngine.NOTE_PREVIEW_MS.toInt())
    }

    override fun playChord(instrument: InstrumentType, midiNotes: List<Int>, durationMs: Long) {
        if (enabled && midiNotes.isNotEmpty()) {
            play(instrument.name, midiNotes.joinToString(","), 0.85f, durationMs.toInt())
        }
    }

    override fun resetToDefault(onResult: (Boolean) -> Unit) = resetSoundFont(onResult)
}

private fun play(instrument: String, notes: String, velocity: Float, durationMs: Int): Unit =
    js("sikSound.play(instrument, notes, velocity, durationMs)")

private fun stopAll(): Unit = js("sikSound.stopAll()")

private fun resetSoundFont(onResult: (Boolean) -> Unit): Unit = js("sikSound.resetToDefault(onResult)")

private fun loadEnabled(): Boolean = js("(() => { try { return localStorage.getItem('sound_enabled') !== 'false'; } catch (e) { return true; } })()")

private fun saveEnabled(value: Boolean): Unit = js("(() => { try { localStorage.setItem('sound_enabled', String(value)); } catch (e) {} })()")
