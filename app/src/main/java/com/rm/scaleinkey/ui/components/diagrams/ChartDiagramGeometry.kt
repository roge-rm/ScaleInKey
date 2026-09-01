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
