package com.yash.speachr.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------------------------
// Brand palette — derived from the Spechr logo coral (#E37054)
// ---------------------------------------------------------------------------------------------

// Core brand coral
val Coral40 = Color(0xFFE37054)   // primary — light theme
val Coral80 = Color(0xFFFFB59D)   // primary — dark theme
val Coral10 = Color(0xFF3A1400)
val Coral90 = Color(0xFFFFDBCB)
val Coral20 = Color(0xFF5C1A00)
val Coral30 = Color(0xFF7D2E0C)

// Warm taupe — secondary, keeps everything in the same warm family instead of
// clashing with an unrelated accent color
val Taupe40 = Color(0xFF77574C)
val Taupe80 = Color(0xFFE7BEAF)
val Taupe10 = Color(0xFF2C1610)
val Taupe90 = Color(0xFFFFDBCB)
val Taupe20 = Color(0xFF432A20)
val Taupe30 = Color(0xFF5C4034)

// Muted gold — tertiary accent, used sparingly (badges, highlights, progress)
val Gold40 = Color(0xFF6B5D3F)
val Gold80 = Color(0xFFD7C58D)
val Gold10 = Color(0xFF231B04)
val Gold90 = Color(0xFFF4E1B8)
val Gold20 = Color(0xFF3B2F09)
val Gold30 = Color(0xFF54461D)

// Neutral warm greys — background/surface, never pure white/black so it doesn't
// read as stock Material
val Neutral99 = Color(0xFFFFF8F5)
val Neutral10 = Color(0xFF231A16)
val Neutral17 = Color(0xFF17120F)
val Neutral90 = Color(0xFFEDE0DA)
val Neutral30 = Color(0xFF53433D)
val Neutral60 = Color(0xFFA08D85)
val Neutral80 = Color(0xFFD8C2BA)
val Neutral87 = Color(0xFFFFEDE7)
val Neutral25 = Color(0xFF392E2A)

// Semantic
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error10 = Color(0xFF410002)
val Error90 = Color(0xFFFFDAD6)
val Error20 = Color(0xFF690005)

// ---------------------------------------------------------------------------------------------
// Glass tokens — translucent surfaces + hairline borders for the frosted-glass look.
// Pair these with the Haze library (see notes) for a real blur; on their own they still give
// a convincing "frosted card" effect via alpha + a subtle border + elevation.
// ---------------------------------------------------------------------------------------------

val GlassSurfaceLight = Color(0xCCFFFFFF)      // ~80% white
val GlassSurfaceLightSubtle = Color(0x99FFFFFF) // ~60% white, for layered cards
val GlassBorderLight = Color(0x33FFFFFF)
val GlassTintLight = Color(0x14E37054)          // faint brand tint over the blur

val GlassSurfaceDark = Color(0xCC1C1613)
val GlassSurfaceDarkSubtle = Color(0x991C1613)
val GlassBorderDark = Color(0x1FFFFFFF)
val GlassTintDark = Color(0x1FFFB59D)