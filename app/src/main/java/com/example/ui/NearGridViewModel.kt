package com.example.ui

import android.app.Application
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
import com.example.data.repository.NearGridRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val repository: NearGridRepository

    private val _uiState = MutableStateFlow(NearGridUiState())
    val uiState: StateFlow<NearGridUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NearGridRepository(db)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val cells: StateFlow<List<GridCell>> = combine(
        repository.allCells,
        repository.allPosts,
        repository.allIssues
    ) { baseCells, posts, issuesList ->
        baseCells.map { cell ->
            val cellPosts = posts.filter { it.authorCellId == cell.id }
            val cellNeeds = cellPosts.count { it.type == "NEED" && it.status == "ACTIVE" }
            val cellOffers = cellPosts.count { it.type == "OFFER" && it.status == "ACTIVE" }
            val cellIssues = issuesList.count { it.cellId == cell.id && it.status != "RESOLVED" }
            val authorCount = cellPosts.map { it.authorName }.filter { it.isNotBlank() }.toSet().size
            val activeNeighbors = authorCount.coerceAtLeast(1)
            cell.copy(
                openNeedsCount = cellNeeds,
                openOffersCount = cellOffers,
                activeIssuesCount = cellIssues,
                activeNeighborsCount = activeNeighbors
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawPosts: StateFlow<List<NeighborPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPosts: StateFlow<List<NeighborPost>> = combine(
        rawPosts,
        _uiState
    ) { posts, state ->
        posts.filter { post ->
            // Filter by type
            val matchesType = when (state.postFilterType) {
                "ALL" -> true
                else -> post.type == state.postFilterType
            }
            // Filter by category
            val matchesCategory = when (state.selectedCategory) {
                "ALL" -> true
                else -> post.category.equals(state.selectedCategory, ignoreCase = true)
            }
            // Radius and cell filtering
            val matchesRadius = when (state.discoveryRadiusMeters) {
                300 -> post.approxDistanceBand.contains("190") || post.approxDistanceBand.contains("280") || post.approxDistanceBand.contains("210") || post.approxDistanceBand.contains("250")
                800 -> !post.approxDistanceBand.contains("1.2 km")
                else -> true // 2km shows all
            }

            // Cell filtering if a cell is specifically locked
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
        repository.findTopIntentMatches(posts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val issues: StateFlow<List<CommunityIssue>> = repository.allIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<CommunityNotice>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<Conversation>> = repository.allConversations
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
        return repository.getChatMessages(activeId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun sendMessageToActive(text: String, isMeetingPoint: Boolean = false, meetingPoint: String? = null) {
        val active = _uiState.value.activeConversation ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(active.id, text.trim(), isMeetingPoint, meetingPoint)
        }
    }

    fun connectFromPost(post: NeighborPost) {
        viewModelScope.launch {
            val convId = repository.startConversationForPost(post)
            val convs = repository.allConversations
            // Set active conversation and switch to inbox
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
            repository.toggleUpvote(issue.id, issue.upvotesCount, issue.hasUserUpvoted)
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
    }

    fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?) {
        viewModelScope.launch {
            repository.updateIssueStatus(issueId, newStatus, volunteer)
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
            repository.createPost(
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
            repository.createPost(
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
            repository.createIssue(
                title = title,
                description = description,
                locationSnippet = location,
                cellId = cell?.id ?: "cell_oakwood_center",
                category = category,
                reporterName = author
            )
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
            repository.createPost(
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
            closeCreateDialog()
        }
    }

    fun resetToFreshGrid() {
        viewModelScope.launch {
            repository.resetToFreshState()
        }
    }
}
