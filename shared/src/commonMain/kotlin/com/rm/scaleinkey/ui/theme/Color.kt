package com.rm.scaleinkey.ui.theme

import androidx.compose.ui.graphics.Color

// Brand: indigo/violet, replacing the stock Material Purple/Pink template.
val BrandIndigo80 = Color(0xFFC7BFFF)
val BrandIndigo40 = Color(0xFF5B4FE3)
val BrandViolet80 = Color(0xFFE3BFFF)
val BrandViolet40 = Color(0xFF8A4FE3)
val BrandTeal80 = Color(0xFFA8E8DC)
val BrandTeal40 = Color(0xFF1F8F76)

val BackgroundLight = Color(0xFFFAFAFC)
val SurfaceLight = Color(0xFFF1F0F8)
val OnSurfaceLight = Color(0xFF1B1B23)

val BackgroundDark = Color(0xFF121016)
val SurfaceDark = Color(0xFF1D1B24)
val OnSurfaceDark = Color(0xFFE8E6F0)

// Full brand-derived Material3 role set — defined explicitly for both themes so no
// role silently falls back to Material's unbranded baseline palette (which clashed,
// e.g. a muddy default tertiaryContainer instead of a brand-tinted one).
val OnPrimaryLight = Color.White
val PrimaryContainerLight = Color(0xFFE4E0FF)
val OnPrimaryContainerLight = Color(0xFF3A2FA8)
val OnSecondaryLight = Color.White
val SecondaryContainerLight = Color(0xFFF3E1FF)
val OnSecondaryContainerLight = Color(0xFF6A2FA0)
val OnTertiaryLight = Color.White
val TertiaryContainerLight = Color(0xFFD3F3EA)
val OnTertiaryContainerLight = Color(0xFF0F6350)
val SurfaceVariantLight = Color(0xFFE6E3F0)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF7A7689)
val OutlineVariantLight = Color(0xFFCAC5D6)
val SurfaceContainerHighLight = Color(0xFFECEAF5)

val OnPrimaryDark = Color(0xFF2C2470)
val PrimaryContainerDark = Color(0xFF433C8F)
val OnPrimaryContainerDark = BrandIndigo80
val OnSecondaryDark = Color(0xFF47216B)
val SecondaryContainerDark = Color(0xFF5E3785)
val OnSecondaryContainerDark = BrandViolet80
val OnTertiaryDark = Color(0xFF0B3C30)
val TertiaryContainerDark = Color(0xFF17594A)
val OnTertiaryContainerDark = BrandTeal80
val SurfaceVariantDark = Color(0xFF3A3745)
val OnSurfaceVariantDark = Color(0xFFCBC5D8)
val OutlineDark = Color(0xFF938F9F)
val OutlineVariantDark = Color(0xFF48445A)
val SurfaceContainerHighDark = Color(0xFF272531)

// Scale/chord highlight palette — kept distinct from Material's ColorScheme so the
// root note always reads apart from generic scale/chord tones on every diagram.
val RootHighlightLight = Color(0xFFE0A62B)
val ScaleToneHighlightLight = BrandTeal40
val ChordToneHighlightLight = BrandViolet40
val InactiveToneLight = Color(0xFFCFCDDA)

val RootHighlightDark = Color(0xFFF2C15C)
val ScaleToneHighlightDark = BrandTeal80
val ChordToneHighlightDark = BrandViolet80
val InactiveToneDark = Color(0xFF454251)
