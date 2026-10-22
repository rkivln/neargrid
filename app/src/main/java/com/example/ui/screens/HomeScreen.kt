package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.ui.components.DiscoveryRadiusSelector
import com.example.ui.components.IntentMatchCard
import com.example.ui.components.PostCard
import com.example.ui.components.TopNeighborhoodBar
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
import com.example.ui.theme.WarmIvory

@Composable
fun HomeScreen(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    posts: List<NeighborPost>,
    topIntentMatches: List<IntentMatchResult>,
    selectedFilterType: String,
    selectedCategory: String,
    onCellClick: () -> Unit,
    onGpsToggle: () -> Unit,
    onRadiusChange: (Int) -> Unit,
    onFilterChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onConnectPost: (NeighborPost) -> Unit,
    onOpenCreateDialog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & Coordinate Bar
            item {
                TopNeighborhoodBar(
                    currentCell = currentCell,
                    discoveryRadiusMeters = discoveryRadiusMeters,
                    gpsEnabled = gpsEnabled,
                    onCellClick = onCellClick,
                    onGpsToggle = onGpsToggle
                )
            }

            // Hero Community Editorial Showcase with hand-drawn line art illustration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Hand-drawn artwork with soft watercolor brush marks
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_neighborhood_editorial),
                                contentDescription = "Neighborhood editorial scene",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Soft gradient bottom blend into cream
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                PureWhite.copy(alpha = 0.15f),
                                                PureWhite.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                            )

                            // Cell resident counter pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite.copy(alpha = 0.92f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                                        text = "${currentCell?.name ?: "Oakwood Grove"} • 42 neighbors nearby",
                                        color = PrimaryText,
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Editorial headline and storytelling copy
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "Your neighborhood, coordinated.",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 28.sp,
                                lineHeight = 33.sp,
                                color = PrimaryText
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "A quiet, coordinate-aware network for sharing tools, lending hands, and organizing local life without revealing your home address.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }

            // Quick Intent Action Grid (Editorial Numbered Tiles)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Coordinate nearby",
                            fontFamily = DMSerifFontFamily,
                            fontSize = 18.sp,
                            color = PrimaryText
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftCream,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = "Zero home pins",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionTile(
                            number = "01",
                            title = "Ask nearby",
                            subtitle = "Tools & practical favors",
                            accentColor = PastelCoral,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_ask_help"),
                            onClick = { onOpenCreateDialog("NEED") }
                        )
                        ActionTile(
                            number = "02",
                            title = "Share & lend",
                            subtitle = "Items, books & skills",
                            accentColor = PastelMint,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_share_lend"),
                            onClick = { onOpenCreateDialog("OFFER") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionTile(
                            number = "03",
                            title = "Find people",
                            subtitle = "Sports, clubs & meetups",
                            accentColor = PastelSoftCyan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_find_people"),
                            onClick = { onOpenCreateDialog("MEETUP") }
                        )
                        ActionTile(
                            number = "04",
                            title = "Civic report",
                            subtitle = "Streetlights, trails & care",
                            accentColor = PastelButterYellow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_report_issue"),
                            onClick = { onOpenCreateDialog("ISSUE") }
                        )
                    }
                }
            }

            // Discovery Radius Selector
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiscoveryRadiusSelector(
                    selectedRadiusMeters = discoveryRadiusMeters,
                    onSelectRadius = onRadiusChange
                )
            }

            // Top Intent Match Spotlight
            if (topIntentMatches.isNotEmpty()) {
                item {
                    val topMatch = topIntentMatches.first()
                    IntentMatchCard(
                        match = topMatch,
                        onConnect = onConnectPost
                    )
                }
            }

            // Feed filter tabs
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    val filterTypes = listOf(
                        "ALL" to "All updates",
                        "NEED" to "Needs",
                        "OFFER" to "Offers",
                        "MEETUP" to "Meetups"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            filterTypes.forEach { (type, label) ->
                                val isSelected = selectedFilterType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SoftCream else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) PastelPeach else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onFilterChange(type) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = DMSansFontFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (isSelected) PrimaryText else SecondaryText
                                    )
                                }
                            }
                        }
                    }

                    // Category horizontal chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
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
                                        if (cat == "ALL") "All categories" else cat.lowercase().replaceFirstChar { it.uppercase() },
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
            }

            // Feed Items list
            if (posts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No posts in this radius",
                            fontFamily = DMSerifFontFamily,
                            fontSize = 20.sp,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Expand your discovery radius to 800m or 2km to find neighbors nearby.",
                            fontFamily = DMSansFontFamily,
                            fontSize = 13.sp,
                            color = SecondaryText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onConnect = onConnectPost
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { onOpenCreateDialog("NEED") },
            containerColor = PrimaryText,
            contentColor = PureWhite,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_post")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Action",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New coordination",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = PureWhite
                )
            }
        }
    }
}

@Composable
fun ActionTile(
    number: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = number,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontFamily = DMSerifFontFamily,
                fontSize = 17.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                color = SecondaryText,
                lineHeight = 15.sp
            )
        }
    }
}
