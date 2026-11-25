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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CommunityIssue
import com.example.data.model.GridCell
import com.example.data.model.NearbyNeighbor
import com.example.data.model.NeighborPost
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.OpenStreetMapView
import com.example.ui.components.PillPrimaryButton
import com.example.ui.components.WhiteHeroPillButton
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
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
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate
import com.example.util.LocationHelper
import com.example.util.UserLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeMapScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    userLocation: UserLocation?,
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    nearbyNeighbors: List<NearbyNeighbor>,
    posts: List<NeighborPost>,
    issues: List<CommunityIssue>,
    onRadiusChange: (Int) -> Unit,
    onNeighborClick: (NearbyNeighbor) -> Unit = {},
    onStartChatWithNeighbor: (NearbyNeighbor) -> Unit = {},
    onBlockNeighbor: (String) -> Unit = {},
    onReportNeighbor: (targetUserId: String, targetDisplayName: String, reason: String, notes: String) -> Unit = { _, _, _, _ -> },
    onPostClick: (NeighborPost) -> Unit = {},
    onIssueClick: (CommunityIssue) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf("ALL") }
    var profileNeighbor by remember { mutableStateOf<NearbyNeighbor?>(null) }
    var reportBlockNeighbor by remember { mutableStateOf<NearbyNeighbor?>(null) }

    val filteredNeighbors = remember(nearbyNeighbors, selectedLayer) {
        if (selectedLayer == "NEEDS_ONLY") {
            nearbyNeighbors.filter { it.latestPost?.type == "NEED" }
        } else if (selectedLayer == "OFFERS_ONLY") {
            nearbyNeighbors.filter { it.latestPost?.type == "OFFER" }
        } else {
            nearbyNeighbors
        }
    }

    val filteredPosts = remember(posts, selectedLayer) {
        when (selectedLayer) {
            "NEIGHBORS_ONLY" -> emptyList()
            "NEEDS_ONLY" -> posts.filter { it.type == "NEED" }
            "OFFERS_ONLY" -> posts.filter { it.type == "OFFER" }
            else -> posts
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("realtime_map_pull_to_refresh")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianCanvas)
        ) {
            // Full-bleed OpenStreetMap View
            OpenStreetMapView(
                userLocation = userLocation,
                nearbyNeighbors = filteredNeighbors,
                discoveryRadiusMeters = discoveryRadiusMeters,
                posts = filteredPosts,
                issues = if (selectedLayer == "ALL") issues else emptyList(),
                onNeighborClick = onNeighborClick,
                onStartChatWithNeighbor = onStartChatWithNeighbor,
                onViewNeighborProfile = { profileNeighbor = it },
                onPostClick = onPostClick,
                onIssueClick = onIssueClick,
                modifier = Modifier.fillMaxSize()
            )

            // Top Floating Dark Glass Header & Controls Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkGlassCard.copy(alpha = 0.93f),
                    border = BorderStroke(1.dp, HairlineGlassBorder),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("realtime_map_header_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(CobaltHeroGradient)
                                        .border(1.dp, CobaltGlow.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "LIVE RADAR SECTOR",
                                        style = AuraKickerStyle,
                                        color = CobaltGlow
                                    )
                                    Text(
                                        text = "Neighborhood Radar",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp,
                                        lineHeight = 25.sp,
                                        color = ObsidianInk
                                    )
                                    Text(
                                        text = userLocation?.displayAreaName ?: currentCell?.name ?: "Acquiring GPS fix...",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 11.sp,
                                        color = MutedSlate
                                    )
                                }
                            }

                            CircularOutlineIconButton(
                                icon = Icons.Default.Refresh,
                                contentDescription = "Refresh GPS",
                                onClick = onRefresh,
                                size = 42.dp,
                                modifier = Modifier.testTag("realtime_map_refresh_button")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats & Radius Pill Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ElevatedGlassSurface,
                                border = BorderStroke(1.dp, HairlineGlassBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldMint)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${nearbyNeighbors.size} ${if (nearbyNeighbors.size == 1) "NEIGHBOR" else "NEIGHBORS"} · ${LocationHelper.formatDistance(discoveryRadiusMeters.toFloat()).uppercase()}",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = ObsidianInk
                                    )
                                }
                            }

                            val radiusOptions = listOf(500, 1000, 2000)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                radiusOptions.forEach { radius ->
                                    val isSelected = discoveryRadiusMeters == radius
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) ElectricCobalt else ElevatedGlassSurface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) CobaltGlow else HairlineGlassBorder
                                        ),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { onRadiusChange(radius) }
                                    ) {
                                        Text(
                                            text = LocationHelper.formatDistance(radius.toFloat()),
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.sp,
                                            color = if (isSelected) PureWhite else MutedSlate,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Layer Selection Capsule Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val layers = listOf(
                        "ALL" to "All in range",
                        "NEIGHBORS_ONLY" to "Neighbors only",
                        "NEEDS_ONLY" to "Needs",
                        "OFFERS_ONLY" to "Offers"
                    )
                    items(layers) { (key, label) ->
                        val isSelected = selectedLayer == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLayer = key },
                            label = {
                                Text(
                                    label,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCobalt,
                                selectedLabelColor = PureWhite,
                                containerColor = DarkGlassCard.copy(alpha = 0.90f),
                                labelColor = MutedSlate
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) CobaltGlow else HairlineGlassBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = CircleShape
                        )
                    }
                }
            }
        }

        profileNeighbor?.let { neighbor ->
            NeighborProfileDialog(
                neighbor = neighbor,
                onDismiss = { profileNeighbor = null },
                onStartChat = {
                    onStartChatWithNeighbor(it)
                    profileNeighbor = null
                },
                onOpenReportBlock = {
                    reportBlockNeighbor = neighbor
                    profileNeighbor = null
                }
            )
        }

        reportBlockNeighbor?.let { neighbor ->
            ReportBlockNeighborDialog(
                neighbor = neighbor,
                onDismiss = { reportBlockNeighbor = null },
                onBlockUser = { userId ->
                    onBlockNeighbor(userId)
                    reportBlockNeighbor = null
                },
                onReportUser = { uid, name, reason, notes ->
                    onReportNeighbor(uid, name, reason, notes)
                }
            )
        }
    }
}

@Composable
fun NeighborProfileDialog(
    neighbor: NearbyNeighbor,
    onDismiss: () -> Unit,
    onStartChat: (NearbyNeighbor) -> Unit,
    onOpenReportBlock: () -> Unit
) {
    val user = neighbor.user
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("neighbor_profile_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEIGHBOR DOSSIER",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Neighbor Profile",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ObsidianInk
                    )
                }
                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    size = 40.dp,
                    modifier = Modifier.testTag("close_neighbor_profile_button")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(CobaltHeroGradient)
                        .border(1.5.dp, CobaltGlow.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.take(1).uppercase().ifBlank { "N" },
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName.ifBlank { "Local Resident" },
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ObsidianInk
                    )
                    Text(
                        text = user.handle.ifBlank { "@resident" },
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!user.hideOnlineStatus) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (user.isOnline) EmeraldMint else SubtleSlate)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.isOnline) "ACTIVE ON NEARGRID" else "RECENTLY IN CELL",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (user.isOnline) EmeraldLight else MutedSlate
                            )
                        }
                    } else {
                        Text(
                            text = "ACTIVE IN NEIGHBORHOOD",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 10.sp,
                            color = MutedSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = CobaltSoft,
                    border = BorderStroke(1.dp, CobaltBorder.copy(alpha = 0.5f))
                ) {
                    val distanceText = if (user.hideExactDistance) {
                        if (neighbor.distanceMeters <= 500) "PROXIMITY < 500M" else "IN YOUR CELL"
                    } else {
                        "${LocationHelper.formatDistance(neighbor.distanceMeters).uppercase()} AWAY"
                    }
                    Text(
                        text = distanceText,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CobaltGlow,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (user.cellName.isNotBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = ElevatedGlassSurface,
                        border = BorderStroke(1.dp, HairlineGlassBorder)
                    ) {
                        Text(
                            text = user.cellName,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObsidianInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ElevatedGlassSurface,
                border = BorderStroke(1.dp, HairlineGlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ABOUT NEIGHBOR",
                        style = AuraKickerStyle,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.bio.ifBlank { "Active local community member participating in localized mutual assistance and neighborhood coordination." },
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = ObsidianInk
                    )
                }
            }

            neighbor.latestPost?.let { post ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedGlassSurface,
                    border = BorderStroke(1.dp, HairlineGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "LATEST DISPATCH",
                            style = AuraKickerStyle,
                            color = SubtleSlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isNeed = post.type == "NEED"
                            Surface(
                                shape = CircleShape,
                                color = if (isNeed) FieryCoralSoft else EmeraldSoft
                            ) {
                                Text(
                                    text = post.type,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNeed) FieryCoralGlow else EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.title,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ObsidianInk,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassSecondaryPillButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dismiss_profile_dialog_button")
                )

                WhiteHeroPillButton(
                    text = "Private Chat",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    onClick = { onStartChat(neighbor) },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("start_chat_from_dialog_button")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            GlassSecondaryPillButton(
                text = "Report or Block Neighbor",
                icon = Icons.Default.Shield,
                onClick = onOpenReportBlock,
                surfaceTint = FieryCoralSoft,
                contentColor = FieryCoralGlow,
                borderColor = FieryCoral.copy(alpha = 0.5f),
                height = 48.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_block_neighbor_button")
            )
        }
    }
}

@Composable
fun ReportBlockNeighborDialog(
    neighbor: NearbyNeighbor,
    onDismiss: () -> Unit,
    onBlockUser: (String) -> Unit,
    onReportUser: (targetUserId: String, targetDisplayName: String, reason: String, notes: String) -> Unit
) {
    val user = neighbor.user
    val reasons = listOf(
        "Harassment or unwanted contact",
        "Spam, selling, or advertising",
        "Inappropriate or offensive content",
        "Safety concern or suspicious activity",
        "Impersonation or fake account"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var notes by remember { mutableStateOf("") }
    var alsoBlock by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("report_block_neighbor_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FieryCoralSoft)
                            .border(1.dp, FieryCoral.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = FieryCoralGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SAFETY REPORT",
                            style = AuraKickerStyle,
                            color = FieryCoralGlow
                        )
                        Text(
                            text = "Report ${user.displayName.ifBlank { "Neighbor" }}",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = ObsidianInk
                        )
                    }
                }
                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    size = 38.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SELECT A REASON",
                style = AuraKickerStyle,
                color = MutedSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reasons.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) CobaltSoft else ElevatedGlassSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ElectricCobalt else HairlineGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedReason = reason }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) CobaltGlow else MutedSlate,
                                        CircleShape
                                    )
                                    .background(if (isSelected) ElectricCobalt else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = reason,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = ObsidianInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= 250) notes = it },
                label = { Text("Additional details (optional)", fontFamily = PlusJakartaFontFamily, color = MutedSlate) },
                placeholder = { Text("Explain context to help moderators...", fontFamily = PlusJakartaFontFamily, color = SubtleSlate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_notes_input"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCobalt,
                    unfocusedBorderColor = HairlineGlassBorder,
                    focusedContainerColor = ElevatedGlassSurface,
                    unfocusedContainerColor = ElevatedGlassSurface,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = ObsidianInk
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = FieryCoralSoft,
                border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { alsoBlock = !alsoBlock }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = FieryCoralGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Also block this neighbor",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ObsidianInk
                        )
                        Text(
                            text = "Hides them from your map and prevents them from messaging you.",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }
                    LiquidGlassSwitch(
                        checked = alsoBlock,
                        onCheckedChange = { alsoBlock = it },
                        activeColor = FieryCoral,
                        modifier = Modifier.testTag("also_block_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PillPrimaryButton(
                text = if (alsoBlock) "Report & Block Neighbor" else "Submit Report",
                useCoral = true,
                onClick = {
                    onReportUser(user.userId, user.displayName, selectedReason, notes.trim())
                    if (alsoBlock) {
                        onBlockUser(user.userId)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_report_button")
            )
        }
    }
}
