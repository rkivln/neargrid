package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.data.model.CommunityIssue
import com.example.data.model.GridCell
import com.example.data.model.NearbyNeighbor
import com.example.data.model.NeighborPost
import com.example.ui.components.OpenStreetMapView
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DarkNavy
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
        modifier = modifier.fillMaxSize().testTag("realtime_map_pull_to_refresh")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
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

            // Top Floating Header & Controls Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite.copy(alpha = 0.96f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().testTag("realtime_map_header_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PastelPeach.copy(alpha = 0.5f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = DarkNavy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Neighborhood Radar",
                                        fontFamily = DMSerifFontFamily,
                                        fontSize = 26.sp,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = userLocation?.displayAreaName ?: currentCell?.name ?: "Acquiring GPS fix...",
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                }
                            }

                            // Refresh GPS action
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SoftCream)
                                    .border(1.dp, WarmBorder, CircleShape)
                                    .testTag("realtime_map_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh GPS",
                                    tint = PrimaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats & Radius Pill Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Neighbor count badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftCream,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(PastelMint)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${nearbyNeighbors.size} ${if (nearbyNeighbors.size == 1) "neighbor" else "neighbors"} in ${LocationHelper.formatDistance(discoveryRadiusMeters.toFloat())}",
                                        fontFamily = DMSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = PrimaryText
                                    )
                                }
                            }

                            // Layer filter pills
                            val radiusOptions = listOf(500, 1000, 2000, 5000, 10000)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                radiusOptions.take(3).forEach { radius ->
                                    val isSelected = discoveryRadiusMeters == radius
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) PastelPeach else SoftCream,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PastelCoral else WarmBorder
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onRadiusChange(radius) }
                                    ) {
                                        Text(
                                            text = LocationHelper.formatDistance(radius.toFloat()),
                                            fontFamily = DMSansFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp,
                                            color = PrimaryText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Layer Selection Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PureWhite,
                                selectedLabelColor = PrimaryText,
                                containerColor = PureWhite.copy(alpha = 0.85f),
                                labelColor = SecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) DarkNavy else WarmBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Dedicated Neighbor Profile Inspection Modal
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

        // Dedicated Report / Block Safety Modal
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
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = PureWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("neighbor_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Top close action & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Neighbor Profile",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftCream)
                            .testTag("close_neighbor_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Avatar, Name, Handle & Online Presence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PastelPeach.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, WarmBorder),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.displayName.take(1).uppercase().ifBlank { "N" },
                                fontFamily = DMSerifFontFamily,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName.ifBlank { "Local Resident" },
                            fontFamily = DMSerifFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = user.handle.ifBlank { "@resident" },
                            fontFamily = DMSansFontFamily,
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Online indicator
                        if (!user.hideOnlineStatus) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (user.isOnline) PastelMint else Color.Gray.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (user.isOnline) "Active now on NearGrid" else "Recently in neighborhood",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText
                                )
                            }
                        } else {
                            Text(
                                text = "Active in neighborhood",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = SecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Distance & Neighborhood Location Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PastelPeach.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        val distanceText = if (user.hideExactDistance) {
                            if (neighbor.distanceMeters <= 500) "📍 Proximity: < 500m away" else "📍 In your neighborhood"
                        } else {
                            "📍 ${LocationHelper.formatDistance(neighbor.distanceMeters)} away"
                        }
                        Text(
                            text = distanceText,
                            fontFamily = DMSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    if (user.cellName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftCream,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = user.cellName,
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bio / Description
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ABOUT NEIGHBOR",
                            fontFamily = DMSansFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio.ifBlank { "Active local community member participating in localized mutual assistance and neighborhood coordination." },
                            fontFamily = DMSansFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = PrimaryText
                        )
                    }
                }

                // Latest post snippet if available
                neighbor.latestPost?.let { post ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (post.type == "NEED") PastelCoral else PastelMint
                                ) {
                                    Text(
                                        text = post.type,
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = post.title,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons: Start Private Chat & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_profile_dialog_button")
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { onStartChat(neighbor) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavy,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("start_chat_from_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Private Chat",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Safety: Report or Block neighbor
                OutlinedButton(
                    onClick = onOpenReportBlock,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelCoral.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PastelCoral
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("report_block_neighbor_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PastelCoral,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report or Block Neighbor",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PastelCoral
                    )
                }
            }
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
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("report_block_neighbor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PastelCoral.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = PastelCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Report Neighbor",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = user.displayName.ifBlank { "Neighbor" },
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELECT A REASON",
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SoftCream else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryText else WarmBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) PrimaryText else SecondaryText,
                                            CircleShape
                                        )
                                        .background(if (isSelected) PrimaryText else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = reason,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 250) notes = it },
                    label = { Text("Additional details (optional)") },
                    placeholder = { Text("Explain context to help moderators...") },
                    modifier = Modifier.fillMaxWidth().testTag("report_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryText,
                        unfocusedBorderColor = WarmBorder,
                        focusedContainerColor = SoftCream,
                        unfocusedContainerColor = SoftCream
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Also block neighbor toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelCoral.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelCoral.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { alsoBlock = !alsoBlock }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = PastelCoral,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Also block this neighbor",
                                fontFamily = DMSansFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText
                            )
                            Text(
                                text = "Hides them from your map and prevents them from messaging you.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                        Switch(
                            checked = alsoBlock,
                            onCheckedChange = { alsoBlock = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            ),
                            modifier = Modifier.testTag("also_block_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        onReportUser(user.userId, user.displayName, selectedReason, notes.trim())
                        if (alsoBlock) {
                            onBlockUser(user.userId)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelCoral,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_report_button")
                ) {
                    Text(
                        text = if (alsoBlock) "Report & Block Neighbor" else "Submit Report",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
