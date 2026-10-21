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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack

@Composable
fun LivingGridScreen(
    cells: List<GridCell>,
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    onSelectCell: (GridCell) -> Unit,
    onRadiusChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeLayer by remember { mutableStateOf("ALL") }

    Box(modifier = modifier.fillMaxSize().background(PureBlack)) {
        // Oversized background typographic word
        Text(
            text = "MESH",
            fontFamily = AntonFontFamily,
            fontSize = 140.sp,
            color = CharcoalBlack.copy(alpha = 0.5f),
            letterSpacing = 6.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp)
                .offset(x = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalBlack,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LIVING SPATIAL MESH",
                                fontFamily = AntonFontFamily,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp,
                                color = BrightCyan
                            )
                            Text(
                                text = "H3 Discrete spatial cells without residential pins",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrightCyan
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = PureBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ZERO PINS",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = PureBlack
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Layer selection chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val layers = listOf(
                            "ALL" to "ALL ACTIVITY",
                            "NEEDS" to "OPEN NEEDS",
                            "OFFERS" to "AVAILABLE RESOURCES",
                            "SAFE_SPOTS" to "SAFE EXCHANGE SPOTS"
                        )
                        items(layers) { (key, label) ->
                            val isSelected = activeLayer == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { activeLayer = key },
                                label = {
                                    Text(
                                        label,
                                        fontFamily = AntonFontFamily,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrightCyan,
                                    selectedLabelColor = PureBlack,
                                    containerColor = DarkSurface,
                                    labelColor = EditorialWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) BrightCyan else DarkCardBorder,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Spatial Grid Canvas Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(310.dp)
                    .testTag("interactive_grid_canvas"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkCardBorder)
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

                        // Coordinate grid mesh
                        val gridColor = Color(0xFF262626)
                        val step = 32f
                        var x = 0f
                        while (x < w) {
                            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                            x += step
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                            y += step
                        }

                        // Discovery Radius Rings in Bright Cyan
                        val ringRadius = when (discoveryRadiusMeters) {
                            300 -> 60f
                            800 -> 110f
                            else -> 145f
                        }

                        drawCircle(
                            color = BrightCyan.copy(alpha = 0.08f),
                            radius = ringRadius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = BrightCyan.copy(alpha = 0.45f),
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5f)
                        )

                        // Cell connection lines
                        val centerOffset = Offset(cx, cy)
                        val elmwoodOffset = Offset(cx - 100f, cy - 45f)
                        val pinecrestOffset = Offset(cx + 95f, cy + 55f)
                        val sunsetOffset = Offset(cx - 20f, cy - 90f)

                        drawLine(Color(0xFF1E4552), centerOffset, elmwoodOffset, strokeWidth = 2.5f)
                        drawLine(Color(0xFF1E4552), centerOffset, pinecrestOffset, strokeWidth = 2.5f)
                        drawLine(Color(0xFF1E4552), centerOffset, sunsetOffset, strokeWidth = 2.5f)

                        // Elmwood Cell
                        val isElmwood = currentCell?.id == "cell_elmwood_north"
                        drawCircle(
                            color = if (isElmwood) BrightCyan else DarkSurface,
                            radius = 28f,
                            center = elmwoodOffset
                        )
                        drawCircle(BrightCyan, radius = 28f, center = elmwoodOffset, style = Stroke(2f))

                        // Pinecrest Cell
                        val isPinecrest = currentCell?.id == "cell_pinecrest_park"
                        drawCircle(
                            color = if (isPinecrest) BrightCyan else DarkSurface,
                            radius = 32f,
                            center = pinecrestOffset
                        )
                        drawCircle(BrightCyan, radius = 32f, center = pinecrestOffset, style = Stroke(2f))

                        // Sunset Ridge Cell
                        val isSunset = currentCell?.id == "cell_sunset_ridge"
                        drawCircle(
                            color = if (isSunset) BrightCyan else DarkSurface,
                            radius = 26f,
                            center = sunsetOffset
                        )
                        drawCircle(BrightCyan, radius = 26f, center = sunsetOffset, style = Stroke(2f))

                        // Center Cell (Oakwood Grove)
                        val isOakwood = currentCell?.id == "cell_oakwood_center" || currentCell == null
                        drawCircle(
                            color = if (isOakwood) BrightCyan else DarkSurface,
                            radius = 38f,
                            center = centerOffset
                        )
                        drawCircle(Color(0xFF7CFF00), radius = 38f, center = centerOffset, style = Stroke(3.5f))

                        // Safe Public Meeting Spots (Neon yellow dots)
                        if (activeLayer == "ALL" || activeLayer == "SAFE_SPOTS") {
                            val spot1 = Offset(cx + 42f, cy - 25f)
                            val spot2 = Offset(cx - 52f, cy + 42f)
                            drawCircle(Color(0xFFFFF000), radius = 7f, center = spot1)
                            drawCircle(PureBlack, radius = 2.5f, center = spot1)
                            drawCircle(Color(0xFFFFF000), radius = 7f, center = spot2)
                            drawCircle(PureBlack, radius = 2.5f, center = spot2)
                        }
                    }

                    // Center Label
                    Text(
                        text = "${currentCell?.name?.uppercase() ?: "OAKWOOD"}\n${currentCell?.code ?: "#H3-8826"}",
                        fontFamily = AntonFontFamily,
                        fontSize = 10.sp,
                        color = PureBlack,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Safe Spot Legend Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureBlack,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF000))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "SAFE SPOTS",
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = EditorialWhite
                            )
                        }
                    }

                    Text(
                        text = "TAP TO SWITCH CELL FOCUS",
                        fontFamily = AntonFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = NeutralGray,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Cell Switcher Carousel
            Text(
                text = "NEIGHBORHOOD CELLS IN CLUSTER",
                fontFamily = AntonFontFamily,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = BrightCyan,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cells) { cell ->
                    val isSelected = cell.id == currentCell?.id
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCell(cell) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSurface else CharcoalBlack
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) BrightCyan else DarkCardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cell.name.uppercase(),
                                    fontFamily = AntonFontFamily,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp,
                                    color = EditorialWhite
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BrightCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = cell.code,
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                color = BrightCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${cell.activeNeighborsCount} NEIGHBORS • ${cell.openNeedsCount} NEEDS",
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = NeutralGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Cell Metrics Details Card
            currentCell?.let { cell ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cell.name.uppercase(),
                                        fontFamily = AntonFontFamily,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp,
                                        color = EditorialWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BrightCyan
                                    ) {
                                        Text(
                                            text = cell.code,
                                            fontFamily = AntonFontFamily,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp,
                                            color = PureBlack,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cell.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Metric Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "NEIGHBORS",
                                value = "${cell.activeNeighborsCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "NEEDS",
                                value = "${cell.openNeedsCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "OFFERS",
                                value = "${cell.openOffersCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "ISSUES",
                                value = "${cell.activeIssuesCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Verified Safe Public Exchange Spots in this cell
                        Text(
                            text = "VERIFIED PUBLIC EXCHANGE POINTS",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = BrightCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PureBlack)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(BrightCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = PureBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Oakwood Community Pavilion",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialWhite
                                )
                                Text(
                                    text = "Lobby & porch with 24/7 lighting and security cameras for safe neighbor handoffs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralGray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
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
        shape = RoundedCornerShape(12.dp),
        color = PureBlack,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = AntonFontFamily,
                fontSize = 20.sp,
                letterSpacing = 0.5.sp,
                color = BrightCyan
            )
            Text(
                text = label,
                fontFamily = AntonFontFamily,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = NeutralGray
            )
        }
    }
}
