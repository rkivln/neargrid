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
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.NearGridRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val discoveryRadiusMeters: Int = 800, // 300m, 800m, 2000m
    val postFilterType: String = "ALL", // ALL, NEED, OFFER, MEETUP
    val selectedCategory: String = "ALL",
    val gpsModeEnabled: Boolean = false,
    val userPrivacyLevel: String = "APPROXIMATE_BAND", // "APPROXIMATE_BAND", "CELL_ONLY"
    val showCreateDialog: Boolean = false,
    val createDialogInitialType: String = "NEED",
    val activeConversation: Conversation? = null,
    val isFilterExpanded: Boolean = false,
    val userNickname: String = "Alex Rivera",
    val userHandle: String = "@alex_oakwood",
    val userBio: String = "Oakwood resident & tool sharing volunteer. Active in Cell #H3-8826.",
    val avatarUri: String? = null,
    val avatarPresetIndex: Int = 0,
    val neighborhoodPrivacyRadiusMeters: Int = 500, // 250m, 500m, 1000m, 2500m
    val fuzzyCoordinateOffset: Boolean = true,
    val incognitoGridPresence: Boolean = false,
    val allowProximityMessaging: Boolean = true,
    val showUserProfileEditor: Boolean = false
)

class NearGridViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository(application)
    private val localRepository: NearGridRepository

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow(NearGridUiState())
    val uiState: StateFlow<NearGridUiState> = _uiState.asStateFlow()

    private val _firestorePosts = MutableStateFlow<List<NeighborPost>>(emptyList())
    private val _firestoreIssues = MutableStateFlow<List<CommunityIssue>>(emptyList())
    private val _firestoreConversations = MutableStateFlow<List<Conversation>>(emptyList())

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
            localRepository.seedInitialDataIfEmpty()
        }

        auth.addAuthStateListener(authStateListener)
        val initialUser = auth.currentUser
        if (initialUser != null) {
            syncUserSession(initialUser)
        }
    }

    private fun syncUserSession(user: FirebaseUser) {
        viewModelScope.launch {
            // Observe or create profile in Firestore
            firestoreRepository.observeUserProfile(user.uid).collect { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        userNickname = profile.displayName.ifBlank { user.displayName ?: "Local Resident" },
                        userHandle = profile.handle.ifBlank { "@${user.email?.substringBefore("@") ?: "neighbor"}" },
                        userBio = profile.bio,
                        avatarUri = profile.avatarUri ?: user.photoUrl?.toString(),
                        avatarPresetIndex = profile.avatarPresetIndex,
                        neighborhoodPrivacyRadiusMeters = profile.privacyRadiusMeters,
                        fuzzyCoordinateOffset = profile.fuzzyCoords,
                        incognitoGridPresence = profile.incognito,
                        allowProximityMessaging = profile.allowDMs
                    )
                } else {
                    // Create baseline profile for Google user
                    val defaultName = user.displayName?.ifBlank { "Local Resident" } ?: "Local Resident"
                    val defaultHandle = "@${user.email?.substringBefore("@") ?: "resident"}"
                    val newProfile = UserProfile(
                        userId = user.uid,
                        email = user.email ?: "",
                        displayName = defaultName,
                        handle = defaultHandle,
                        bio = "Oakwood neighborhood resident. Connected via Google Resident Pass.",
                        cellId = _uiState.value.selectedCell?.id ?: "cell_oakwood_center",
                        cellName = _uiState.value.selectedCell?.name ?: "Oakwood Grove",
                        avatarUri = user.photoUrl?.toString(),
                        privacyRadiusMeters = 500,
                        fuzzyCoords = true,
                        incognito = false,
                        allowDMs = true
                    )
                    firestoreRepository.saveUserProfile(newProfile)
                }
            }
        }

        // Observe real-time posts from Firestore
        viewModelScope.launch {
            firestoreRepository.observePosts().collect { posts ->
                _firestorePosts.value = posts
            }
        }

        // Observe real-time issues from Firestore
        viewModelScope.launch {
            firestoreRepository.observeIssues().collect { issuesList ->
                _firestoreIssues.value = issuesList
            }
        }

        // Observe real-time conversations from Firestore
        viewModelScope.launch {
            firestoreRepository.observeConversations().collect { convs ->
                _firestoreConversations.value = convs
            }
        }
    }

    val cells: StateFlow<List<GridCell>> = combine(
        localRepository.allCells,
        _firestorePosts,
        localRepository.allPosts,
        _firestoreIssues,
        localRepository.allIssues
    ) { baseCells, firePosts, locPosts, fireIssues, locIssues ->
        val combinedPosts = if (firePosts.isNotEmpty()) firePosts else locPosts
        val combinedIssues = if (fireIssues.isNotEmpty()) fireIssues else locIssues

        baseCells.map { cell ->
            val cellPosts = combinedPosts.filter { it.authorCellId == cell.id }
            val cellNeeds = cellPosts.count { it.type == "NEED" && it.status == "ACTIVE" }
            val cellOffers = cellPosts.count { it.type == "OFFER" && it.status == "ACTIVE" }
            val cellIssuesCount = combinedIssues.count { it.cellId == cell.id && it.status != "RESOLVED" }
            val authorCount = cellPosts.map { it.authorName }.filter { it.isNotBlank() }.toSet().size
            val activeNeighbors = authorCount.coerceAtLeast(1)
            cell.copy(
                openNeedsCount = cellNeeds,
                openOffersCount = cellOffers,
                activeIssuesCount = cellIssuesCount,
                activeNeighborsCount = activeNeighbors
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawPosts: StateFlow<List<NeighborPost>> = combine(
        _firestorePosts,
        localRepository.allPosts
    ) { firestoreList, localList ->
        if (firestoreList.isNotEmpty()) firestoreList else localList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPosts: StateFlow<List<NeighborPost>> = combine(
        rawPosts,
        _uiState
    ) { posts, state ->
        posts.filter { post ->
            val matchesType = when (state.postFilterType) {
                "ALL" -> true
                else -> post.type == state.postFilterType
            }
            val matchesCategory = when (state.selectedCategory) {
                "ALL" -> true
                else -> post.category.equals(state.selectedCategory, ignoreCase = true)
            }
            val matchesRadius = when (state.discoveryRadiusMeters) {
                300 -> post.approxDistanceBand.contains("150") || post.approxDistanceBand.contains("190") || post.approxDistanceBand.contains("280") || post.approxDistanceBand.contains("210") || post.approxDistanceBand.contains("250")
                800 -> !post.approxDistanceBand.contains("1.2 km")
                else -> true
            }
            val matchesCell = state.selectedCell?.let { cell ->
                if (state.discoveryRadiusMeters <= 300) {
                    post.authorCellId == cell.id
                } else {
                    true
                }
            } ?: true

            matchesType && matchesCategory && matchesRadius && matchesCell
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topIntentMatches: StateFlow<List<IntentMatchResult>> = combine(
        rawPosts,
        _uiState
    ) { posts, _ ->
        localRepository.findTopIntentMatches(posts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val issues: StateFlow<List<CommunityIssue>> = combine(
        _firestoreIssues,
        localRepository.allIssues
    ) { firestoreList, localList ->
        if (firestoreList.isNotEmpty()) firestoreList else localList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<CommunityNotice>> = localRepository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<Conversation>> = combine(
        _firestoreConversations,
        localRepository.allConversations
    ) { firestoreList, localList ->
        if (firestoreList.isNotEmpty()) firestoreList else localList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        val activeId = _uiState.value.activeConversation?.id ?: ""
        return localRepository.getChatMessages(activeId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun sendMessageToActive(text: String, isMeetingPoint: Boolean = false, meetingPoint: String? = null) {
        val active = _uiState.value.activeConversation ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            localRepository.sendMessage(active.id, text.trim(), isMeetingPoint, meetingPoint)
            if (auth.currentUser != null) {
                firestoreRepository.sendMessage(active.id, text.trim(), isMeetingPoint, meetingPoint)
            }
        }
    }

    fun connectFromPost(post: NeighborPost) {
        viewModelScope.launch {
            val convId = localRepository.startConversationForPost(post)
            val conv = Conversation(
                id = convId,
                participantName = post.authorName,
                relatedPostTitle = post.title,
                relatedPostType = post.type,
                lastMessage = "Started connection",
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
            localRepository.toggleUpvote(issue.id, issue.upvotesCount, issue.hasUserUpvoted)
            if (auth.currentUser != null) {
                firestoreRepository.toggleIssueUpvote(issue.id, issue.upvotesCount, issue.hasUserUpvoted)
            }
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
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = nickname.trim().ifBlank { "Neighbor" },
                    handle = sanitizedHandle,
                    bio = bio.trim(),
                    cellId = _uiState.value.selectedCell?.id ?: "cell_oakwood_center",
                    cellName = _uiState.value.selectedCell?.name ?: "Oakwood Grove",
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
            localRepository.updateIssueStatus(issueId, newStatus, volunteer)
            if (auth.currentUser != null) {
                firestoreRepository.updateIssueStatus(issueId, newStatus, volunteer)
            }
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
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { "You (Local Resident)" }
            localRepository.createPost(
                type = "NEED",
                title = title,
                description = description,
                category = category,
                authorName = author,
                cellName = cell?.name ?: "Oakwood Grove",
                cellId = cell?.id ?: "cell_oakwood_center",
                urgency = urgency,
                expiresInHours = expiresInHours,
                distanceBand = "within 150 m"
            )

            if (auth.currentUser != null) {
                val post = NeighborPost(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "",
                    type = "NEED",
                    title = title,
                    description = description,
                    category = category,
                    authorName = author,
                    authorInitials = author.take(2).uppercase(),
                    authorCellId = cell?.id ?: "cell_oakwood_center",
                    cellName = cell?.name ?: "Oakwood Grove",
                    urgency = urgency,
                    expiresInHours = expiresInHours,
                    approxDistanceBand = "within 150 m",
                    status = "ACTIVE"
                )
                firestoreRepository.createPost(post)
            }
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
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { "You (Local Resident)" }
            localRepository.createPost(
                type = "OFFER",
                title = title,
                description = description,
                category = category,
                authorName = author,
                cellName = cell?.name ?: "Oakwood Grove",
                cellId = cell?.id ?: "cell_oakwood_center",
                urgency = "NORMAL",
                expiresInHours = expiresInHours,
                distanceBand = "within 150 m"
            )

            if (auth.currentUser != null) {
                val post = NeighborPost(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "",
                    type = "OFFER",
                    title = title,
                    description = description,
                    category = category,
                    authorName = author,
                    authorInitials = author.take(2).uppercase(),
                    authorCellId = cell?.id ?: "cell_oakwood_center",
                    cellName = cell?.name ?: "Oakwood Grove",
                    urgency = "NORMAL",
                    expiresInHours = expiresInHours,
                    approxDistanceBand = "within 150 m",
                    status = "ACTIVE"
                )
                firestoreRepository.createPost(post)
            }
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
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { "Resident" }
            localRepository.createIssue(
                title = title,
                description = description,
                locationSnippet = location,
                cellId = cell?.id ?: "cell_oakwood_center",
                category = category,
                reporterName = author
            )

            if (auth.currentUser != null) {
                val issue = CommunityIssue(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    locationSnippet = location,
                    cellId = cell?.id ?: "cell_oakwood_center",
                    category = category,
                    reporterName = author,
                    reporterId = auth.currentUser?.uid ?: ""
                )
                firestoreRepository.createIssue(issue)
            }
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
            val cell = _uiState.value.selectedCell
            val author = _uiState.value.userNickname.ifBlank { "You (Local Resident)" }
            localRepository.createPost(
                type = "MEETUP",
                title = title,
                description = description,
                category = category,
                authorName = author,
                cellName = cell?.name ?: "Oakwood Grove",
                cellId = cell?.id ?: "cell_oakwood_center",
                urgency = "NORMAL",
                expiresInHours = timeframeHours,
                distanceBand = "within 200 m"
            )

            if (auth.currentUser != null) {
                val post = NeighborPost(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "",
                    type = "MEETUP",
                    title = title,
                    description = description,
                    category = category,
                    authorName = author,
                    authorInitials = author.take(2).uppercase(),
                    authorCellId = cell?.id ?: "cell_oakwood_center",
                    cellName = cell?.name ?: "Oakwood Grove",
                    urgency = "NORMAL",
                    expiresInHours = timeframeHours,
                    approxDistanceBand = "within 200 m",
                    status = "ACTIVE"
                )
                firestoreRepository.createPost(post)
            }
            closeCreateDialog()
        }
    }

    fun resetToFreshGrid() {
        viewModelScope.launch {
            localRepository.resetToFreshState()
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                // ignore
            }
            auth.signOut()
            _currentUser.value = null
            _firestorePosts.value = emptyList()
            _firestoreIssues.value = emptyList()
            _firestoreConversations.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
