package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationDestination
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
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

/**
 * Apple iOS 26 Liquid Glass Unified 5-Tab Navigation Capsule powered by io.github.kyant0:backdrop.
 * Features a dark smoked glass dock with crisp specular rims and a sliding clear magnifying
 * jelly lens capsule with chromatic edge dispersion over the underlying tab icons and labels.
 */
@Composable
fun LiquidGlassBottomBar(
    backdrop: LayerBackdrop,
    destinations: List<NavigationDestination>,
    currentDestination: NavigationDestination,
    onSelectDestination: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val selectedIndex = destinations.indexOf(currentDestination).coerceAtLeast(0)
    val animatedIndex = remember { Animatable(selectedIndex.toFloat()) }

    var isScrubbing by remember { mutableStateOf(false) }
    var dragVelocityBoost by remember { mutableFloatStateOf(0f) }
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var lastHapticSlot by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex, isScrubbing) {
        if (!isScrubbing) {
            lastHapticSlot = selectedIndex
            animatedIndex.animateTo(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.54f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val pillShape = RoundedCornerShape(50)
    val innerPillShape = RoundedCornerShape(50)
    val horizontalInsetDp = 5.dp
    val horizontalInsetPx = with(density) { horizontalInsetDp.toPx() }

    val itemCount = destinations.size.coerceAtLeast(1)
    val usableWidthPx = (containerWidthPx - horizontalInsetPx * 2f).coerceAtLeast(1f)
    val slotWidthPx = usableWidthPx / itemCount

    val rawVelocity = abs(animatedIndex.velocity) + dragVelocityBoost
    val stretchFactor by animateFloatAsState(
        targetValue = (rawVelocity * 0.06f).coerceIn(0f, 0.30f) + if (isScrubbing) 0.08f else 0f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "jellyStretch"
    )
    val swellFactor by animateFloatAsState(
        targetValue = if (isScrubbing || rawVelocity > 0.2f) 0.08f else 0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = Spring.StiffnessMediumLow),
        label = "jellySwell"
    )
    val jellyScaleX = 1f + stretchFactor + swellFactor
    val jellyScaleY = (1f - stretchFactor * 0.38f + swellFactor).coerceAtLeast(0.82f)

    val dockBackdrop = rememberLayerBackdrop()
    val activeGlassBlue = Color(0xFF60A5FF)

    Box(
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .height(66.dp)
            .testTag("bottom_nav_bar")
            .onSizeChanged { containerWidthPx = it.width }
            .pointerInput(destinations, containerWidthPx) {
                if (containerWidthPx <= 0) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isScrubbing = true
                        dragVelocityBoost = 1.5f
                        val normalized = ((offset.x - horizontalInsetPx) / slotWidthPx)
                            .coerceIn(0f, (itemCount - 1).toFloat())
                        coroutineScope.launch {
                            animatedIndex.snapTo(normalized)
                        }
                        val slot = normalized.roundToInt().coerceIn(0, itemCount - 1)
                        if (slot != lastHapticSlot) {
                            lastHapticSlot = slot
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectDestination(destinations[slot])
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragVelocityBoost = (abs(dragAmount) / 12f).coerceIn(0.4f, 4.2f)
                        val normalized = ((change.position.x - horizontalInsetPx) / slotWidthPx)
                            .coerceIn(0f, (itemCount - 1).toFloat())
                        coroutineScope.launch {
                            animatedIndex.snapTo(normalized)
                        }
                        val slot = normalized.roundToInt().coerceIn(0, itemCount - 1)
                        if (slot != lastHapticSlot) {
                            lastHapticSlot = slot
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectDestination(destinations[slot])
                        }
                    },
                    onDragEnd = {
                        isScrubbing = false
                        dragVelocityBoost = 0f
                        val targetSlot = animatedIndex.value.roundToInt().coerceIn(0, itemCount - 1)
                        onSelectDestination(destinations[targetSlot])
                        coroutineScope.launch {
                            animatedIndex.animateTo(
                                targetValue = targetSlot.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = 0.54f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        isScrubbing = false
                        dragVelocityBoost = 0f
                        coroutineScope.launch {
                            animatedIndex.animateTo(
                                targetValue = selectedIndex.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = 0.54f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                )
            }
    ) {
        // Layer 1: Dark Smoked Liquid Glass Dock captured into dockBackdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(dockBackdrop)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { pillShape },
                        effects = {
                            vibrancy()
                            blur(20.dp.toPx())
                            lens(
                                refractionHeight = 14.dp.toPx(),
                                refractionAmount = 28.dp.toPx(),
                                depthEffect = true,
                                chromaticAberration = true
                            )
                        },
                        highlight = { Highlight.Default },
                        shadow = {
                            Shadow(
                                radius = 24.dp,
                                offset = DpOffset(0.dp, 10.dp),
                                color = PureBlack.copy(alpha = 0.52f)
                            )
                        },
                        innerShadow = { InnerShadow.Default },
                        onDrawSurface = {
                            drawRect(Color(0xFF0E121C).copy(alpha = 0.72f))
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                PureWhite.copy(alpha = 0.30f),
                                HairlineGlassBorder.copy(alpha = 0.65f)
                            )
                        ),
                        shape = pillShape
                    )
            )
        }

        // Layer 2 + Layer 3: Clear Jelly Glass Pill underneath razor-sharp tab icons and labels
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalInsetDp, vertical = 5.dp)
        ) {
            // Clear Chromatic Jelly Lens Capsule (no blur; placed behind icons/labels so contents remain crystal clear)
            if (containerWidthPx > 0) {
                val indicatorWidthDp = with(density) { slotWidthPx.toDp() }
                val indicatorOffsetX = (animatedIndex.value * slotWidthPx).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = indicatorOffsetX, y = 0) }
                        .width(indicatorWidthDp)
                        .fillMaxHeight()
                        .drawBackdrop(
                            backdrop = dockBackdrop,
                            shape = { innerPillShape },
                            effects = {
                                vibrancy()
                                lens(
                                    refractionHeight = 10.dp.toPx(),
                                    refractionAmount = 22.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            },
                            highlight = { Highlight.Default },
                            shadow = {
                                Shadow(
                                    radius = 12.dp,
                                    offset = DpOffset(0.dp, 4.dp),
                                    color = PureBlack.copy(alpha = 0.28f)
                                )
                            },
                            innerShadow = { InnerShadow.Default },
                            layerBlock = {
                                this.scaleX = jellyScaleX
                                this.scaleY = jellyScaleY
                            },
                            onDrawSurface = {
                                drawRect(PureWhite.copy(alpha = 0.11f))
                            }
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    PureWhite.copy(alpha = 0.58f),
                                    activeGlassBlue.copy(alpha = 0.28f),
                                    PureWhite.copy(alpha = 0.36f)
                                )
                            ),
                            shape = innerPillShape
                        )
                )
            }

            // Crisp Destination Slots rendered cleanly above the clear glass pill without blur
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = index == selectedIndex
                    val focusProximity = (1f - abs(animatedIndex.value - index)).coerceIn(0f, 1f)
                    val contentScale = 1f + 0.08f * focusProximity

                    val (iconFilled, iconOutlined) = when (destination) {
                        NavigationDestination.HOME -> Icons.Filled.Home to Icons.Outlined.Home
                        NavigationDestination.MAP -> Icons.Filled.Map to Icons.Outlined.Map
                        NavigationDestination.RADAR -> Icons.Filled.Radar to Icons.Outlined.Radar
                        NavigationDestination.COMMUNITY -> Icons.Filled.Campaign to Icons.Outlined.Campaign
                        NavigationDestination.INBOX -> Icons.Filled.ChatBubble to Icons.Outlined.ChatBubbleOutline
                        NavigationDestination.PROFILE -> Icons.Filled.Person to Icons.Outlined.Person
                    }

                    val compactDisplayLabel = when (destination) {
                        NavigationDestination.HOME -> "Feed"
                        NavigationDestination.MAP -> "Map"
                        NavigationDestination.RADAR -> "Radar"
                        NavigationDestination.COMMUNITY -> "Pulse"
                        NavigationDestination.INBOX -> "Inbox"
                        NavigationDestination.PROFILE -> "Profile"
                    }

                    val tintColor by animateColorAsState(
                        targetValue = when {
                            isSelected && destination == NavigationDestination.COMMUNITY -> FieryCoralGlow
                            isSelected -> activeGlassBlue
                            else -> PureWhite.copy(alpha = 0.90f)
                        },
                        label = "navTint"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(innerPillShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true, color = PureWhite.copy(alpha = 0.14f)),
                                role = Role.Tab,
                                onClick = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectDestination(destination)
                                    }
                                }
                            )
                            .graphicsLayer {
                                scaleX = contentScale
                                scaleY = contentScale
                            }
                            .testTag("nav_item_${destination.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) iconFilled else iconOutlined,
                            contentDescription = destination.label,
                            tint = tintColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = compactDisplayLabel,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            color = tintColor
                        )
                    }
                }
            }
        }
    }
}
