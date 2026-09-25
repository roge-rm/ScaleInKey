package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.music.ChordVoicing
import com.rm.scaleinkey.music.DiatonicChord
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.display
import com.rm.scaleinkey.music.previewMidiNotes
import com.rm.scaleinkey.ui.LocalSoundEngine

private const val CHORDS_PER_ROW = 4

/**
 * The chord "source" for a progression: the same fixed-width chord-card grid as [ChordRow], but
 * every tap appends to the sequence instead of selecting/deselecting. Triad/7th voicing is read
 * from [voicingFor] — the same shared per-degree choice [ChordRow] toggles on the Explore screen —
 * and long-press here toggles that same shared state via [onSeventhToggled], so a 7th chord can be
 * queued up without leaving this screen. [onChordAppended] is handed the voicing shown on the card
 * at tap time, which [ChordProgressionState.append] captures into that one queued slot — so later
 * toggling a degree's voicing here changes what a *future* tap of that card appends, but never
 * retroactively changes a chord already queued.
 */
@Composable
fun ChordPalette(
    chords: List<DiatonicChord>,
    voicingFor: (Int) -> ChordVoicing,
    instrument: InstrumentType,
    onChordAppended: (degree: Int, voicing: ChordVoicing) -> Unit,
    onSeventhToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soundEngine = LocalSoundEngine.current
    val spacing = 10.dp
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val cardWidth = (maxWidth - spacing * (CHORDS_PER_ROW - 1)) / CHORDS_PER_ROW
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            chords.chunked(CHORDS_PER_ROW).forEach { rowChords ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, alignment = Alignment.CenterHorizontally),
                ) {
                    rowChords.forEach { chord ->
                        val voicing = voicingFor(chord.degree)
                        PaletteChordCard(
                            chord = chord,
                            voicing = voicing,
                            onTapped = {
                                onChordAppended(chord.degree, voicing)
                                val display = chord.display(voicing)
                                val midiNotes = previewMidiNotes(instrument, display.orderedTones.map { it.pitchClass })
                                soundEngine.playChord(instrument, midiNotes)
                            },
                            onSeventhToggled = {
                                onSeventhToggled(chord.degree)
                                val newVoicing = if (voicing == ChordVoicing.SEVENTH) ChordVoicing.TRIAD else ChordVoicing.SEVENTH
                                val display = chord.display(newVoicing)
                                val midiNotes = previewMidiNotes(instrument, display.orderedTones.map { it.pitchClass })
                                soundEngine.playChord(instrument, midiNotes)
                            },
                            modifier = Modifier.width(cardWidth),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PaletteChordCard(
    chord: DiatonicChord,
    voicing: ChordVoicing,
    onTapped: () -> Unit,
    onSeventhToggled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val display = chord.display(voicing)
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .height(84.dp) // matches ChordRow's ChordCard, for the same reason: glyph-height parity.
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape)
            // Same long-press-toggles-7th / plain-tap-acts convention as ChordRow's ChordCard —
            // see its comment for why the "7th" indicator below has no hitbox of its own.
            .combinedClickable(onClick = onTapped, onLongClick = onSeventhToggled)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = display.romanNumeral,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = display.symbol,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        SeventhChip(
            active = voicing == ChordVoicing.SEVENTH,
            contentColor = contentColor,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.6f),
        )
    }
}
