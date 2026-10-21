package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.GpsNotFixed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividPink

@Composable
fun TopNeighborhoodBar(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    onCellClick: () -> Unit,
    onGpsToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CharcoalBlack,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Neighborhood Cell Selector Chip with pulsing Electric Lime indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureBlack)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onCellClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("cell_selector_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Cell location",
                        tint = PureBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentCell?.name?.uppercase() ?: "OAKWOOD GROVE",
                            fontFamily = AntonFontFamily,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp,
                            color = EditorialWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Change cell",
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ElectricLime)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentCell?.code ?: "#H3-8826"} • 0 PINS",
                            fontFamily = AntonFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = NeutralGray
                        )
                    }
                }
            }

            // GPS vs Privacy Cell Toggle
            Button(
                onClick = onGpsToggle,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gpsEnabled) ElectricLime else DarkSurface
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("gps_toggle_button")
            ) {
                Icon(
                    imageVector = if (gpsEnabled) Icons.Outlined.GpsFixed else Icons.Outlined.GpsNotFixed,
                    contentDescription = "GPS Toggle",
                    modifier = Modifier.size(16.dp),
                    tint = if (gpsEnabled) PureBlack else NeutralGray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (gpsEnabled) "LIVE GPS" else "GRID CELL",
                    fontFamily = AntonFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    color = if (gpsEnabled) PureBlack else NeutralGray
                )
            }
        }
    }
}

@Composable
fun DiscoveryRadiusSelector(
    selectedRadiusMeters: Int,
    onSelectRadius: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = CharcoalBlack,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RADIUS MESH",
                fontFamily = AntonFontFamily,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = ElectricLime
            )

            val radii = listOf(300 to "300M", 800 to "800M", 2000 to "2KM")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                radii.forEach { (radius, label) ->
                    val isSelected = selectedRadiusMeters == radius
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricLime else PureBlack)
                            .clickable { onSelectRadius(radius) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("radius_chip_$radius"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = if (isSelected) PureBlack else NeutralGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntentMatchCard(
    match: IntentMatchResult,
    onConnect: (NeighborPost) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("intent_match_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricLime)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Intent Match",
                        tint = ElectricLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEARGRID MATCH ENGINE",
                        fontFamily = AntonFontFamily,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        color = ElectricLime
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricLime
                ) {
                    Text(
                        text = "${match.compatibilityPercent}% MATCH",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = PureBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Need section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureBlack)
                    .border(1.dp, VividPink.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(4.dp), color = VividPink) {
                    Text(
                        text = "NEED",
                        fontFamily = AntonFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = EditorialWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.need.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.need.authorName} • ${match.need.approxDistanceBand}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Arrow indicator
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PureBlack)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Matches",
                        tint = ElectricLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COMPATIBLE INTENT",
                        fontFamily = AntonFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = ElectricLime
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Offer section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureBlack)
                    .border(1.dp, ElectricLime.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(4.dp), color = ElectricLime) {
                    Text(
                        text = "OFFER",
                        fontFamily = AntonFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = PureBlack,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.matchingOffer.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.matchingOffer.authorName} • ${match.matchingOffer.approxDistanceBand}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Reason: ${match.matchReason}",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onConnect(match.matchingOffer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("connect_match_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.NearMe, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START COORDINATION WITH ${match.matchingOffer.authorName.uppercase()}",
                        fontFamily = AntonFontFamily,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = PureBlack
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: NeighborPost,
    onConnect: (NeighborPost) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNeed = post.type == "NEED"
    val isOffer = post.type == "OFFER"
    val accentColor = if (isNeed) VividPink else if (isOffer) ElectricLime else BrightCyan

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type badge, Distance band, Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeText, badgeBg, badgeFg) = when (post.type) {
                        "NEED" -> Triple("ASK FOR HELP", VividPink, EditorialWhite)
                        "OFFER" -> Triple("OFFER / SHARE", ElectricLime, PureBlack)
                        else -> Triple("MEETUP", BrightCyan, PureBlack)
                    }

                    Surface(shape = RoundedCornerShape(4.dp), color = badgeBg) {
                        Text(
                            text = badgeText,
                            color = badgeFg,
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    val categoryIcon = getCategoryIcon(post.category)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = NeutralGray
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.category,
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = NeutralGray
                            )
                        }
                    }
                }

                // Distance Band Chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PureBlack
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = post.approxDistanceBand.uppercase(),
                            fontFamily = AntonFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = EditorialWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EditorialWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Post Description
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Author, Expiry countdown, and Connect Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorInitials,
                            fontFamily = AntonFontFamily,
                            fontSize = 13.sp,
                            color = PureBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EditorialWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = NeutralGray
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "EXPIRES IN ${post.expiresInHours}H",
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = NeutralGray
                            )
                        }
                    }
                }

                Button(
                    onClick = { onConnect(post) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.testTag("connect_post_${post.id}")
                ) {
                    Text(
                        text = if (isNeed) "OFFER HELP" else if (isOffer) "REQUEST ITEM" else "JOIN MEETUP",
                        fontFamily = AntonFontFamily,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isNeed) EditorialWhite else PureBlack
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "TOOLS" -> Icons.Default.Handyman
        "STUDY" -> Icons.Default.School
        "HOUSEHOLD" -> Icons.Default.HomeRepairService
        "SPORTS" -> Icons.Default.SportsCricket
        "HELP" -> Icons.Default.VolunteerActivism
        else -> Icons.Default.HelpOutline
    }
}
