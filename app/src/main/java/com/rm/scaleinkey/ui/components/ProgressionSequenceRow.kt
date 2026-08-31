package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rm.scaleinkey.music.ChordVoicing
import com.rm.scaleinkey.music.DiatonicChord
import com.rm.scaleinkey.music.display
import com.rm.scaleinkey.ui.ProgressionSlot
import kotlin.math.abs

private val CHIP_WIDTH = 88.dp
private val CHIP_HEIGHT = 84.dp
private val CHIP_SPACING = 10.dp

/**
 * The built progression, as a horizontally-scrollable row of fixed-size chips (a plain `Row`, not
 * `LazyRow` — matches this app's existing "plain layouts, predictable sizing" convention, and
 * avoids `LazyRow` item recycling complicating the position tracking the drag math below depends
 * on). Each chip: a plain tap removes it; a long-press-then-drag reorders it — the two gestures
 * are disambiguated by requiring the long-press before a drag starts, since a chip that's both a
 * tap target and a drag handle can't otherwise tell "tap to remove" from "the start of a drag"
 * apart (see ChordRow.kt's 7th-toggle fix for the related lesson: never give a small/ambiguous
 * gesture two conflicting meanings on the same hitbox).
 */
@Composable
fun ProgressionSequenceRow(
    slots: List<ProgressionSlot>,
    chordFor: (Int) -> DiatonicChord?,
    voicingFor: (Int) -> ChordVoicing,
    currentStepIndex: Int?,
    onRemove: (Long) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (slots.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Tap a chord above to start building a progression",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 20.dp),
            )
        }
        return
    }

    // Read via rememberUpdatedState, not the raw `slots` parameter, inside the drag callbacks
    // below: pointerInput is keyed only on slot.id (deliberately, so a live reorder mid-drag
    // doesn't restart — and therefore lose — the gesture that caused it), so the gesture-detection
    // coroutine is long-lived across multiple moves within one drag and needs a way to see the
    // latest slot order without being torn down and restarted every time it changes.
    val currentSlots by rememberUpdatedState(slots)
    val density = LocalDensity.current
    val stepPx = with(density) { (CHIP_WIDTH + CHIP_SPACING).toPx() }

    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    // Each chip's last-measured horizontal center, keyed by its stable id (not its index, which
    // changes on every reorder) — the drag math below finds the nearest chip to swap with by
    // comparing these positions.
    val chipCenters = remember { mutableStateMapOf<Long, Float>() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING),
    ) {
        slots.forEachIndexed { index, slot ->
            val isDragging = slot.id == draggingId
            Box(
                modifier = Modifier
                    .width(CHIP_WIDTH)
                    .height(CHIP_HEIGHT)
                    .onGloballyPositioned { coords ->
                        chipCenters[slot.id] = coords.boundsInParent().center.x
                    }
                    .graphicsLayer { translationX = if (isDragging) dragOffsetX else 0f }
                    .zIndex(if (isDragging) 1f else 0f)
                    .pointerInput(slot.id) {
                        detectTapGestures(onTap = { onRemove(slot.id) })
                    }
                    .pointerInput(slot.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId = slot.id
                                dragOffsetX = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount.x
                                val liveSlots = currentSlots
                                val draggedIndex = liveSlots.indexOfFirst { it.id == slot.id }
                                if (draggedIndex == -1) return@detectDragGesturesAfterLongPress
                                val draggedCenter = (chipCenters[slot.id] ?: 0f) + dragOffsetX
                                val targetIndex = liveSlots.indices.minByOrNull { i ->
                                    abs((chipCenters[liveSlots[i].id] ?: 0f) - draggedCenter)
                                } ?: draggedIndex
                                if (targetIndex != draggedIndex) {
                                    // Correct the accumulated visual offset by one chip-step so the
                                    // dragged chip keeps tracking the finger smoothly across the
                                    // swap instead of jumping once the list underneath reorders.
                                    dragOffsetX -= if (targetIndex > draggedIndex) stepPx else -stepPx
                                    onMove(draggedIndex, targetIndex)
                                }
                            },
                            onDragEnd = {
                                draggingId = null
                                dragOffsetX = 0f
                            },
                            onDragCancel = {
                                draggingId = null
                                dragOffsetX = 0f
                            },
                        )
                    },
            ) {
                SequenceChip(
                    chord = chordFor(slot.degree),
                    voicing = voicingFor(slot.degree),
                    isCurrentStep = currentStepIndex == index,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SequenceChip(
    chord: DiatonicChord?,
    voicing: ChordVoicing,
    isCurrentStep: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    // The chord this slot's degree resolved to on the currently-selected scale may not exist
    // (e.g. the user switched to a scale with fewer/no diatonic chords) — show a dim placeholder
    // rather than omitting the slot, since omitting it would desync this row's positions from
    // ChordProgressionState.slots' indices, which the drag-reorder math above depends on staying
    // 1:1 with what's on screen.
    if (chord == null) {
        Column(
            modifier = modifier
                .clip(shape)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val background = if (isCurrentStep) {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    } else {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHigh)
        )
    }
    val contentColor = if (isCurrentStep) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val display = chord.display(voicing)
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(
                if (isCurrentStep) {
                    Modifier
                } else {
                    Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape)
                }
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = display.romanNumeral,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = display.symbol,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
