package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.example.ui.theme.NeutralGray
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividPink

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 4

    Box(modifier = modifier.fillMaxSize()) {
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
            label = "OnboardingSlides"
        ) { slide ->
            when (slide) {
                0 -> SlideAVividMinimalism(
                    onNext = { currentSlide = 1 },
                    onSkip = onFinish
                )
                1 -> SlideBDarkEditorial(
                    onNext = { currentSlide = 2 },
                    onSkip = onFinish
                )
                2 -> SlideCPlayfulDimensional(
                    onNext = { currentSlide = 3 },
                    onSkip = onFinish
                )
                else -> SlideDExperimentalGraphic(
                    onGetStarted = onFinish
                )
            }
        }

        // Top Navigation Bar (Skip and Page Indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Dots Indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until totalSlides) {
                    val isCurrent = i == currentSlide
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isCurrent) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (currentSlide == 0) (if (isCurrent) PureBlack else PureBlack.copy(alpha = 0.25f))
                                else if (currentSlide == 2) (if (isCurrent) PureBlack else PureBlack.copy(alpha = 0.25f))
                                else (if (isCurrent) ElectricLime else EditorialWhite.copy(alpha = 0.3f))
                            )
                    )
                }
            }

            if (currentSlide < totalSlides - 1) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "SKIP",
                        fontFamily = AntonFontFamily,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = if (currentSlide == 0 || currentSlide == 2) PureBlack else EditorialWhite
                    )
                }
            }
        }
    }
}

/**
 * Slide A — Vivid Minimalism
 * Electric lime / acid-yellow background, bold black typography, centered product illustration, strong black CTA.
 */
@Composable
fun SlideAVividMinimalism(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElectricLime)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Center Hero: Neighborhood Illustration Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, PureBlack, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_community_banner),
                contentDescription = "Neighborhood illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Asymmetric Swiss Label Tag
            Surface(
                color = PureBlack,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "DISCOVERY / RADIUS: 800M",
                    color = ElectricLime,
                    fontFamily = AntonFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Editorial Copy
        Column {
            Text(
                text = "HYPERLOCAL\nCOORDINATION.",
                fontFamily = AntonFontFamily,
                fontSize = 46.sp,
                lineHeight = 46.sp,
                color = PureBlack,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Real neighbors, nearby resources, and direct assistance—connected through privacy-preserving coordinate cells.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PureBlack.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }

        // High contrast Black CTA
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = PureBlack),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_next_1")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CONTINUE",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = ElectricLime
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ElectricLime,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Slide B — Dark Editorial
 * Nearly black background, oversized charcoal typography in the background, central 3D abstract object, bright accent CTA.
 */
@Composable
fun SlideBDarkEditorial(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Oversized background words extending beyond screen
        Text(
            text = "INTENT",
            fontFamily = AntonFontFamily,
            fontSize = 120.sp,
            color = CharcoalBlack.copy(alpha = 0.8f),
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Central 3D Abstract Sculpture (Chrome & Lime Torus)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_3d_mesh_torus),
                    contentDescription = "3D Chrome Torus Sculpture",
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            // Headline & Description
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INTENT-BASED MATCHING",
                        color = ElectricLime,
                        fontFamily = AntonFontFamily,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "NEEDS MEET\nNEARBY OFFERS.",
                    fontFamily = AntonFontFamily,
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    color = EditorialWhite,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No aimless social scrolling. NearGrid intelligently pairs your requests for tools or favors with what nearby neighbors can share.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralGray,
                    lineHeight = 20.sp
                )
            }

            // Lime CTA
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_2")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "EXPLORE MATCHING",
                        fontFamily = AntonFontFamily,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = PureBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PureBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Slide C — Playful Dimensional
 * Clean white background, vivid pink and electric blue geometric 3D elements, organic shapes.
 */
@Composable
fun SlideCPlayfulDimensional(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditorialWhite)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Central Visual: Playful 3D spheres & geometry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_3d_sphere_abstract),
                contentDescription = "Playful 3D geometric visual",
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        // Headline & Description
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = VividPink,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CRICKET",
                        color = EditorialWhite,
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = ElectricBlue,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "STUDY",
                        color = EditorialWhite,
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = AcidYellow,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "GARDENING",
                        color = PureBlack,
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FIND YOUR\nLOCAL PEOPLE.",
                fontFamily = AntonFontFamily,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                color = PureBlack,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Discover spontaneous micro-meetups, casual sports games, and interest circles happening right around your corner.",
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalBlack.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        // Vibrant Pink CTA
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = VividPink),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_next_3")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NEXT",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = EditorialWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = EditorialWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Slide D — Experimental Graphic
 * High-contrast composition with large condensed typography, irregular silhouettes, and privacy guarantees.
 */
@Composable
fun SlideDExperimentalGraphic(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Graphic Card with privacy manifesto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrightCyan, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureBlack)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BrightCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PRIVACY MANIFESTO",
                            fontFamily = AntonFontFamily,
                            fontSize = 18.sp,
                            color = BrightCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "• ZERO RESIDENTIAL HOME PINS",
                        fontFamily = AntonFontFamily,
                        fontSize = 16.sp,
                        color = EditorialWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• DISTANCE BANDS ONLY (< 400M)",
                        fontFamily = AntonFontFamily,
                        fontSize = 16.sp,
                        color = EditorialWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• VERIFIED PUBLIC MEETING SPOTS",
                        fontFamily = AntonFontFamily,
                        fontSize = 16.sp,
                        color = EditorialWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• CIVIC ISSUE RESOLUTION",
                        fontFamily = AntonFontFamily,
                        fontSize = 16.sp,
                        color = EditorialWhite,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Headline
            Column {
                Text(
                    text = "RADICAL PRIVACY.\nREAL NEIGHBORS.",
                    fontFamily = AntonFontFamily,
                    fontSize = 46.sp,
                    lineHeight = 48.sp,
                    color = EditorialWhite,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Coordinates empower discovery without exposing your location. Welcome to your new living neighborhood grid.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralGray,
                    lineHeight = 20.sp
                )
            }

            // Get Started Cyan CTA
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = BrightCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_get_started")
            ) {
                Text(
                    text = "ENTER NEARGRID",
                    fontFamily = AntonFontFamily,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp,
                    color = PureBlack
                )
            }
        }
    }
}
