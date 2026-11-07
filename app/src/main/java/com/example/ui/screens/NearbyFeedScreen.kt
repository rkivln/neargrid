package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.NeighborPost
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BlueGray
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightLime
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.PrimarySurface
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SoftLimeSurface
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.SubtleDivider
import com.example.util.LocationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * NearbyFeedScreen is a production-ready Jetpack Compose screen that fetches local
 * content in real-time from Firestore based on the user's current coordinates and
 * displays them in a scrollable list with image previews, distance badges, and filters.
 */
@Composable
fun NearbyFeedScreen(
    userLatitude: Double,
    userLongitude: Double,
    localityName: String = "",
    onBackClick: (() -> Unit)? = null,
    onConnectPost: ((NeighborPost) -> Unit)? = null,
    onOpenComments: ((NeighborPost) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            NearbyFeedTopHeader(
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                localityName = localityName,
                onBackClick = onBackClick
            )

            // Core Nearby Feed Component
            NearbyFeedComponent(
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                onConnectPost = onConnectPost,
                onOpenComments = onOpenComments,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Reusable NearbyFeedComponent that can be embedded anywhere.
 * Manages Firestore coordinate query, distance calculation, filtering, and scrollable list.
 */
@Composable
fun NearbyFeedComponent(
    userLatitude: Double,
    userLongitude: Double,
    initialRadiusMeters: Int = 3000,
    onConnectPost: ((NeighborPost) -> Unit)? = null,
    onOpenComments: ((NeighborPost) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedRadiusMeters by remember { mutableIntStateOf(initialRadiusMeters) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, NEED, OFFER, MEETUP
    var onlyWithPhotos by remember { mutableStateOf(false) }
    var sortByDistance by remember { mutableStateOf(true) } // true = closest, false = newest

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val rawPosts = remember { mutableStateListOf<NeighborPost>() }

    // Selected image for full-screen viewer lightbox
    var activeViewerImageUri by remember { mutableStateOf<String?>(null) }
    var activeViewerTitle by remember { mutableStateOf("") }

    // Fetch and stream posts from Firestore based on coordinates
    val coroutineScope = rememberCoroutineScope()

    fun loadFirestorePosts() {
        coroutineScope.launch {
            isLoading = rawPosts.isEmpty()
            isRefreshing = rawPosts.isNotEmpty()
            errorMessage = null

            try {
                val db = try {
                    FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
                } catch (e: Exception) {
                    null
                }

                if (db != null) {
                    val snapshot = withContext(Dispatchers.IO) {
                        try {
                            db.collection("posts")
                                .whereEqualTo("status", "ACTIVE")
                                .get()
                                .await()
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val currentUid = Firebase.auth.currentUser?.uid ?: ""
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                val upvoters = (data["upvoterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                NeighborPost(
                                    id = doc.id,
                                    userId = data["userId"] as? String ?: "",
                                    type = data["type"] as? String ?: "NEED",
                                    title = data["title"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    category = data["category"] as? String ?: "TOOLS",
                                    authorName = data["authorName"] as? String ?: "Neighbor",
                                    authorInitials = data["authorInitials"] as? String ?: "N",
                                    authorCellId = data["authorCellId"] as? String ?: "cell_local",
                                    cellName = data["cellName"] as? String ?: "Nearby Block",
                                    lat = (data["lat"] as? Number)?.toDouble() ?: 0.0,
                                    lng = (data["lng"] as? Number)?.toDouble() ?: 0.0,
                                    approxDistanceBand = data["approxDistanceBand"] as? String ?: "within 300 m",
                                    urgency = data["urgency"] as? String ?: "NORMAL",
                                    expiresInHours = (data["expiresInHours"] as? Number)?.toInt() ?: 48,
                                    status = data["status"] as? String ?: "ACTIVE",
                                    mediaUris = (data["mediaUris"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    mediaTypes = (data["mediaTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    upvotesCount = (data["upvotesCount"] as? Number)?.toInt() ?: upvoters.size,
                                    hasUserUpvoted = upvoters.contains(currentUid),
                                    upvoterIds = upvoters,
                                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                                        ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        rawPosts.clear()
                        rawPosts.addAll(items)
                    } else if (rawPosts.isEmpty()) {
                        // If Firestore collection has no items yet, load curated seed posts around current coordinates
                        rawPosts.clear()
                        rawPosts.addAll(generateSeedPostsForCoordinates(userLatitude, userLongitude))
                    }
                } else if (rawPosts.isEmpty()) {
                    rawPosts.clear()
                    rawPosts.addAll(generateSeedPostsForCoordinates(userLatitude, userLongitude))
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to fetch nearby posts"
                if (rawPosts.isEmpty()) {
                    rawPosts.addAll(generateSeedPostsForCoordinates(userLatitude, userLongitude))
                }
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Load on launch and when coordinates update
    LaunchedEffect(userLatitude, userLongitude) {
        loadFirestorePosts()
    }

    // Attach real-time snapshot listener
    DisposableEffect(Unit) {
        var listenerRegistration: ListenerRegistration? = null
        try {
            val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            listenerRegistration = db.collection("posts")
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val currentUid = Firebase.auth.currentUser?.uid ?: ""
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                val upvoters = (data["upvoterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                NeighborPost(
                                    id = doc.id,
                                    userId = data["userId"] as? String ?: "",
                                    type = data["type"] as? String ?: "NEED",
                                    title = data["title"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    category = data["category"] as? String ?: "TOOLS",
                                    authorName = data["authorName"] as? String ?: "Neighbor",
                                    authorInitials = data["authorInitials"] as? String ?: "N",
                                    authorCellId = data["authorCellId"] as? String ?: "cell_local",
                                    cellName = data["cellName"] as? String ?: "Nearby Block",
                                    lat = (data["lat"] as? Number)?.toDouble() ?: 0.0,
                                    lng = (data["lng"] as? Number)?.toDouble() ?: 0.0,
                                    approxDistanceBand = data["approxDistanceBand"] as? String ?: "within 300 m",
                                    urgency = data["urgency"] as? String ?: "NORMAL",
                                    expiresInHours = (data["expiresInHours"] as? Number)?.toInt() ?: 48,
                                    status = data["status"] as? String ?: "ACTIVE",
                                    mediaUris = (data["mediaUris"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    mediaTypes = (data["mediaTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    upvotesCount = (data["upvotesCount"] as? Number)?.toInt() ?: upvoters.size,
                                    hasUserUpvoted = upvoters.contains(currentUid),
                                    upvoterIds = upvoters,
                                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                                        ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (items.isNotEmpty()) {
                            rawPosts.clear()
                            rawPosts.addAll(items)
                            isLoading = false
                        }
                    }
                }
        } catch (e: Throwable) {
            // Graceful fallback for non-Firebase environments
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    // Filter and sort items based on coordinates and active filter settings
    val filteredPosts = remember(rawPosts.toList(), userLatitude, userLongitude, selectedRadiusMeters, selectedTypeFilter, onlyWithPhotos, sortByDistance) {
        rawPosts.map { post ->
            val distMeters = if (userLatitude != 0.0 && userLongitude != 0.0 && post.lat != 0.0 && post.lng != 0.0) {
                LocationHelper.calculateDistanceMeters(userLatitude, userLongitude, post.lat, post.lng)
            } else null

            val distanceBand = if (distMeters != null) {
                LocationHelper.formatDistance(distMeters)
            } else {
                post.approxDistanceBand.ifBlank { "Nearby" }
            }
            Pair(post.copy(approxDistanceBand = distanceBand), distMeters)
        }.filter { (post, distMeters) ->
            val matchesType = selectedTypeFilter == "ALL" || post.type == selectedTypeFilter
            val matchesPhoto = !onlyWithPhotos || post.mediaUris.isNotEmpty()
            val matchesRadius = distMeters == null || distMeters <= selectedRadiusMeters
            matchesType && matchesPhoto && matchesRadius
        }.sortedWith { a, b ->
            if (sortByDistance) {
                val distA = a.second ?: Float.MAX_VALUE
                val distB = b.second ?: Float.MAX_VALUE
                distA.compareTo(distB)
            } else {
                b.first.createdAt.compareTo(a.first.createdAt)
            }
        }.map { it.first }
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Coordinate Banner & Distance Range Selector
            NearbyControlsBar(
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                selectedRadiusMeters = selectedRadiusMeters,
                onRadiusSelect = { selectedRadiusMeters = it },
                selectedType = selectedTypeFilter,
                onTypeSelect = { selectedTypeFilter = it },
                onlyWithPhotos = onlyWithPhotos,
                onTogglePhotos = { onlyWithPhotos = !onlyWithPhotos },
                sortByDistance = sortByDistance,
                onToggleSort = { sortByDistance = !sortByDistance },
                itemCount = filteredPosts.size,
                isRefreshing = isRefreshing,
                onRefresh = { loadFirestorePosts() }
            )

            // Content Feed
            if (isLoading) {
                NearbyFeedLoadingSkeleton()
            } else if (filteredPosts.isEmpty()) {
                NearbyFeedEmptyState(
                    radiusMeters = selectedRadiusMeters,
                    onlyWithPhotos = onlyWithPhotos,
                    onResetFilters = {
                        selectedRadiusMeters = 10000
                        selectedTypeFilter = "ALL"
                        onlyWithPhotos = false
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("nearby_feed_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        NearbyFeedCard(
                            post = post,
                            onConnect = { onConnectPost?.invoke(post) },
                            onOpenComments = { onOpenComments?.invoke(post) },
                            onImageClick = { uri, title ->
                                activeViewerImageUri = uri
                                activeViewerTitle = title
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // Fullscreen Lightbox Image Viewer Modal
        if (activeViewerImageUri != null) {
            FullScreenImageViewerDialog(
                imageUri = activeViewerImageUri!!,
                title = activeViewerTitle,
                onDismiss = { activeViewerImageUri = null }
            )
        }
    }
}

/**
 * Top Header bar with back action and locality coordinates info.
 */
@Composable
fun NearbyFeedTopHeader(
    userLatitude: Double,
    userLongitude: Double,
    localityName: String,
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PrimarySurface,
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftSurface)
                            .testTag("nearby_feed_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nearby Feed",
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryText
                        )
                    }
                    Text(
                        text = if (localityName.isNotBlank()) {
                            "$localityName • ${String.format(Locale.US, "%.3f, %.3f", userLatitude, userLongitude)}"
                        } else {
                            String.format(Locale.US, "GPS: %.4f, %.4f", userLatitude, userLongitude)
                        },
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftSurface,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Live GPS",
                        tint = SlateBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateBlue
                    )
                }
            }
        }
    }
}

/**
 * Filter and Controls Bar for Distance Radius, Intent Filter, and Photo Toggle.
 */
@Composable
fun NearbyControlsBar(
    userLatitude: Double,
    userLongitude: Double,
    selectedRadiusMeters: Int,
    onRadiusSelect: (Int) -> Unit,
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    onlyWithPhotos: Boolean,
    onTogglePhotos: () -> Unit,
    sortByDistance: Boolean,
    onToggleSort: () -> Unit,
    itemCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PrimarySurface,
        border = BorderStroke(1.dp, SubtleDivider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Distance Radius Capsule Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Proximity Radius",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val radiusOptions = listOf(500 to "500m", 1000 to "1km", 3000 to "3km", 5000 to "5km", 10000 to "10km")
                    radiusOptions.forEach { (meters, label) ->
                        val isSelected = selectedRadiusMeters == meters
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PrimaryText else SoftSurface,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryText else CardBorderColor),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onRadiusSelect(meters) }
                                .testTag("radius_chip_$label")
                        ) {
                            Text(
                                text = label,
                                fontFamily = ManropeFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else PrimaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips and Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val types = listOf(
                        "ALL" to "All",
                        "NEED" to "Needs",
                        "OFFER" to "Offers",
                        "MEETUP" to "Meetups"
                    )
                    items(types) { (key, label) ->
                        val isSelected = selectedType == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SlateBlue else SoftSurface,
                            border = BorderStroke(1.dp, if (isSelected) SlateBlue else CardBorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTypeSelect(key) }
                                .testTag("type_filter_$key")
                        ) {
                            Text(
                                text = label,
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) PureWhite else PrimaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Photos Only Filter Pill
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (onlyWithPhotos) AccentLime else SoftSurface,
                            border = BorderStroke(1.dp, if (onlyWithPhotos) LightLime else CardBorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTogglePhotos() }
                                .testTag("photos_only_filter_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = PrimaryText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Photos Only",
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort and Refresh Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSort,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SoftSurface)
                            .testTag("nearby_feed_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = if (sortByDistance) "Sorted by Distance" else "Sorted by Date",
                            tint = if (sortByDistance) SlateBlue else PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SoftSurface)
                            .testTag("nearby_feed_refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = SlateBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = PrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Count summary readout
            Text(
                text = "$itemCount posts found within ${LocationHelper.formatDistance(selectedRadiusMeters.toFloat())}",
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                color = SecondaryText
            )
        }
    }
}

/**
 * Individual Card in the Nearby Feed with Prominent Image Previews,
 * Proximity Badge, Category Pill, and Action Buttons.
 */
@Composable
fun NearbyFeedCard(
    post: NeighborPost,
    onConnect: () -> Unit,
    onOpenComments: () -> Unit,
    onImageClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var upvoted by remember(post.id) { mutableStateOf(post.hasUserUpvoted) }
    var upvotesCount by remember(post.id) { mutableIntStateOf(post.upvotesCount) }

    val isOffer = post.type == "OFFER"
    val isMeetup = post.type == "MEETUP"

    val typeColor = when {
        isOffer -> AccentLime
        isMeetup -> BlueGray
        else -> SlateBlue
    }

    val typeLabel = when {
        isOffer -> "OFFER"
        isMeetup -> "MEETUP"
        else -> "NEED"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nearby_feed_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimarySurface),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Author Info + Distance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isOffer) AccentLime.copy(alpha = 0.35f) else SoftSurface)
                            .border(1.dp, CardBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorInitials.ifBlank { "N" },
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = post.authorName.ifBlank { "Neighbor" },
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PrimaryText
                        )
                        Text(
                            text = post.cellName.ifBlank { "Local Block" },
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                }

                // Proximity Distance Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftSurface,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Distance",
                            tint = SlateBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.approxDistanceBand,
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Title
            Text(
                text = post.title,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                color = PrimaryText
            )

            if (post.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.description,
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = SecondaryText,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Image Previews Section
            if (post.mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                NearbyImagePreviewSection(
                    mediaUris = post.mediaUris,
                    postTitle = post.title,
                    onImageClick = onImageClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tags & Meta Row (Type, Category, Urgency)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = typeLabel,
                            fontFamily = ManropeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = post.category,
                            fontFamily = ManropeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Urgency Indicator if urgent
                    if (post.urgency == "URGENT") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Text(
                                text = "URGENT",
                                fontFamily = ManropeFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Relative Date or Hours
                Text(
                    text = "${post.expiresInHours}h left",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Footer (Upvote, Comment, Connect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (upvoted) SoftLimeSurface else SoftSurface,
                        border = BorderStroke(1.dp, if (upvoted) LightLime else CardBorderColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                upvoted = !upvoted
                                upvotesCount += if (upvoted) 1 else -1
                            }
                            .testTag("nearby_post_upvote_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (upvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote",
                                tint = if (upvoted) PrimaryText else SecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$upvotesCount",
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText
                            )
                        }
                    }

                    // Comments Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSurface,
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenComments() }
                            .testTag("nearby_post_comments_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = SecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chat",
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryText
                            )
                        }
                    }
                }

                // Connect Action Button
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText,
                        contentColor = PureWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("nearby_post_connect_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOffer) "Claim Offer" else "Coordinate",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Image Preview Section supporting single hero images and multiple thumbnail carousels.
 */
@Composable
fun NearbyImagePreviewSection(
    mediaUris: List<String>,
    postTitle: String,
    onImageClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaUris.size == 1) {
        // Single Hero Image Banner
        val uri = mediaUris.first()
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkNavy)
                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                .clickable { onImageClick(uri, postTitle) }
                .testTag("nearby_image_preview_single")
        ) {
            AsyncImage(
                model = uri,
                error = painterResource(id = R.drawable.img_neighborhood_connection),
                contentDescription = "Image preview for $postTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Zoom pill indicator
            Surface(
                shape = CircleShape,
                color = PureWhite.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Tap to expand ↗",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    } else {
        // Multi-image preview row / carousel
        Column(modifier = modifier.fillMaxWidth()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(mediaUris) { index, uri ->
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .width(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkNavy)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
                            .clickable { onImageClick(uri, "$postTitle (${index + 1}/${mediaUris.size})") }
                            .testTag("nearby_image_preview_thumb_$index")
                    ) {
                        AsyncImage(
                            model = uri,
                            error = painterResource(id = R.drawable.img_neighborhood_connection),
                            contentDescription = "Photo ${index + 1} for $postTitle",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Index badge
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "${index + 1}/${mediaUris.size}",
                                fontFamily = ManropeFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Lightbox Image Viewer Modal when a resident clicks on an image preview.
 */
@Composable
fun FullScreenImageViewerDialog(
    imageUri: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
                .testTag("fullscreen_image_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.ifBlank { "Photo Preview" },
                        fontFamily = ManropeFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("close_fullscreen_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close preview",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        error = painterResource(id = R.drawable.img_neighborhood_connection),
                        contentDescription = "Full photo preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Bottom Hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tap anywhere to dismiss",
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Loading Skeleton when fetching Firestore content.
 */
@Composable
fun NearbyFeedLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimarySurface),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SoftSurface)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftSurface)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftSurface)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SoftSurface)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftSurface)
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no posts match radius or coordinate criteria.
 */
@Composable
fun NearbyFeedEmptyState(
    radiusMeters: Int,
    onlyWithPhotos: Boolean,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("nearby_feed_empty_state"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimarySurface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SoftSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = SlateBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Nearby Content in Range",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (onlyWithPhotos) {
                    "No posts with photo previews found within ${LocationHelper.formatDistance(radiusMeters.toFloat())}."
                } else {
                    "There are currently no active neighbor posts within ${LocationHelper.formatDistance(radiusMeters.toFloat())} of your coordinates."
                },
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onResetFilters,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryText,
                    contentColor = PureWhite
                ),
                modifier = Modifier.testTag("nearby_feed_expand_radius_button")
            ) {
                Text(
                    text = "Expand Radius to 10 km",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Generates sample seed posts around the provided coordinates to ensure
 * instantaneous visual feedback even on fresh environments without pre-seeded Firestore records.
 */
fun generateSeedPostsForCoordinates(lat: Double, lng: Double): List<NeighborPost> {
    val baseLat = if (lat != 0.0) lat else 37.7749
    val baseLng = if (lng != 0.0) lng else -122.4194

    return listOf(
        NeighborPost(
            id = "nearby_seed_1",
            userId = "seed_user_1",
            type = "OFFER",
            title = "Cordless Garden Trimmer & Hedge Shears",
            description = "Recently finished landscaping my front lawn. Offering my battery-powered hedge shears to any neighbor working on yard maintenance this weekend.",
            category = "TOOLS",
            authorName = "Marcus Vance",
            authorInitials = "MV",
            cellName = "Maple Ridge Sector",
            lat = baseLat + 0.0018,
            lng = baseLng + 0.0012,
            approxDistanceBand = "180 m away",
            urgency = "NORMAL",
            expiresInHours = 36,
            mediaUris = listOf(
                "https://images.unsplash.com/photo-1592417817098-8f3d69102a56?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1589923188900-85dae523342b?auto=format&fit=crop&w=800&q=80"
            ),
            upvotesCount = 7,
            createdAt = System.currentTimeMillis() - 3600000
        ),
        NeighborPost(
            id = "nearby_seed_2",
            userId = "seed_user_2",
            type = "NEED",
            title = "Tall Aluminum Step Ladder (8ft or higher)",
            description = "Need to clean second-story rain gutters after the autumn rain. Just need it for 2 hours on Saturday morning. Will return promptly!",
            category = "HOUSEHOLD",
            authorName = "Dr. Priya Sen",
            authorInitials = "PS",
            cellName = "Sunnyvale Avenue",
            lat = baseLat - 0.0035,
            lng = baseLng + 0.0028,
            approxDistanceBand = "420 m away",
            urgency = "TIME_SENSITIVE",
            expiresInHours = 24,
            mediaUris = listOf(
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80"
            ),
            upvotesCount = 12,
            createdAt = System.currentTimeMillis() - 7200000
        ),
        NeighborPost(
            id = "nearby_seed_3",
            userId = "seed_user_3",
            type = "MEETUP",
            title = "Weekend Pocket Park Community Gardening",
            description = "Join 8 neighbors for our monthly native flower planting and path mulching session at the corner pocket garden. Cold cider provided!",
            category = "COMMUNITY",
            authorName = "Elena Rostova",
            authorInitials = "ER",
            cellName = "Elmwood Commons",
            lat = baseLat + 0.0062,
            lng = baseLng - 0.0045,
            approxDistanceBand = "780 m away",
            urgency = "NORMAL",
            expiresInHours = 48,
            mediaUris = listOf(
                "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=800&q=80"
            ),
            upvotesCount = 19,
            createdAt = System.currentTimeMillis() - 14400000
        ),
        NeighborPost(
            id = "nearby_seed_4",
            userId = "seed_user_4",
            type = "OFFER",
            title = "Extra Sourdough Starter & Fresh Hearth Loaf",
            description = "Baked three artisan sourdough loaves this morning. Have one extra warm loaf and active starter jars available for porch pickup.",
            category = "HELP",
            authorName = "David Chen",
            authorInitials = "DC",
            cellName = "Oakwood Grove",
            lat = baseLat - 0.0085,
            lng = baseLng - 0.0070,
            approxDistanceBand = "1.2 km away",
            urgency = "NORMAL",
            expiresInHours = 12,
            mediaUris = listOf(
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80"
            ),
            upvotesCount = 24,
            createdAt = System.currentTimeMillis() - 21600000
        )
    )
}
