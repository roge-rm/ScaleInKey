package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rm.scaleinkey.ui.BeatsPerChord

private const val BPM_STEP = 5
private const val BPM_MIN = 40
private const val BPM_MAX = 240

/** Tempo, per-chord duration, loop, and transport controls for the progression sequencer. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    bpm: Int,
    onBpmChanged: (Int) -> Unit,
    beatsPerChord: BeatsPerChord,
    onBeatsPerChordChanged: (BeatsPerChord) -> Unit,
    loop: Boolean,
    onLoopChanged: (Boolean) -> Unit,
    isPlaying: Boolean,
    hasSlots: Boolean,
    onPlayStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StepperButton(symbol = "−", onClick = { onBpmChanged((bpm - BPM_STEP).coerceIn(BPM_MIN, BPM_MAX)) })
                Text(
                    text = "$bpm BPM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StepperButton(symbol = "+", onClick = { onBpmChanged((bpm + BPM_STEP).coerceIn(BPM_MIN, BPM_MAX)) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Beats/chord",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleChoiceSegmentedButtonRow {
                    BeatsPerChord.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = option == beatsPerChord,
                            onClick = { onBeatsPerChordChanged(option) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = BeatsPerChord.entries.size),
                        ) { Text(option.label) }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Loop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(checked = loop, onCheckedChange = onLoopChanged)
            }

            Button(
                onClick = onPlayStop,
                enabled = hasSlots,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isPlaying) "■ Stop" else "▶ Play")
            }
        }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
