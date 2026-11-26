package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.AuraOrange
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltHeroGradient
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

val LocalAuraBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun rememberResolvedBackdrop(explicitBackdrop: Backdrop? = null): Backdrop {
    if (explicitBackdrop != null) return explicitBackdrop
    val aura = LocalAuraBackdrop.current
    if (aura != null) return aura
    return rememberCanvasBackdrop {
        drawRect(ObsidianCanvas)
    }
}

@Composable
private fun rememberJellyPressScale(
    interactionSource: MutableInteractionSource
): Pair<Float, Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleX by animateFloatAsState(
        targetValue = if (isPressed) 1.045f else 1f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "jellyPressX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed) 0.945f else 1f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "jellyPressY"
    )
    return scaleX to scaleY
}

enum class AuraVariant {
    COBALT,
    CORAL,
    MIXED
}

/**
 * Full-bleed Obsidian Canvas with multi-stop blurred radial cobalt/coral glow blooms
 * matching the Zentra, MRR, and Fishbowl atmospheric background aesthetic.
 */
@Composable
fun AuraMeshBackground(
    variant: AuraVariant = AuraVariant.COBALT,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val meshBackdrop = rememberLayerBackdrop()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCanvas)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(meshBackdrop)
        ) {
            val w = size.width
            val h = size.height

            when (variant) {
                AuraVariant.COBALT -> {
                    // Upper-right bright cobalt/cyan-white bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF9AC2FF).copy(alpha = 0.58f),
                                ElectricCobalt.copy(alpha = 0.45f),
                                CobaltDeep.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.72f, h * 0.16f),
                            radius = w * 0.92f
                        ),
                        center = Offset(w * 0.72f, h * 0.16f),
                        radius = w * 0.92f
                    )
                    // Mid-left saturated electric cobalt bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricCobalt.copy(alpha = 0.55f),
                                CobaltDeep.copy(alpha = 0.26f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.18f, h * 0.34f),
                            radius = w * 0.85f
                        ),
                        center = Offset(w * 0.18f, h * 0.34f),
                        radius = w * 0.85f
                    )
                }

                AuraVariant.CORAL -> {
                    // Top warm amber-orange glow (Zentra right screen)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB25E).copy(alpha = 0.62f),
                                AuraOrange.copy(alpha = 0.44f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.36f, h * 0.12f),
                            radius = w * 0.95f
                        ),
                        center = Offset(w * 0.36f, h * 0.12f),
                        radius = w * 0.95f
                    )
                    // Center fiery coral-red bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FieryCoral.copy(alpha = 0.65f),
                                Color(0xFFB91C1C).copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.62f, h * 0.36f),
                            radius = w * 0.90f
                        ),
                        center = Offset(w * 0.62f, h * 0.36f),
                        radius = w * 0.90f
                    )
                }

                AuraVariant.MIXED -> {
                    // Top-left cobalt ambient glow + bottom subtle coral accent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricCobalt.copy(alpha = 0.48f),
                                CobaltDeep.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.20f, h * 0.14f),
                            radius = w * 0.88f
                        ),
                        center = Offset(w * 0.20f, h * 0.14f),
                        radius = w * 0.88f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CobaltGlow.copy(alpha = 0.26f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.88f, h * 0.22f),
                            radius = w * 0.65f
                        ),
                        center = Offset(w * 0.88f, h * 0.22f),
                        radius = w * 0.65f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FieryCoral.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.85f, h * 0.88f),
                            radius = w * 0.60f
                        ),
                        center = Offset(w * 0.85f, h * 0.88f),
                        radius = w * 0.60f
                    )
                }
            }
        }
        CompositionLocalProvider(LocalAuraBackdrop provides meshBackdrop) {
            content()
        }
    }
}

/**
 * Translucent dark glass card with 24.dp corners and crisp 1.dp hairline border.
 */
@Composable
fun DarkGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = DarkGlassCard,
    borderColor: Color = HairlineGlassBorder,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(cornerRadius))
                        .clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Tonesmith-inspired electric cobalt gradient hero card (28.dp rounded)
 * with glowing inner specular wash, avatar/icon, headline, optional progress bar,
 * pure white hero pill CTA, and circular top-right action button.
 */
@Composable
fun CobaltHeroCard(
    modifier: Modifier = Modifier,
    eyebrowText: String,
    titleText: String,
    subtitleText: String? = null,
    avatarInitials: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    trailingIconTestTag: String? = null,
    progressValue: Float? = null,
    progressLabel: String? = null,
    primaryCtaText: String? = null,
    primaryCtaIcon: ImageVector? = null,
    onPrimaryCtaClick: (() -> Unit)? = null,
    primaryCtaTestTag: String? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CobaltHeroGradient)
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
    ) {
        // Soft atmospheric sheen across the hero card
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.82f, size.height * 0.22f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.82f, size.height * 0.22f),
                radius = size.width * 0.55f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onAvatarClick != null) {
                                Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onAvatarClick() }
                                    .padding(vertical = 2.dp)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    if (avatarInitials != null) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitials,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eyebrowText,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = titleText,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 26.sp,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (trailingIcon != null && onTrailingIconClick != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    CircularOutlineIconButton(
                        icon = trailingIcon,
                        contentDescription = trailingIconDescription,
                        onClick = onTrailingIconClick,
                        containerColor = Color.White.copy(alpha = 0.14f),
                        borderColor = Color.White.copy(alpha = 0.40f),
                        iconTint = PureWhite,
                        modifier = if (trailingIconTestTag != null) {
                            Modifier.testTag(trailingIconTestTag)
                        } else {
                            Modifier
                        }
                    )
                }
            }

            if (!subtitleText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subtitleText,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = PureWhite.copy(alpha = 0.88f)
                )
            }

            if (progressValue != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressValue.coerceIn(0.08f, 1f))
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                        )
                    }
                    if (!progressLabel.isNullOrBlank()) {
                        Text(
                            text = progressLabel,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = PureWhite
                        )
                    }
                }
            }

            if (extraContent != null) {
                Spacer(modifier = Modifier.height(12.dp))
                extraContent()
            }

            if (!primaryCtaText.isNullOrBlank() && onPrimaryCtaClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                WhiteHeroPillButton(
                    text = primaryCtaText,
                    icon = primaryCtaIcon,
                    onClick = onPrimaryCtaClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (primaryCtaTestTag != null) {
                                Modifier.testTag(primaryCtaTestTag)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

/**
 * Tonesmith-style 22.dp dark glass capsule action row with left circular/rounded icon badge,
 * bold title + optional subtitle, and outlined circular right-chevron button.
 */
@Composable
fun CapsuleChevronRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = ElevatedGlassSurface,
    iconTint: Color = PureWhite,
    badgeText: String? = null,
    badgeColor: Color = ElectricCobalt
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackgroundColor)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ObsidianInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.22f))
                                .border(1.dp, badgeColor.copy(alpha = 0.55f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.24f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = title,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Luminous pearl-white Apple Liquid Glass hero capsule CTA (#FFFFFF frosted glass, #090B10 ink).
 */
@Composable
fun WhiteHeroPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    backdrop: Backdrop? = null
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (pressScaleX, pressScaleY) = rememberJellyPressScale(interactionSource)

    Box(
        modifier = modifier
            .height(50.dp)
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(10.dp.toPx())
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 18.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 16.dp,
                        offset = DpOffset(0.dp, 6.dp),
                        color = PureBlack.copy(alpha = 0.30f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                layerBlock = {
                    scaleX = pressScaleX
                    scaleY = pressScaleY
                    alpha = if (enabled) 1f else 0.45f
                },
                onDrawSurface = {
                    drawRect(PureWhite.copy(alpha = 0.88f))
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        PureWhite,
                        PureWhite.copy(alpha = 0.55f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = ObsidianBlack.copy(alpha = 0.12f)),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ObsidianBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Full-pill glowing electric cobalt or coral Apple Liquid Glass CTA button with jelly press physics.
 */
@Composable
fun PillPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    useCoral: Boolean = false,
    enabled: Boolean = true,
    backdrop: Backdrop? = null
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (pressScaleX, pressScaleY) = rememberJellyPressScale(interactionSource)

    val tintStart = if (useCoral) FieryCoral.copy(alpha = 0.78f) else CobaltGlow.copy(alpha = 0.72f)
    val tintEnd = if (useCoral) Color(0xFFD93829).copy(alpha = 0.84f) else CobaltDeep.copy(alpha = 0.84f)
    val borderCol = if (useCoral) Color(0xFFFFB2A2).copy(alpha = 0.55f) else PureWhite.copy(alpha = 0.36f)

    Box(
        modifier = modifier
            .height(50.dp)
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(12.dp.toPx())
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 20.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 16.dp,
                        offset = DpOffset(0.dp, 6.dp),
                        color = (if (useCoral) FieryCoral else ElectricCobalt).copy(alpha = 0.32f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                layerBlock = {
                    scaleX = pressScaleX
                    scaleY = pressScaleY
                    alpha = if (enabled) 1f else 0.45f
                },
                onDrawSurface = {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(tintStart, tintEnd))
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderCol,
                        borderCol.copy(alpha = 0.25f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PureWhite.copy(alpha = 0.22f)),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Smoked dark Apple Liquid Glass secondary capsule button with optical lens refraction and specular rim.
 */
@Composable
fun GlassSecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    backdrop: Backdrop? = null,
    surfaceTint: Color = Color(0xFF121726).copy(alpha = 0.64f),
    contentColor: Color = ObsidianInk,
    borderColor: Color? = null,
    height: Dp = 48.dp
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (pressScaleX, pressScaleY) = rememberJellyPressScale(interactionSource)

    Box(
        modifier = modifier
            .height(height)
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(14.dp.toPx())
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 20.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 12.dp,
                        offset = DpOffset(0.dp, 4.dp),
                        color = PureBlack.copy(alpha = 0.34f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                layerBlock = {
                    scaleX = pressScaleX
                    scaleY = pressScaleY
                    alpha = if (enabled) 1f else 0.45f
                },
                onDrawSurface = {
                    drawRect(surfaceTint)
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor ?: PureWhite.copy(alpha = 0.28f),
                        borderColor?.copy(alpha = 0.45f) ?: HairlineGlassBorder.copy(alpha = 0.72f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PureWhite.copy(alpha = 0.16f)),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Smoked dark Apple Liquid Glass circular icon button (matching the iOS 26 circular glass orb buttons).
 */
@Composable
fun CircularOutlineIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = Color(0xFF121726).copy(alpha = 0.62f),
    borderColor: Color = Color.White.copy(alpha = 0.28f),
    iconTint: Color = PureWhite,
    backdrop: Backdrop? = null
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (pressScaleX, pressScaleY) = rememberJellyPressScale(interactionSource)

    Box(
        modifier = modifier
            .size(size)
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(12.dp.toPx())
                    lens(
                        refractionHeight = 9.dp.toPx(),
                        refractionAmount = 18.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 12.dp,
                        offset = DpOffset(0.dp, 4.dp),
                        color = PureBlack.copy(alpha = 0.34f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                layerBlock = {
                    scaleX = pressScaleX
                    scaleY = pressScaleY
                },
                onDrawSurface = {
                    drawRect(containerColor)
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.35f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PureWhite.copy(alpha = 0.18f)),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Smoked Apple Liquid Glass `< Back` capsule header button matching iOS 26 navigation bars.
 */
@Composable
fun LiquidGlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Back",
    backdrop: Backdrop? = null
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (pressScaleX, pressScaleY) = rememberJellyPressScale(interactionSource)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .height(44.dp)
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(14.dp.toPx())
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 20.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 12.dp,
                        offset = DpOffset(0.dp, 4.dp),
                        color = PureBlack.copy(alpha = 0.34f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                layerBlock = {
                    scaleX = pressScaleX
                    scaleY = pressScaleY
                },
                onDrawSurface = {
                    drawRect(Color(0xFF121726).copy(alpha = 0.64f))
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        PureWhite.copy(alpha = 0.28f),
                        HairlineGlassBorder.copy(alpha = 0.72f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PureWhite.copy(alpha = 0.16f)),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = label,
                tint = PureWhite,
                modifier = Modifier.size(18.dp)
            )
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PureWhite,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Apple iOS 26 Liquid Glass Jelly Toggle Switch.
 * Features a color-morphing smoked track and a sliding clear refractive lens droplet thumb
 * that bends and illuminates the underlying track edge during spring travel.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = EmeraldMint,
    enabled: Boolean = true
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val trackWidthDp = 58.dp
    val trackHeightDp = 32.dp
    val thumbWidthDp = 34.dp
    val thumbHeightDp = 26.dp
    val thumbInsetDp = 3.dp

    val maxTravelPx = with(density) { (trackWidthDp - thumbWidthDp - thumbInsetDp * 2).toPx() }
    val progress = remember { Animatable(if (checked) 1f else 0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragVelocityBoost by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(checked, isDragging) {
        if (!isDragging) {
            progress.animateTo(
                targetValue = if (checked) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.54f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.92f) else Color(0xFF1A2030).copy(alpha = 0.88f),
        animationSpec = tween(durationMillis = 220),
        label = "glassSwitchTrack"
    )
    val trackBorder by animateColorAsState(
        targetValue = if (checked) PureWhite.copy(alpha = 0.34f) else PureWhite.copy(alpha = 0.18f),
        animationSpec = tween(durationMillis = 220),
        label = "glassSwitchBorder"
    )

    val rawSpeed = abs(progress.velocity) + dragVelocityBoost
    val isInMotion = isDragging || rawSpeed > 0.18f
    val jellyStretch by animateFloatAsState(
        targetValue = (rawSpeed * 0.09f).coerceIn(0f, 0.28f) + if (isDragging) 0.12f else 0f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "glassSwitchJelly"
    )
    val thumbScaleX = 1f + jellyStretch
    val thumbScaleY = (1f - jellyStretch * 0.36f).coerceAtLeast(0.84f)
    val thumbGlassAlpha by animateFloatAsState(
        targetValue = if (isInMotion) 0.18f else 0.82f,
        animationSpec = tween(durationMillis = 160),
        label = "glassSwitchThumbAlpha"
    )

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 58.dp, minHeight = 48.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { next ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(next)
                }
            )
            .pointerInput(enabled, maxTravelPx, checked) {
                if (!enabled || maxTravelPx <= 0f) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragVelocityBoost = 0.8f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragVelocityBoost = (abs(dragAmount) / 6f).coerceIn(0.3f, 2.5f)
                        val delta = dragAmount / maxTravelPx
                        coroutineScope.launch {
                            progress.snapTo((progress.value + delta).coerceIn(0f, 1f))
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        dragVelocityBoost = 0f
                        val nextChecked = progress.value >= 0.5f
                        if (nextChecked != checked) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCheckedChange(nextChecked)
                        } else {
                            coroutineScope.launch {
                                progress.animateTo(
                                    targetValue = if (checked) 1f else 0f,
                                    animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        dragVelocityBoost = 0f
                        coroutineScope.launch {
                            progress.animateTo(
                                targetValue = if (checked) 1f else 0f,
                                animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = trackWidthDp, height = trackHeightDp)
        ) {
            // Track layer captured for optical refraction by the sliding jelly thumb
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(trackBackdrop)
                    .clip(CircleShape)
                    .background(trackColor)
                    .border(1.dp, trackBorder, CircleShape)
            )

            // Sliding refractive Liquid Glass droplet thumb
            Box(
                modifier = Modifier
                    .padding(thumbInsetDp)
                    .offset {
                        IntOffset(x = (progress.value * maxTravelPx).roundToInt(), y = 0)
                    }
                    .size(width = thumbWidthDp, height = thumbHeightDp)
                    .drawBackdrop(
                        backdrop = trackBackdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            lens(
                                refractionHeight = 11.dp.toPx(),
                                refractionAmount = 22.dp.toPx(),
                                depthEffect = true,
                                chromaticAberration = true
                            )
                        },
                        highlight = { Highlight.Default },
                        shadow = {
                            Shadow(
                                radius = 8.dp,
                                offset = DpOffset(0.dp, 2.dp),
                                color = PureBlack.copy(alpha = 0.34f)
                            )
                        },
                        innerShadow = { InnerShadow.Default },
                        layerBlock = {
                            scaleX = thumbScaleX
                            scaleY = thumbScaleY
                        },
                        onDrawSurface = {
                            drawRect(PureWhite.copy(alpha = thumbGlassAlpha))
                        }
                    )
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                PureWhite.copy(alpha = 0.90f),
                                PureWhite.copy(alpha = 0.25f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Crisp Liquid Glass Sector Telemetry Badge replacing the 3D sphere on Radar and Sector Drawer.
 * Surfaces the geohash cell, discovery perimeter, and live neighbor & dispatch counters.
 */
@Composable
fun LiquidGlassSectorBadge(
    sectorName: String,
    geohash: String,
    radiusMeters: Int,
    neighborCount: Int,
    dispatchCount: Int,
    isScanning: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val badgeShape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
    val perimeterLabel = if (radiusMeters >= 1000) {
        val km = radiusMeters / 1000f
        if (km % 1f == 0f) "${km.toInt()} km" else "${String.format("%.1f", km)} km"
    } else {
        "${radiusMeters} m"
    }
    val cellLabel = if (geohash.isNotBlank()) {
        "Cell #${geohash.removePrefix("#").uppercase()}"
    } else {
        "Sector Active"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = resolvedBackdrop,
                shape = { badgeShape },
                effects = {
                    vibrancy()
                    blur(18.dp.toPx())
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 20.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 18.dp,
                        offset = DpOffset(0.dp, 8.dp),
                        color = PureBlack.copy(alpha = 0.36f)
                    )
                },
                innerShadow = { InnerShadow.Default },
                onDrawSurface = {
                    drawRect(Color(0xFF0E1422).copy(alpha = 0.74f))
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        PureWhite.copy(alpha = 0.32f),
                        HairlineGlassBorder.copy(alpha = 0.65f)
                    )
                ),
                shape = badgeShape
            )
            .padding(if (compact) 14.dp else 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top sector geohash + perimeter pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = CobaltSoft.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, CobaltBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) EmeraldMint else CobaltGlow)
                        )
                        Text(
                            text = cellLabel,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PureWhite
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = "$perimeterLabel zone",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = ObsidianInk,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = sectorName.ifBlank { "Local Sector" },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 19.sp else 23.sp,
                lineHeight = if (compact) 23.sp else 27.sp,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isScanning) {
                    "Scanning local sector frequencies..."
                } else {
                    "Encrypted coarse-grid neighborhood perimeter"
                },
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

            // Side-by-side crisp neighbor and broadcast counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectorCounterTile(
                    icon = Icons.Default.Group,
                    valueText = neighborCount.toString(),
                    label = if (neighborCount == 1) "Neighbor" else "Neighbors",
                    accentBg = CobaltSoft,
                    accentTint = CobaltGlow,
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
                SectorCounterTile(
                    icon = Icons.Default.Sensors,
                    valueText = dispatchCount.toString(),
                    label = if (dispatchCount == 1) "Broadcast" else "Broadcasts",
                    accentBg = EmeraldSoft,
                    accentTint = EmeraldLight,
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SectorCounterTile(
    icon: ImageVector,
    valueText: String,
    label: String,
    accentBg: Color,
    accentTint: Color,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(tileShape)
            .background(PureWhite.copy(alpha = 0.06f))
            .border(1.dp, PureWhite.copy(alpha = 0.14f), tileShape)
            .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 9.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 32.dp else 36.dp)
                .clip(CircleShape)
                .background(accentBg)
                .border(1.dp, accentTint.copy(alpha = 0.38f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentTint,
                modifier = Modifier.size(if (compact) 16.dp else 18.dp)
            )
        }
        Column {
            Text(
                text = valueText,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 18.sp else 20.sp,
                lineHeight = if (compact) 20.sp else 22.sp,
                color = PureWhite
            )
            Text(
                text = label,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MutedSlate
            )
        }
    }
}

/**
 * Single compact Apple Liquid Glass burger menu button with an optional active-filter count badge.
 */
@Composable
fun LiquidGlassBurgerMenuButton(
    activeFilterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    contentDescription: String = "Open sort and filter menu"
) {
    val resolvedBackdrop = rememberResolvedBackdrop(backdrop)
    val interactionSource = remember { MutableInteractionSource() }
    val (jellyX, jellyY) = rememberJellyPressScale(interactionSource)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .drawBackdrop(
                    backdrop = resolvedBackdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(14.dp.toPx())
                        lens(
                            refractionHeight = 8.dp.toPx(),
                            refractionAmount = 16.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    },
                    highlight = { Highlight.Default },
                    shadow = {
                        Shadow(
                            radius = 12.dp,
                            offset = DpOffset(0.dp, 4.dp),
                            color = PureBlack.copy(alpha = 0.34f)
                        )
                    },
                    innerShadow = { InnerShadow.Default },
                    layerBlock = {
                        scaleX = jellyX
                        scaleY = jellyY
                    },
                    onDrawSurface = {
                        val tint = if (activeFilterCount > 0) {
                            Color(0xFF1A346A).copy(alpha = 0.80f)
                        } else {
                            Color(0xFF121826).copy(alpha = 0.72f)
                        }
                        drawRect(tint)
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (activeFilterCount > 0) CobaltGlow.copy(alpha = 0.65f) else PureWhite.copy(alpha = 0.35f),
                            HairlineGlassBorder
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = contentDescription,
                tint = if (activeFilterCount > 0) PureWhite else ObsidianInk,
                modifier = Modifier.size(20.dp)
            )
        }

        if (activeFilterCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(ElectricCobalt)
                    .border(1.dp, PureWhite.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeFilterCount.toString(),
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PureWhite
                )
            }
        }
    }
}

/**
 * Elevated Frosted Glass Burger Popover Menu anchored beneath the single menu button.
 */
@Composable
fun FrostedGlassBurgerPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    width: Dp = 296.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    if (!expanded) return

    val popupBackdrop = rememberCanvasBackdrop {
        drawRect(Color(0xFF0B0F1A))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricCobalt.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.85f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.12f),
            radius = size.width * 0.85f
        )
    }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, with(density) { 52.dp.roundToPx() }),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        val menuShape = RoundedCornerShape(24.dp)
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(150)) + scaleIn(
                initialScale = 0.92f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)
            ),
            exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.94f)
        ) {
            Box(
                modifier = Modifier
                    .width(width)
                    .drawBackdrop(
                        backdrop = popupBackdrop,
                        shape = { menuShape },
                        effects = {
                            vibrancy()
                            blur(22.dp.toPx())
                            lens(
                                refractionHeight = 10.dp.toPx(),
                                refractionAmount = 18.dp.toPx(),
                                depthEffect = true,
                                chromaticAberration = true
                            )
                        },
                        highlight = { Highlight.Default },
                        shadow = {
                            Shadow(
                                radius = 24.dp,
                                offset = DpOffset(0.dp, 12.dp),
                                color = PureBlack.copy(alpha = 0.56f)
                            )
                        },
                        innerShadow = { InnerShadow.Default },
                        onDrawSurface = {
                            drawRect(Color(0xFF101624).copy(alpha = 0.88f))
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                PureWhite.copy(alpha = 0.34f),
                                HairlineGlassBorder.copy(alpha = 0.7f)
                            )
                        ),
                        shape = menuShape
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun FrostedMenuSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = AuraKickerStyle,
        color = MutedSlate,
        modifier = modifier
    )
}

@Composable
fun FrostedMenuOptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = ElectricCobalt
) {
    val haptic = LocalHapticFeedback.current
    val pillShape = CircleShape
    Box(
        modifier = modifier
            .clip(pillShape)
            .background(
                if (selected) selectedColor.copy(alpha = 0.90f) else PureWhite.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (selected) PureWhite.copy(alpha = 0.42f) else PureWhite.copy(alpha = 0.14f),
                shape = pillShape
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = if (selected) PureWhite else ObsidianInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Single-icon NearGrid brand emblem: bold white N-pulse monogram on Electric Blue.
 */
@Composable
fun NearGridBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    cornerRadius: Dp = 11.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(ElectricCobalt)
            .border(1.dp, PureWhite.copy(alpha = 0.28f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.22f)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = (w * 0.16f).coerceAtLeast(2.dp.toPx())

            // Bold N geometry
            val nPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.08f, h * 0.92f)
                lineTo(w * 0.08f, h * 0.10f)
                lineTo(w * 0.44f, h * 0.56f)
                moveTo(w * 0.56f, h * 0.44f)
                lineTo(w * 0.92f, h * 0.90f)
                lineTo(w * 0.92f, h * 0.08f)
            }
            drawPath(
                path = nPath,
                color = PureWhite,
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Crisp central signal pulse crossing the N
            val pulsePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h * 0.52f)
                lineTo(w * 0.28f, h * 0.52f)
                lineTo(w * 0.41f, h * 0.72f)
                lineTo(w * 0.59f, h * 0.26f)
                lineTo(w * 0.72f, h * 0.52f)
                lineTo(w, h * 0.52f)
            }
            drawPath(
                path = pulsePath,
                color = PureWhite,
                style = Stroke(
                    width = (strokeW * 0.62f).coerceAtLeast(1.5.dp.toPx()),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}

