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

// Padding as a fraction of canvas *width* on every side — including top and bottom, even though
// those measure a vertical distance. See FrettedInstrumentDiagram's identical top/bottom padding
// comment for why: a height-fraction pad shrinks along with this content-derived box, while the
// content it needs to clear (marker text, note-circle radius) is sized from width, so it needs a
// width-based (not height-based) pad to stay sufficient regardless of box height.
private const val LEFT_PAD_FRACTION = 0.14f // room for a "Nfr" position label when startFret > 0
private const val RIGHT_PAD_FRACTION = 0.06f
private const val TOP_PAD_FRACTION = 0.11f // room for the open/mute (○/✕) marker row
private const val BOTTOM_PAD_FRACTION = 0.03f
private const val FRET_ROWS = 4

// Box height content-derived from FRET_ROWS at the shared FRET_SPACING_FRACTION (frets run
// vertically here — see that constant's doc), scaled by CHART_MODE_SCALE and padding included.
// The caller wraps this diagram in Modifier.animateContentSize() so switching to/from
// FrettedInstrumentDiagram's windowed scale-box (a different natural height) animates smoothly
// instead of jumping.
private const val CONTENT_ASPECT_RATIO = 1f / (CHART_MODE_SCALE * (TOP_PAD_FRACTION + BOTTOM_PAD_FRACTION + FRET_ROWS * FRET_SPACING_FRACTION))

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
            .aspectRatio(CONTENT_ASPECT_RATIO)
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
        val leftPad = size.width * LEFT_PAD_FRACTION * CHART_MODE_SCALE
        val rightPad = size.width * RIGHT_PAD_FRACTION * CHART_MODE_SCALE
        val topPad = size.width * TOP_PAD_FRACTION * CHART_MODE_SCALE
        val bottomPad = size.width * BOTTOM_PAD_FRACTION * CHART_MODE_SCALE

        val gridWidth = size.width - leftPad - rightPad

        // A shared absolute string spacing (see STRING_SPACING_FRACTION), scaled by
        // CHART_MODE_SCALE — see that constant's doc. Not gridWidth/(numStrings-1), otherwise this
        // diagram's own generous grid width stretches strings apart further than
        // FrettedInstrumentDiagram's neck/scale-box views. Centered in the leftover width, same as
        // a narrower instrument's fewer strings always were.
        val stringSpacing = size.width * STRING_SPACING_FRACTION * CHART_MODE_SCALE
        val gridLeftOffset = ((gridWidth - stringSpacing * (numStrings - 1)) / 2f).coerceAtLeast(0f)
        fun stringX(stringIndex: Int) = leftPad + gridLeftOffset + stringIndex * stringSpacing

        // A shared absolute row height (see FRET_SPACING_FRACTION), scaled by CHART_MODE_SCALE —
        // not gridHeight/FRET_ROWS, otherwise this grid's 4 rows fill the whole available height,
        // taller per row than the full neck view's per-fret width. The grid simply doesn't reach
        // the bottom of the canvas (row 0 stays anchored at topPad, like a real chord chart's nut
        // sitting near the top).
        val rowSpacing = size.width * FRET_SPACING_FRACTION * CHART_MODE_SCALE
        fun rowY(row: Int) = topPad + row * rowSpacing // row 0 = nut/position line, 1..FRET_ROWS = frets

        // Same shared absolute size FrettedInstrumentDiagram uses (see NOTE_CIRCLE_RADIUS_FRACTION),
        // scaled by CHART_MODE_SCALE — rather than one derived from this diagram's own per-cell
        // space, so a chord shape's dots match the neck/scale-box views' size (times the shared
        // chart-mode scale-up) instead of coming out an unrelated size.
        val dotRadius = size.width * NOTE_CIRCLE_RADIUS_FRACTION * CHART_MODE_SCALE

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
        val markerFontSize = size.width * DIAGRAM_LABEL_FONT_FRACTION * CHART_MODE_SCALE
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
                fontSizePx = size.width * DIAGRAM_LABEL_FONT_FRACTION * CHART_MODE_SCALE,
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
    val leftPad = width * LEFT_PAD_FRACTION * CHART_MODE_SCALE
    val rightPad = width * RIGHT_PAD_FRACTION * CHART_MODE_SCALE
    val gridWidth = width - leftPad - rightPad
    if (gridWidth <= 0f) return null
    if (numStrings <= 1) return 0
    val stringSpacing = width * STRING_SPACING_FRACTION * CHART_MODE_SCALE
    val gridLeftOffset = ((gridWidth - stringSpacing * (numStrings - 1)) / 2f).coerceAtLeast(0f)
    return ((x - leftPad - gridLeftOffset) / stringSpacing).roundToInt().coerceIn(0, numStrings - 1)
}
