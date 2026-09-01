package com.rm.scaleinkey.ui.components.diagrams

/**
 * Shared canvas aspect ratio (width / height) for whichever fretted-instrument diagram is showing
 * while chart mode is on: [ChordShapeDiagram] (a chord is selected) or [FrettedInstrumentDiagram]
 * windowed to [com.rm.scaleinkey.music.scaleBoxWindow] (no chord selected). Both used to compute
 * their own, unrelated aspect ratio — the windowed scale-box reused [FrettedInstrumentDiagram]'s
 * full-neck formula (tuned for a 12-fret-wide landscape box) with `fretCount` swapped to 4, which
 * produces a tall portrait box instead, roughly twice the height of [ChordShapeDiagram]'s box for
 * the same width. Selecting or deselecting a chord while chart mode is on then visibly resized the
 * diagram. Sharing one constant keeps the box the same height across that transition.
 */
internal const val CHART_MODE_ASPECT_RATIO = 6f / (4f * 1.35f) // REFERENCE_STRINGS / (FRET_ROWS * 1.35)

/**
 * Width of one fret, as a fraction of canvas width, shared by every fretted-instrument diagram:
 * [FrettedInstrumentDiagram]'s full 12-fret neck view, its windowed 4-fret scale-box, and
 * [ChordShapeDiagram]'s 4-row chord grid (frets run vertically there, but the same "one fret's
 * worth of neck" unit still applies, just as row height instead of column width). Each diagram
 * used to derive its own fret spacing by dividing its own usable width/height across however many
 * frets *it* shows — which packs a diagram showing fewer frets (4, for both the scale-box and the
 * chord grid) far more loosely than the neck view's 12-frets-in-the-same-width. This is the neck
 * view's own value (`0.91` usable-width fraction ÷ 12 frets), the smallest of the three, so a
 * fingering looks like the same physical span of neck regardless of which diagram is showing it.
 * Diagrams with fewer frets than 12 simply don't use their full available space — see each
 * diagram's own comments for how the leftover space is anchored/left blank.
 */
internal const val FRET_SPACING_FRACTION = 0.91f / 12f

/**
 * Shared note-circle radius, as a fraction of canvas width — see [FRET_SPACING_FRACTION]'s doc for
 * why a shared fraction-of-width is used at all. This is 34% of one fret's width, matching the
 * proportion [FrettedInstrumentDiagram]'s neck view originally used for its own (smallest) circles.
 */
internal const val NOTE_CIRCLE_RADIUS_FRACTION = FRET_SPACING_FRACTION * 0.34f

/**
 * Shared font size, as a fraction of canvas width, for the smaller "peripheral" text around each
 * fretted-instrument diagram's grid — the fret-number guide row and string-name labels in
 * [FrettedInstrumentDiagram], and the open/mute markers and moveable-shape position label ("3fr")
 * in [ChordShapeDiagram]. (Not the note-circle labels themselves — those already scale with
 * [NOTE_CIRCLE_RADIUS_FRACTION].) Each of these used to derive its size from its own diagram's
 * height, which — like the fret spacing above — differs by diagram, producing inconsistently-sized
 * text depending on which diagram was showing. 40% of one fret's width keeps this legible without
 * crowding the smallest available spacing (string spacing) in any diagram/instrument combination.
 */
internal const val DIAGRAM_LABEL_FONT_FRACTION = FRET_SPACING_FRACTION * 0.4f
