package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.NeighborPost
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.FrostedGlassBurgerPopup
import com.example.ui.components.FrostedMenuOptionPill
import com.example.ui.components.FrostedMenuSectionTitle
import com.example.ui.components.LiquidGlassBackButton
import com.example.ui.components.LiquidGlassBurgerMenuButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.PillPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltHeroGradient
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
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.util.LocationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

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
    AuraMeshBackground(
        variant = AuraVariant.MIXED,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NearbyFeedTopHeader(
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                localityName = localityName,
                onBackClick = onBackClick
            )

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
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var onlyWithPhotos by remember { mutableStateOf(false) }
    var sortByDistance by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val rawPosts = remember { mutableStateListOf<NeighborPost>() }

    var activeViewerImageUri by remember { mutableStateOf<String?>(null) }
    var activeViewerTitle by remember { mutableStateOf("") }

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

    LaunchedEffect(userLatitude, userLongitude) {
        loadFirestorePosts()
    }

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
            // Graceful fallback
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

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
            NearbyControlsBar(
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Spacer(
                            modifier = Modifier
                                .height(110.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }

        if (activeViewerImageUri != null) {
            FullScreenImageViewerDialog(
                imageUri = activeViewerImageUri!!,
                title = activeViewerTitle,
                onDismiss = { activeViewerImageUri = null }
            )
        }
    }
}

@Composable
fun NearbyFeedTopHeader(
    userLatitude: Double,
    userLongitude: Double,
    localityName: String,
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBackClick != null) {
                LiquidGlassBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("nearby_feed_back_button")
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = "PROXIMITY WIRE · LIVE",
                    style = AuraKickerStyle,
                    color = CobaltGlow
                )
                Text(
                    text = "Nearby Feed",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    color = ObsidianInk
                )
                Text(
                    text = if (localityName.isNotBlank()) {
                        "$localityName · ${String.format(Locale.US, "%.3f, %.3f", userLatitude, userLongitude)}"
                    } else {
                        String.format(Locale.US, "GPS: %.4f, %.4f", userLatitude, userLongitude)
                    },
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = MutedSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = EmeraldSoft,
            border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Live GPS",
                    tint = EmeraldLight,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "LIVE",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = EmeraldLight
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyControlsBar(
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
    var showMenu by remember { mutableStateOf(false) }
    val activeCount = (if (selectedType != "ALL") 1 else 0) +
        (if (onlyWithPhotos) 1 else 0) +
        (if (!sortByDistance) 1 else 0)
    val typeSummary = when (selectedType) {
        "NEED" -> "Needs"
        "OFFER" -> "Offers"
        "MEETUP" -> "Meetups"
        else -> "All"
    }
    val sortSummary = if (sortByDistance) "Closest first" else "Newest first"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$itemCount nearby · ${LocationHelper.formatDistance(selectedRadiusMeters.toFloat())}",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$typeSummary · $sortSummary" + if (onlyWithPhotos) " · Photos" else "",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = if (activeCount > 0) CobaltGlow else MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CircularOutlineIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Refresh",
                onClick = onRefresh,
                size = 42.dp,
                containerColor = ElevatedGlassSurface,
                borderColor = HairlineGlassBorder,
                iconTint = PureWhite,
                modifier = Modifier.testTag("nearby_feed_refresh_button")
            )

            Box {
                LiquidGlassBurgerMenuButton(
                    activeFilterCount = activeCount,
                    onClick = { showMenu = !showMenu },
                    contentDescription = "Open nearby sort and filter menu",
                    modifier = Modifier.testTag("nearby_filter_menu_button")
                )

                FrostedGlassBurgerPopup(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    width = 304.dp
                ) {
                    Text(
                        text = "Nearby Filters",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PureWhite
                    )

                    HorizontalDivider(thickness = 1.dp, color = com.example.ui.theme.SubtleGlassDivider)

                    FrostedMenuSectionTitle("Sort Order")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FrostedMenuOptionPill(
                            label = "Closest First",
                            selected = sortByDistance,
                            onClick = {
                                if (!sortByDistance) onToggleSort()
                                showMenu = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nearby_feed_sort_button")
                        )
                        FrostedMenuOptionPill(
                            label = "Newest First",
                            selected = !sortByDistance,
                            onClick = {
                                if (sortByDistance) onToggleSort()
                                showMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FrostedMenuSectionTitle("Post Type")
                    val types = listOf(
                        "ALL" to "All",
                        "NEED" to "Needs",
                        "OFFER" to "Offers",
                        "MEETUP" to "Meetups"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { (key, label) ->
                            val selectedBg = when (key) {
                                "NEED" -> FieryCoral
                                "OFFER" -> EmeraldMint
                                "MEETUP" -> AmberGlow
                                else -> ElectricCobalt
                            }
                            FrostedMenuOptionPill(
                                label = label,
                                selected = selectedType == key,
                                selectedColor = selectedBg,
                                onClick = {
                                    onTypeSelect(key)
                                    showMenu = false
                                },
                                modifier = Modifier.testTag("type_filter_$key")
                            )
                        }
                    }

                    FrostedMenuSectionTitle("Radius")
                    val radiusOptions = listOf(500 to "500m", 1000 to "1km", 3000 to "3km", 5000 to "5km", 10000 to "10km")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        radiusOptions.forEach { (meters, label) ->
                            FrostedMenuOptionPill(
                                label = label,
                                selected = selectedRadiusMeters == meters,
                                onClick = {
                                    onRadiusSelect(meters)
                                    showMenu = false
                                },
                                modifier = Modifier.testTag("radius_chip_$label")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Photos only",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PureWhite
                        )
                        LiquidGlassSwitch(
                            checked = onlyWithPhotos,
                            onCheckedChange = { onTogglePhotos() },
                            modifier = Modifier.testTag("photos_only_filter_button")
                        )
                    }
                }
            }
        }
    }
}

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
    val isNeed = post.type == "NEED" || (!isOffer && !isMeetup)

    val (badgeBg, badgeBorder, badgeText) = when {
        isNeed -> Triple(FieryCoralSoft, FieryCoral.copy(alpha = 0.5f), FieryCoralGlow)
        isOffer -> Triple(EmeraldSoft, EmeraldMint.copy(alpha = 0.5f), EmeraldLight)
        else -> Triple(AmberSoft, AmberGlow.copy(alpha = 0.5f), AmberLight)
    }

    val typeLabel = when {
        isOffer -> "OFFER"
        isMeetup -> "MEETUP"
        else -> "NEED"
    }

    DarkGlassCard(
        modifier = modifier.testTag("nearby_feed_card_${post.id}"),
        cornerRadius = 24.dp,
        containerColor = DarkGlassCard,
        borderColor = HairlineGlassBorder,
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        // Header: Author Info + Distance Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CobaltHeroGradient)
                        .border(1.dp, CobaltGlow.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorInitials.ifBlank { "N" },
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.authorName.ifBlank { "Neighbor" },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ObsidianInk
                    )
                    Text(
                        text = post.cellName.ifBlank { "Local Block" },
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = MutedSlate
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = ElevatedGlassSurface,
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        tint = CobaltGlow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.approxDistanceBand,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObsidianInk
                    )
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
            color = ObsidianInk,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        if (post.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.description,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = MutedSlate,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        if (post.mediaUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            NearbyImagePreviewSection(
                mediaUris = post.mediaUris,
                postTitle = post.title,
                onImageClick = onImageClick
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeBorder)
                ) {
                    Text(
                        text = typeLabel,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ElevatedGlassSurface,
                    border = BorderStroke(1.dp, HairlineGlassBorder)
                ) {
                    Text(
                        text = post.category.uppercase(),
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = MutedSlate,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                if (post.urgency == "URGENT") {
                    Surface(
                        shape = CircleShape,
                        color = FieryCoralSoft,
                        border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "URGENT",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = FieryCoralGlow,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = "${post.expiresInHours}H LEFT",
                style = AuraKickerStyle,
                color = MutedSlate
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (upvoted) CobaltSoft else ElevatedGlassSurface,
                    border = BorderStroke(1.dp, if (upvoted) CobaltBorder else HairlineGlassBorder),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            upvoted = !upvoted
                            upvotesCount += if (upvoted) 1 else -1
                        }
                        .testTag("nearby_post_upvote_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (upvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (upvoted) CobaltGlow else MutedSlate,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$upvotesCount",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ObsidianInk
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = ElevatedGlassSurface,
                    border = BorderStroke(1.dp, HairlineGlassBorder),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onOpenComments() }
                        .testTag("nearby_post_comments_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MutedSlate,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Chat",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObsidianInk
                        )
                    }
                }
            }

            // Connect Hero Pill CTA
            Surface(
                shape = CircleShape,
                color = PureWhite,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onConnect() }
                    .testTag("nearby_post_connect_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOffer) "Claim Offer" else "Coordinate",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack
                    )
                }
            }
        }
    }
}

@Composable
fun NearbyImagePreviewSection(
    mediaUris: List<String>,
    postTitle: String,
    onImageClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaUris.size == 1) {
        val uri = mediaUris.first()
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(210.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCanvas)
                .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
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

            Surface(
                shape = CircleShape,
                color = ObsidianCanvas.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Full view ↗",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(mediaUris) { index, uri ->
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .width(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianCanvas)
                            .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
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

                        Surface(
                            shape = CircleShape,
                            color = ObsidianCanvas.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}/${mediaUris.size}",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                .background(ObsidianCanvas.copy(alpha = 0.96f))
                .clickable { onDismiss() }
                .testTag("fullscreen_image_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.ifBlank { "Photo Preview" },
                        fontFamily = OutfitFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close preview",
                        onClick = onDismiss,
                        size = 40.dp,
                        modifier = Modifier.testTag("close_fullscreen_image_button")
                    )
                }

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
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tap anywhere to dismiss",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                }
            }
        }
    }
}

@Composable
fun NearbyFeedLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            DarkGlassCard(
                cornerRadius = 24.dp,
                containerColor = DarkGlassCard,
                borderColor = HairlineGlassBorder
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElevatedGlassSurface)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(14.dp)
                                .clip(CircleShape)
                                .background(ElevatedGlassSurface)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(ElevatedGlassSurface)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(18.dp)
                        .clip(CircleShape)
                        .background(ElevatedGlassSurface)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElevatedGlassSurface)
                )
            }
        }
    }
}

@Composable
fun NearbyFeedEmptyState(
    radiusMeters: Int,
    onlyWithPhotos: Boolean,
    onResetFilters: () -> Unit
) {
    DarkGlassCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("nearby_feed_empty_state"),
        cornerRadius = 24.dp,
        containerColor = DarkGlassCard,
        borderColor = HairlineGlassBorder,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 34.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(CobaltSoft)
                    .border(1.dp, CobaltBorder.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = CobaltGlow,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Nearby Content in Range",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = ObsidianInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (onlyWithPhotos) {
                    "No posts with photo previews found within ${LocationHelper.formatDistance(radiusMeters.toFloat())}."
                } else {
                    "There are currently no active neighbor posts within ${LocationHelper.formatDistance(radiusMeters.toFloat())} of your coordinates."
                },
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            PillPrimaryButton(
                text = "Expand Radius to 10 km",
                onClick = onResetFilters,
                modifier = Modifier.testTag("nearby_feed_expand_radius_button")
            )
        }
    }
}

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
