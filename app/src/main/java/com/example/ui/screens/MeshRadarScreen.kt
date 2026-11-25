package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeohashMessage
import com.example.data.model.GridCell
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CapsuleChevronRow
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GeohashMessageComposer
import com.example.ui.components.GlowingOrbVisualizer
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshRadarScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    currentCell: GridCell?,
    currentGeohash: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    localityName: String = "",
    isLocating: Boolean = false,
    isPostingGeohashMessage: Boolean = false,
    geohashPostStatus: String? = null,
    geohashMessages: List<GeohashMessage> = emptyList(),
    onRefreshLocation: () -> Unit = {},
    onPostGeohashMessage: (String) -> Unit = {},
    onDismissGeohashStatus: () -> Unit = {},
    onOpenCreateDialog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("mesh_radar_pull_to_refresh")
    ) {
        AuraMeshBackground(variant = AuraVariant.MIXED) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (localityName.isNotBlank()) "Geohash Mesh · $localityName" else "Hyperlocal Spatial Mesh",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = CobaltGlow
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mesh Radar & Intent",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 30.sp,
                                color = PureWhite
                            )
                        }

                        CircularOutlineIconButton(
                            icon = Icons.Default.GpsFixed,
                            contentDescription = "Refresh GPS Location",
                            onClick = onRefreshLocation,
                            modifier = Modifier.testTag("radar_refresh_location_button")
                        )
                    }
                }

                // Glowing Cosmic Cobalt Orb Card (Orb style)
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DarkGlassCard(
                            cornerRadius = 28.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = CobaltSoft,
                                        border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = if (currentGeohash.isNotBlank()) "Cell #${currentGeohash.uppercase()}" else "Cell Active",
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldSoft,
                                        border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = if (isLocating) "Scanning..." else "${geohashMessages.size} Dispatches",
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = EmeraldLight,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                GlowingOrbVisualizer(
                                    modifier = Modifier.size(195.dp),
                                    activeNodesCount = (geohashMessages.size + 4).coerceIn(4, 8),
                                    radiusMeters = 1000,
                                    isScanning = isLocating
                                )

                                Text(
                                    text = currentCell?.name ?: localityName.ifBlank { "Oakwood Sector" },
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "1 km privacy-preserving radio broadcast zone",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }

                // Geohash Message Composer
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DarkGlassCard(
                            cornerRadius = 24.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            GeohashMessageComposer(
                                currentGeohash = currentGeohash,
                                latitude = latitude,
                                longitude = longitude,
                                localityName = localityName,
                                isLocating = isLocating,
                                isPosting = isPostingGeohashMessage,
                                postStatus = geohashPostStatus,
                                messages = geohashMessages,
                                onRefreshLocation = onRefreshLocation,
                                onPostMessage = onPostGeohashMessage,
                                onDismissStatus = onDismissGeohashStatus
                            )
                        }
                    }
                }

                // Tonesmith-style Capsule Chevron Quick Dispatch Rows
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Quick Intent Dispatches",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        CapsuleChevronRow(
                            title = "Request a Need",
                            subtitle = "Ask nearby neighbors for tools or urgent favors",
                            icon = Icons.Default.HelpOutline,
                            iconBackgroundColor = FieryCoralSoft,
                            iconTint = FieryCoralGlow,
                            badgeText = "Need",
                            badgeColor = FieryCoralGlow,
                            onClick = { onOpenCreateDialog("NEED") },
                            modifier = Modifier.testTag("create_need_tile")
                        )

                        CapsuleChevronRow(
                            title = "Lend or Offer Help",
                            subtitle = "Share equipment, supplies, or local expertise",
                            icon = Icons.Default.VolunteerActivism,
                            iconBackgroundColor = EmeraldSoft,
                            iconTint = EmeraldLight,
                            badgeText = "Offer",
                            badgeColor = EmeraldMint,
                            onClick = { onOpenCreateDialog("OFFER") },
                            modifier = Modifier.testTag("create_offer_tile")
                        )

                        CapsuleChevronRow(
                            title = "Host a Meetup",
                            subtitle = "Organize neighborhood walks, cleanup, or study",
                            icon = Icons.Default.AutoAwesome,
                            iconBackgroundColor = CobaltSoft,
                            iconTint = CobaltGlow,
                            badgeText = "Meetup",
                            badgeColor = ElectricCobalt,
                            onClick = { onOpenCreateDialog("MEETUP") },
                            modifier = Modifier.testTag("create_meetup_tile")
                        )
                    }
                }

                // Protocol Note Dark Glass Card
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DarkGlassCard(cornerRadius = 24.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ElevatedGlassSurface)
                                        .border(1.dp, HairlineGlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = CobaltGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "About Mesh Broadcasts",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = PureWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Mesh broadcasts use base32 geohashes to connect neighbors within a 1 km spatial zone without sharing your street address or personal identity.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .height(118.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}
