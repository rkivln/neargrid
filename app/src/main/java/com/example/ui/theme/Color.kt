package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// NearGrid Obsidian Cobalt Aura Design System
// Inspired by Zentra, MRR, Fishbowl, and Tonesmith minimal dark interfaces.
// =========================================================================

// 1. Core Obsidian Surfaces & Hairlines
val ObsidianCanvas = Color(0xFF090B10)         // Deepest void obsidian backdrop
val DarkGlassCard = Color(0xFF141720)          // 24dp rounded dark glass card surface
val ElevatedGlassSurface = Color(0xFF1B1F2B)   // Elevated capsules, chips, active surfaces
val HairlineGlassBorder = Color(0xFF262C3D)    // Crisp 1dp structural glass hairline
val SubtleGlassDivider = Color(0xFF1A1F2B)     // Subtle internal divider
val PureBlack = Color(0xFF000000)

// 2. Crisp Luminous Ink & Contrast
val PureWhite = Color(0xFFFFFFFF)              // Pure white for hero CTAs & primary icons
val ObsidianInk = Color(0xFFF4F7FF)            // Primary off-white display & body text
val ObsidianBlack = Color(0xFF090B10)          // High-contrast dark ink for white hero buttons
val MutedSlate = Color(0xFF94A3B8)             // Secondary readable slate ink
val SubtleSlate = Color(0xFF64748B)            // Captions, timestamps, inactive labels

// 3. Electric Cobalt Blue Spectrum (Hero Banners, Active Pills, Chat Bubbles)
val ElectricCobalt = Color(0xFF2E6BFF)         // Primary signature electric cobalt
val CobaltDeep = Color(0xFF1744C8)             // Deep saturated royal cobalt
val CobaltGlow = Color(0xFF60A5FA)             // Luminous highlight / plasma rim
val CobaltSoft = Color(0xFF172554)             // Translucent dark cobalt wash
val CobaltBorder = Color(0xFF3B82F6)           // Cobalt highlight stroke

// 4. Fiery Coral-Orange Spectrum (Aura Alternation, Alerts, NEED Dispatches)
val FieryCoral = Color(0xFFFF4D32)             // Signature coral-red aura & alert
val FieryCoralGlow = Color(0xFFFF7A66)         // Soft luminous coral highlight
val FieryCoralSoft = Color(0xFF3A120E)         // Deep translucent coral wash
val AuraOrange = Color(0xFFFF8C42)             // Warm amber-orange aura edge

// 5. Amber Glow & Emerald Mint (Metrics, Offers, Active Presence)
val AmberGlow = Color(0xFFF59E0B)              // Amber metric & GPS search pulse
val AmberSoft = Color(0xFF331D05)              // Deep translucent amber wash
val AmberLight = Color(0xFFFBBF24)             // Bright amber text highlight
val EmeraldMint = Color(0xFF10B981)            // Verified active node & OFFER accent
val EmeraldSoft = Color(0xFF063729)            // Deep translucent emerald wash
val EmeraldLight = Color(0xFF34D399)           // Bright emerald text highlight

// =========================================================================
// Semantic Theme Mappings & Compatibility Aliases
// =========================================================================

val ObsidianPlate = DarkGlassCard
val ObsidianPlateElevated = ElevatedGlassSurface
val GlassBorder = HairlineGlassBorder
val ObsidianMuted = MutedSlate
val ObsidianSubtle = SubtleSlate

val CobaltBlue = ElectricCobalt
val CobaltDark = CobaltGlow

val AmberAlert = AmberGlow
val AmberDark = AmberLight

val RedAlert = FieryCoral
val RedSoft = FieryCoralSoft
val RedDark = FieryCoralGlow

val MeshGreen = EmeraldMint
val MeshGreenSoft = EmeraldSoft
val MeshGreenDark = EmeraldLight

// Legacy compatibility mappings cleanly migrated to Obsidian Cobalt Aura
// Note: PureWhite stays #FFFFFF; InkBlack stays #F4F7FF (use ObsidianBlack for dark-on-white!)
val PaperCanvas = ObsidianCanvas
val SoftPaperSurface = DarkGlassCard
val PaperBorder = HairlineGlassBorder
val SubtlePaperDivider = SubtleGlassDivider

val InkBlack = ObsidianInk
val MutedInk = MutedSlate
val SubtleInk = SubtleSlate

val SignalRed = FieryCoral
val SignalRedSoft = FieryCoralSoft
val SignalRedDark = FieryCoralGlow

val Sage = EmeraldMint
val SageSoft = EmeraldSoft
val SageDark = EmeraldLight

val Ochre = AmberGlow
val OchreSoft = AmberSoft
val OchreDark = AmberLight

val BackgroundCanvas = ObsidianCanvas
val PrimarySurface = DarkGlassCard
val SoftSurface = ElevatedGlassSurface
val CardBorderColor = HairlineGlassBorder
val SubtleDivider = SubtleGlassDivider

val PrimaryText = ObsidianInk
val SecondaryText = MutedSlate
val MutedText = SubtleSlate

val AccentHome = ElectricCobalt
val AccentGrid = EmeraldMint
val AccentCommunity = FieryCoral
val AccentInbox = ElectricCobalt
val AccentProfile = AmberGlow

val AccentLime = ElectricCobalt
val LightLime = CobaltSoft
val SoftLimeSurface = CobaltSoft
val BlueGray = ElectricCobalt
val SlateBlue = ElectricCobalt
val DeepNavy = ObsidianCanvas
val DarkNavy = Color(0xFF06080D)

val WarmIvory = ObsidianCanvas
val SoftCream = DarkGlassCard
val PaleWarmGray = DarkGlassCard
val WarmBorder = HairlineGlassBorder
val WarmDivider = SubtleGlassDivider
val CharcoalBlack = ObsidianCanvas
val EditorialWhite = PureWhite
val NeutralGray = MutedSlate
val Terracotta = FieryCoral
val MutedGray = MutedSlate
val DarkSurface = DarkGlassCard
val DarkCardBorder = HairlineGlassBorder
val ElectricLime = ElectricCobalt
val AcidYellow = AmberGlow
val VividPink = FieryCoral
val ElectricBlue = ElectricCobalt
val BrightCyan = CobaltGlow
val VividViolet = FieryCoral
val LimeOnBlack = ObsidianBlack
val PinkOnWhite = FieryCoral
val BlueOnBlack = ObsidianInk
val PastelPeach = AmberSoft
val PastelCoral = FieryCoralSoft
val PastelPowderBlue = CobaltSoft
val PastelSoftCyan = CobaltSoft
val PastelMint = EmeraldSoft
val PastelButterYellow = AmberSoft
val PastelLavenderPink = FieryCoralSoft

// =========================================================================
// Obsidian Cobalt Aura Gradients & Brushes
// =========================================================================

val CobaltHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF102A7A), ElectricCobalt, Color(0xFF5A96FF))
)

val CobaltPillGradient = Brush.horizontalGradient(
    colors = listOf(CobaltDeep, ElectricCobalt, CobaltGlow)
)

val CoralPillGradient = Brush.horizontalGradient(
    colors = listOf(FieryCoral, AuraOrange)
)

val CobaltGlowGradient = CobaltPillGradient
val AmberGlowGradient = Brush.horizontalGradient(listOf(AmberGlow, AmberLight))
val RedGlowGradient = CoralPillGradient
val ObsidianPlateGradient = Brush.verticalGradient(listOf(DarkGlassCard, ElevatedGlassSurface))

val SignalRedHighlightGradient = CoralPillGradient
val SageHighlightGradient = Brush.horizontalGradient(listOf(EmeraldMint, EmeraldLight))
val OchreHighlightGradient = AmberGlowGradient
val PaperSurfaceGradient = ObsidianPlateGradient
val BlueGrayDashboardGradient = CobaltHeroGradient
val DeepNavyViewportGradient = Brush.verticalGradient(listOf(ObsidianCanvas, Color(0xFF101522)))
val LimeHighlightGradient = CobaltPillGradient
val SoftSurfaceGradient = ObsidianPlateGradient
val EditorialWarmGradient = ObsidianPlateGradient
val PeachToCoralGradient = CoralPillGradient
val CyanToBlueGradient = CobaltPillGradient
val MintToYellowGradient = AmberGlowGradient
val SoftLavenderGradient = ObsidianPlateGradient
val LimeAccentGradient = CobaltPillGradient
val PinkAccentGradient = CoralPillGradient
val CyanAccentGradient = CobaltPillGradient
val EditorialDarkGradient = DeepNavyViewportGradient
