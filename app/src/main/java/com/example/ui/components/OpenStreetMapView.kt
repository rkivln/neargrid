package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CommunityIssue
import com.example.data.model.NearbyNeighbor
import com.example.data.model.NeighborPost
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltHeroGradient
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
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
import com.example.util.LocationHelper
import com.example.util.UserLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/**
 * Interactive OpenStreetMap (OSM) view component styled for the Obsidian Cobalt Aura theme.
 */
@Composable
fun OpenStreetMapView(
    userLocation: UserLocation?,
    nearbyNeighbors: List<NearbyNeighbor> = emptyList(),
    discoveryRadiusMeters: Int = 1000,
    posts: List<NeighborPost> = emptyList(),
    issues: List<CommunityIssue> = emptyList(),
    onNeighborClick: (NearbyNeighbor) -> Unit = {},
    onStartChatWithNeighbor: (NearbyNeighbor) -> Unit = {},
    onViewNeighborProfile: (NearbyNeighbor) -> Unit = {},
    onPostClick: (NeighborPost) -> Unit = {},
    onIssueClick: (CommunityIssue) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedNeighbor by remember { mutableStateOf<NearbyNeighbor?>(null) }
    var selectedPost by remember { mutableStateOf<NeighborPost?>(null) }
    var selectedIssue by remember { mutableStateOf<CommunityIssue?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val defaultLat = userLocation?.latitude ?: 37.7749
    val defaultLng = userLocation?.longitude ?: -122.4194

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("open_street_map_view")
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(GeoPoint(defaultLat, defaultLng))
                    mapViewInstance = this
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // 1. Discovery Radius Circle & User GPS Marker
                userLocation?.let { loc ->
                    if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                        val userPoint = GeoPoint(loc.latitude, loc.longitude)

                        try {
                            val circle = Polygon().apply {
                                points = Polygon.pointsAsCircle(userPoint, discoveryRadiusMeters.toDouble())
                                fillPaint.color = AndroidColor.argb(38, 46, 107, 255)
                                outlinePaint.color = AndroidColor.argb(215, 96, 165, 250)
                                outlinePaint.strokeWidth = 4.0f
                            }
                            mapView.overlays.add(circle)
                        } catch (ignored: Exception) {}

                        val userMarker = Marker(mapView).apply {
                            position = userPoint
                            title = "You (${loc.displayAreaName})"
                            snippet = "Discovery Radius: ${LocationHelper.formatDistance(discoveryRadiusMeters.toFloat())}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedNeighbor = null
                                selectedPost = null
                                selectedIssue = null
                                mapView.controller.animateTo(userPoint)
                                true
                            }
                        }
                        mapView.overlays.add(userMarker)
                    }
                }

                // 2. Nearby Neighbors
                nearbyNeighbors.forEach { neighbor ->
                    val u = neighbor.user
                    if (u.latitude != 0.0 && u.longitude != 0.0) {
                        val neighborPoint = GeoPoint(u.latitude, u.longitude)
                        val distSnippet = if (u.hideExactDistance) {
                            if (neighbor.distanceMeters <= 500) "< 500 m away" else "In neighborhood"
                        } else {
                            "${LocationHelper.formatDistance(neighbor.distanceMeters)} away"
                        }
                        val neighborMarker = Marker(mapView).apply {
                            position = neighborPoint
                            title = u.displayName.ifBlank { "Neighbor" }
                            snippet = distSnippet
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedNeighbor = neighbor
                                selectedPost = null
                                selectedIssue = null
                                onNeighborClick(neighbor)
                                mapView.controller.animateTo(neighborPoint)
                                true
                            }
                        }
                        mapView.overlays.add(neighborMarker)
                    }
                }

                // 3. Neighbor Post Markers
                posts.forEach { post ->
                    if (post.lat != 0.0 && post.lng != 0.0) {
                        val postPoint = GeoPoint(post.lat, post.lng)
                        val postMarker = Marker(mapView).apply {
                            position = postPoint
                            title = post.title
                            snippet = "${post.type} • ${post.category}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedPost = post
                                selectedNeighbor = null
                                selectedIssue = null
                                mapView.controller.animateTo(postPoint)
                                true
                            }
                        }
                        mapView.overlays.add(postMarker)
                    }
                }

                // 4. Community Issue Markers
                issues.forEach { issue ->
                    val issuePoint = GeoPoint(37.7749, -122.4194)
                    val issueMarker = Marker(mapView).apply {
                        position = issuePoint
                        title = issue.title
                        snippet = "ISSUE • ${issue.category}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedIssue = issue
                            selectedNeighbor = null
                            selectedPost = null
                            mapView.controller.animateTo(issuePoint)
                            true
                        }
                    }
                    mapView.overlays.add(issueMarker)
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Dark Glass Map Controls (Zoom In / Out & Recenter GPS)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 186.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = DarkGlassCard.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { mapViewInstance?.controller?.zoomIn() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = PureWhite
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(HairlineGlassBorder)
                    )
                    IconButton(
                        onClick = { mapViewInstance?.controller?.zoomOut() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = PureWhite
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkGlassCard.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, CobaltBorder.copy(alpha = 0.65f)),
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = {
                        userLocation?.let { loc ->
                            mapViewInstance?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                        }
                    },
                    modifier = Modifier.testTag("map_recenter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = CobaltGlow
                    )
                }
            }
        }

        // --- Nearby Neighbor Details Preview Overlay ---
        selectedNeighbor?.let { neighbor ->
            val u = neighbor.user
            DarkGlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 96.dp)
                    .testTag("map_selected_neighbor_card"),
                cornerRadius = 26.dp,
                containerColor = DarkGlassCard.copy(alpha = 0.96f),
                borderColor = HairlineGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CobaltHeroGradient)
                            .border(1.dp, CobaltGlow.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = u.displayName.take(1).uppercase().ifBlank { "N" },
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = u.displayName.ifBlank { "Neighbor" },
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ObsidianInk
                            )
                            if (!u.hideOnlineStatus) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (u.isOnline) EmeraldMint else SubtleSlate)
                                )
                            }
                        }
                        if (u.handle.isNotBlank()) {
                            Text(
                                text = u.handle,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate
                            )
                        }
                    }

                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = { selectedNeighbor = null },
                        size = 40.dp,
                        modifier = Modifier.testTag("close_neighbor_preview_button")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CobaltSoft,
                        border = BorderStroke(1.dp, CobaltBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val distLabel = if (u.hideExactDistance) {
                                if (neighbor.distanceMeters <= 500) "< 500 m away" else "In neighborhood"
                            } else {
                                "${LocationHelper.formatDistance(neighbor.distanceMeters)} away"
                            }
                            Text(
                                text = distLabel,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                        }
                    }

                    if (u.cellName.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = ElevatedGlassSurface,
                            border = BorderStroke(1.dp, HairlineGlassBorder)
                        ) {
                            Text(
                                text = u.cellName,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                color = MutedSlate,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                neighbor.latestPost?.let { post ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ElevatedGlassSurface,
                        border = BorderStroke(1.dp, HairlineGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isNeed = post.type == "NEED"
                            Surface(
                                shape = CircleShape,
                                color = if (isNeed) FieryCoralSoft else EmeraldSoft,
                                border = BorderStroke(
                                    1.dp,
                                    (if (isNeed) FieryCoral else EmeraldMint).copy(alpha = 0.45f)
                                )
                            ) {
                                Text(
                                    text = post.type,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNeed) FieryCoralGlow else EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.title,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ObsidianInk,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassSecondaryPillButton(
                        text = "Profile",
                        icon = Icons.Default.Person,
                        onClick = {
                            onViewNeighborProfile(neighbor)
                            selectedNeighbor = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_neighbor_profile_button")
                    )

                    PillPrimaryButton(
                        text = "Private Chat",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = {
                            onStartChatWithNeighbor(neighbor)
                            selectedNeighbor = null
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("chat_with_neighbor_button")
                    )
                }
            }
        }

        // --- Marker Details Popup Card for Neighbor Post ---
        selectedPost?.let { post ->
            DarkGlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 96.dp)
                    .testTag("map_selected_post_card"),
                cornerRadius = 26.dp,
                containerColor = DarkGlassCard.copy(alpha = 0.96f),
                borderColor = HairlineGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNeed = post.type == "NEED"
                    val isOffer = post.type == "OFFER"
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isNeed -> FieryCoralSoft
                            isOffer -> EmeraldSoft
                            else -> AmberSoft
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isNeed -> FieryCoral.copy(alpha = 0.5f)
                                isOffer -> EmeraldMint.copy(alpha = 0.5f)
                                else -> AmberGlow.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Text(
                            text = post.type,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isNeed -> FieryCoralGlow
                                isOffer -> EmeraldLight
                                else -> AmberLight
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.cellName,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularOutlineIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { selectedPost = null },
                            size = 34.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = post.title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = ObsidianInk
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.description,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                PillPrimaryButton(
                    text = "Connect / Discuss",
                    onClick = {
                        onPostClick(post)
                        selectedPost = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- Marker Details Popup Card for Community Issue ---
        selectedIssue?.let { issue ->
            DarkGlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 96.dp)
                    .testTag("map_selected_issue_card"),
                cornerRadius = 26.dp,
                containerColor = DarkGlassCard.copy(alpha = 0.96f),
                borderColor = FieryCoral.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMMUNITY ISSUE",
                        style = AuraKickerStyle,
                        color = FieryCoralGlow
                    )
                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = { selectedIssue = null },
                        size = 34.dp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = issue.title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = ObsidianInk
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = issue.description,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                PillPrimaryButton(
                    text = "View Community Issue",
                    useCoral = true,
                    onClick = {
                        onIssueClick(issue)
                        selectedIssue = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
