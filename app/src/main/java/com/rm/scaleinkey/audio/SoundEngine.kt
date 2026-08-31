package com.rm.scaleinkey.audio

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.rm.scaleinkey.music.InstrumentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Loads a SoundFont (bundled default or a user-picked .sf2) into the native Oboe/TinySoundFont
 * engine ([NativeSoundEngine]) and plays short note/chord previews. All rendering and real-time
 * audio work happens in native code — see native_sound_engine.cpp for why. Lives for the app
 * process lifetime; construct once and share.
 */
class SoundEngine(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var pendingStopJob: Job? = null
    @Volatile private var loaded = false

    @Volatile var enabled: Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        private set

    @Volatile var usingCustomSoundFont: Boolean = false
        private set

    private var pianoPreset = 0
    private var guitarPreset = 0
    private var bassPreset = 0

    init {
        scope.launch {
            val customUri = prefs.getString(KEY_CUSTOM_SF2_URI, null)
            val loadedCustom = customUri?.let { loadFromUriInternal(Uri.parse(it), persist = false) } ?: false
            if (!loadedCustom) {
                loadDefaultInternal()
            }
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        if (!value) {
            NativeSoundEngine.nativeStop()
        } else if (loaded) {
            NativeSoundEngine.nativeStart()
        }
    }

    fun playNote(instrument: InstrumentType, midiNote: Int) {
        triggerNotes(instrument, listOf(midiNote), 0.9f, NOTE_PREVIEW_MS)
    }

    fun playChord(instrument: InstrumentType, midiNotes: List<Int>) {
        if (midiNotes.isEmpty()) return
        triggerNotes(instrument, midiNotes, 0.85f, CHORD_PREVIEW_MS)
    }

    private fun triggerNotes(instrument: InstrumentType, midiNotes: List<Int>, velocity: Float, previewMs: Long) {
        if (!enabled || !loaded) return
        val preset = presetFor(instrument)
        if (!NativeSoundEngine.nativeStart()) return
        pendingStopJob?.cancel()
        midiNotes.forEach { NativeSoundEngine.nativeNoteOn(preset, it, velocity) }
        pendingStopJob = scope.launch {
            delay(previewMs)
            midiNotes.forEach { NativeSoundEngine.nativeNoteOff(preset, it) }
        }
    }

    /** Reads and loads a user-picked .sf2 file, persisting it as the chosen soundfont. */
    fun loadFromUri(uri: Uri, onResult: (Boolean) -> Unit) {
        scope.launch {
            val ok = loadFromUriInternal(uri, persist = true)
            onResult(ok)
        }
    }

    fun resetToDefault(onResult: (Boolean) -> Unit) {
        scope.launch {
            prefs.edit().remove(KEY_CUSTOM_SF2_URI).apply()
            val ok = loadDefaultInternal()
            onResult(ok)
        }
    }

    private fun loadFromUriInternal(uri: Uri, persist: Boolean): Boolean {
        return try {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return false
            val ok = loadBytes(bytes)
            if (ok) {
                usingCustomSoundFont = true
                if (persist) {
                    try {
                        appContext.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Could not persist URI permission", e)
                    }
                    prefs.edit().putString(KEY_CUSTOM_SF2_URI, uri.toString()).apply()
                }
            }
            ok
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load soundfont from $uri", e)
            false
        }
    }

    private fun loadDefaultInternal(): Boolean {
        return try {
            val bytes = appContext.assets.open(DEFAULT_ASSET_NAME).use { it.readBytes() }
            val ok = loadBytes(bytes)
            usingCustomSoundFont = false
            ok
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bundled default soundfont", e)
            false
        }
    }

    private fun loadBytes(bytes: ByteArray): Boolean {
        if (!NativeSoundEngine.nativeLoadSoundFont(bytes)) {
            Log.e(TAG, "Native engine failed to parse soundfont")
            return false
        }
        pianoPreset = NativeSoundEngine.nativeGetPresetIndex(0, 0).takeIf { it >= 0 } ?: 0
        guitarPreset = NativeSoundEngine.nativeGetPresetIndex(0, 24).takeIf { it >= 0 } ?: 0
        bassPreset = NativeSoundEngine.nativeGetPresetIndex(0, 32).takeIf { it >= 0 } ?: 0
        loaded = true
        if (enabled) NativeSoundEngine.nativeStart()
        return true
    }

    private fun presetFor(instrument: InstrumentType): Int = when (instrument) {
        InstrumentType.PIANO -> pianoPreset
        InstrumentType.GUITAR, InstrumentType.UKULELE -> guitarPreset
        InstrumentType.BASS -> bassPreset
    }

    companion object {
        private const val TAG = "SoundEngine"
        private const val PREFS_NAME = "scaleinkey_sound"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_CUSTOM_SF2_URI = "custom_sf2_uri"
        const val DEFAULT_ASSET_NAME = "scaleinkey_default.sf2"
        private const val NOTE_PREVIEW_MS = 700L
        private const val CHORD_PREVIEW_MS = 1000L
    }
}
