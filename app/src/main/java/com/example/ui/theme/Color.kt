package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Swiss-Inspired Avant-Garde Editorial Palette
val ElectricLime = Color(0xFF7CFF00)
val AcidYellow = Color(0xFFFFF000)
val VividPink = Color(0xFFFF438F)
val ElectricBlue = Color(0xFF3183FF)
val BrightCyan = Color(0xFF32C5F4)
val VividViolet = Color(0xFF792EFF)

val CharcoalBlack = Color(0xFF171717)
val PureBlack = Color(0xFF050505)
val EditorialWhite = Color(0xFFFFFFFF)
val NeutralGray = Color(0xFFD8D8D8)
val Terracotta = Color(0xFFC86D27)
val MutedGray = Color(0xFF8E8E8E)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCardBorder = Color(0xFF2C2C2C)

// Semantic Screen Accent Assignments:
// Home: Electric Lime (#7CFF00)
// Living Grid: Bright Cyan (#32C5F4)
// Community: Vivid Pink (#FF438F)
// Inbox: Vivid Violet (#792EFF)
// Profile: Acid Yellow (#FFF000)

val LimeOnBlack = ElectricLime
val PinkOnWhite = VividPink
val BlueOnBlack = ElectricBlue

// Gradients & Textures
val EditorialDarkGradient = Brush.verticalGradient(listOf(PureBlack, CharcoalBlack))
val LimeAccentGradient = Brush.horizontalGradient(listOf(ElectricLime, Color(0xFFA8FF4D)))
val PinkAccentGradient = Brush.horizontalGradient(listOf(VividPink, Color(0xFFFF72AC)))
val CyanAccentGradient = Brush.horizontalGradient(listOf(BrightCyan, ElectricBlue))
