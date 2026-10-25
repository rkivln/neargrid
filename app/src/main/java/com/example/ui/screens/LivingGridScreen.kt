package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.PaleWarmGray
import com.example.ui.theme.PastelButterYellow
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelPowderBlue
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
fun LivingGridScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    cells: List<GridCell>,
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    onSelectCell: (GridCell) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onShowCellQr: ((GridCell) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeLayer by remember { mutableStateOf("ALL") }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().testTag("grid_pull_to_refresh")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
                                text = "Living spatial mesh",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 26.sp,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Discrete H3 hexagonal zones without private home pins",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentCell != null && onShowCellQr != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftCream,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onShowCellQr(currentCell) }
                                        .testTag("living_grid_qr_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "Cell QR",
                                            tint = PrimaryText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Invite QR",
                                            fontFamily = DMSansFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PastelMint.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PastelMint)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PrimaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Private grid",
                                        fontFamily = DMSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = PrimaryText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Layer selection chips
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
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelSoftCyan.copy(alpha = 0.35f),
                                    selectedLabelColor = PrimaryText,
                                    containerColor = SoftCream,
                                    labelColor = SecondaryText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) PastelSoftCyan else WarmBorder,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Spatial Grid Canvas Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(180.dp)
                    .testTag("interactive_grid_canvas"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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

                        // Delicate paper coordinate grid
                        val gridLineColor = Color(0xFFF0EFE8)
                        val step = 28f
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

                        // Discovery Radius Rings in soft pastel cyan & peach
                        val ringRadius = when (discoveryRadiusMeters) {
                            300 -> 38f
                            800 -> 65f
                            else -> 85f
                        }

                        drawCircle(
                            color = PastelPowderBlue.copy(alpha = 0.2f),
                            radius = ringRadius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = PastelSoftCyan,
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2f)
                        )

                        // Cell connection lines
                        val centerOffset = Offset(cx, cy)
                        val elmwoodOffset = Offset(cx - 80f, cy - 30f)
                        val pinecrestOffset = Offset(cx + 75f, cy + 35f)
                        val sunsetOffset = Offset(cx - 15f, cy - 55f)

                        val linkColor = Color(0xFFDCD8CC)
                        drawLine(linkColor, centerOffset, elmwoodOffset, strokeWidth = 1.5f)
                        drawLine(linkColor, centerOffset, pinecrestOffset, strokeWidth = 1.5f)
                        drawLine(linkColor, centerOffset, sunsetOffset, strokeWidth = 1.5f)

                        // Elmwood Cell
                        val isElmwood = currentCell?.id == "cell_elmwood_north"
                        drawCircle(
                            color = if (isElmwood) PastelSoftCyan else SoftCream,
                            radius = 20f,
                            center = elmwoodOffset
                        )
                        drawCircle(PastelCoral.copy(alpha = 0.6f), radius = 20f, center = elmwoodOffset, style = Stroke(1.5f))

                        // Pinecrest Cell
                        val isPinecrest = currentCell?.id == "cell_pinecrest_park"
                        drawCircle(
                            color = if (isPinecrest) PastelSoftCyan else SoftCream,
                            radius = 22f,
                            center = pinecrestOffset
                        )
                        drawCircle(PastelMint.copy(alpha = 0.8f), radius = 22f, center = pinecrestOffset, style = Stroke(1.5f))

                        // Sunset Ridge Cell
                        val isSunset = currentCell?.id == "cell_sunset_ridge"
                        drawCircle(
                            color = if (isSunset) PastelSoftCyan else SoftCream,
                            radius = 18f,
                            center = sunsetOffset
                        )
                        drawCircle(PastelPeach, radius = 18f, center = sunsetOffset, style = Stroke(1.5f))

                        // Center Cell (Oakwood Grove / Local Area)
                        val isOakwood = currentCell?.id == "cell_oakwood_center" || currentCell == null
                        drawCircle(
                            color = if (isOakwood) PastelPeach.copy(alpha = 0.6f) else SoftCream,
                            radius = 26f,
                            center = centerOffset
                        )
                        drawCircle(PastelCoral, radius = 26f, center = centerOffset, style = Stroke(2f))

                        // Safe Public Meeting Spots (Soft Butter Yellow Dots)
                        if (activeLayer == "ALL" || activeLayer == "SAFE_SPOTS") {
                            val spot1 = Offset(cx + 32f, cy - 18f)
                            val spot2 = Offset(cx - 38f, cy + 28f)
                            drawCircle(PastelButterYellow, radius = 6f, center = spot1)
                            drawCircle(PrimaryText, radius = 2f, center = spot1)
                            drawCircle(PastelButterYellow, radius = 6f, center = spot2)
                            drawCircle(PrimaryText, radius = 2f, center = spot2)
                        }
                    }

                    // Center Label
                    Text(
                        text = "${currentCell?.name ?: "Oakwood"}\n${currentCell?.code ?: "#H3-8826"}",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = PrimaryText,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Safe Spot Legend Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PastelButterYellow)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Safe meeting spot",
                                fontFamily = DMSansFontFamily,
                                fontSize = 10.sp,
                                color = PrimaryText
                            )
                        }
                    }

                    Text(
                        text = "Tap zone to focus",
                        fontFamily = DMSansFontFamily,
                        fontSize = 10.sp,
                        color = SecondaryText,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Cell Switcher Carousel Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Neighborhood cells in cluster",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 18.sp,
                    color = PrimaryText
                )

                // Recalibrate Button (triggers Section 8 progress process)
                Button(
                    onClick = { showCalibrationDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftCream,
                        contentColor = PrimaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = PrimaryText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recalibrate",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cells) { cell ->
                    val isSelected = cell.id == currentCell?.id
                    Card(
                        modifier = Modifier
                            .width(210.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectCell(cell) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SoftCream else PureWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PastelCoral.copy(alpha = 0.5f) else WarmBorder
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
                                    fontFamily = DMSerifFontFamily,
                                    fontSize = 17.sp,
                                    color = PrimaryText
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PastelCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = cell.code,
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${cell.activeNeighborsCount} neighbors • ${cell.openNeedsCount} needs",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Selected Cell Metrics Details Card
            currentCell?.let { cell ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cell.name,
                                        fontFamily = DMSerifFontFamily,
                                        fontSize = 20.sp,
                                        color = PrimaryText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PastelSoftCyan.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = cell.code,
                                            fontFamily = DMSansFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            color = PrimaryText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cell.description,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Metric Pills
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

                        Spacer(modifier = Modifier.height(18.dp))

                        // Verified Safe Public Exchange Spots in this cell
                        Text(
                            text = "Verified Public Exchange Point",
                            fontFamily = DMSerifFontFamily,
                            fontSize = 15.sp,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoftCream)
                                .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(PastelMint.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = PrimaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Oakwood Community Pavilion",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = PrimaryText
                                )
                                Text(
                                    text = "Well-lit lobby and veranda with open daytime access for safe neighborhood exchanges.",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
        shape = RoundedCornerShape(14.dp),
        color = SoftCream,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = DMSerifFontFamily,
                fontSize = 20.sp,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                color = SecondaryText
            )
        }
    }
}

// Section 8 Progress Screen implementation
@Composable
fun MeshCalibrationProgressDialog(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvory),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Refining spatial mesh",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 24.sp,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aligning local H3 coordinate rings and discovering safe community exchange points.",
                    fontFamily = DMSansFontFamily,
                    fontSize = 13.sp,
                    color = SecondaryText,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WarmDivider)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Stacked processing steps
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "Complete calibration",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }
            }
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
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isActive) PastelCoral else SecondaryText
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) PastelMint else if (isActive) PastelCoral else PaleWarmGray
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = DMSansFontFamily,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (isActive) PrimaryText else if (isCompleted) PrimaryText else SecondaryText
        )
    }
}
