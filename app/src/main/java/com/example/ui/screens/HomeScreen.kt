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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.Terracotta
import com.example.ui.theme.VividPink

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
    Box(modifier = modifier.fillMaxSize().background(PureBlack)) {
        // Subtle oversized background typographic word
        Text(
            text = "GRID",
            fontFamily = AntonFontFamily,
            fontSize = 140.sp,
            color = CharcoalBlack.copy(alpha = 0.5f),
            letterSpacing = 6.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp)
                .offset(x = 20.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar
            item {
                TopNeighborhoodBar(
                    currentCell = currentCell,
                    discoveryRadiusMeters = discoveryRadiusMeters,
                    gpsEnabled = gpsEnabled,
                    onCellClick = onCellClick,
                    onGpsToggle = onGpsToggle
                )
            }

            // Hero Community Editorial Showcase
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkCardBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_community_banner),
                            contentDescription = "Neighborhood illustration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // High-contrast gradient scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            PureBlack.copy(alpha = 0.25f),
                                            PureBlack.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        // Editorial Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricLime
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(PureBlack)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${currentCell?.name?.uppercase() ?: "OAKWOOD GROVE"} • 42 ACTIVE",
                                        color = PureBlack,
                                        fontFamily = AntonFontFamily,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "YOUR NEIGHBORHOOD,\nCOORDINATED.",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 28.sp,
                                    lineHeight = 29.sp,
                                    color = EditorialWhite,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Intent-based proximity exchange without private coordinate exposure.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralGray
                                )
                            }
                        }
                    }
                }
            }

            // Quick Intent Action Grid (Editorial High-Contrast Tiles)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COORDINATE NEARBY",
                            fontFamily = AntonFontFamily,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            color = ElectricLime
                        )
                        Text(
                            text = "NO HOME PINS",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = NeutralGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            number = "01",
                            title = "ASK NEARBY",
                            subtitle = "Tools / Favors",
                            accentColor = ElectricLime,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_ask_help"),
                            onClick = { onOpenCreateDialog("NEED") }
                        )
                        ActionTile(
                            number = "02",
                            title = "SHARE & LEND",
                            subtitle = "Items / Skills",
                            accentColor = VividPink,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_share_lend"),
                            onClick = { onOpenCreateDialog("OFFER") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            number = "03",
                            title = "FIND PEOPLE",
                            subtitle = "Sports / Circles",
                            accentColor = Color(0xFF32C5F4),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_find_people"),
                            onClick = { onOpenCreateDialog("MEETUP") }
                        )
                        ActionTile(
                            number = "04",
                            title = "CIVIC REPORT",
                            subtitle = "Street / Issues",
                            accentColor = Color(0xFFFFF000),
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
                DiscoveryRadiusSelector(
                    selectedRadiusMeters = discoveryRadiusMeters,
                    onSelectRadius = onRadiusChange
                )
            }

            // Top Intent Match Spotlight
            if (topIntentMatches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUTOMATED INTENT MATCH",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = ElectricLime
                        )
                    }
                }

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
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    val filterTypes = listOf("ALL" to "ALL FEED", "NEED" to "NEEDS", "OFFER" to "OFFERS", "MEETUP" to "MEETUPS")
                    TabRow(
                        selectedTabIndex = filterTypes.indexOfFirst { it.first == selectedFilterType }.coerceAtLeast(0),
                        containerColor = DarkSurface,
                        contentColor = ElectricLime,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        filterTypes.forEach { (type, label) ->
                            val isSelected = selectedFilterType == type
                            Tab(
                                selected = isSelected,
                                onClick = { onFilterChange(type) },
                                text = {
                                    Text(
                                        text = label,
                                        fontFamily = AntonFontFamily,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp,
                                        color = if (isSelected) ElectricLime else NeutralGray
                                    )
                                }
                            )
                        }
                    }

                    // Category horizontal chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf("ALL", "TOOLS", "HOUSEHOLD", "HELP", "STUDY", "SPORTS")
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategoryChange(cat) },
                                label = {
                                    Text(
                                        if (cat == "ALL") "ALL" else cat,
                                        fontFamily = AntonFontFamily,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricLime,
                                    selectedLabelColor = PureBlack,
                                    containerColor = DarkSurface,
                                    labelColor = EditorialWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) ElectricLime else DarkCardBorder,
                                    enabled = true,
                                    selected = isSelected
                                )
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
                            tint = NeutralGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO POSTS IN THIS RADIUS",
                            fontFamily = AntonFontFamily,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = EditorialWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Expand your discovery radius to 800m or 2km to discover more neighbors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralGray,
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
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { onOpenCreateDialog("NEED") },
            containerColor = ElectricLime,
            contentColor = PureBlack,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_create_post")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Action", tint = PureBlack)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "POST ACTION",
                    fontFamily = AntonFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = PureBlack
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = number,
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontFamily = AntonFontFamily,
                fontSize = 16.sp,
                color = EditorialWhite,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray,
                fontSize = 11.sp
            )
        }
    }
}
