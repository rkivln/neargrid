package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.PaleWarmGray
import com.example.ui.theme.PastelButterYellow
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelPowderBlue
import com.example.ui.theme.PastelSoftCyan
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 4

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
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
            label = "WarmEditorialOnboarding"
        ) { slide ->
            when (slide) {
                0 -> OnboardingSlideIntroduction(
                    onNext = { currentSlide = 1 },
                    onSkip = onFinish
                )
                1 -> OnboardingSlideDiscovery(
                    onNext = { currentSlide = 2 },
                    onSkip = onFinish
                )
                2 -> OnboardingSlideConnection(
                    onNext = { currentSlide = 3 },
                    onSkip = onFinish
                )
                else -> OnboardingSlideGetStarted(
                    onGetStarted = onFinish
                )
            }
        }

        // Top Navigation Bar (Progress Dots & Skip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant slim progress pill indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until totalSlides) {
                    val isCurrent = i == currentSlide
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(if (isCurrent) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isCurrent) PrimaryText else PaleWarmGray)
                    )
                }
            }

            if (currentSlide < totalSlides - 1) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }
            } else {
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Slide 1 — Introduction
@Composable
fun OnboardingSlideIntroduction(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Upper-middle: Concise, elegant DM Serif headline with highlighted phrase
        Column {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftCream,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Text(
                    text = "Welcome to NearGrid",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Neighborhood\ncoordination,\nwithout the noise.",
                fontFamily = DMSerifFontFamily,
                fontSize = 38.sp,
                lineHeight = 44.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted phrase with a soft pastel brush stroke underline
            Box {
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .height(10.dp)
                        .width(220.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PastelPeach.copy(alpha = 0.65f))
                )
                Text(
                    text = "Real people, nearby places.",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 18.sp,
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Discover who lives around you to lend tools, solve civic issues, and build real community trust.",
                fontFamily = DMSansFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = SecondaryText
            )
        }

        // Bottom artwork: Fine black ink line art with pastel watercolor washes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, WarmBorder, RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_neighborhood_editorial),
                contentDescription = "Neighbors illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_next_1"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryText,
                contentColor = PureWhite
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Slide 2 — Discovery
@Composable
fun OnboardingSlideDiscovery(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Centered illustration with generous negative space
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, WarmBorder, RoundedCornerShape(26.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_community_gathering),
                contentDescription = "Discovery scene",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PastelMint.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "H3 Spatial Mesh",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PrimaryText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Privacy without isolation",
                fontFamily = DMSerifFontFamily,
                fontSize = 28.sp,
                color = PrimaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Distance determines which requests and community events you discover, using geographic cell rings without ever exposing your exact home pin.",
                fontFamily = DMSansFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_next_2"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryText,
                contentColor = PureWhite
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Explore the mesh",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Slide 3 — Connection
@Composable
fun OnboardingSlideConnection(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Character illustration with warm pastel watercolor brush strokes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, WarmBorder, RoundedCornerShape(26.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_neighborhood_connection),
                contentDescription = "Warm lantern doorway",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PastelSoftCyan.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "Autonomous Intent Matching",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PrimaryText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Needs meet neighborly offers",
                fontFamily = DMSerifFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Instead of endlessly scrolling social media feeds, NearGrid matches you with someone who has the ladder you need or wants to join your local cricket game.",
                fontFamily = DMSansFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = SecondaryText
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_next_3"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryText,
                contentColor = PureWhite
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "See how it works",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Slide 4 — Get started
@Composable
fun OnboardingSlideGetStarted(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PastelPeach.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Oakwood Grove Cluster Ready",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PrimaryText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Connect with your\nneighborhood today.",
                fontFamily = DMSerifFontFamily,
                fontSize = 36.sp,
                lineHeight = 42.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your digital neighborhood pass is ready. You can browse local requests, offer skills, and arrange safe handoffs at the Oakwood Pavilion.",
                fontFamily = DMSansFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = SecondaryText
            )
        }

        // Summary editorial card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Community Principles",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 16.sp,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Zero public home GPS coordinates\n• Transparent civic issue tracking\n• Verified public meeting spots\n• Pure local utility, no algorithmic feed rage",
                    fontFamily = DMSansFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = SecondaryText
                )
            }
        }

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_finish_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryText,
                contentColor = PureWhite
            )
        ) {
            Text(
                text = "Enter NearGrid",
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = PureWhite
            )
        }
    }
}
