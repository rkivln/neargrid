package com.example.ui

import android.app.Application
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.Conversation
import com.example.data.model.GeohashMessage
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.NearGridRepository
import com.example.util.LocationHelper
import com.example.util.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class NavigationDestination(val label: String) {
    HOME("Home"),
    GRID("Living Grid"),
    COMMUNITY("Community"),
    INBOX("Inbox"),
    PROFILE("Profile")
}

data class NearGridUiState(
    val currentDestination: NavigationDestination = NavigationDestination.HOME,
    val selectedCell: GridCell? = null,
    val allCells: List<GridCell> = emptyList(),
    val discoveryRadiusMeters: Int = 1000, // 300m, 1000m, 3000m, 10000m
    val postFilterType: String = "ALL", // ALL, NEED, OFFER, MEETUP
    val selectedCategory: String = "ALL",
    val gpsModeEnabled: Boolean = true,
    val isLocating: Boolean = false,
    val currentLocation: UserLocation? = null,
    val currentGeohash: String = "",
    val isPostingGeohashMessage: Boolean = false,
    val geohashPostStatus: String? = null,
    val userPrivacyLevel: String = "EXACT_PROXIMITY",
    val showCreateDialog: Boolean = false,
    val createDialogInitialType: String = "NEED",
    val activeConversation: Conversation? = null,
    val isFilterExpanded: Boolean = false,
    val userNickname: String = "Local Resident",
    val userHandle: String = "@resident",
    val userBio: String = "Civic community member connected via Google Resident Pass.",
    val avatarUri: String? = null,
    val avatarPresetIndex: Int = 0,
    val neighborhoodPrivacyRadiusMeters: Int = 1000,
    val fuzzyCoordinateOffset: Boolean = false,
    val incognitoGridPresence: Boolean = false,
    val allowProximityMessaging: Boolean = true,
    val showUserProfileEditor: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedQrCell: GridCell? = null,
    val showQrScannerDialog: Boolean = false,
    val showGridQrScreen: Boolean = false
)

class NearGridViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository(application)
    private val localRepository: NearGridRepository

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    private val _uiState = MutableStateFlow(NearGridUiState())
    val uiState: StateFlow<NearGridUiState> = _uiState.asStateFlow()

    // 100% Real-time Firestore live streams
    private val _firestorePosts = MutableStateFlow<List<NeighborPost>>(emptyList())
    private val _firestoreIssues = MutableStateFlow<List<CommunityIssue>>(emptyList())
    private val _firestoreConversations = MutableStateFlow<List<Conversation>>(emptyList())

    private val _currentGeohash = MutableStateFlow("")
    val currentGeohash: StateFlow<String> = _currentGeohash.asStateFlow()

    // 100% Real-time Firestore geohash messages
    val geohashMessages: StateFlow<List<GeohashMessage>> = combine(
        _currentUser,
        _currentGeohash
    ) { user, hash ->
        user to hash
    }.flatMapLatest { (user, hash) ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            firestoreRepository.observeGeohashMessages(hash.ifBlank { null })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val authStateListener = FirebaseAuth.AuthStateListener { fbAuth ->
        val user = fbAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            syncUserSession(user)
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        localRepository = NearGridRepository(db)

        viewModelScope.launch {
            // Clear any lingering synthetic mock data
            localRepository.seedInitialDataIfEmpty()
        }

        auth.addAuthStateListener(authStateListener)
        val initialUser = auth.currentUser
        if (initialUser != null) {
            syncUserSession(initialUser)
        }

        // Immediately attempt fresh device GPS acquisition
        refreshGridData()
    }

    fun refreshDeviceLocation() {
        refreshGridData()
    }

    fun refreshGridData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, isLocating = true)
            val location = LocationHelper.getFreshDeviceLocation(getApplication())
            if (location != null) {
                _currentLocation.value = location
                val computedGeohash = LocationHelper.computeGeohash(location.latitude, location.longitude, 7)
                _currentGeohash.value = computedGeohash
                val cellCode = String.format(
                    Locale.US,
                    "#GPS-%.3f,%.3f",
                    location.latitude,
                    location.longitude
                )
                val dynamicCell = GridCell(
                    id = "geo_${location.localityName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}",
                    name = location.displayAreaName,
                    code = cellCode,
                    activeNeighborsCount = 1,
                    openNeedsCount = 0,
                    openOffersCount = 0,
                    activeIssuesCount = 0,
                    centerLat = location.latitude,
                    centerLng = location.longitude,
                    description = "Real-time dynamic neighborhood mesh based on device GPS."
                )
                _uiState.value = _uiState.value.copy(
                    currentLocation = location,
                    currentGeohash = computedGeohash,
                    selectedCell = dynamicCell,
                    isLocating = false,
                    isRefreshing = false,
                    gpsModeEnabled = true
                )
            } else {
                _uiState.value = _uiState.value.copy(isLocating = false, isRefreshing = false)
            }
        }
    }

    private fun syncUserSession(user: FirebaseUser) {
        viewModelScope.launch {
            firestoreRepository.observeUserProfile(user.uid).collect { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        userNickname = profile.displayName.ifBlank { user.displayName ?: "Local Resident" },
                        userHandle = profile.handle.ifBlank { "@${user.email?.substringBefore("@") ?: "resident"}" },
                        userBio = profile.bio,
                        avatarUri = profile.avatarUri ?: user.photoUrl?.toString(),
                        avatarPresetIndex = profile.avatarPresetIndex,
                        neighborhoodPrivacyRadiusMeters = profile.privacyRadiusMeters,
                        fuzzyCoordinateOffset = profile.fuzzyCoords,
                        incognitoGridPresence = profile.incognito,
                        allowProximityMessaging = profile.allowDMs
                    )
                } else {
                    val defaultName = user.displayName?.ifBlank { "Local Resident" } ?: "Local Resident"
                    val defaultHandle = "@${user.email?.substringBefore("@") ?: "resident"}"
                    val loc = _currentLocation.value
                    val newProfile = UserProfile(
                        userId = user.uid,
                        email = user.email ?: "",
                        displayName = defaultName,
                        handle = defaultHandle,
                        bio = "Civic resident connected via Google Resident Pass.",
                        cellId = _uiState.value.selectedCell?.id ?: "cell_local",
                        cellName = loc?.displayAreaName ?: "My Neighborhood",
                        avatarUri = user.photoUrl?.toString(),
                        privacyRadiusMeters = 1000,
                        fuzzyCoords = false,
                        incognito = false,
                        allowDMs = true
                    )
                    firestoreRepository.saveUserProfile(newProfile)
                }
            }
        }

        // Live real-time posts from Firestore
        viewModelScope.launch {
            firestoreRepository.observePosts().collect { posts ->
                _firestorePosts.value = posts
            }
        }

        // Live real-time community issues from Firestore
        viewModelScope.launch {
            firestoreRepository.observeIssues().collect { issuesList ->
                _firestoreIssues.value = issuesList
            }
        }

        // Live real-time conversations from Firestore
        viewModelScope.launch {
            firestoreRepository.observeConversations().collect { convs ->
                _firestoreConversations.value = convs
            }
        }
    }

    // Dynamic grid cells synthesized from current GPS fix & real active Firestore posts
    val cells: StateFlow<List<GridCell>> = combine(
        _currentLocation,
        _firestorePosts,
        _firestoreIssues
    ) { location, posts, issuesList ->
        val cellMap = mutableMapOf<String, GridCell>()

        // Current device GPS locality cell
        if (location != null) {
            val cellId = "geo_${location.localityName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}"
            val cellCode = String.format(Locale.US, "#GPS-%.3f,%.3f", location.latitude, location.longitude)
            cellMap[cellId] = GridCell(
                id = cellId,
                name = location.displayAreaName,
                code = cellCode,
                activeNeighborsCount = 1,
                openNeedsCount = posts.count { it.type == "NEED" && it.status == "ACTIVE" },
                openOffersCount = posts.count { it.type == "OFFER" && it.status == "ACTIVE" },
                activeIssuesCount = issuesList.count { it.status != "RESOLVED" },
                centerLat = location.latitude,
                centerLng = location.longitude,
                description = "Active physical mesh at your current device location."
            )
        }

        // Distinct cells from active posts
        posts.forEach { post ->
            val cId = post.authorCellId.ifBlank { "cell_${post.cellName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}" }
            val existing = cellMap[cId]
            if (existing != null) {
                cellMap[cId] = existing.copy(
                    openNeedsCount = posts.count { it.authorCellId == cId && it.type == "NEED" && it.status == "ACTIVE" },
                    openOffersCount = posts.count { it.authorCellId == cId && it.type == "OFFER" && it.status == "ACTIVE" }
                )
            } else {
                cellMap[cId] = GridCell(
                    id = cId,
                    name = post.cellName.ifBlank { "Local Area" },
                    code = String.format(Locale.US, "#GPS-%.2f,%.2f", post.lat, post.lng),
                    activeNeighborsCount = 1,
                    openNeedsCount = posts.count { it.authorCellId == cId && it.type == "NEED" && it.status == "ACTIVE" },
                    openOffersCount = posts.count { it.authorCellId == cId && it.type == "OFFER" && it.status == "ACTIVE" },
                    activeIssuesCount = 0,
                    centerLat = post.lat,
                    centerLng = post.lng,
                    description = "Neighbor community area."
                )
            }
        }

        cellMap.values.toList().ifEmpty {
            listOf(
                GridCell(
                    id = "local_grid",
                    name = location?.displayAreaName ?: "My Neighborhood",
                    code = "#GPS-LIVE",
                    activeNeighborsCount = 1,
                    openNeedsCount = 0,
                    openOffersCount = 0,
                    activeIssuesCount = 0,
                    centerLat = location?.latitude ?: 0.0,
                    centerLng = location?.longitude ?: 0.0,
                    description = "Local community coordination cell."
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-Time Live Posts with Dynamic Real GPS Distance Calculation
    val filteredPosts: StateFlow<List<NeighborPost>> = combine(
        _firestorePosts,
        _currentLocation,
        _uiState
    ) { posts, location, state ->
        posts.map { post ->
            val distanceM = if (location != null && post.lat != 0.0 && post.lng != 0.0) {
                LocationHelper.calculateDistanceMeters(
                    location.latitude,
                    location.longitude,
                    post.lat,
                    post.lng
                )
            } else null

            val distanceBand = if (distanceM != null) {
                LocationHelper.formatDistance(distanceM)
            } else {
                post.approxDistanceBand.ifBlank { "Proximity pending" }
            }

            post.copy(approxDistanceBand = distanceBand)
        }.filter { post ->
            val matchesType = when (state.postFilterType) {
                "ALL" -> true
                else -> post.type == state.postFilterType
            }
            val matchesCategory = when (state.selectedCategory) {
                "ALL" -> true
                else -> post.category.equals(state.selectedCategory, ignoreCase = true)
            }

            // Proximity radius filter
            val withinRadius = if (location != null && post.lat != 0.0 && post.lng != 0.0) {
                val dist = LocationHelper.calculateDistanceMeters(
                    location.latitude,
                    location.longitude,
                    post.lat,
                    post.lng
                )
                dist <= state.discoveryRadiusMeters
            } else {
                true
            }

            matchesType && matchesCategory && withinRadius
        }.sortedWith(
            compareByDescending<NeighborPost> { it.urgency == "URGENT" }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topIntentMatches: StateFlow<List<IntentMatchResult>> = combine(
        _firestorePosts,
        _uiState
    ) { posts, _ ->
        localRepository.findTopIntentMatches(posts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-time Firestore issues
    val issues: StateFlow<List<CommunityIssue>> = _firestoreIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<CommunityNotice>> = localRepository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-time Firestore conversations
    val conversations: StateFlow<List<Conversation>> = _firestoreConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            cells.collect { cellList ->
                if (cellList.isNotEmpty() && _uiState.value.selectedCell == null) {
                    _uiState.value = _uiState.value.copy(
                        selectedCell = cellList.first(),
                        allCells = cellList
                    )
                }
            }
        }
    }

    fun setDestination(dest: NavigationDestination) {
        _uiState.value = _uiState.value.copy(currentDestination = dest)
    }

    fun showCellQrDialog(cell: GridCell) {
        _uiState.value = _uiState.value.copy(selectedQrCell = cell)
    }

    fun dismissCellQrDialog() {
        _uiState.value = _uiState.value.copy(selectedQrCell = null)
    }

    fun openQrScannerDialog() {
        _uiState.value = _uiState.value.copy(showQrScannerDialog = true)
    }

    fun closeQrScannerDialog() {
        _uiState.value = _uiState.value.copy(showQrScannerDialog = false)
    }

    fun openGridQrScreen() {
        _uiState.value = _uiState.value.copy(showGridQrScreen = true)
    }

    fun closeGridQrScreen() {
        _uiState.value = _uiState.value.copy(showGridQrScreen = false)
    }

    fun joinGridCellByCode(codeOrUrl: String) {
        val cleanCode = when {
            codeOrUrl.contains("cell=") -> "#" + codeOrUrl.substringAfter("cell=").substringBefore("&")
            codeOrUrl.startsWith("#") -> codeOrUrl
            else -> "#$codeOrUrl"
        }

        val matchingCell = _uiState.value.allCells.find { it.code.equals(cleanCode, ignoreCase = true) }
            ?: GridCell(
                id = "scanned_${cleanCode.lowercase().replace("#", "")}",
                name = "Neighbor Grid $cleanCode",
                code = cleanCode,
                activeNeighborsCount = 18,
                openNeedsCount = 3,
                openOffersCount = 5,
                description = "Neighborhood spatial grid joined via QR Invitation code scan."
            )

        _uiState.value = _uiState.value.copy(
            selectedCell = matchingCell,
            showQrScannerDialog = false
        )
    }

    fun selectCell(cell: GridCell) {
        _uiState.value = _uiState.value.copy(selectedCell = cell)
    }

    fun setDiscoveryRadius(radiusMeters: Int) {
        _uiState.value = _uiState.value.copy(discoveryRadiusMeters = radiusMeters)
    }

    fun setPostFilter(type: String) {
        _uiState.value = _uiState.value.copy(postFilterType = type)
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleGpsMode() {
        val newGps = !_uiState.value.gpsModeEnabled
        _uiState.value = _uiState.value.copy(gpsModeEnabled = newGps)
        if (newGps) {
            refreshDeviceLocation()
        }
    }

    fun openCreateDialog(initialType: String = "NEED") {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            createDialogInitialType = initialType
        )
    }

    fun closeCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun openConversation(conversation: Conversation) {
        _uiState.value = _uiState.value.copy(activeConversation = conversation)
    }

    fun closeConversation() {
        _uiState.value = _uiState.value.copy(activeConversation = null)
    }

    fun getChatMessagesForActive(): StateFlow<List<ChatMessage>> {
        return _uiState.flatMapLatest { state ->
            val activeId = state.activeConversation?.id
            if (activeId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                firestoreRepository.observeMessages(activeId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun sendMessageToActive(text: String, isMeetingPoint: Boolean = false, meetingPoint: String? = null) {
        val active = _uiState.value.activeConversation ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            firestoreRepository.sendMessage(active.id, text.trim(), isMeetingPoint, meetingPoint)
        }
    }

    fun connectFromPost(post: NeighborPost) {
        viewModelScope.launch {
            val result = firestoreRepository.startConversationForPost(post)
            val convId = result.getOrNull() ?: localRepository.startConversationForPost(post)
            val conv = Conversation(
                id = convId,
                participantName = post.authorName,
                relatedPostTitle = post.title,
                relatedPostType = post.type,
                lastMessage = "Started connection regarding \"${post.title}\"",
                distanceBand = post.approxDistanceBand
            )
            _uiState.value = _uiState.value.copy(
                activeConversation = conv,
                currentDestination = NavigationDestination.INBOX
            )
        }
    }

    fun toggleIssueUpvote(issue: CommunityIssue) {
        viewModelScope.launch {
            firestoreRepository.toggleIssueUpvote(issue.id, issue.upvotesCount, issue.hasUserUpvoted)
        }
    }

    fun openUserProfileEditor() {
        _uiState.value = _uiState.value.copy(showUserProfileEditor = true)
    }

    fun closeUserProfileEditor() {
        _uiState.value = _uiState.value.copy(showUserProfileEditor = false)
    }

    fun updateUserProfile(
        nickname: String,
        handle: String,
        bio: String,
        avatarUri: String?,
        avatarPresetIndex: Int,
        privacyRadiusMeters: Int,
        fuzzyCoords: Boolean,
        incognito: Boolean,
        allowDMs: Boolean
    ) {
        val sanitizedHandle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}"
        _uiState.value = _uiState.value.copy(
            userNickname = nickname.trim().ifBlank { "Neighbor" },
            userHandle = sanitizedHandle,
            userBio = bio.trim(),
            avatarUri = avatarUri,
            avatarPresetIndex = avatarPresetIndex,
            neighborhoodPrivacyRadiusMeters = privacyRadiusMeters,
            discoveryRadiusMeters = privacyRadiusMeters,
            fuzzyCoordinateOffset = fuzzyCoords,
            incognitoGridPresence = incognito,
            allowProximityMessaging = allowDMs,
            showUserProfileEditor = false
        )

        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                val loc = _currentLocation.value
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = nickname.trim().ifBlank { "Neighbor" },
                    handle = sanitizedHandle,
                    bio = bio.trim(),
                    cellId = _uiState.value.selectedCell?.id ?: "cell_local",
                    cellName = loc?.displayAreaName ?: _uiState.value.selectedCell?.name ?: "Local Area",
                    avatarPresetIndex = avatarPresetIndex,
                    avatarUri = avatarUri,
                    privacyRadiusMeters = privacyRadiusMeters,
                    fuzzyCoords = fuzzyCoords,
                    incognito = incognito,
                    allowDMs = allowDMs
                )
                firestoreRepository.saveUserProfile(profile)
            }
        }
    }

    fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?) {
        viewModelScope.launch {
            firestoreRepository.updateIssueStatus(issueId, newStatus, volunteer)
        }
    }

    fun createNeed(
        title: String,
        description: String,
        category: String,
        urgency: String,
        expiresInHours: Int
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            val loc = _currentLocation.value
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = loc?.displayAreaName ?: cell?.name ?: "Local Area"
            val lat = loc?.latitude ?: 0.0
            val lng = loc?.longitude ?: 0.0

            val post = NeighborPost(
                id = UUID.randomUUID().toString(),
                userId = user?.uid ?: "",
                type = "NEED",
                title = title,
                description = description,
                category = category,
                authorName = author,
                authorInitials = author.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("").ifEmpty { "ME" },
                authorCellId = cell?.id ?: "geo_${realLocName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}",
                cellName = realLocName,
                lat = lat,
                lng = lng,
                urgency = urgency,
                expiresInHours = expiresInHours,
                approxDistanceBand = "< 50 m",
                status = "ACTIVE"
            )
            firestoreRepository.createPost(post)
            closeCreateDialog()
        }
    }

    fun createOffer(
        title: String,
        description: String,
        category: String,
        expiresInHours: Int
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            val loc = _currentLocation.value
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = loc?.displayAreaName ?: cell?.name ?: "Local Area"
            val lat = loc?.latitude ?: 0.0
            val lng = loc?.longitude ?: 0.0

            val post = NeighborPost(
                id = UUID.randomUUID().toString(),
                userId = user?.uid ?: "",
                type = "OFFER",
                title = title,
                description = description,
                category = category,
                authorName = author,
                authorInitials = author.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("").ifEmpty { "ME" },
                authorCellId = cell?.id ?: "geo_${realLocName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}",
                cellName = realLocName,
                lat = lat,
                lng = lng,
                urgency = "NORMAL",
                expiresInHours = expiresInHours,
                approxDistanceBand = "< 50 m",
                status = "ACTIVE"
            )
            firestoreRepository.createPost(post)
            closeCreateDialog()
        }
    }

    fun createIssue(
        title: String,
        description: String,
        location: String,
        category: String
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            val loc = _currentLocation.value
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = if (location.isNotBlank()) location else loc?.displayAreaName ?: cell?.name ?: "Local Area"

            val issue = CommunityIssue(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                locationSnippet = realLocName,
                cellId = cell?.id ?: "geo_${realLocName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}",
                category = category,
                reporterName = author,
                reporterId = user?.uid ?: ""
            )
            firestoreRepository.createIssue(issue)
            closeCreateDialog()
            _uiState.value = _uiState.value.copy(currentDestination = NavigationDestination.COMMUNITY)
        }
    }

    fun createMeetup(
        title: String,
        description: String,
        category: String,
        timeframeHours: Int
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            val loc = _currentLocation.value
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = loc?.displayAreaName ?: cell?.name ?: "Local Area"
            val lat = loc?.latitude ?: 0.0
            val lng = loc?.longitude ?: 0.0

            val post = NeighborPost(
                id = UUID.randomUUID().toString(),
                userId = user?.uid ?: "",
                type = "MEETUP",
                title = title,
                description = description,
                category = category,
                authorName = author,
                authorInitials = author.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("").ifEmpty { "ME" },
                authorCellId = cell?.id ?: "geo_${realLocName.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "_")}",
                cellName = realLocName,
                lat = lat,
                lng = lng,
                urgency = "NORMAL",
                expiresInHours = timeframeHours,
                approxDistanceBand = "< 50 m",
                status = "ACTIVE"
            )
            firestoreRepository.createPost(post)
            closeCreateDialog()
        }
    }

    fun postGeohashMessage(
        text: String,
        onComplete: ((Result<String>) -> Unit)? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPostingGeohashMessage = true, geohashPostStatus = null)
            val loc = _currentLocation.value
            val lat = loc?.latitude ?: _uiState.value.selectedCell?.centerLat ?: 37.7749
            val lon = loc?.longitude ?: _uiState.value.selectedCell?.centerLng ?: -122.4194
            val geohash = _currentGeohash.value.ifBlank {
                LocationHelper.computeGeohash(lat, lon, 7)
            }
            val user = auth.currentUser
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val locality = loc?.displayAreaName ?: _uiState.value.selectedCell?.name ?: "Local Area"

            val result = firestoreRepository.postGeohashMessage(
                text = trimmed,
                latitude = lat,
                longitude = lon,
                geohash = geohash,
                authorName = author,
                localityName = locality
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isPostingGeohashMessage = false,
                    geohashPostStatus = "Broadcast posted to geohash #$geohash"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isPostingGeohashMessage = false,
                    geohashPostStatus = "Error: ${result.exceptionOrNull()?.message}"
                )
            }
            onComplete?.invoke(result)
        }
    }

    fun clearGeohashPostStatus() {
        _uiState.value = _uiState.value.copy(geohashPostStatus = null)
    }

    fun resetToFreshGrid() {
        viewModelScope.launch {
            localRepository.resetToFreshState()
            refreshDeviceLocation()
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                // Ignore credential manager clear failures
            }
            auth.signOut()
            _currentUser.value = null
        }
    }
}
