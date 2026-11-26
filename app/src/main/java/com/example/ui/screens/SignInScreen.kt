package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.data.auth.findValidActivity
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.NearGridBrandMark
import com.example.ui.components.WhiteHeroPillButton
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleSlate

@Composable
fun SignInScreen(
    authState: AuthState = AuthState.Unauthenticated,
    onSignInClick: (Activity) -> Unit = {},
    onDismissError: () -> Unit = {},
    onSignInSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var localContextError by remember { mutableStateOf<String?>(null) }

    val isLoading = authState is AuthState.Authenticating
    val errorMessage = localContextError ?: (authState as? AuthState.Error)?.message

    val handleGoogleSignIn = {
        if (!isLoading) {
            localContextError = null
            onDismissError()
            val activity = context.findValidActivity()
            if (activity == null) {
                localContextError = "Unable to start Google Sign-In because the screen is not ready. Please try again."
            } else {
                onSignInClick(activity)
            }
        }
    }

    AuraMeshBackground(
        variant = AuraVariant.COBALT,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Zentra-style Brand Badge + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NearGridBrandMark(size = 36.dp)
                    Text(
                        text = "NearGrid",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PureWhite
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "Zero-Trust Mesh",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Pure Atmospheric Cobalt Aura + Crisp Outfit Headline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Own Your Block,\nProtect ")
                        withStyle(SpanStyle(color = CobaltGlow)) {
                            append("Your Privacy.")
                        }
                    },
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    lineHeight = 41.sp,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Spatial mesh mutual aid and hyper-local civic resilience for verified neighbors.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MutedSlate
                )
            }

            // 24dp Translucent Dark Glass Pillars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkGlassCard(
                    cornerRadius = 22.dp,
                    containerColor = DarkGlassCard.copy(alpha = 0.86f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    FeaturePillar(
                        icon = Icons.Default.Security,
                        title = "Zero GPS Tracking",
                        description = "Coarse hexagonal sectors keep exact home coordinates private.",
                        accentColor = EmeraldSoft,
                        iconTint = EmeraldLight
                    )
                }
                DarkGlassCard(
                    cornerRadius = 22.dp,
                    containerColor = DarkGlassCard.copy(alpha = 0.86f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    FeaturePillar(
                        icon = Icons.Default.Handshake,
                        title = "Real-Time Mutual Aid",
                        description = "Direct neighbor needs, tool sharing, and civic dispatches.",
                        accentColor = AmberSoft,
                        iconTint = AmberLight
                    )
                }
                DarkGlassCard(
                    cornerRadius = 22.dp,
                    containerColor = DarkGlassCard.copy(alpha = 0.86f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    FeaturePillar(
                        icon = Icons.Default.Lock,
                        title = "Verified Resident Pass",
                        description = "Sign in with Google to protect your block board from spam.",
                        accentColor = FieryCoralSoft,
                        iconTint = FieryCoralGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Full-Pill Pure White CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = FieryCoralSoft,
                        border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("sign_in_error_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = ObsidianInk,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = ObsidianInk,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        localContextError = null
                                        onDismissError()
                                    }
                            )
                        }
                    }
                }

                WhiteHeroPillButton(
                    text = if (isLoading) "Authenticating..." else "Continue with Google",
                    icon = if (isLoading) null else Icons.Default.LocationOn,
                    enabled = !isLoading,
                    onClick = { handleGoogleSignIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Protected by NearGrid Zero-Trust Privacy Rules",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = SubtleSlate,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeaturePillar(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor)
                .border(1.dp, HairlineGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PureWhite
            )
            Text(
                text = description,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MutedSlate
            )
        }
    }
}
