package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BlueGray
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DisplayNumeralLarge
import com.example.ui.theme.LightLime
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.PrimarySurface
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.SubtleDivider

/**
 * Topographical contour lines canvas decoration.
 * Draws subtle organic, flowing curves across panels.
 */
@Composable
fun ContourLinesDecoration(
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryText.copy(alpha = 0.12f),
    strokeWidth: Float = 2.5f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Flowing contour 1
        val p1 = Path().apply {
            moveTo(0f, h * 0.25f)
            cubicTo(w * 0.25f, h * 0.10f, w * 0.55f, h * 0.55f, w, h * 0.20f)
        }
        drawPath(p1, color = lineColor, style = Stroke(width = strokeWidth))

        // Flowing contour 2
        val p2 = Path().apply {
            moveTo(0f, h * 0.50f)
            cubicTo(w * 0.30f, h * 0.30f, w * 0.65f, h * 0.80f, w, h * 0.45f)
        }
        drawPath(p2, color = lineColor, style = Stroke(width = strokeWidth))

        // Flowing contour 3
        val p3 = Path().apply {
            moveTo(0f, h * 0.75f)
            cubicTo(w * 0.35f, h * 0.60f, w * 0.70f, h * 0.95f, w, h * 0.70f)
        }
        drawPath(p3, color = lineColor, style = Stroke(width = strokeWidth))

        // Concentric loop contour in corner
        val p4 = Path().apply {
            moveTo(w * 0.55f, 0f)
            cubicTo(w * 0.75f, h * 0.35f, w * 0.90f, h * 0.40f, w, h * 0.10f)
        }
        drawPath(p4, color = lineColor, style = Stroke(width = strokeWidth * 0.8f))
    }
}

/**
 * Circular Action Button matching the header controls in the reference.
 */
@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = PrimarySurface,
    contentColor: Color = PrimaryText,
    borderColor: Color = CardBorderColor
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Pill-shaped floating label or category badge.
 */
@Composable
fun PillBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PrimarySurface,
    textColor: Color = PrimaryText,
    borderColor: Color = CardBorderColor
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = textColor
        )
    }
}

/**
 * Wide lime-colored panel with fine topographic contour lines,
 * prominent numerical text on the left and supporting text on the right.
 */
@Composable
fun LimeMetricPanel(
    metricValue: String,
    metricLabel: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = AccentLime),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightLime)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Contour lines pattern in background of lime card
            ContourLinesDecoration(
                modifier = Modifier.matchParentSize(),
                lineColor = PrimaryText.copy(alpha = 0.10f),
                strokeWidth = 2.2f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Prominent numerical text (44–54sp regular weight)
                Column(modifier = Modifier.weight(0.45f)) {
                    Text(
                        text = metricValue,
                        style = DisplayNumeralLarge,
                        color = PrimaryText
                    )
                    Text(
                        text = metricLabel,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp,
                        color = PrimaryText.copy(alpha = 0.75f)
                    )
                }

                // Right: Supporting text & indicator pill
                Column(
                    modifier = Modifier.weight(0.55f),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryText)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE ACTIVE",
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = AccentLime
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = supportingText,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = PrimaryText.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Editorial Dashboard Top Navigation Bar (Template B Header)
 */
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
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Circular cell/neighborhood selector
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimarySurface)
                    .border(1.dp, CardBorderColor, CircleShape)
                    .clickable { onCellClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("cell_selector_chip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(AccentLime)
                        .border(1.dp, PrimaryText.copy(alpha = 0.2f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentCell?.name ?: "Select Cell",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = PrimaryText
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Change cell",
                    tint = SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Center: Compact cell code
            Text(
                text = currentCell?.code ?: "#H3-8826",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                color = SlateBlue
            )

            // Right: Circular GPS Mode Toggle Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (gpsEnabled) AccentLime else PrimarySurface)
                    .border(1.dp, if (gpsEnabled) LightLime else CardBorderColor, CircleShape)
                    .clickable { onGpsToggle() }
                    .testTag("gps_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (gpsEnabled) Icons.Outlined.GpsFixed else Icons.Outlined.GpsNotFixed,
                    contentDescription = "GPS Toggle",
                    modifier = Modifier.size(18.dp),
                    tint = PrimaryText
                )
            }
        }
    }
}

/**
 * Minimalist Rounded Capsule Radius Selector
 */
@Composable
fun DiscoveryRadiusSelector(
    selectedRadiusMeters: Int,
    onSelectRadius: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(26.dp),
        color = PrimarySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discovery range",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = PrimaryText
                )
                Text(
                    text = "Local neighborhood radius",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = SecondaryText
                )
            }

            val radii = listOf(300 to "300m", 800 to "800m", 2000 to "2 km")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                radii.forEach { (radius, label) ->
                    val isSelected = selectedRadiusMeters == radius
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentLime else SoftSurface)
                            .border(
                                1.dp,
                                if (isSelected) LightLime else CardBorderColor,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectRadius(radius) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("radius_chip_$radius"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = ManropeFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = PrimaryText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layered white card with 30dp corner radius and refined typography for Intent Matches
 */
@Composable
fun IntentMatchCard(
    match: IntentMatchResult,
    onConnect: (NeighborPost) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("intent_match_card"),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = PrimarySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: Section tag & confidence pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentLime)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "01",
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTONOMOUS MATCH",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftSurface)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${match.compatibilityPercent}% match",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = PrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Nearby Need Meets Neighborly Offer",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Need Sub-card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftSurface)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepNavy)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "NEED",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.need.title,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.need.authorName} • ${match.need.approxDistanceBand}",
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Swap indicator
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AccentLime)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Matches",
                        tint = PrimaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mutual alignment",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = PrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Offer Sub-card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftSurface)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentLime)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "OFFER",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = PrimaryText
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.matchingOffer.title,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.matchingOffer.authorName} • ${match.matchingOffer.approxDistanceBand}",
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = match.matchReason,
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = SecondaryText,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onConnect(match.matchingOffer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("connect_match_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryText,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coordinate with ${match.matchingOffer.authorName}",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }
            }
        }
    }
}

/**
 * Redesigned Post Card with 30dp rounded corners, pill badges, and clean geometric typography
 */
@Composable
fun PostCard(
    post: NeighborPost,
    onConnect: (NeighborPost) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNeed = post.type == "NEED"
    val isOffer = post.type == "OFFER"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = PrimarySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Type badge, Category, Distance band
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeLabel = when (post.type) {
                        "NEED" -> "Need help"
                        "OFFER" -> "Offering"
                        else -> "Community meetup"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOffer) AccentLime else if (isNeed) DeepNavy else SoftSurface)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = if (isNeed) PureWhite else PrimaryText,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val categoryIcon = getCategoryIcon(post.category)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftSurface)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = SecondaryText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.category.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }

                // Distance Band Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SlateBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = post.approxDistanceBand,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Post Title
            Text(
                text = post.title,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Post Description
            Text(
                text = post.description,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SecondaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Row: Author, Expiry countdown, Connect Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isOffer) AccentLime.copy(alpha = 0.35f) else SoftSurface)
                            .border(1.dp, CardBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorInitials,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = PrimaryText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = SecondaryText
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Expires in ${post.expiresInHours}h",
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }

                Button(
                    onClick = { onConnect(post) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 7.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNeed) DeepNavy else PrimaryText,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier.testTag("connect_post_${post.id}")
                ) {
                    Text(
                        text = if (isNeed) "Offer help" else if (isOffer) "Borrow / Share" else "Join meetup",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = PureWhite
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
