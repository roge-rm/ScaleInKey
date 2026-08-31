package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.music.InstrumentType
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.assignAscendingMidiNotes
import com.rm.scaleinkey.ui.LocalSoundEngine
import com.rm.scaleinkey.ui.theme.scaleColors

@Composable
fun ScaleInfoHeader(
    root: Note,
    scaleDisplayName: String,
    notes: List<Note>,
    instrument: InstrumentType,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.scaleColors
    val soundEngine = LocalSoundEngine.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "${root.displayName()} $scaleDisplayName",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            // Reserves 2 lines of height always, so a long name (e.g. "F♯ Aeolian
            // (Natural Minor)") wrapping to a 2nd line doesn't shift everything below
            // relative to a short one (e.g. "C Ionian (Major)") that fits on 1 line.
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        // A plain Row (not a wrapping FlowRow) so this is always exactly one line —
        // otherwise differing note-name widths per root/scale could wrap to a 2nd
        // line and shift the chord row and diagram below.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            notes.forEachIndexed { index, note ->
                // Same amber used for "root" everywhere else (diagram dots, legend),
                // so the root note reads consistently across the whole screen.
                val isRoot = index == 0
                // Fixed height on the pill itself (not just wrap-content around the
                // text): an accidental glyph (♯/♭) has different font metrics than a
                // plain letter, so "F♯" and "C" measure to slightly different heights
                // — without a fixed height, pills in the same row would be uneven,
                // and different roots would resize this whole row inconsistently.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(
                            color = if (isRoot) {
                                palette.root.copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable {
                            val midiNote = assignAscendingMidiNotes(listOf(note.pitchClass)).first()
                            soundEngine.playNote(instrument, midiNote)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = note.displayName(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Normal,
                        color = if (isRoot) palette.root else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
