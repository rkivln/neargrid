package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.FrostedGlassFilterSheet
import com.example.ui.components.FrostedMenuOptionPill
import com.example.ui.components.FrostedMenuSectionTitle
import com.example.ui.components.FrostedMenuWrapRow
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassBurgerMenuButton
import com.example.ui.components.PillPrimaryButton
import com.example.ui.components.PostMediaCarousel
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
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
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    notices: List<CommunityNotice>,
    issues: List<CommunityIssue>,
    onToggleUpvote: (CommunityIssue) -> Unit,
    onUpdateIssueStatus: (issueId: String, newStatus: String, volunteer: String?) -> Unit,
    onOpenReportIssue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Noticeboard, 1 = Issue Tracker
    var issueStatusFilter by remember { mutableStateOf("ALL") }
    var showIssueFilterMenu by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("community_pull_to_refresh")
    ) {
        AuraMeshBackground(variant = AuraVariant.MIXED) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header + Segment Toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Civic Noticeboard",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FieryCoralGlow
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Community Pulse",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 30.sp,
                                color = PureWhite
                            )
                            Text(
                                text = "Municipal bulletin & neighbor-led resolutions",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate
                            )
                        }

                        if (selectedTab == 1) {
                            Box(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(CircleShape)
                                    .background(CobaltPillGradient)
                                    .border(1.dp, CobaltGlow.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onOpenReportIssue() }
                                    .padding(horizontal = 14.dp)
                                    .testTag("report_issue_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Report",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        color = DarkGlassCard,
                        border = BorderStroke(1.dp, HairlineGlassBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == 0) CobaltPillGradient else Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    )
                                    .clickable { selectedTab = 0 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Noticeboard (${notices.size})",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == 0) PureWhite else MutedSlate
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == 1) CobaltPillGradient else Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    )
                                    .clickable { selectedTab = 1 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Issue tracker (${issues.size})",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == 1) PureWhite else MutedSlate
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    if (notices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                                .padding(start = 24.dp, end = 24.dp, bottom = 92.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DarkGlassCard(
                                modifier = Modifier.testTag("empty_notices_card"),
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
                                            .background(FieryCoralSoft)
                                            .border(1.dp, FieryCoral.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = FieryCoralGlow,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "No Civic Notices",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp,
                                        color = PureWhite
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Scheduled maintenance, municipal updates, and neighborhood advisories will appear here.",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 13.sp,
                                        color = MutedSlate,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(notices, key = { it.id }) { notice ->
                                NoticeCard(notice = notice)
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
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            val statuses = listOf(
                                "ALL" to "All issues",
                                "REPORTED" to "Reported",
                                "ACKNOWLEDGED" to "Acknowledged",
                                "IN_PROGRESS" to "In progress",
                                "RESOLVED" to "Resolved"
                            )
                            val activeLabel = statuses.firstOrNull { it.first == issueStatusFilter }?.second ?: "All issues"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Showing: $activeLabel",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (issueStatusFilter != "ALL") CobaltGlow else MutedSlate
                                )

                                LiquidGlassBurgerMenuButton(
                                    activeFilterCount = if (issueStatusFilter != "ALL") 1 else 0,
                                    onClick = { showIssueFilterMenu = true },
                                    contentDescription = "Filter civic issues",
                                    modifier = Modifier.testTag("community_filter_menu_button")
                                )
                            }
                        }

                        val filteredIssues = issues.filter {
                            if (issueStatusFilter == "ALL") true else it.status == issueStatusFilter
                        }

                        if (filteredIssues.isEmpty()) {
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
                                                text = "All Clear in the Neighborhood",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = PureWhite
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No civic maintenance issues or safety hazards reported. Spot a broken lamp or pothole?",
                                                fontFamily = PlusJakartaFontFamily,
                                                fontSize = 13.sp,
                                                color = MutedSlate,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 19.sp
                                            )
                                            Spacer(modifier = Modifier.height(18.dp))
                                            PillPrimaryButton(
                                                text = "Report an Issue",
                                                onClick = onOpenReportIssue,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("fresh_report_issue_button")
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredIssues, key = { it.id }) { issue ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    IssueCard(
                                        issue = issue,
                                        onUpvote = { onToggleUpvote(issue) },
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

        val communityStatuses = listOf(
            "ALL" to "All issues",
            "REPORTED" to "Reported",
            "ACKNOWLEDGED" to "Acknowledged",
            "IN_PROGRESS" to "In progress",
            "RESOLVED" to "Resolved"
        )
        FrostedGlassFilterSheet(
            expanded = showIssueFilterMenu,
            onDismissRequest = { showIssueFilterMenu = false },
            eyebrow = "Civic Filters",
            title = "Issue Status",
            sheetTestTag = "community_filter_sheet",
            closeButtonDescription = "Close Issue Status sheet"
        ) {
            FrostedMenuSectionTitle("Filter By Status")
            FrostedMenuWrapRow(
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp
            ) {
                communityStatuses.forEach { (status, label) ->
                    FrostedMenuOptionPill(
                        label = label,
                        selected = issueStatusFilter == status,
                        onClick = {
                            issueStatusFilter = status
                            showIssueFilterMenu = false
                        },
                        modifier = Modifier.testTag("issue_status_pill_${status.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: CommunityNotice) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card_${notice.id}"),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(
            1.dp,
            if (notice.isUrgent) FieryCoral.copy(alpha = 0.55f) else HairlineGlassBorder
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notice.isPinned) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(AmberSoft)
                                .border(1.dp, AmberGlow.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = AmberLight,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (notice.isUrgent) {
                        Surface(
                            shape = CircleShape,
                            color = FieryCoralSoft,
                            border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "Urgent",
                                color = FieryCoralGlow,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = notice.authorityName,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CobaltGlow
                    )
                }

                Text(
                    text = notice.dateLabel,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = SubtleSlate
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.content,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = MutedSlate
            )
        }
    }
}

@Composable
fun IssueCard(
    issue: CommunityIssue,
    onUpvote: () -> Unit,
    onAdvanceStatus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("issue_card_${issue.id}"),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    Text(
                        text = issue.category.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MutedSlate,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val (statusColor, statusBg, statusBorder) = when (issue.status) {
                    "REPORTED" -> Triple(FieryCoralGlow, FieryCoralSoft, FieryCoral.copy(alpha = 0.55f))
                    "ACKNOWLEDGED" -> Triple(AmberLight, AmberSoft, AmberGlow.copy(alpha = 0.55f))
                    "IN_PROGRESS" -> Triple(CobaltGlow, CobaltSoft, ElectricCobalt.copy(alpha = 0.55f))
                    else -> Triple(EmeraldLight, EmeraldSoft, EmeraldMint.copy(alpha = 0.55f))
                }

                Surface(
                    shape = CircleShape,
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder)
                ) {
                    Text(
                        text = issue.status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = issue.title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = issue.description,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = MutedSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location: ${issue.locationSnippet}",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 11.sp,
                color = SubtleSlate
            )

            issue.assignedVolunteer?.let { volunteer ->
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Assigned: $volunteer",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = EmeraldLight
                )
            }

            if (issue.mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(18.dp))) {
                    PostMediaCarousel(
                        mediaUris = issue.mediaUris,
                        mediaTypes = issue.mediaTypes
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            StatusTimeline(currentStatus = issue.status)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassSecondaryPillButton(
                    text = "${issue.upvotesCount} Upvotes",
                    icon = if (issue.hasUserUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    onClick = onUpvote,
                    surfaceTint = if (issue.hasUserUpvoted) FieryCoralSoft else ElevatedGlassSurface,
                    contentColor = if (issue.hasUserUpvoted) FieryCoralGlow else ObsidianInk,
                    borderColor = if (issue.hasUserUpvoted) FieryCoral else HairlineGlassBorder,
                    height = 40.dp,
                    modifier = Modifier.testTag("upvote_issue_${issue.id}")
                )

                if (issue.status != "RESOLVED") {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(CobaltPillGradient)
                            .border(1.dp, CobaltGlow.copy(alpha = 0.5f), CircleShape)
                            .clickable { onAdvanceStatus() }
                            .padding(horizontal = 16.dp)
                            .testTag("advance_issue_${issue.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (issue.status) {
                                "REPORTED" -> "Acknowledge"
                                "ACKNOWLEDGED" -> "Start Work"
                                else -> "Mark Resolved"
                            },
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(currentStatus: String) {
    val stages = listOf("REPORTED", "ACKNOWLEDGED", "IN_PROGRESS", "RESOLVED")
    val currentIndex = stages.indexOf(currentStatus).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(ElevatedGlassSurface)
            .border(1.dp, HairlineGlassBorder, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isPassedOrCurrent = index <= currentIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isPassedOrCurrent) ElectricCobalt else HairlineGlassBorder)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = when (stage) {
                        "REPORTED" -> "Report"
                        "ACKNOWLEDGED" -> "Ack"
                        "IN_PROGRESS" -> "Work"
                        else -> "Done"
                    },
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = if (isPassedOrCurrent) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (isPassedOrCurrent) PureWhite else SubtleSlate
                )
            }
        }
    }
}
