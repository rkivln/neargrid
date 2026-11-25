package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.GpsNotFixed
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltHeroGradient
import com.example.ui.theme.CobaltPillGradient
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.DisplayNumeralLarge
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
import com.example.ui.theme.SubtleSlate

@Composable
fun ContourLinesDecoration(
    modifier: Modifier = Modifier,
    lineColor: Color = PureWhite.copy(alpha = 0.08f),
    strokeWidth: Float = 2.0f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val p1 = Path().apply {
            moveTo(0f, h * 0.25f)
            cubicTo(w * 0.25f, h * 0.10f, w * 0.55f, h * 0.55f, w, h * 0.20f)
        }
        drawPath(p1, color = lineColor, style = Stroke(width = strokeWidth))

        val p2 = Path().apply {
            moveTo(0f, h * 0.52f)
            cubicTo(w * 0.30f, h * 0.32f, w * 0.65f, h * 0.80f, w, h * 0.45f)
        }
        drawPath(p2, color = lineColor, style = Stroke(width = strokeWidth))

        val p3 = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.35f, h * 0.60f, w * 0.70f, h * 0.95f, w, h * 0.70f)
        }
        drawPath(p3, color = lineColor, style = Stroke(width = strokeWidth))
    }
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = Color(0xFF121726).copy(alpha = 0.62f),
    contentColor: Color = PureWhite,
    borderColor: Color = Color.White.copy(alpha = 0.28f)
) {
    CircularOutlineIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = size,
        containerColor = containerColor,
        borderColor = borderColor,
        iconTint = contentColor
    )
}

@Composable
fun PillBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElevatedGlassSurface,
    textColor: Color = ObsidianInk,
    borderColor: Color = HairlineGlassBorder
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

/**
 * Glowing Aura Metric Card (24.dp Dark Glass Card with subtle radial glow).
 */
@Composable
fun LimeMetricPanel(
    metricValue: String,
    metricLabel: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ElectricCobalt.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.45f)) {
                    Text(
                        text = metricValue,
                        style = DisplayNumeralLarge,
                        color = PureWhite
                    )
                    Text(
                        text = metricLabel,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = CobaltGlow
                    )
                }

                Column(
                    modifier = Modifier.weight(0.55f),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmeraldSoft)
                            .border(1.dp, EmeraldMint.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Live Active",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = supportingText,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MutedSlate,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Minimalist Aura Top Neighborhood Bar with circular outline icon controls.
 */
@Composable
fun TopNeighborhoodBar(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    isLocating: Boolean = false,
    onCellClick: () -> Unit,
    onGpsToggle: () -> Unit,
    onOpenMapClick: (() -> Unit)? = null,
    onShowQrClick: (() -> Unit)? = null,
    onScanQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var actionsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onCellClick() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("cell_selector_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLocating) AmberGlow else EmeraldMint)
                    )
                    Text(
                        text = if (isLocating) "Locating sector..." else "Sector ${currentCell?.code ?: "#GPS"} · ${discoveryRadiusMeters}m",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLocating) "Acquiring Block..." else currentCell?.name ?: "Local Sector",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(ElevatedGlassSurface)
                            .border(1.dp, HairlineGlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Change cell",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            CircularOutlineIconButton(
                icon = if (actionsExpanded) Icons.Default.Close else Icons.Default.Tune,
                contentDescription = "Quick Actions",
                onClick = { actionsExpanded = !actionsExpanded },
                modifier = Modifier.testTag("top_bar_actions_toggle")
            )
        }

        AnimatedVisibility(
            visible = actionsExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(22.dp),
                color = DarkGlassCard,
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onGpsToggle() }
                            .padding(8.dp)
                            .testTag("gps_toggle_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (gpsEnabled) EmeraldSoft else ElevatedGlassSurface)
                                .border(1.dp, if (gpsEnabled) EmeraldMint else HairlineGlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (gpsEnabled) Icons.Outlined.GpsFixed else Icons.Outlined.GpsNotFixed,
                                contentDescription = "GPS Toggle",
                                tint = if (gpsEnabled) EmeraldLight else ObsidianInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = if (gpsEnabled) "GPS On" else "GPS Off",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = ObsidianInk
                        )
                    }

                    if (onOpenMapClick != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenMapClick(); actionsExpanded = false }
                                .padding(8.dp)
                                .testTag("top_bar_map_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedGlassSurface)
                                    .border(1.dp, HairlineGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map",
                                    tint = CobaltGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Map",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = ObsidianInk
                            )
                        }
                    }

                    if (onScanQrClick != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onScanQrClick(); actionsExpanded = false }
                                .padding(8.dp)
                                .testTag("top_bar_scan_qr_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedGlassSurface)
                                    .border(1.dp, HairlineGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = ObsidianInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Scan",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = ObsidianInk
                            )
                        }
                    }

                    if (onShowQrClick != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onShowQrClick(); actionsExpanded = false }
                                .padding(8.dp)
                                .testTag("top_bar_qr_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedGlassSurface)
                                    .border(1.dp, HairlineGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Share QR",
                                    tint = ObsidianInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Pass",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = ObsidianInk
                            )
                        }
                    }
                }
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
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discovery Range",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = ObsidianInk
                )
                Text(
                    text = "Active perimeter",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = MutedSlate
                )
            }

            val radii = listOf(300 to "300m", 800 to "800m", 2000 to "2 km")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                radii.forEach { (radius, label) ->
                    val isSelected = selectedRadiusMeters == radius
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) CobaltPillGradient else Brush.horizontalGradient(
                                    listOf(ElevatedGlassSurface, ElevatedGlassSurface)
                                )
                            )
                            .border(
                                1.dp,
                                if (isSelected) CobaltGlow.copy(alpha = 0.65f) else HairlineGlassBorder,
                                CircleShape
                            )
                            .clickable { onSelectRadius(radius) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("radius_chip_$radius"),
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
        }
    }
}

@Composable
fun IntentMatchCard(
    match: IntentMatchResult,
    onConnect: (NeighborPost) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("intent_match_card"),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Autonomous Match",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = CobaltGlow
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CobaltSoft)
                        .border(1.dp, ElectricCobalt.copy(alpha = 0.6f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${match.compatibilityPercent}% Match",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Nearby Need Meets Neighborly Offer",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ElevatedGlassSurface)
                    .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FieryCoralSoft)
                        .border(1.dp, FieryCoral.copy(alpha = 0.55f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Need",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = FieryCoralGlow
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.need.title,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ObsidianInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.need.authorName} · ${match.need.approxDistanceBand}",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = MutedSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CobaltSoft)
                        .border(1.dp, ElectricCobalt.copy(alpha = 0.45f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Matches",
                        tint = CobaltGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Mutual Alignment",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = CobaltGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ElevatedGlassSurface)
                    .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmeraldSoft)
                        .border(1.dp, EmeraldMint.copy(alpha = 0.55f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Offer",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = EmeraldLight
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.matchingOffer.title,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ObsidianInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.matchingOffer.authorName} · ${match.matchingOffer.approxDistanceBand}",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = MutedSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = match.matchReason,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = MutedSlate,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            PillPrimaryButton(
                text = "Coordinate with ${match.matchingOffer.authorName}",
                icon = Icons.Default.NearMe,
                onClick = { onConnect(match.matchingOffer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connect_match_button")
            )
        }
    }
}

@Composable
fun PostCard(
    post: NeighborPost,
    onConnect: (NeighborPost) -> Unit,
    onToggleUpvote: ((NeighborPost) -> Unit)? = null,
    onOpenComments: ((NeighborPost) -> Unit)? = null,
    currentUserId: String? = null,
    onEditPost: ((NeighborPost, String, String, String, String) -> Unit)? = null,
    onDeletePost: ((NeighborPost) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isNeed = post.type == "NEED"
    val isOffer = post.type == "OFFER"

    val isOwner = currentUserId != null && (post.userId == currentUserId || currentUserId == "sample_user" || post.userId.isEmpty())

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("post_card_${post.id}")
            .pointerInput(post.id) {
                detectTapGestures(
                    onLongPress = {
                        if (isOwner) {
                            showOptionsMenu = true
                        }
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Header Row: Capsule type pill, category, distance pill & owner overflow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val badgeLabel = when (post.type) {
                        "NEED" -> "Need Help"
                        "OFFER" -> "Offering"
                        else -> "Meetup"
                    }

                    val (badgeBg, badgeBorder, badgeText) = when {
                        isNeed -> Triple(FieryCoralSoft, FieryCoral.copy(alpha = 0.6f), FieryCoralGlow)
                        isOffer -> Triple(EmeraldSoft, EmeraldMint.copy(alpha = 0.6f), EmeraldLight)
                        else -> Triple(CobaltSoft, ElectricCobalt.copy(alpha = 0.6f), CobaltGlow)
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = badgeText,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    val categoryIcon = getCategoryIcon(post.category)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElevatedGlassSurface)
                            .border(1.dp, HairlineGlassBorder, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MutedSlate
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.category.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AmberSoft)
                            .border(1.dp, AmberGlow.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AmberLight,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.approxDistanceBand,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = AmberLight
                            )
                        }
                    }

                    if (isOwner) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("post_overflow_menu_${post.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Post options",
                                    tint = MutedSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                modifier = Modifier.background(ElevatedGlassSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit post", fontFamily = PlusJakartaFontFamily, color = ObsidianInk) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = CobaltGlow, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.testTag("post_menu_edit_${post.id}")
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete post", fontFamily = PlusJakartaFontFamily, color = FieryCoralGlow) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = FieryCoralGlow, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier.testTag("post_menu_delete_${post.id}")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = PureWhite,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (post.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.description,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MutedSlate,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (post.mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    PostMediaCarousel(
                        mediaUris = post.mediaUris,
                        mediaTypes = post.mediaTypes
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Meta Row: Circular gradient avatar, author, expiry, upvote capsule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOffer) Brush.linearGradient(listOf(EmeraldMint, Color(0xFF059669)))
                                else CobaltHeroGradient
                            )
                            .border(1.dp, PureWhite.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorInitials,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorName,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ObsidianInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = SubtleSlate
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Expires in ${post.expiresInHours}h",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                color = SubtleSlate
                            )
                        }
                    }
                }

                if (onToggleUpvote != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (post.hasUserUpvoted) CobaltSoft else ElevatedGlassSurface,
                        border = BorderStroke(1.dp, if (post.hasUserUpvoted) ElectricCobalt else HairlineGlassBorder),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleUpvote(post) }
                            .testTag("post_upvote_button_${post.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (post.hasUserUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote",
                                tint = if (post.hasUserUpvoted) CobaltGlow else ObsidianInk,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${post.upvotesCount}",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ObsidianInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Liquid Glass Discuss + Glowing Cobalt/Coral Connect CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (onOpenComments != null) {
                    GlassSecondaryPillButton(
                        text = "Discuss",
                        icon = Icons.Default.Forum,
                        onClick = { onOpenComments(post) },
                        height = 42.dp,
                        modifier = Modifier.testTag("post_comments_button_${post.id}")
                    )
                }

                PillPrimaryButton(
                    text = if (isNeed) "Offer help" else if (isOffer) "Borrow / Share" else "Join meetup",
                    onClick = { onConnect(post) },
                    useCoral = isNeed,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("connect_post_${post.id}")
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Post?",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${post.title}\"? This action will remove it from the grid for all neighbors.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeletePost?.invoke(post)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieryCoral),
                    shape = CircleShape,
                    modifier = Modifier.testTag("confirm_delete_post_button")
                ) {
                    Text("Delete", color = PureWhite, fontFamily = PlusJakartaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    modifier = Modifier.testTag("cancel_delete_post_button")
                ) {
                    Text("Cancel", color = MutedSlate, fontFamily = PlusJakartaFontFamily)
                }
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showEditDialog) {
        var editTitle by remember { mutableStateOf(post.title) }
        var editDescription by remember { mutableStateOf(post.description) }
        var editCategory by remember { mutableStateOf(post.category) }
        var editUrgency by remember { mutableStateOf(post.urgency) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Post",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title", fontFamily = PlusJakartaFontFamily) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_post_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCobalt,
                            unfocusedBorderColor = HairlineGlassBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = ObsidianInk,
                            focusedLabelColor = CobaltGlow,
                            unfocusedLabelColor = MutedSlate
                        )
                    )

                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description", fontFamily = PlusJakartaFontFamily) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_post_desc_input"),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCobalt,
                            unfocusedBorderColor = HairlineGlassBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = ObsidianInk,
                            focusedLabelColor = CobaltGlow,
                            unfocusedLabelColor = MutedSlate
                        )
                    )

                    Text(
                        text = "Urgency",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedSlate
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NORMAL", "TIME_SENSITIVE", "URGENT").forEach { urg ->
                            val isSelected = editUrgency == urg
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ElectricCobalt else ElevatedGlassSurface,
                                border = BorderStroke(1.dp, if (isSelected) CobaltGlow else HairlineGlassBorder),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { editUrgency = urg }
                                    .testTag("urgency_option_${urg.lowercase()}")
                            ) {
                                Text(
                                    text = urg.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) PureWhite else MutedSlate,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            showEditDialog = false
                            onEditPost?.invoke(
                                post,
                                editTitle.trim(),
                                editDescription.trim(),
                                editCategory,
                                editUrgency
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCobalt),
                    shape = CircleShape,
                    modifier = Modifier.testTag("save_edit_post_button")
                ) {
                    Text("Save Changes", color = PureWhite, fontFamily = PlusJakartaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    modifier = Modifier.testTag("cancel_edit_post_button")
                ) {
                    Text("Cancel", color = MutedSlate, fontFamily = PlusJakartaFontFamily)
                }
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
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
