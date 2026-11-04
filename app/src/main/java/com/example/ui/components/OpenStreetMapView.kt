package com.example.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.data.model.NeighborPost
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.util.UserLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * An interactive OpenStreetMap (OSM) map view component powered by osmdroid.
 * Displays user's live GPS coordinates, nearby posts, and community issues with custom interactive pin markers.
 */
@Composable
fun OpenStreetMapView(
    userLocation: UserLocation?,
    posts: List<NeighborPost>,
    issues: List<CommunityIssue>,
    onPostClick: (NeighborPost) -> Unit,
    onIssueClick: (CommunityIssue) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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

                // 1. Add User Current GPS Marker
                userLocation?.let { loc ->
                    val userPoint = GeoPoint(loc.latitude, loc.longitude)
                    val userMarker = Marker(mapView).apply {
                        position = userPoint
                        title = "You (${loc.displayAreaName})"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedPost = null
                            selectedIssue = null
                            mapView.controller.animateTo(userPoint)
                            true
                        }
                    }
                    mapView.overlays.add(userMarker)
                }

                // 2. Add Active Neighbor Post Markers (Needs, Offers, Meetups)
                posts.forEach { post ->
                    val postPoint = GeoPoint(post.lat, post.lng)
                    val postMarker = Marker(mapView).apply {
                        position = postPoint
                        title = post.title
                        snippet = "${post.type} - ${post.category}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedPost = post
                            selectedIssue = null
                            mapView.controller.animateTo(postPoint)
                            true
                        }
                    }
                    mapView.overlays.add(postMarker)
                }

                // 3. Add Community Issue Markers
                issues.forEach { issue ->
                    val issuePoint = GeoPoint(37.7749, -122.4194) // Default fallback if no coordinates
                    val issueMarker = Marker(mapView).apply {
                        position = issuePoint
                        title = issue.title
                        snippet = "ISSUE - ${issue.category}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedIssue = issue
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

        // Marker Details Popup Card
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
