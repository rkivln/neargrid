package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.GeohashMessage
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.data.update.AppUpdateInfo
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.CobaltHeroCard
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GeohashMessageComposer
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.GlowingOrbVisualizer
import com.example.ui.components.IntentMatchCard
import com.example.ui.components.PillPrimaryButton
import com.example.ui.components.PostCard
import com.example.ui.components.TopNeighborhoodBar
import com.example.ui.components.UpdateBanner
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltPillGradient
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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    isLocating: Boolean = false,
    currentGeohash: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    localityName: String = "",
    isPostingGeohashMessage: Boolean = false,
    geohashPostStatus: String? = null,
    geohashMessages: List<GeohashMessage> = emptyList(),
    onRefreshLocation: () -> Unit = {},
    onPostGeohashMessage: (String) -> Unit = {},
    onDismissGeohashStatus: () -> Unit = {},
    posts: List<NeighborPost>,
    notices: List<CommunityNotice> = emptyList(),
    issues: List<CommunityIssue> = emptyList(),
    topIntentMatches: List<IntentMatchResult>,
    selectedFilterType: String,
    selectedCategory: String,
    onCellClick: () -> Unit,
    onGpsToggle: () -> Unit,
    onOpenMapClick: (() -> Unit)? = null,
    onShowQrClick: (() -> Unit)? = null,
    onScanQrClick: (() -> Unit)? = null,
    onRadiusChange: (Int) -> Unit,
    onFilterChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onConnectPost: (NeighborPost) -> Unit,
    onToggleUpvote: (NeighborPost) -> Unit = {},
    onOpenComments: (NeighborPost) -> Unit = {},
    onToggleIssueUpvote: (CommunityIssue) -> Unit = {},
    onUpdateIssueStatus: (issueId: String, newStatus: String, volunteer: String?) -> Unit = { _, _, _ -> },
    currentUserId: String? = null,
    onEditPost: ((NeighborPost, String, String, String, String) -> Unit)? = null,
    onDeletePost: ((NeighborPost) -> Unit)? = null,
    onOpenCreateDialog: (String) -> Unit,
    updateInfo: AppUpdateInfo? = null,
    isUpdateBannerDismissed: Boolean = false,
    onDismissUpdateBanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isNearbyFeedMode by remember { mutableStateOf(false) }
    var showRadarDrawer by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_pull_to_refresh")
    ) {
        AuraMeshBackground(variant = AuraVariant.MIXED) {
            val feedBackdrop = rememberLayerBackdrop()
            if (isNearbyFeedMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(feedBackdrop)
                ) {
                    TopNeighborhoodBar(
                        currentCell = currentCell,
                        discoveryRadiusMeters = discoveryRadiusMeters,
                        gpsEnabled = gpsEnabled,
                        isLocating = isLocating,
                        onCellClick = onCellClick,
                        onGpsToggle = onGpsToggle,
                        onOpenMapClick = onOpenMapClick,
                        onShowQrClick = onShowQrClick,
                        onScanQrClick = onScanQrClick
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby Distance Feed",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassSecondaryPillButton(
                                text = "Radar",
                                onClick = { showRadarDrawer = true },
                                contentColor = CobaltGlow,
                                height = 36.dp,
                                modifier = Modifier.testTag("home_radar_drawer_button")
                            )
                            PillPrimaryButton(
                                text = "Back to Feed",
                                onClick = { isNearbyFeedMode = false },
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("feed_mode_grid")
                            )
                        }
                    }

                    NearbyFeedComponent(
                        userLatitude = latitude,
                        userLongitude = longitude,
                        initialRadiusMeters = discoveryRadiusMeters,
                        onConnectPost = onConnectPost,
                        onOpenComments = onOpenComments,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(feedBackdrop),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TopNeighborhoodBar(
                            currentCell = currentCell,
                            discoveryRadiusMeters = discoveryRadiusMeters,
                            gpsEnabled = gpsEnabled,
                            isLocating = isLocating,
                            onCellClick = onCellClick,
                            onGpsToggle = onGpsToggle,
                            onOpenMapClick = onOpenMapClick,
                            onShowQrClick = onShowQrClick,
                            onScanQrClick = onScanQrClick
                        )
                    }

                    item(key = "app_update_banner") {
                        UpdateBanner(
                            updateInfoOverride = updateInfo,
                            isDismissedOverride = isUpdateBannerDismissed,
                            onDismissOverride = onDismissUpdateBanner
                        )
                    }

                    // Streamlined Cobalt Hero Card: 1-tap New Post + Inline Radius Chips + 1-tap Radar Drawer
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            val sectorName = currentCell?.name ?: localityName.ifBlank { "Local Sector" }
                            CobaltHeroCard(
                                eyebrowText = "$sectorName · ${posts.size} Live",
                                titleText = "Block Feed",
                                trailingIcon = Icons.Default.Radar,
                                trailingIconDescription = "Open Sector Radar Drawer",
                                onTrailingIconClick = { showRadarDrawer = true },
                                trailingIconTestTag = "home_radar_drawer_button",
                                primaryCtaText = "New Post",
                                primaryCtaIcon = Icons.Default.Add,
                                onPrimaryCtaClick = { onOpenCreateDialog("NEED") },
                                primaryCtaTestTag = "hero_new_dispatch_button",
                                extraContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val radii = listOf(300 to "300m", 800 to "800m", 2000 to "2 km")
                                        radii.forEach { (radius, label) ->
                                            val isSelected = discoveryRadiusMeters == radius
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) PureWhite else Color.White.copy(alpha = 0.14f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) PureWhite else Color.White.copy(alpha = 0.25f),
                                                        CircleShape
                                                    )
                                                    .clickable { onRadiusChange(radius) }
                                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                                    .testTag("radius_chip_$radius"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontFamily = PlusJakartaFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) ElectricCobalt else PureWhite
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.14f))
                                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                                .clickable { isNearbyFeedMode = true }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("feed_mode_nearby"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NearMe,
                                                contentDescription = "Nearby distance sort",
                                                tint = PureWhite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Nearby",
                                                fontFamily = PlusJakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = PureWhite
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Capsule Filter Bar (All | Needs | Offers | Meetups | Civic)
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val filterTypes = listOf(
                                "ALL" to "All",
                                "NEED" to "Needs",
                                "OFFER" to "Offers",
                                "MEETUP" to "Meetups",
                                "CIVIC" to "Civic"
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filterTypes) { (type, label) ->
                                    val isSelected = selectedFilterType == type
                                    val activeBg = when (type) {
                                        "NEED" -> FieryCoral
                                        "OFFER" -> EmeraldMint
                                        "CIVIC" -> FieryCoral
                                        else -> ElectricCobalt
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) activeBg else DarkGlassCard)
                                            .border(
                                                1.dp,
                                                if (isSelected) PureWhite.copy(alpha = 0.35f) else HairlineGlassBorder,
                                                CircleShape
                                            )
                                            .clickable { onFilterChange(type) }
                                            .padding(horizontal = 16.dp, vertical = 9.dp)
                                            .testTag("filter_pill_${type.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) PureWhite else MutedSlate
                                        )
                                    }
                                }
                            }

                            if (selectedFilterType != "CIVIC") {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val categories = listOf("ALL", "TOOLS", "HOUSEHOLD", "HELP", "STUDY", "SPORTS")
                                    items(categories) { cat ->
                                        val isSelected = selectedCategory == cat
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onCategoryChange(cat) },
                                            label = {
                                                Text(
                                                    if (cat == "ALL") "All topics" else cat.lowercase().replaceFirstChar { it.uppercase() },
                                                    fontFamily = PlusJakartaFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CobaltSoft,
                                                selectedLabelColor = PureWhite,
                                                containerColor = DarkGlassCard,
                                                labelColor = MutedSlate
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = if (isSelected) ElectricCobalt else HairlineGlassBorder,
                                                enabled = true,
                                                selected = isSelected
                                            ),
                                            shape = CircleShape
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedFilterType == "CIVIC") {
                        // Inline Civic Bulletin & Issue Tracker
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Civic Noticeboard & Issues",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "${notices.size} advisories · ${issues.size} local reports",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 12.sp,
                                        color = MutedSlate
                                    )
                                }

                                PillPrimaryButton(
                                    text = "Report Issue",
                                    icon = Icons.Default.Add,
                                    onClick = { onOpenCreateDialog("ISSUE") },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("report_issue_button")
                                )
                            }
                        }

                        if (notices.isEmpty() && issues.isEmpty()) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    DarkGlassCard(
                                        modifier = Modifier.testTag("empty_issues_card"),
                                        cornerRadius = 24.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldSoft)
                                                    .border(1.dp, EmeraldMint.copy(alpha = 0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = EmeraldLight,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "All Clear in Your Sector",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = PureWhite
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No civic notices or maintenance hazards right now. Spot an issue nearby?",
                                                fontFamily = PlusJakartaFontFamily,
                                                fontSize = 13.sp,
                                                color = MutedSlate,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 19.sp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            PillPrimaryButton(
                                                text = "Report an Issue",
                                                onClick = { onOpenCreateDialog("ISSUE") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("fresh_report_issue_button")
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(notices, key = { "notice_${it.id}" }) { notice ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    NoticeCard(notice = notice)
                                }
                            }
                            items(issues, key = { "issue_${it.id}" }) { issue ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    IssueCard(
                                        issue = issue,
                                        onUpvote = { onToggleIssueUpvote(issue) },
                                        onAdvanceStatus = {
                                            val nextStatus = when (issue.status) {
                                                "REPORTED" -> "ACKNOWLEDGED"
                                                "ACKNOWLEDGED" -> "IN_PROGRESS"
                                                "IN_PROGRESS" -> "RESOLVED"
                                                else -> "RESOLVED"
                                            }
                                            onUpdateIssueStatus(issue.id, nextStatus, "Assigned Local Volunteers")
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        if (topIntentMatches.isNotEmpty() && selectedFilterType == "ALL") {
                            item {
                                IntentMatchCard(
                                    match = topIntentMatches.first(),
                                    onConnect = onConnectPost
                                )
                            }
                        }

                        if (posts.isEmpty()) {
                            item {
                                if (!gpsEnabled || (latitude == 0.0 && longitude == 0.0)) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        DarkGlassCard(
                                            modifier = Modifier.testTag("location_required_card"),
                                            cornerRadius = 24.dp
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(FieryCoralSoft)
                                                        .border(1.dp, FieryCoral.copy(alpha = 0.5f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOff,
                                                        contentDescription = null,
                                                        tint = FieryCoralGlow,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(
                                                    text = "Location Required for Local Grid",
                                                    fontFamily = OutfitFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 21.sp,
                                                    color = PureWhite,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Enable location access to discover posts inside your local block.",
                                                    fontFamily = PlusJakartaFontFamily,
                                                    fontSize = 13.sp,
                                                    color = MutedSlate,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 19.sp
                                                )
                                                Spacer(modifier = Modifier.height(18.dp))
                                                PillPrimaryButton(
                                                    text = "Enable Location",
                                                    onClick = onGpsToggle,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("enable_gps_button")
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        DarkGlassCard(
                                            modifier = Modifier.testTag("fresh_grid_empty_card"),
                                            cornerRadius = 24.dp
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(CobaltSoft)
                                                        .border(1.dp, ElectricCobalt.copy(alpha = 0.5f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.VolunteerActivism,
                                                        contentDescription = null,
                                                        tint = CobaltGlow,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(
                                                    text = "No Posts Yet in ${localityName.ifBlank { "Your Block" }}",
                                                    fontFamily = OutfitFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = PureWhite,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Be the first neighbor nearby to ask for a hand or lend a spare tool.",
                                                    fontFamily = PlusJakartaFontFamily,
                                                    fontSize = 13.sp,
                                                    color = MutedSlate,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 19.sp
                                                )
                                                Spacer(modifier = Modifier.height(18.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    PillPrimaryButton(
                                                        text = "Ask Help",
                                                        onClick = { onOpenCreateDialog("NEED") },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("fresh_ask_help_button")
                                                    )
                                                    GlassSecondaryPillButton(
                                                        text = "Lend Tool",
                                                        onClick = { onOpenCreateDialog("OFFER") },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("fresh_offer_button")
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(posts, key = { it.id }) { post ->
                                PostCard(
                                    post = post,
                                    onConnect = onConnectPost,
                                    onToggleUpvote = onToggleUpvote,
                                    onOpenComments = onOpenComments,
                                    currentUserId = currentUserId,
                                    onEditPost = onEditPost,
                                    onDeletePost = onDeletePost
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

            // 1-Tap Liquid Glass Post Pill Floating Action Button above 5-tab Glass Dock
            PillPrimaryButton(
                text = "Post",
                icon = Icons.Default.Add,
                onClick = { onOpenCreateDialog("NEED") },
                backdrop = feedBackdrop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 92.dp)
                    .height(50.dp)
                    .testTag("fab_create_post")
            )
        }
    }

    // Quick Radar & Geohash Broadcast Drawer
    if (showRadarDrawer) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRadarDrawer = false },
            sheetState = sheetState,
            containerColor = ObsidianCanvas,
            contentColor = PureWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "LIVE SECTOR DRAWER",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CobaltGlow
                        )
                        Text(
                            text = "Radar & Quick Broadcast",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PureWhite
                        )
                    }
                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close Radar Drawer",
                        onClick = { showRadarDrawer = false },
                        size = 40.dp
                    )
                }

                DarkGlassCard(
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GlowingOrbVisualizer(
                            modifier = Modifier.size(96.dp),
                            activeNodesCount = geohashMessages.size.coerceAtLeast(3),
                            radiusMeters = discoveryRadiusMeters,
                            isScanning = isLocating || latitude != 0.0
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentCell?.name ?: localityName.ifBlank { "Local Sector" },
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (currentGeohash.isNotBlank()) {
                                    "Geohash #${currentGeohash.removePrefix("#").uppercase()} · ${discoveryRadiusMeters}m"
                                } else {
                                    "Perimeter · ${discoveryRadiusMeters}m"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${geohashMessages.size} recent radio broadcasts",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)

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
}

@Composable
fun AppUpdateBanner(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("app_update_banner"),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CobaltSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "New App Version",
                            tint = CobaltGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "New Version Available: v${updateInfo.latestVersion}",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "Tap to update to the latest release",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = MutedSlate
                        )
                    }
                }

                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Dismiss Banner",
                    onClick = onDismiss,
                    size = 36.dp,
                    modifier = Modifier.testTag("dismiss_update_banner_button")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillPrimaryButton(
                    text = "Update (v${updateInfo.latestVersion})",
                    icon = Icons.Default.Download,
                    onClick = {
                        val targetUrl = updateInfo.apkDownloadUrl.ifBlank { updateInfo.releaseHtmlUrl }
                        if (targetUrl.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("download_update_button")
                )

                if (updateInfo.releaseHtmlUrl.isNotBlank()) {
                    GlassSecondaryPillButton(
                        text = "Release Notes",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releaseHtmlUrl))
                            context.startActivity(intent)
                        },
                        height = 44.dp,
                        modifier = Modifier.testTag("view_release_notes_button")
                    )
                }
            }
        }
    }
}
