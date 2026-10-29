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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CommunityNotice
import com.example.ui.components.PostMediaCarousel
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.PaleWarmGray
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
import com.example.ui.theme.WarmDivider
import com.example.ui.theme.WarmIvory

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().testTag("community_pull_to_refresh")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Community pulse",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 26.sp,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Municipal bulletin and neighbor-led civic resolutions",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }

                        if (selectedTab == 1) {
                            Button(
                                onClick = onOpenReportIssue,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryText,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("report_issue_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Report",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = PureWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segment Tabs: Noticeboard vs Issue Tracker
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedTab == 0) PureWhite else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (selectedTab == 0) WarmBorder else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Noticeboard (${notices.size})",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = PrimaryText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedTab == 1) PureWhite else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (selectedTab == 1) WarmBorder else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Issue tracker (${issues.size})",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // Noticeboard Feed
                if (notices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_notices_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(PastelPeach.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = PastelCoral,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Civic Notices",
                                    fontFamily = DMSerifFontFamily,
                                    fontSize = 20.sp,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Scheduled maintenance, water valve diagnostics, and neighborhood advisories from civic organizers will appear here.",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 13.sp,
                                    color = SecondaryText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notices, key = { it.id }) { notice ->
                            NoticeCard(notice = notice)
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            } else {
                // Issue-to-Resolution Tracker
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Filter chips for issue statuses
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            val statuses = listOf(
                                "ALL" to "All issues",
                                "REPORTED" to "Reported",
                                "ACKNOWLEDGED" to "Acknowledged",
                                "IN_PROGRESS" to "In progress",
                                "RESOLVED" to "Resolved"
                            )
                            items(statuses) { (status, label) ->
                                val isSelected = issueStatusFilter == status
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { issueStatusFilter = status },
                                    label = {
                                        Text(
                                            label,
                                            fontFamily = DMSansFontFamily,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PastelPeach,
                                        selectedLabelColor = PrimaryText,
                                        containerColor = PureWhite,
                                        labelColor = SecondaryText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) PastelCoral.copy(alpha = 0.5f) else WarmBorder,
                                        enabled = true,
                                        selected = isSelected
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    val filteredIssues = issues.filter {
                        if (issueStatusFilter == "ALL") true else it.status == issueStatusFilter
                    }

                    if (filteredIssues.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("empty_issues_card"),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(PastelMint.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = PrimaryText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "All Clear in the Neighborhood",
                                        fontFamily = DMSerifFontFamily,
                                        fontSize = 20.sp,
                                        color = PrimaryText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No civic maintenance issues or safety hazards reported. Spot a broken lamp, pothole, or fallen branch?",
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 13.sp,
                                        color = SecondaryText,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = onOpenReportIssue,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryText,
                                            contentColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("fresh_report_issue_button")
                                    ) {
                                        Text(
                                            text = "Report an Issue",
                                            fontFamily = DMSansFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredIssues, key = { it.id }) { issue ->
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

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
}

@Composable
fun NoticeCard(notice: CommunityNotice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card_${notice.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (notice.isUrgent) PastelCoral else WarmBorder
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PastelPeach.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = PrimaryText,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (notice.isUrgent) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelCoral.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Urgent",
                                color = PastelCoral,
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = notice.authorityName,
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }

                Text(
                    text = notice.dateLabel,
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.title,
                fontFamily = DMSerifFontFamily,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.content,
                fontFamily = DMSansFontFamily,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = SecondaryText
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("issue_card_${issue.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Category and Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftCream
                ) {
                    Text(
                        text = issue.category.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val (statusColor, statusBg) = when (issue.status) {
                    "REPORTED" -> PastelCoral to PastelCoral.copy(alpha = 0.15f)
                    "ACKNOWLEDGED" -> PrimaryText to PastelButterYellow.copy(alpha = 0.5f)
                    "IN_PROGRESS" -> PrimaryText to PastelSoftCyan.copy(alpha = 0.35f)
                    else -> PrimaryText to PastelMint.copy(alpha = 0.5f)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = issue.status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = issue.title,
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = issue.description,
                fontFamily = DMSansFontFamily,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location: ${issue.locationSnippet}",
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                color = SecondaryText
            )

            issue.assignedVolunteer?.let { volunteer ->
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Assigned: $volunteer",
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    color = PastelCoral
                )
            }

            if (issue.mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                PostMediaCarousel(
                    mediaUris = issue.mediaUris,
                    mediaTypes = issue.mediaTypes
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Timeline Stepper
            StatusTimeline(currentStatus = issue.status)

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Upvotes button and Status Progress button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Upvote Button
                OutlinedButton(
                    onClick = onUpvote,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (issue.hasUserUpvoted) PastelCoral else WarmBorder
                    ),
                    modifier = Modifier.testTag("upvote_issue_${issue.id}")
                ) {
                    Icon(
                        imageVector = if (issue.hasUserUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Upvote",
                        tint = if (issue.hasUserUpvoted) PastelCoral else SecondaryText,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.upvotesCount} upvotes",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = if (issue.hasUserUpvoted) PastelCoral else PrimaryText
                    )
                }

                // Advance status action
                if (issue.status != "RESOLVED") {
                    Button(
                        onClick = onAdvanceStatus,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryText,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier.testTag("advance_issue_${issue.id}")
                    ) {
                        Text(
                            text = when (issue.status) {
                                "REPORTED" -> "Acknowledge"
                                "ACKNOWLEDGED" -> "Start work"
                                else -> "Mark resolved"
                            },
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.Medium,
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
            .clip(RoundedCornerShape(12.dp))
            .background(SoftCream)
            .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isPassedOrCurrent = index <= currentIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPassedOrCurrent) PastelCoral else PaleWarmGray)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = when (stage) {
                        "REPORTED" -> "Report"
                        "ACKNOWLEDGED" -> "Ack"
                        "IN_PROGRESS" -> "Work"
                        else -> "Done"
                    },
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    color = if (isPassedOrCurrent) PrimaryText else SecondaryText
                )
            }
        }
    }
}
