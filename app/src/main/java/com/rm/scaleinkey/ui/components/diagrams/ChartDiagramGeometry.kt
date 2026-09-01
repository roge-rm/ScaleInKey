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
 * Shared note-circle radius, as a fraction of canvas width, for every fretted-instrument diagram:
 * [FrettedInstrumentDiagram] (both the full 12-fret neck view and the windowed scale-box) and
 * [ChordShapeDiagram]. Each used to derive its own radius from `min(per-fret spacing, per-string
 * spacing) * 0.34` — a reasonable rule per diagram in isolation, but since the three diagrams pack
 * very different amounts of neck into the same canvas width (12 frets vs. 4, vs. a fixed 4-string
 * reference grid), that produced visibly different-sized note circles depending on which diagram
 * happened to be showing. The full 12-fret neck view always came out smallest (the most frets
 * sharing the least width), so this is that same value — `(0.91 usable-width fraction × 0.34) ÷ 12
 * frets` — pulled out as one constant so all three diagrams draw the same absolute-sized circle
 * regardless of how much per-cell space they'd otherwise have to spare.
 */
internal const val NOTE_CIRCLE_RADIUS_FRACTION = 0.91f * 0.34f / 12f
