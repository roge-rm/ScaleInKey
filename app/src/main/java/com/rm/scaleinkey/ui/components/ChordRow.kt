package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rm.scaleinkey.music.ChordVoicing
import com.rm.scaleinkey.music.DiatonicChord
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.assignAscendingMidiNotes
import com.rm.scaleinkey.music.display
import com.rm.scaleinkey.ui.LocalSoundEngine

private const val CHORDS_PER_ROW = 4

@Composable
fun ChordRow(
    chords: List<DiatonicChord>,
    selectedDegree: Int?,
    voicingFor: (Int) -> ChordVoicing,
    instrument: InstrumentType,
    onChordTapped: (Int) -> Unit,
    onSeventhToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soundEngine = LocalSoundEngine.current
    // Fixed CHORDS_PER_ROW-per-row grid (not a wrapping FlowRow): the number of rows
    // — and so this section's total height — stays constant across every root/scale
    // combination, even though chord symbol text length varies a lot (e.g. "G7" vs
    // "F♯m7♭5"). A content-driven wrap would reflow into a different row count and
    // shift the diagram below every time the selection changed.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        chords.chunked(CHORDS_PER_ROW).forEach { rowChords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowChords.forEach { chord ->
                    val voicing = voicingFor(chord.degree)
                    ChordCard(
                        chord = chord,
                        voicing = voicing,
                        selected = chord.degree == selectedDegree,
                        onCardTapped = {
                            onChordTapped(chord.degree)
                            val display = chord.display(voicing)
                            val midiNotes = assignAscendingMidiNotes(display.orderedTones.map { it.pitchClass })
                            soundEngine.playChord(instrument, midiNotes)
                        },
                        onSeventhToggled = {
                            onSeventhToggled(chord.degree)
                            val newVoicing = if (voicing == ChordVoicing.SEVENTH) ChordVoicing.TRIAD else ChordVoicing.SEVENTH
                            val display = chord.display(newVoicing)
                            val midiNotes = assignAscendingMidiNotes(display.orderedTones.map { it.pitchClass })
                            soundEngine.playChord(instrument, midiNotes)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(CHORDS_PER_ROW - rowChords.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChordCard(
    chord: DiatonicChord,
    voicing: ChordVoicing,
    selected: Boolean,
    onCardTapped: () -> Unit,
    onSeventhToggled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
        )
    } else {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHigh)
        )
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val display = chord.display(voicing)
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .height(84.dp) // fixed: ♭/♯/ø/° glyphs measure taller than plain text,
            // which would otherwise make cards uneven within/across rows.
            .clip(shape)
            .background(background)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape,
                    )
                }
            )
            .clickable(onClick = onCardTapped)
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
            onClick = onSeventhToggled,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun SeventhChip(
    active: Boolean,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(shape)
            .background(contentColor.copy(alpha = if (active) 0.35f else 0.08f))
            .border(BorderStroke(1.dp, contentColor.copy(alpha = if (active) 0.7f else 0.25f)), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "7th",
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
        )
    }
}
