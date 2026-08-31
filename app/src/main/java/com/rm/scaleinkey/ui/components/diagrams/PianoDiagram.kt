package com.rm.scaleinkey.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.PianoKey
import com.rm.scaleinkey.music.buildPianoKeys
import com.rm.scaleinkey.ui.theme.scaleColors

private const val OCTAVES = 2

@Composable
fun PianoDiagram(
    rootPitchClass: Int,
    highlightedNotes: List<Note>,
    isChordSelection: Boolean,
    onKeyTapped: (PianoKey) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.scaleColors
    val keys = remember(OCTAVES) { buildPianoKeys(octaves = OCTAVES) }
    val whiteKeyCount = keys.count { !it.isBlack }
    val labelByPitchClass = remember(highlightedNotes) {
        highlightedNotes.associateBy { it.pitchClass }
    }
    val textMeasurer = rememberTextMeasurer()

    val outlineColor = MaterialTheme.colorScheme.outline
    val whiteKeyColor = MaterialTheme.colorScheme.surface
    val blackKeyColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(whiteKeyCount / 4f)
            .pointerInput(keys) {
                detectTapGestures { offset ->
                    hitTestPianoKey(offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), keys, whiteKeyCount)
                        ?.let(onKeyTapped)
                }
            }
    ) {
        val whiteKeyWidth = size.width / whiteKeyCount
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = size.height * 0.6f

        fun colorFor(pitchClass: Int): Color? = when {
            pitchClass !in labelByPitchClass -> null
            pitchClass == rootPitchClass -> palette.root
            isChordSelection -> palette.chordTone
            else -> palette.scaleTone
        }

        fun drawLabel(note: Note, center: Offset, fontSizePx: Float) {
            val style = TextStyle(
                color = palette.onHighlight,
                fontSize = fontSizePx.toSp(),
                fontWeight = FontWeight.Bold,
            )
            val layout = textMeasurer.measure(note.displayName(), style)
            drawText(
                textMeasurer = textMeasurer,
                text = note.displayName(),
                topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f),
                style = style,
            )
        }

        var whiteSlot = -1
        // White keys first so black keys draw on top.
        keys.filter { !it.isBlack }.forEach { key ->
            whiteSlot += 1
            val x = whiteSlot * whiteKeyWidth
            drawRect(
                color = whiteKeyColor,
                topLeft = Offset(x, 0f),
                size = Size(whiteKeyWidth, size.height),
            )
            drawRect(
                color = outlineColor,
                topLeft = Offset(x, 0f),
                size = Size(whiteKeyWidth, size.height),
                style = Stroke(width = 1.5f),
            )
            colorFor(key.pitchClass)?.let { color ->
                val center = Offset(x + whiteKeyWidth / 2f, size.height * 0.82f)
                val radius = whiteKeyWidth * 0.34f
                drawCircle(color = color, radius = radius, center = center)
                labelByPitchClass[key.pitchClass]?.let { note ->
                    drawLabel(note, center, radius * 0.9f)
                }
            }
        }

        whiteSlot = -1
        keys.forEach { key ->
            if (!key.isBlack) {
                whiteSlot += 1
                return@forEach
            }
            val x = (whiteSlot + 1) * whiteKeyWidth - blackKeyWidth / 2f
            drawRect(
                color = blackKeyColor,
                topLeft = Offset(x, 0f),
                size = Size(blackKeyWidth, blackKeyHeight),
            )
            colorFor(key.pitchClass)?.let { color ->
                val center = Offset(x + blackKeyWidth / 2f, blackKeyHeight * 0.72f)
                val radius = blackKeyWidth * 0.34f
                drawCircle(color = color, radius = radius, center = center)
                labelByPitchClass[key.pitchClass]?.let { note ->
                    drawLabel(note, center, radius * 0.85f)
                }
            }
        }
    }
}

/** Mirrors the draw geometry above: black keys sit on top in the upper [blackKeyHeight] band. */
private fun hitTestPianoKey(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    keys: List<PianoKey>,
    whiteKeyCount: Int,
): PianoKey? {
    val whiteKeyWidth = width / whiteKeyCount
    val blackKeyWidth = whiteKeyWidth * 0.6f
    val blackKeyHeight = height * 0.6f

    if (y <= blackKeyHeight) {
        var whiteSlot = -1
        for (key in keys) {
            if (!key.isBlack) {
                whiteSlot += 1
                continue
            }
            val bx = (whiteSlot + 1) * whiteKeyWidth - blackKeyWidth / 2f
            if (x in bx..(bx + blackKeyWidth)) return key
        }
    }

    val targetSlot = (x / whiteKeyWidth).toInt().coerceIn(0, whiteKeyCount - 1)
    var whiteSlot = -1
    for (key in keys) {
        if (!key.isBlack) {
            whiteSlot += 1
            if (whiteSlot == targetSlot) return key
        }
    }
    return null
}
