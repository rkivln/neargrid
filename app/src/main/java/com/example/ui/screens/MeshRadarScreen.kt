package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeohashMessage
import com.example.data.model.GridCell
import com.example.ui.components.GeohashMessageComposer
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.PastelButterYellow
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelSoftCyan
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory

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
        modifier = modifier.fillMaxSize().testTag("mesh_radar_pull_to_refresh")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                item {
                    Surface(
                        color = PureWhite,
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = PastelSoftCyan.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, WarmBorder),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Radar,
                                            contentDescription = null,
                                            tint = PrimaryText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Mesh Radar & Intent",
                                        fontFamily = DMSerifFontFamily,
                                        fontSize = 26.sp,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = if (localityName.isNotBlank()) "Geohash Area: $localityName" else "Hyperlocal area broadcast & actions",
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRefreshLocation,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SoftCream)
                                    .border(1.dp, WarmBorder, CircleShape)
                                    .testTag("radar_refresh_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "Refresh GPS Location",
                                    tint = PrimaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Hyperlocal Geohash Broadcast Area Composer
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
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

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Quick Intent Action Grid (Editorial Numbered Tiles)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QUICK INTENT ACTIONS",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 01 Ask Need
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PastelCoral.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, WarmBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCreateDialog("NEED") }
                                    .testTag("create_need_tile")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "01", fontFamily = DMSerifFontFamily, fontSize = 18.sp, color = PrimaryText)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Request Need", fontFamily = DMSansFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                                    Text(text = "Tools, labor, supplies", fontFamily = DMSansFontFamily, fontSize = 10.sp, color = SecondaryText)
                                }
                            }

                            // 02 Offer Skill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PastelMint.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, WarmBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCreateDialog("OFFER") }
                                    .testTag("create_offer_tile")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "02", fontFamily = DMSerifFontFamily, fontSize = 18.sp, color = PrimaryText)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Lend / Offer", fontFamily = DMSansFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                                    Text(text = "Spare items, skills", fontFamily = DMSansFontFamily, fontSize = 10.sp, color = SecondaryText)
                                }
                            }

                            // 03 Meetup
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PastelButterYellow.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, WarmBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCreateDialog("MEETUP") }
                                    .testTag("create_meetup_tile")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "03", fontFamily = DMSerifFontFamily, fontSize = 18.sp, color = PrimaryText)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Host Meetup", fontFamily = DMSansFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                                    Text(text = "Walks, cleanups", fontFamily = DMSansFontFamily, fontSize = 10.sp, color = SecondaryText)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // How Mesh Radar Works Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = PrimaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "About Mesh Broadcasts",
                                    fontFamily = DMSerifFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mesh broadcasts use base32 geohashes to connect neighbors within a 1km spatial zone without sharing your house address or personal identity.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
