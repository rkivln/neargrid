package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividPink

@Composable
fun CommunityScreen(
    notices: List<CommunityNotice>,
    issues: List<CommunityIssue>,
    onToggleUpvote: (CommunityIssue) -> Unit,
    onUpdateIssueStatus: (issueId: String, newStatus: String, volunteer: String?) -> Unit,
    onOpenReportIssue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Noticeboard, 1 = Issue Tracker
    var issueStatusFilter by remember { mutableStateOf("ALL") }

    Box(modifier = modifier.fillMaxSize().background(PureBlack)) {
        // Oversized background typographic word
        Text(
            text = "PULSE",
            fontFamily = AntonFontFamily,
            fontSize = 140.sp,
            color = CharcoalBlack.copy(alpha = 0.5f),
            letterSpacing = 6.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp)
                .offset(x = 10.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalBlack,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "COMMUNITY PULSE",
                                fontFamily = AntonFontFamily,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp,
                                color = VividPink
                            )
                            Text(
                                text = "Official municipal noticeboard & neighborhood civic resolution",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }

                        if (selectedTab == 1) {
                            Button(
                                onClick = onOpenReportIssue,
                                colors = ButtonDefaults.buttonColors(containerColor = VividPink),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("report_issue_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = EditorialWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "REPORT",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = EditorialWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segment Tabs: Noticeboard vs Issue Tracker
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurface,
                        contentColor = VividPink,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "NOTICEBOARD (${notices.size})",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (selectedTab == 0) VividPink else NeutralGray
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "ISSUE TRACKER (${issues.size})",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (selectedTab == 1) VividPink else NeutralGray
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // Noticeboard Feed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        NoticeCard(notice = notice)
                    }
                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            } else {
                // Issue-to-Resolution Tracker
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Filter chips for issue statuses
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            val statuses = listOf("ALL", "REPORTED", "ACKNOWLEDGED", "IN_PROGRESS", "RESOLVED")
                            items(statuses) { status ->
                                val isSelected = issueStatusFilter == status
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { issueStatusFilter = status },
                                    label = {
                                        Text(
                                            status.replace("_", " "),
                                            fontFamily = AntonFontFamily,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VividPink,
                                        selectedLabelColor = EditorialWhite,
                                        containerColor = DarkSurface,
                                        labelColor = EditorialWhite
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) VividPink else DarkCardBorder,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }

                    val filteredIssues = issues.filter {
                        if (issueStatusFilter == "ALL") true else it.status == issueStatusFilter
                    }

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

                    item {
                        Spacer(modifier = Modifier.height(96.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (notice.isUrgent) VividPink else DarkCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                                .background(VividPink.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = VividPink,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (notice.isUrgent) {
                        Surface(shape = RoundedCornerShape(4.dp), color = VividPink) {
                            Text(
                                text = "URGENT",
                                color = EditorialWhite,
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = notice.authorityName.uppercase(),
                        fontFamily = AntonFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = NeutralGray
                    )
                }

                Text(
                    text = notice.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EditorialWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category and Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkSurface
                ) {
                    Text(
                        text = issue.category,
                        fontFamily = AntonFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = EditorialWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val (statusColor, statusBg) = when (issue.status) {
                    "REPORTED" -> VividPink to VividPink.copy(alpha = 0.15f)
                    "ACKNOWLEDGED" -> AcidYellow to AcidYellow.copy(alpha = 0.18f)
                    "IN_PROGRESS" -> Color(0xFF32C5F4) to Color(0xFF32C5F4).copy(alpha = 0.15f)
                    else -> ElectricLime to ElectricLime.copy(alpha = 0.2f)
                }

                Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                    Text(
                        text = issue.status.replace("_", " "),
                        color = statusColor,
                        fontFamily = AntonFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = issue.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EditorialWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 LOCATION: ${issue.locationSnippet.uppercase()}",
                fontFamily = AntonFontFamily,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = NeutralGray
            )

            issue.assignedVolunteer?.let { volunteer ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👷 ASSIGNED: ${volunteer.uppercase()}",
                    fontFamily = AntonFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = VividPink
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Timeline Stepper
            StatusTimeline(currentStatus = issue.status)

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Upvotes button and Status Progress button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Upvote Button
                OutlinedButton(
                    onClick = onUpvote,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (issue.hasUserUpvoted) VividPink else DarkCardBorder
                    ),
                    modifier = Modifier.testTag("upvote_issue_${issue.id}")
                ) {
                    Icon(
                        imageVector = if (issue.hasUserUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Upvote",
                        tint = if (issue.hasUserUpvoted) VividPink else NeutralGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.upvotesCount} UPVOTES",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = if (issue.hasUserUpvoted) VividPink else EditorialWhite
                    )
                }

                // Advance status action
                if (issue.status != "RESOLVED") {
                    Button(
                        onClick = onAdvanceStatus,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividPink),
                        modifier = Modifier.testTag("advance_issue_${issue.id}")
                    ) {
                        Text(
                            text = when (issue.status) {
                                "REPORTED" -> "ACKNOWLEDGE"
                                "ACKNOWLEDGED" -> "START WORK"
                                else -> "MARK RESOLVED"
                            },
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = EditorialWhite
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
            .clip(RoundedCornerShape(8.dp))
            .background(PureBlack)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isPassedOrCurrent = index <= currentIndex
            val isCurrent = index == currentIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPassedOrCurrent) VividPink else Color(0xFF333333))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (stage) {
                        "REPORTED" -> "REPORT"
                        "ACKNOWLEDGED" -> "ACK"
                        "IN_PROGRESS" -> "WORK"
                        else -> "DONE"
                    },
                    fontFamily = AntonFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = if (isPassedOrCurrent) VividPink else Color(0xFF666666)
                )
            }
        }
    }
}
