package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CommunityIssue
import com.example.data.model.GridCell
import com.example.data.model.NeighborPost
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CobaltHeroCard
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.OpenStreetMapView
import com.example.ui.components.PillPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltGlow
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
import com.example.util.UserLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingGridScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    cells: List<GridCell>,
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    userLocation: UserLocation? = null,
    posts: List<NeighborPost> = emptyList(),
    issues: List<CommunityIssue> = emptyList(),
    onPostClick: (NeighborPost) -> Unit = {},
    onIssueClick: (CommunityIssue) -> Unit = {},
    onSelectCell: (GridCell) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onShowCellQr: ((GridCell) -> Unit)? = null,
    onScanQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeLayer by remember { mutableStateOf("ALL") }
    var isMapViewActive by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("grid_pull_to_refresh")
    ) {
        AuraMeshBackground(variant = AuraVariant.MIXED) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Cobalt Gradient Spatial Mesh Header Card
                CobaltHeroCard(
                    eyebrowText = "SPATIAL CARTOGRAPHY",
                    titleText = "Living spatial mesh",
                    subtitleText = "Discrete H3 hexagonal zones without private home pins",
                    extraContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onScanQrClick != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onScanQrClick() }
                                        .testTag("living_grid_scan_qr_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "Scan QR",
                                            tint = PureWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Scan",
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }

                            if (currentCell != null && onShowCellQr != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = PureWhite,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onShowCellQr(currentCell) }
                                        .testTag("living_grid_qr_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "Cell QR",
                                            tint = com.example.ui.theme.ObsidianBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Invite",
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = com.example.ui.theme.ObsidianBlack
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PRIVATE",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Layer selection capsule chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val layers = listOf(
                        "ALL" to "All activities",
                        "NEEDS" to "Open requests",
                        "OFFERS" to "Shared resources",
                        "SAFE_SPOTS" to "Safe meeting spots"
                    )
                    items(layers) { (key, label) ->
                        val isSelected = activeLayer == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeLayer = key },
                            label = {
                                Text(
                                    label,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCobalt,
                                selectedLabelColor = PureWhite,
                                containerColor = DarkGlassCard,
                                labelColor = MutedSlate
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) CobaltGlow else HairlineGlassBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Capsule View Mode Selector Bar: Grid Matrix vs Interactive OSM Map
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("living_grid_view_mode_bar"),
                    shape = CircleShape,
                    color = DarkGlassCard,
                    border = BorderStroke(1.dp, HairlineGlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable { isMapViewActive = false }
                                .testTag("view_mode_grid_button"),
                            color = if (!isMapViewActive) ElectricCobalt else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = if (!isMapViewActive) PureWhite else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Grid Matrix",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!isMapViewActive) PureWhite else MutedSlate
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable { isMapViewActive = true }
                                .testTag("view_mode_map_button"),
                            color = if (isMapViewActive) ElectricCobalt else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (isMapViewActive) PureWhite else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Interactive OSM Map",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMapViewActive) PureWhite else MutedSlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isMapViewActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, HairlineGlassBorder, RoundedCornerShape(24.dp))
                    ) {
                        OpenStreetMapView(
                            userLocation = userLocation,
                            posts = posts,
                            issues = issues,
                            onPostClick = onPostClick,
                            onIssueClick = onIssueClick
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Interactive Glowing Spatial Grid Canvas Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkGlassCard)
                        .border(1.dp, HairlineGlassBorder, RoundedCornerShape(24.dp))
                        .testTag("interactive_grid_canvas")
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val tappedCell = when {
                                        offset.x < cx - 40 && offset.y < cy -> cells.find { it.id == "cell_elmwood_north" }
                                        offset.x > cx + 40 && offset.y > cy -> cells.find { it.id == "cell_pinecrest_park" }
                                        offset.y < cy - 40 -> cells.find { it.id == "cell_sunset_ridge" }
                                        else -> cells.find { it.id == "cell_oakwood_center" }
                                    }
                                    tappedCell?.let { onSelectCell(it) }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        val gridLineColor = HairlineGlassBorder.copy(alpha = 0.45f)
                        val step = 30f
                        var x = 0f
                        while (x < w) {
                            drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                            x += step
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(gridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                            y += step
                        }

                        val ringRadius = when (discoveryRadiusMeters) {
                            300 -> 44f
                            800 -> 70f
                            else -> 92f
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricCobalt.copy(alpha = 0.28f),
                                    CobaltDeep.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = ringRadius * 1.4f
                            ),
                            radius = ringRadius * 1.4f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = CobaltGlow,
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        val centerOffset = Offset(cx, cy)
                        val elmwoodOffset = Offset(cx - 88f, cy - 34f)
                        val pinecrestOffset = Offset(cx + 84f, cy + 38f)
                        val sunsetOffset = Offset(cx - 20f, cy - 62f)

                        drawLine(HairlineGlassBorder, centerOffset, elmwoodOffset, strokeWidth = 1.5f)
                        drawLine(HairlineGlassBorder, centerOffset, pinecrestOffset, strokeWidth = 1.5f)
                        drawLine(HairlineGlassBorder, centerOffset, sunsetOffset, strokeWidth = 1.5f)

                        val isElmwood = currentCell?.id == "cell_elmwood_north"
                        drawCircle(
                            color = if (isElmwood) FieryCoralSoft else ElevatedGlassSurface,
                            radius = 20f,
                            center = elmwoodOffset
                        )
                        drawCircle(FieryCoral, radius = 20f, center = elmwoodOffset, style = Stroke(1.8f))

                        val isPinecrest = currentCell?.id == "cell_pinecrest_park"
                        drawCircle(
                            color = if (isPinecrest) EmeraldSoft else ElevatedGlassSurface,
                            radius = 22f,
                            center = pinecrestOffset
                        )
                        drawCircle(EmeraldMint, radius = 22f, center = pinecrestOffset, style = Stroke(1.8f))

                        val isSunset = currentCell?.id == "cell_sunset_ridge"
                        drawCircle(
                            color = if (isSunset) AmberSoft else ElevatedGlassSurface,
                            radius = 18f,
                            center = sunsetOffset
                        )
                        drawCircle(AmberGlow, radius = 18f, center = sunsetOffset, style = Stroke(1.8f))

                        val isOakwood = currentCell?.id == "cell_oakwood_center" || currentCell == null
                        drawCircle(
                            color = if (isOakwood) CobaltSoft else ElevatedGlassSurface,
                            radius = 29f,
                            center = centerOffset
                        )
                        drawCircle(ElectricCobalt, radius = 29f, center = centerOffset, style = Stroke(2.5f))

                        if (activeLayer == "ALL" || activeLayer == "SAFE_SPOTS") {
                            val spot1 = Offset(cx + 38f, cy - 20f)
                            val spot2 = Offset(cx - 44f, cy + 30f)
                            drawCircle(AmberGlow, radius = 6.5f, center = spot1)
                            drawCircle(PureWhite, radius = 2.2f, center = spot1)
                            drawCircle(AmberGlow, radius = 6.5f, center = spot2)
                            drawCircle(PureWhite, radius = 2.2f, center = spot2)
                        }
                    }

                    Text(
                        text = "${currentCell?.name ?: "Oakwood"}\n${currentCell?.code ?: "#H3-8826"}",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PureWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = CircleShape,
                        color = AmberSoft,
                        border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AmberGlow)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "SAFE MEETING SPOT",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = AmberLight
                            )
                        }
                    }

                    Text(
                        text = "TAP ZONE TO FOCUS",
                        style = AuraKickerStyle,
                        color = SubtleSlate,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Cluster Directory Header & Horizontal Cell Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CLUSTER DIRECTORY",
                            style = AuraKickerStyle,
                            color = CobaltGlow
                        )
                        Text(
                            text = "Neighborhood cells",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ObsidianInk
                        )
                    }

                    GlassSecondaryPillButton(
                        text = "Recalibrate",
                        icon = Icons.Default.Refresh,
                        onClick = { showCalibrationDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cells) { cell ->
                        val isSelected = cell.id == currentCell?.id
                        Surface(
                            modifier = Modifier
                                .width(214.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onSelectCell(cell) },
                            shape = RoundedCornerShape(22.dp),
                            color = if (isSelected) CobaltSoft else DarkGlassCard,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) ElectricCobalt else HairlineGlassBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cell.name,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = ObsidianInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = CobaltGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = cell.code.uppercase(),
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isSelected) CobaltGlow else MutedSlate
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${cell.activeNeighborsCount} neighbors · ${cell.openNeedsCount} needs",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Cell Dossier Dark Glass Card
                currentCell?.let { cell ->
                    val sanitizedName = cell.name.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim(' ', ',', '-')
                    val sanitizedDesc = cell.description.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()
                    DarkGlassCard(
                        cornerRadius = 24.dp,
                        containerColor = DarkGlassCard,
                        borderColor = HairlineGlassBorder
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACTIVE CELL DOSSIER",
                                    style = AuraKickerStyle,
                                    color = EmeraldLight
                                )
                                Text(
                                    text = sanitizedName.ifBlank { "Local Neighborhood" },
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                    lineHeight = 25.sp,
                                    color = ObsidianInk,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sanitizedDesc.ifBlank { "Active physical mesh at your current location." },
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = MutedSlate,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = ElevatedGlassSurface,
                                border = BorderStroke(1.dp, HairlineGlassBorder)
                            ) {
                                Text(
                                    text = cell.code.uppercase(),
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = CobaltGlow,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "Neighbors",
                                value = "${cell.activeNeighborsCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Needs",
                                value = "${cell.openNeedsCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Offers",
                                value = "${cell.openOffersCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Civic",
                                value = "${cell.activeIssuesCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "VERIFIED PUBLIC EXCHANGE POINT",
                            style = AuraKickerStyle,
                            color = SubtleSlate
                        )
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSoft)
                                    .border(1.dp, EmeraldMint.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Oakwood Community Pavilion",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = ObsidianInk
                                )
                                Text(
                                    text = "Well-lit lobby and veranda with open daytime access for safe neighborhood exchanges.",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(112.dp))
            }

            if (showCalibrationDialog) {
                MeshCalibrationProgressDialog(
                    onDismiss = { showCalibrationDialog = false }
                )
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ElevatedGlassSurface,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                color = MutedSlate
            )
        }
    }
}

@Composable
fun MeshCalibrationProgressDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            contentPadding = PaddingValues(22.dp)
        ) {
            Text(
                text = "MESH CALIBRATION",
                style = AuraKickerStyle,
                color = CobaltGlow
            )
            Text(
                text = "Refining spatial mesh",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = ObsidianInk
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Aligning local H3 coordinate rings and discovering safe community exchange points.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.sp,
                color = MutedSlate,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(14.dp))

            ProcessingStepItem(
                stepNumber = "01",
                title = "Locating neighborhood coordinate band",
                isCompleted = true,
                isActive = false
            )
            ProcessingStepItem(
                stepNumber = "02",
                title = "Mapping verified exchange hubs",
                isCompleted = true,
                isActive = false
            )
            ProcessingStepItem(
                stepNumber = "03",
                title = "Connecting active Oakwood neighbors",
                isCompleted = false,
                isActive = true
            )
            ProcessingStepItem(
                stepNumber = "04",
                title = "Securing privacy-shielded resident pass",
                isCompleted = false,
                isActive = false
            )

            Spacer(modifier = Modifier.height(22.dp))

            PillPrimaryButton(
                text = "Complete calibration",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProcessingStepItem(
    stepNumber: String,
    title: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stepNumber,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isActive) FieryCoralGlow else if (isCompleted) EmeraldLight else MutedSlate
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) EmeraldMint else if (isActive) FieryCoral else HairlineGlassBorder
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (isActive || isCompleted) ObsidianInk else MutedSlate
        )
    }
}
