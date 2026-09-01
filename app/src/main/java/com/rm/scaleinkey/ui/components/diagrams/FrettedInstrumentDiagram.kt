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
import com.rm.scaleinkey.music.CANONICAL_ROOTS
import com.rm.scaleinkey.music.Note
import com.rm.scaleinkey.music.StringInstrumentTuning
import com.rm.scaleinkey.music.fretPitchClass
import com.rm.scaleinkey.ui.theme.scaleColors
import kotlin.math.roundToInt

private val MARKER_FRETS = setOf(3, 5, 7, 9)
private const val DOUBLE_MARKER_FRET = 12

// Fretboard padding, expressed as a fraction of the canvas size. Shared between drawing and
// tap hit-testing so the two geometries can never drift apart.
private const val LEFT_PAD_FRACTION = 0.07f
private const val RIGHT_PAD_FRACTION = 0.02f
private const val TOP_PAD_FRACTION = 0.06f
private const val BOTTOM_PAD_FRACTION = 0.16f

@Composable
fun FrettedInstrumentDiagram(
    tuning: StringInstrumentTuning,
    rootPitchClass: Int,
    highlightedNotes: List<Note>,
    isChordSelection: Boolean,
    // Null keeps this diagram's own numFrets/numStrings-derived shape (the full 12-fret neck
    // view). Chart mode's windowed scale-box (fretCount == 4, via scaleBoxWindow()) passes
    // CHART_MODE_ASPECT_RATIO instead, so it matches ChordShapeDiagram's box exactly — otherwise
    // this diagram's own formula, tuned for a wide 12-fret box, produces a tall portrait box for
    // only 4 frets, and the diagram visibly jumps in size whenever a chord is selected/deselected
    // (switching between this and ChordShapeDiagram) while chart mode stays on.
    aspectRatioOverride: Float? = null,
    onFretTapped: (stringIndex: Int, fret: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.scaleColors
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fretColor = MaterialTheme.colorScheme.outline
    val nutColor = MaterialTheme.colorScheme.onSurface
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val markerColor = MaterialTheme.colorScheme.outlineVariant
    val pressedColor = MaterialTheme.colorScheme.primary

    val labelByPitchClass = remember(highlightedNotes) {
        highlightedNotes.associateBy { it.pitchClass }
    }
    val stringLabels = remember(tuning) {
        tuning.openStringPitchClasses.map { CANONICAL_ROOTS[it].displayName() }
    }
    val textMeasurer = rememberTextMeasurer()

    val numStrings = tuning.openStringMidiNotes.size
    val numFrets = tuning.fretCount

    // hitTestFret always resolves to *some* cell (coerced into range), so the pressed cell can
    // be one with no highlight at all — still worth flashing, since that's exactly the case that
    // previously drew nothing and gave no confirmation of where the tap landed.
    var pressedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Box height content-derived from the string axis alone (frets live on the width axis, which
    // is always the full available width regardless of numFrets — see positionSpacing below), so
    // this diagram's own aspect ratio needs only enough height for numStrings-1 gaps at the shared
    // STRING_SPACING_FRACTION, padding included. aspectRatioOverride (the windowed scale-box) skips
    // this in favor of a box height shared with ChordShapeDiagram — see CHART_MODE_ASPECT_RATIO.
    val contentAspectRatio = if (numStrings > 1) {
        (1f - TOP_PAD_FRACTION - BOTTOM_PAD_FRACTION) / ((numStrings - 1) * STRING_SPACING_FRACTION)
    } else {
        1f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatioOverride ?: contentAspectRatio)
            .pointerInput(tuning) {
                detectTapGestures(
                    onPress = { offset ->
                        pressedCell = hitTestFret(offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), numStrings, numFrets)
                        tryAwaitRelease()
                        pressedCell = null
                    },
                    onTap = { offset ->
                        hitTestFret(offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), numStrings, numFrets)
                            ?.let { (stringIndex, fret) -> onFretTapped(stringIndex, fret) }
                    },
                )
            }
    ) {
        val leftPad = size.width * LEFT_PAD_FRACTION
        val rightPad = size.width * RIGHT_PAD_FRACTION
        val topPad = size.height * TOP_PAD_FRACTION
        val bottomPad = size.height * BOTTOM_PAD_FRACTION

        val fretboardWidth = size.width - leftPad - rightPad
        val fretboardHeight = size.height - topPad - bottomPad

        // A shared absolute fret width (see FRET_SPACING_FRACTION), not fretboardWidth/numFrets —
        // otherwise the windowed scale-box (only 4 frets) spreads those 4 frets across the full
        // available width, spacing them much further apart than the full 12-fret neck view. When
        // numFrets < the neck view's 12, the drawn grid is centered in the leftover width rather
        // than reaching the right edge — see the string-line drawing below, which stops at the
        // last actual fret rather than continuing into that unused space.
        val positionSpacing = size.width * FRET_SPACING_FRACTION
        val fretGridLeftOffset = ((fretboardWidth - numFrets * positionSpacing) / 2f).coerceAtLeast(0f)
        fun positionX(fret: Int) = leftPad + fretGridLeftOffset + fret * positionSpacing

        // A shared absolute string spacing (see STRING_SPACING_FRACTION), not
        // fretboardHeight/(numStrings-1) — otherwise a diagram with a taller box than its own
        // string count strictly needs (the windowed scale-box, whose height instead matches
        // ChordShapeDiagram via aspectRatioOverride) stretches its strings apart to fill that
        // height rather than keeping the same physical spacing as every other diagram.
        val stringSpacing = size.width * STRING_SPACING_FRACTION
        // Row 0 of tuning.openStringMidiNotes is the lowest-pitched string (see the "Low E2 ...
        // high E4" comments on InstrumentTunings) — draw it at the bottom and the highest-pitched
        // string at the top, matching how a player looking down at their own instrument's neck
        // sees the strings, rather than the reverse.
        fun stringY(stringIndex: Int) = topPad + (numStrings - 1 - stringIndex) * stringSpacing

        // A shared absolute size (see NOTE_CIRCLE_RADIUS_FRACTION) rather than one derived from
        // this diagram's own, more generous per-cell space — otherwise the windowed scale-box
        // (only 4 frets wide) draws visibly larger circles than the full 12-fret neck view.
        val markerRadius = size.width * NOTE_CIRCLE_RADIUS_FRACTION

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

        // Fretboard position markers (real fretboard inlay dots), drawn behind everything.
        for (f in 0..numFrets) {
            if (f == 0) continue
            val midX = positionX(f) - positionSpacing / 2f
            when {
                f == DOUBLE_MARKER_FRET -> {
                    val third = fretboardHeight / 3f
                    drawCircle(color = markerColor, radius = markerRadius * 0.6f, center = Offset(midX, topPad + third))
                    drawCircle(color = markerColor, radius = markerRadius * 0.6f, center = Offset(midX, topPad + 2 * third))
                }
                f in MARKER_FRETS -> {
                    drawCircle(
                        color = markerColor,
                        radius = markerRadius * 0.6f,
                        center = Offset(midX, topPad + fretboardHeight / 2f),
                    )
                }
            }
        }

        // Strings — end exactly at the last drawn fret (positionX(numFrets)), not the canvas edge,
        // so a windowed view (numFrets < 12) doesn't draw a string trailing off into blank space
        // past where the grid itself stops.
        for (s in 0 until numStrings) {
            val y = stringY(s)
            drawLine(
                color = stringColor,
                start = Offset(positionX(0), y),
                end = Offset(positionX(numFrets), y),
                strokeWidth = 2.5f,
            )
            drawCenteredText(
                text = stringLabels[s],
                center = Offset(leftPad * 0.4f, y),
                fontSizePx = size.width * DIAGRAM_LABEL_FONT_FRACTION,
                color = labelTextColor,
                bold = true,
            )
        }

        // Frets (position 0 is the nut).
        for (f in 0..numFrets) {
            val x = positionX(f)
            drawLine(
                color = if (f == 0) nutColor else fretColor,
                start = Offset(x, topPad),
                end = Offset(x, topPad + fretboardHeight),
                strokeWidth = if (f == 0) 6f else 2f,
            )
        }

        // Fret number guide row.
        val fretLabelY = topPad + fretboardHeight + bottomPad * 0.55f
        val fretLabelFontSize = size.width * DIAGRAM_LABEL_FONT_FRACTION
        for (f in 0..numFrets) {
            val x = if (f == 0) positionX(f) else positionX(f) - positionSpacing / 2f
            val isMarkerFret = f == DOUBLE_MARKER_FRET || f in MARKER_FRETS
            drawCenteredText(
                text = f.toString(),
                center = Offset(x, fretLabelY),
                fontSizePx = fretLabelFontSize,
                color = if (isMarkerFret) nutColor else labelTextColor,
                bold = isMarkerFret,
            )
        }

        fun colorFor(pitchClass: Int): Color? = when {
            pitchClass !in labelByPitchClass -> null
            pitchClass == rootPitchClass -> palette.root
            isChordSelection -> palette.chordTone
            else -> palette.scaleTone
        }

        for (s in 0 until numStrings) {
            for (f in 0..numFrets) {
                val pitchClass = fretPitchClass(tuning, s, f)
                val color = colorFor(pitchClass) ?: continue
                val x = if (f == 0) positionX(f) + markerRadius * 1.5f else positionX(f) - positionSpacing / 2f
                val center = Offset(x, stringY(s))
                drawCircle(color = color, radius = markerRadius, center = center)
                labelByPitchClass[pitchClass]?.displayName()?.let { text ->
                    drawCenteredText(
                        text = text,
                        center = center,
                        fontSizePx = markerRadius * 0.95f,
                        color = palette.onHighlight,
                        bold = true,
                    )
                }
            }
        }

        // Press feedback, drawn last so it overrides whatever (if anything) was drawn at this
        // cell above — flashes even on an unhighlighted fret, so every tap gets a visible response.
        pressedCell?.let { (s, f) ->
            val x = if (f == 0) positionX(f) + markerRadius * 1.5f else positionX(f) - positionSpacing / 2f
            val center = Offset(x, stringY(s))
            drawCircle(color = pressedColor, radius = markerRadius, center = center)
            val pressedPitchClass = fretPitchClass(tuning, s, f)
            labelByPitchClass[pressedPitchClass]?.displayName()?.let { text ->
                drawCenteredText(
                    text = text,
                    center = center,
                    fontSizePx = markerRadius * 0.95f,
                    color = palette.onHighlight,
                    bold = true,
                )
            }
        }
    }
}

/** Mirrors the draw geometry above using the same padding fractions. */
private fun hitTestFret(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    numStrings: Int,
    numFrets: Int,
): Pair<Int, Int>? {
    val leftPad = width * LEFT_PAD_FRACTION
    val rightPad = width * RIGHT_PAD_FRACTION
    val topPad = height * TOP_PAD_FRACTION
    val bottomPad = height * BOTTOM_PAD_FRACTION

    val fretboardWidth = width - leftPad - rightPad
    val fretboardHeight = height - topPad - bottomPad
    if (fretboardWidth <= 0f || fretboardHeight <= 0f) return null

    val positionSpacing = width * FRET_SPACING_FRACTION
    val fretGridLeftOffset = ((fretboardWidth - numFrets * positionSpacing) / 2f).coerceAtLeast(0f)
    val stringSpacing = width * STRING_SPACING_FRACTION

    val stringIndex = if (numStrings > 1) {
        // Mirrors stringY's top-to-bottom = high-to-low flip above: row 0 on screen (y closest to
        // topPad) is the highest-pitched string, so the raw row needs inverting back to the
        // tuning-array index (row 0 = lowest string) that the rest of the app expects.
        val row = ((y - topPad) / stringSpacing).roundToInt().coerceIn(0, numStrings - 1)
        numStrings - 1 - row
    } else {
        0
    }
    val fret = ((x - leftPad - fretGridLeftOffset) / positionSpacing).roundToInt().coerceIn(0, numFrets)
    return stringIndex to fret
}
