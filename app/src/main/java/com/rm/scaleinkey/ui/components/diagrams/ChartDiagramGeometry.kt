package com.rm.scaleinkey.ui.components.diagrams

/**
 * Height of one string row in [FrettedInstrumentDiagram] (or, in [ChordShapeDiagram], width of one
 * string column — strings run horizontally there instead), as a fraction of canvas width. Every
 * diagram used to derive its own string spacing by dividing whatever height/width *it* had left
 * for strings by however many strings *it* showed, which — like the old per-diagram fret spacing —
 * produced inconsistent, non-square cells: the full 12-fret Guitar neck view packed its 6 strings
 * the tightest of any diagram/instrument combination, so that's the value adopted here (matches
 * [FrettedInstrumentDiagram]'s original, pre-unification formula for Guitar exactly: 6 strings,
 * 1.15x fudge factor, 78% of height usable after the nut/fret-label padding — `6 * 1.15 * 0.78 /
 * (12 * 5)`). A diagram with fewer strings (Ukulele/Bass) or fewer visible rows (the scale-box,
 * chord shapes) than Guitar's 6 simply doesn't fill all the space its box makes room for — see each
 * diagram's own comments for how that leftover space is anchored/left blank.
 */
internal const val STRING_SPACING_FRACTION = 0.0897f

/**
 * Shared canvas aspect ratio (width / height) for whichever fretted-instrument diagram is showing
 * while chart mode is on: [ChordShapeDiagram] (a chord is selected) or [FrettedInstrumentDiagram]
 * windowed to [com.rm.scaleinkey.music.scaleBoxWindow] (no chord selected). Selecting or
 * deselecting a chord while chart mode is on must not visibly resize the diagram, so both diagrams
 * share this one box height rather than each computing its own. It's set to exactly the tallest
 * content either diagram ever needs to show — Guitar's 6 strings at [STRING_SPACING_FRACTION] (the
 * scale-box's tallest case, since [ChordShapeDiagram]'s fixed 4 fret rows at
 * [FRET_SPACING_FRACTION] need noticeably less height) — so nothing overflows the box, for any
 * instrument or chord shape. This is numerically identical to [FrettedInstrumentDiagram]'s own
 * (unwindowed) Guitar aspect ratio, since Guitar's scale-box content need is exactly its full neck
 * view's content need with fewer frets shown (frets don't affect box height — see
 * [FRET_SPACING_FRACTION]'s doc). Narrower instruments/shorter content simply leave blank space
 * below their content rather than stretching to fill this height.
 */
internal const val CHART_MODE_ASPECT_RATIO = 12f / (6f * 1.15f)

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
