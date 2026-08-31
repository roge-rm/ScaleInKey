package com.rm.scaleinkey.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ScaleColorPalette(
    val root: Color,
    val scaleTone: Color,
    val chordTone: Color,
    val inactive: Color,
    // Text color to draw on top of root/scaleTone/chordTone dots — those three share
    // similar lightness within a theme mode, so one contrast color covers all of them.
    val onHighlight: Color,
)

val LightScaleColorPalette = ScaleColorPalette(
    root = RootHighlightLight,
    scaleTone = ScaleToneHighlightLight,
    chordTone = ChordToneHighlightLight,
    inactive = InactiveToneLight,
    onHighlight = Color.White,
)

val DarkScaleColorPalette = ScaleColorPalette(
    root = RootHighlightDark,
    scaleTone = ScaleToneHighlightDark,
    chordTone = ChordToneHighlightDark,
    inactive = InactiveToneDark,
    onHighlight = OnSurfaceLight,
)

val LocalScaleColorPalette = staticCompositionLocalOf { LightScaleColorPalette }

val MaterialTheme.scaleColors: ScaleColorPalette
    @Composable
    get() = LocalScaleColorPalette.current
