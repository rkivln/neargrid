package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Minimalist Template Color System
// Neutral Surfaces & Canvas
val BackgroundCanvas = Color(0xFFE9EAEB)
val PrimarySurface = Color(0xFFFFFFFF)
val SoftSurface = Color(0xFFF5F5F3)
val CardBorderColor = Color(0xFFE2E4E6)
val SubtleDivider = Color(0xFFECEEEF)

// Blue-Gray and Navy Hierarchy
val BlueGray = Color(0xFF7F9EAE)
val SlateBlue = Color(0xFF526F80)
val DeepNavy = Color(0xFF243A50)
val DarkNavy = Color(0xFF1B2B3C)

// Vibrant Lime Accents
val AccentLime = Color(0xFFD9FF38)
val LightLime = Color(0xFFE7FF72)
val SoftLimeSurface = Color(0xFFF4FDC6)

// Typography & Contrast
val PrimaryText = Color(0xFF151719)
val SecondaryText = Color(0xFF92999F)
val MutedText = Color(0xFFA6ACB2)
val PureWhite = Color(0xFFFFFFFF)

// Semantic Screen Accents
val AccentHome = AccentLime
val AccentGrid = BlueGray
val AccentCommunity = SlateBlue
val AccentInbox = DeepNavy
val AccentProfile = LightLime

// Backward compatibility aliases
val WarmIvory = BackgroundCanvas
val SoftCream = SoftSurface
val PaleWarmGray = BackgroundCanvas
val WarmBorder = CardBorderColor
val WarmDivider = SubtleDivider
val CharcoalBlack = PrimaryText
val PureBlack = PrimaryText
val EditorialWhite = PureWhite
val NeutralGray = SecondaryText
val Terracotta = SlateBlue
val MutedGray = SecondaryText
val DarkSurface = DeepNavy
val DarkCardBorder = SlateBlue.copy(alpha = 0.3f)
val ElectricLime = AccentLime
val AcidYellow = LightLime
val VividPink = DeepNavy
val ElectricBlue = BlueGray
val BrightCyan = BlueGray
val VividViolet = SlateBlue
val LimeOnBlack = PrimaryText
val PinkOnWhite = DeepNavy
val BlueOnBlack = PureWhite
val PastelPeach = SoftSurface
val PastelCoral = SlateBlue
val PastelPowderBlue = BlueGray.copy(alpha = 0.25f)
val PastelSoftCyan = BlueGray
val PastelMint = AccentLime
val PastelButterYellow = LightLime
val PastelLavenderPink = SoftSurface

// Sophisticated Gradients & Brushes
val BlueGrayDashboardGradient = Brush.verticalGradient(
    listOf(BlueGray, SlateBlue)
)

val DeepNavyViewportGradient = Brush.verticalGradient(
    listOf(DeepNavy, DarkNavy)
)

val LimeHighlightGradient = Brush.horizontalGradient(
    listOf(AccentLime, LightLime)
)

val SoftSurfaceGradient = Brush.verticalGradient(
    listOf(PrimarySurface, SoftSurface)
)

val EditorialWarmGradient = SoftSurfaceGradient
val PeachToCoralGradient = LimeHighlightGradient
val CyanToBlueGradient = BlueGrayDashboardGradient
val MintToYellowGradient = LimeHighlightGradient
val SoftLavenderGradient = SoftSurfaceGradient
val LimeAccentGradient = LimeHighlightGradient
val PinkAccentGradient = BlueGrayDashboardGradient
val CyanAccentGradient = BlueGrayDashboardGradient
val EditorialDarkGradient = DeepNavyViewportGradient
