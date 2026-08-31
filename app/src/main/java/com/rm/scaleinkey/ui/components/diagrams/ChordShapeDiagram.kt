package com.rm.scaleinkey.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import com.rm.scaleinkey.music.ChordShape
import com.rm.scaleinkey.ui.theme.scaleColors
import kotlin.math.roundToInt

private const val LEFT_PAD_FRACTION = 0.14f // room for a "Nfr" position label when startFret > 0
private const val RIGHT_PAD_FRACTION = 0.06f
private const val TOP_PAD_FRACTION = 0.18f // room for the open/mute (○/✕) marker row
private const val BOTTOM_PAD_FRACTION = 0.04f
private const val FRET_ROWS = 4

// Guitar's string count. The canvas aspect ratio and per-string spacing are always computed as
// if there were this many strings — a narrower instrument (Ukulele/Bass, 4 strings) gets a
// correspondingly narrower grid centered in the same space, rather than stretching to fill the
// full width. Without this, fewer strings meant *more* space per string and so visibly larger
// dots/text than Guitar's, which looked inconsistent switching between instrument tabs.
private const val REFERENCE_STRINGS = 6

/**
 * Traditional vertical songbook chord-box: one specific voicing, one mark per string (open/muted/
 * fretted-with-finger-number), unlike [FrettedInstrumentDiagram] which shows every occurrence of
 * a pitch class across the whole neck. [shape] already carries everything needed to render and to
 * resolve a tap back to a playable fret — no tuning import needed here.
 */
@Composable
fun ChordShapeDiagram(
    shape: ChordShape,
    onFretTapped: (stringIndex: Int, fret: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.scaleColors
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fretColor = MaterialTheme.colorScheme.outline
    val nutColor = MaterialTheme.colorScheme.onSurface
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pressedColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    val numStrings = shape.marks.size

    // Only a fretted or open string (fret != null) is actually tappable — a muted string has
    // nothing to preview — but track the column regardless so the draw code can decide.
    var pressedStringIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(REFERENCE_STRINGS / (FRET_ROWS.toFloat() * 1.35f))
            .pointerInput(shape) {
                detectTapGestures(
                    onPress = { offset ->
                        pressedStringIndex = hitTestChordShapeString(offset.x, size.width.toFloat(), numStrings)
                        tryAwaitRelease()
                        pressedStringIndex = null
                    },
                    onTap = { offset ->
                        hitTestChordShapeString(offset.x, size.width.toFloat(), numStrings)?.let { stringIndex ->
                            shape.marks[stringIndex].fret?.let { fret -> onFretTapped(stringIndex, fret) }
                        }
                    },
                )
            },
    ) {
        val leftPad = size.width * LEFT_PAD_FRACTION
        val rightPad = size.width * RIGHT_PAD_FRACTION
        val topPad = size.height * TOP_PAD_FRACTION
        val bottomPad = size.height * BOTTOM_PAD_FRACTION

        val gridWidth = size.width - leftPad - rightPad
        val gridHeight = size.height - topPad - bottomPad

        // Always spaced as if there were REFERENCE_STRINGS strings, then centered — see the
        // constant's doc comment above for why.
        val stringSpacing = gridWidth / (REFERENCE_STRINGS - 1)
        val gridLeftOffset = (gridWidth - stringSpacing * (numStrings - 1)) / 2f
        fun stringX(stringIndex: Int) = leftPad + gridLeftOffset + stringIndex * stringSpacing

        val rowSpacing = gridHeight / FRET_ROWS
        fun rowY(row: Int) = topPad + row * rowSpacing // row 0 = nut/position line, 1..FRET_ROWS = frets

        val dotRadius = minOf(stringSpacing, rowSpacing) * 0.34f

        fun drawCenteredText(text: String, center: Offset, fontSizePx: Float, color: Color, bold: Boolean = false) {
            val style = TextStyle(
                color = color,
                fontSize = fontSizePx.toSp(),
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
            val layout = textMeasurer.measure(text, style)
            drawText(
                textMeasurer = textMeasurer,
                text = text,
                topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f),
                style = style,
            )
        }

        // Open/mute markers above the nut.
        val markerY = topPad * 0.5f
        val markerFontSize = topPad * 0.5f
        for (s in 0 until numStrings) {
            val fret = shape.marks[s].fret
            val symbol = when (fret) {
                null -> "✕"
                0 -> "○"
                else -> null
            }
            symbol?.let {
                drawCenteredText(
                    text = it,
                    center = Offset(stringX(s), markerY),
                    fontSizePx = markerFontSize,
                    color = if (fret == null) palette.inactive else labelTextColor,
                    bold = true,
                )
            }
        }

        // Nut (open-position shapes) or a thin line + position label (movable/barre shapes).
        val gridLeft = leftPad + gridLeftOffset
        val gridRight = leftPad + gridLeftOffset + stringSpacing * (numStrings - 1)
        if (shape.startFret == 0) {
            drawLine(
                color = nutColor,
                start = Offset(gridLeft, rowY(0)),
                end = Offset(gridRight, rowY(0)),
                strokeWidth = 6f,
            )
        } else {
            drawLine(
                color = fretColor,
                start = Offset(gridLeft, rowY(0)),
                end = Offset(gridRight, rowY(0)),
                strokeWidth = 2f,
            )
            drawCenteredText(
                text = "${shape.startFret + 1}fr",
                center = Offset(gridLeft - stringSpacing * 0.6f, rowY(0) + rowSpacing / 2f),
                fontSizePx = rowSpacing * 0.28f,
                color = labelTextColor,
                bold = true,
            )
        }

        // Fret lines.
        for (row in 1..FRET_ROWS) {
            drawLine(
                color = fretColor,
                start = Offset(gridLeft, rowY(row)),
                end = Offset(gridRight, rowY(row)),
                strokeWidth = 2f,
            )
        }

        // Strings.
        for (s in 0 until numStrings) {
            val x = stringX(s)
            drawLine(color = stringColor, start = Offset(x, rowY(0)), end = Offset(x, rowY(FRET_ROWS)), strokeWidth = 2.5f)
        }

        // Fretted dots (open strings are already marked above, not drawn on the grid).
        for (s in 0 until numStrings) {
            val mark = shape.marks[s]
            val fret = mark.fret ?: continue
            if (fret == 0) continue
            val row = fret - shape.startFret
            if (row !in 1..FRET_ROWS) continue
            val center = Offset(stringX(s), rowY(row) - rowSpacing / 2f)
            val color = if (mark.isRoot) palette.root else palette.chordTone
            drawCircle(color = color, radius = dotRadius, center = center)
            mark.finger?.let { finger ->
                drawCenteredText(
                    text = finger.toString(),
                    center = center,
                    fontSizePx = dotRadius * 0.95f,
                    color = palette.onHighlight,
                    bold = true,
                )
            }
        }

        // Press feedback, drawn last so it overrides the resting color at that string while
        // held. Muted strings aren't tappable, so they get none.
        pressedStringIndex?.let { s ->
            val fret = shape.marks[s].fret ?: return@let
            val center = if (fret == 0) {
                Offset(stringX(s), markerY)
            } else {
                val row = fret - shape.startFret
                Offset(stringX(s), rowY(row) - rowSpacing / 2f)
            }
            drawCircle(color = pressedColor, radius = dotRadius, center = center)
            shape.marks[s].finger?.let { finger ->
                drawCenteredText(
                    text = finger.toString(),
                    center = center,
                    fontSizePx = dotRadius * 0.95f,
                    color = palette.onHighlight,
                    bold = true,
                )
            }
        }
    }
}

/** Mirrors the string-spacing geometry above. Null only when the canvas has no real width yet. */
private fun hitTestChordShapeString(x: Float, width: Float, numStrings: Int): Int? {
    val leftPad = width * LEFT_PAD_FRACTION
    val rightPad = width * RIGHT_PAD_FRACTION
    val gridWidth = width - leftPad - rightPad
    if (gridWidth <= 0f) return null
    if (numStrings <= 1) return 0
    val referenceSpacing = gridWidth / (REFERENCE_STRINGS - 1)
    val gridLeftOffset = (gridWidth - referenceSpacing * (numStrings - 1)) / 2f
    return ((x - leftPad - gridLeftOffset) / referenceSpacing).roundToInt().coerceIn(0, numStrings - 1)
}
