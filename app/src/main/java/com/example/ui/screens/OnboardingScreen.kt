package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.GlowingOrbVisualizer
import com.example.ui.components.NearGridBrandMark
import com.example.ui.components.rememberResolvedBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 4

    val auraVariant = when (currentSlide) {
        0 -> AuraVariant.COBALT
        1 -> AuraVariant.CORAL
        2 -> AuraVariant.COBALT
        else -> AuraVariant.MIXED
    }

    AuraMeshBackground(
        variant = auraVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Minimal Brand Bar + Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NearGridBrandMark(size = 34.dp)
                    Text(
                        text = "NEARGRID",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        color = PureWhite
                    )
                }

                if (currentSlide < totalSlides - 1) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PureWhite.copy(alpha = 0.85f)
                        )
                    }
                } else {
                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onFinish,
                        size = 40.dp
                    )
                }
            }

            // Animated Slide Body
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "AuraOnboardingSlides"
            ) { slide ->
                when (slide) {
                    0 -> OnboardingSlideIntroduction()
                    1 -> OnboardingSlideDiscovery()
                    2 -> OnboardingSlideConnection()
                    else -> OnboardingSlideGetStarted()
                }
            }

            // MRR-style Segmented Bar + Bottom Stepper Dock
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSlides) {
                        val isCurrent = i == currentSlide
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isCurrent) 28.dp else 10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) PureWhite else PureWhite.copy(alpha = 0.28f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // MRR Capsule Dock: [ <- ]  [ ( -> ) ]  [ Start >>> ]
                val actionTag = when (currentSlide) {
                    0 -> "onboarding_next_1"
                    1 -> "onboarding_next_2"
                    2 -> "onboarding_next_3"
                    else -> "onboarding_finish_button"
                }

                val dockBackdrop = rememberResolvedBackdrop()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .drawBackdrop(
                            backdrop = dockBackdrop,
                            shape = { CircleShape },
                            effects = {
                                vibrancy()
                                blur(18.dp.toPx())
                                lens(
                                    refractionHeight = 14.dp.toPx(),
                                    refractionAmount = 26.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            },
                            highlight = { Highlight.Default },
                            shadow = {
                                Shadow(
                                    radius = 20.dp,
                                    offset = DpOffset(0.dp, 8.dp),
                                    color = Color.Black.copy(alpha = 0.45f)
                                )
                            },
                            innerShadow = { InnerShadow.Default },
                            onDrawSurface = {
                                drawRect(Color(0xFF0E121C).copy(alpha = 0.72f))
                            }
                        )
                        .border(1.dp, PureWhite.copy(alpha = 0.24f), CircleShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Back Button
                        CircularOutlineIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous slide",
                            onClick = { if (currentSlide > 0) currentSlide -= 1 else onFinish() },
                            size = 48.dp,
                            containerColor = Color(0xFF161B28).copy(alpha = 0.72f),
                            borderColor = PureWhite.copy(alpha = 0.22f),
                            iconTint = if (currentSlide > 0) PureWhite else MutedSlate
                        )

                        // Luminous Pearl White Liquid Glass Next / Enter Button
                        CircularOutlineIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = if (currentSlide < totalSlides - 1) "Continue" else "Enter NearGrid",
                            onClick = {
                                if (currentSlide < totalSlides - 1) currentSlide += 1 else onFinish()
                            },
                            size = 50.dp,
                            containerColor = PureWhite.copy(alpha = 0.90f),
                            borderColor = PureWhite,
                            iconTint = ObsidianBlack,
                            modifier = Modifier.testTag(actionTag)
                        )

                        // Right "Continue >>>" Smoked Liquid Glass Track
                        GlassSecondaryPillButton(
                            text = when (currentSlide) {
                                0 -> "Continue"
                                1 -> "Explore"
                                2 -> "Match"
                                else -> "Enter Grid"
                            },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            onClick = {
                                if (currentSlide < totalSlides - 1) currentSlide += 1 else onFinish()
                            },
                            height = 48.dp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrostedOnboardingTag(
    text: String,
    accentColor: Color = CobaltGlow
) {
    Surface(
        shape = CircleShape,
        color = PureWhite.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = accentColor.takeIf { it == PureWhite } ?: PureWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun OnboardingSlideIntroduction() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, PureWhite.copy(alpha = 0.20f), RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_neighborhood_editorial),
                contentDescription = "Neighbors illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF090B10).copy(alpha = 0.55f)
                            )
                        )
                    )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FrostedOnboardingTag(text = "Today on Your Block")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Neighborhood Aid,\n")
                    withStyle(SpanStyle(color = CobaltGlow)) {
                        append("Zero Noise.")
                    }
                },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 33.sp,
                lineHeight = 38.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Discover real neighbors nearby to lend tools, coordinate local favors, and solve civic issues together.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun OnboardingSlideDiscovery() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp),
            contentAlignment = Alignment.Center
        ) {
            GlowingOrbVisualizer(
                modifier = Modifier.size(230.dp),
                activeNodesCount = 6,
                radiusMeters = 800,
                isScanning = true
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FrostedOnboardingTag(text = "H3 Spatial Mesh")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Privacy Without\n")
                    withStyle(SpanStyle(color = FieryCoralGlow)) {
                        append("Isolation.")
                    }
                },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 33.sp,
                lineHeight = 38.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Geographic rings determine nearby discovery without ever revealing your exact residential pin.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun OnboardingSlideConnection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, PureWhite.copy(alpha = 0.20f), RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_neighborhood_connection),
                contentDescription = "Neighbor connection",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF090B10).copy(alpha = 0.55f)
                            )
                        )
                    )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FrostedOnboardingTag(text = "Autonomous Intent")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Needs Meet\n")
                    withStyle(SpanStyle(color = CobaltGlow)) {
                        append("Neighbor Offers.")
                    }
                },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 33.sp,
                lineHeight = 38.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Skip endless feeds. NearGrid pairs someone who needs a ladder with a neighbor ready to lend one.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun OnboardingSlideGetStarted() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FrostedOnboardingTag(text = "Grid Cluster Ready")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Connect With\n")
                    withStyle(SpanStyle(color = CobaltGlow)) {
                        append("Your Sector.")
                    }
                },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 39.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your resident pass is ready. Browse needs, lend skills, and coordinate safe local handoffs.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center
            )
        }

        DarkGlassCard(
            cornerRadius = 24.dp,
            containerColor = DarkGlassCard.copy(alpha = 0.88f)
        ) {
            Text(
                text = "Community Charter",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            val principles = listOf(
                "Zero public home GPS coordinates",
                "Verified public pavilion meeting spots",
                "Transparent local civic issue tracking",
                "Pure local utility without feed clutter"
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                principles.forEach { line ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = line,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 13.sp,
                            color = ObsidianInk
                        )
                    }
                }
            }
        }
    }
}
