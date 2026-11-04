package com.example.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CommunityIssue
import com.example.data.model.NearbyNeighbor
import com.example.data.model.NeighborPost
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.util.LocationHelper
import com.example.util.UserLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/**
 * An interactive OpenStreetMap (OSM) map view component powered by osmdroid.
 * Displays user's live GPS coordinates, dynamic discovery radius circle,
 * nearby neighbors within radius in real time, and active community posts.
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
        // Initialize OSM User-Agent configuration to comply with OpenStreetMap tile usage policy
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val defaultLat = userLocation?.latitude ?: 37.7749
    val defaultLng = userLocation?.longitude ?: -122.4194

    Box(modifier = modifier.fillMaxSize().testTag("open_street_map_view")) {
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

                // 1. Add Discovery Radius Circle & User GPS Marker
                userLocation?.let { loc ->
                    if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                        val userPoint = GeoPoint(loc.latitude, loc.longitude)

                        // Visual Discovery Radius Circle
                        try {
                            val circle = Polygon().apply {
                                points = Polygon.pointsAsCircle(userPoint, discoveryRadiusMeters.toDouble())
                                fillPaint.color = AndroidColor.argb(30, 240, 140, 110) // Soft peach translucent fill
                                outlinePaint.color = AndroidColor.argb(180, 220, 100, 70) // Crisp border
                                outlinePaint.strokeWidth = 3.5f
                            }
                            mapView.overlays.add(circle)
                        } catch (ignored: Exception) {}

                        // User GPS Marker
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

                // 2. Add Nearby Neighbors (Real-Time Users in Radius)
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

                // 3. Add Active Neighbor Post Markers (Needs, Offers, Meetups)
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

                // 4. Add Community Issue Markers
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

        // Floating Map Controls (Zoom In / Out & Recenter GPS)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DeepNavy.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    IconButton(
                        onClick = { mapViewInstance?.controller?.zoomIn() },
                        modifier = Modifier.testTag("map_zoom_in_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", tint = PureWhite)
                    }
                    IconButton(
                        onClick = { mapViewInstance?.controller?.zoomOut() },
                        modifier = Modifier.testTag("map_zoom_out_button")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", tint = PureWhite)
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = PastelMint,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = {
                        userLocation?.let { loc ->
                            mapViewInstance?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                        }
                    },
                    modifier = Modifier.testTag("map_recenter_button")
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location", tint = DarkNavy)
                }
            }
        }

        // --- Nearby Neighbor Details Preview Card ---
        selectedNeighbor?.let { neighbor ->
            val u = neighbor.user
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag("map_selected_neighbor_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header row: Avatar, Info, and Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar badge
                        Surface(
                            shape = CircleShape,
                            color = PastelPeach.copy(alpha = 0.6f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = u.displayName.take(1).uppercase().ifBlank { "N" },
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = u.displayName.ifBlank { "Neighbor" },
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                if (!u.hideOnlineStatus) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Online indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (u.isOnline) PastelMint else Color.Gray.copy(alpha = 0.5f))
                                    )
                                }
                            }
                            if (u.handle.isNotBlank()) {
                                Text(
                                    text = u.handle,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                        }

                        // Close preview card button
                        IconButton(
                            onClick = { selectedNeighbor = null },
                            modifier = Modifier.size(32.dp).testTag("close_neighbor_preview_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Distance and Neighborhood Cell Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftCream,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PastelCoral,
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
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText
                                )
                            }
                        }

                        if (u.cellName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SoftCream,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                            ) {
                                Text(
                                    text = u.cellName,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    color = SecondaryText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Optional Recent Post snippet
                    neighbor.latestPost?.let { post ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (post.type == "NEED") PastelCoral else PastelMint
                                ) {
                                    Text(
                                        text = post.type,
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = post.title,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    color = PrimaryText,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions: View Profile & Private Chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onViewNeighborProfile(neighbor)
                                selectedNeighbor = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_neighbor_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DarkNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Profile",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkNavy,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                onStartChatWithNeighbor(neighbor)
                                selectedNeighbor = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkNavy,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_with_neighbor_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Private Chat",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Marker Details Popup Card for Neighbor Post ---
        selectedPost?.let { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag("map_selected_post_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftCream),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (post.type) {
                                "NEED" -> PastelCoral
                                "OFFER" -> PastelMint
                                else -> DeepNavy
                            }
                        ) {
                            Text(
                                text = post.type,
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (post.type == "MEETUP") PureWhite else DarkNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = post.cellName,
                            fontFamily = DMSansFontFamily,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = post.title,
                        fontFamily = DMSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    Text(
                        text = post.description,
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryText,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                onPostClick(post)
                                selectedPost = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Connect / Discuss", fontFamily = DMSansFontFamily, color = PureWhite)
                        }
                    }
                }
            }
        }

        // --- Marker Details Popup Card for Community Issue ---
        selectedIssue?.let { issue ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag("map_selected_issue_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftCream),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COMMUNITY ISSUE",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCoral
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = issue.title,
                        fontFamily = DMSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    Text(
                        text = issue.description,
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryText,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                onIssueClick(issue)
                                selectedIssue = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Community Issue", fontFamily = DMSansFontFamily, color = PureWhite)
                        }
                    }
                }
            }
        }
    }
}
