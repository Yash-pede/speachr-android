package com.yash.speachr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Softer, more rounded than stock Material3 defaults (4/8/12/16/28dp).
// Bigger radii are most of what makes an app stop "reading" as Material at a glance —
// it's what every glassy / neo-morphic UI (iOS widgets, macOS Big Sur, etc.) leans on.
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)