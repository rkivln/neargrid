package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.auth.AuthSignInOutcome
import com.example.data.auth.AuthState
import com.example.data.auth.AuthenticatedUser
import com.example.data.auth.GoogleAuthRepository
import com.example.data.auth.SessionValidationResult
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.Conversation
import com.example.data.model.GeohashMessage
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NearbyNeighbor
import com.example.data.model.NeighborPost
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseBootstrap
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.NearGridRepository
import com.example.data.update.AppUpdateInfo
import com.example.util.CrashTelemetryManager
import com.example.util.LocationHelper
import com.example.util.QrCodePayloadHelper
import com.example.util.StorageCacheManager
import com.example.util.StorageUsage
import com.example.util.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

enum class NavigationDestination(val label: String) {
    HOME("Home"),
    MAP("Map"),
    RADAR("Mesh Radar"),
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
    val locationPrecision: String = "EXACT", // "EXACT", "APPROXIMATE", "NEIGHBORHOOD"
    val hideOnlineStatus: Boolean = false,
    val hideExactDistance: Boolean = false,
    val blockedUserIds: List<String> = emptyList(),
    val showUserProfileEditor: Boolean = false,
    val showAppSettingsScreen: Boolean = false,
    val appSettings: AppSettings = AppSettings(),
    val storageUsage: StorageUsage = StorageUsage(),
    val supportTicketStatus: String? = null,
    val isRefreshing: Boolean = false,
    val selectedQrCell: GridCell? = null,
    val showQrScannerDialog: Boolean = false,
    val showGridQrScreen: Boolean = false
)

sealed interface FeatureStartupState {
    data object Initializing : FeatureStartupState
    data object Ready : FeatureStartupState
    data class Error(val message: String) : FeatureStartupState
}

class NearGridViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineExceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            android.util.Log.e("NearGridViewModel", "Caught unhandled coroutine exception in ViewModel", throwable)
        }
    }

    private val auth: FirebaseAuth? by lazy {
        FirebaseBootstrap.getAuth(application)
    }

    @Volatile
    private var _firestoreRepository: FirestoreRepository? = null

    @Volatile
    private var _localRepository: NearGridRepository? = null

    @Volatile
    internal var startupDependencyResolver: (suspend (Application) -> Pair<FirestoreRepository, NearGridRepository>)? = null

    private fun requireFirestoreRepository(): FirestoreRepository =
        _firestoreRepository ?: FirestoreRepository(getApplication()).also { _firestoreRepository = it }

    private fun requireLocalRepository(): NearGridRepository =
        _localRepository ?: NearGridRepository(AppDatabase.getDatabase(getApplication())).also { _localRepository = it }

    private val _startupState = MutableStateFlow<FeatureStartupState>(FeatureStartupState.Initializing)
    val startupState: StateFlow<FeatureStartupState> = _startupState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private var startupJob: Job? = null
    private var sessionJob: Job? = null
    private var activeSessionUid: String? = null
    private var authListenerAttached: Boolean = false

    private fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
        return generateSequence(this) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    private val _uiState = MutableStateFlow(NearGridUiState())
    val uiState: StateFlow<NearGridUiState> = _uiState.asStateFlow()

    // 100% Real-time Firestore live streams
    private val _firestorePosts = MutableStateFlow<List<NeighborPost>>(emptyList())
    private val _firestoreIssues = MutableStateFlow<List<CommunityIssue>>(emptyList())
    private val _firestoreConversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _firestoreUsers = MutableStateFlow<List<UserProfile>>(emptyList())

    // Real-time nearby users within selected discovery radius
    val nearbyNeighbors: StateFlow<List<NearbyNeighbor>> = combine(
        _currentUser,
        _currentLocation,
        _uiState,
        _firestoreUsers,
        _firestorePosts
    ) { user, loc, state, users, posts ->
        if (loc == null || (loc.latitude == 0.0 && loc.longitude == 0.0)) {
            emptyList()
        } else {
            val myUid = user?.uid
            val radius = state.discoveryRadiusMeters
            val blocked = state.blockedUserIds.toSet()
            val activePostsByUser = posts.filter { it.status == "ACTIVE" }
                .groupBy { it.userId }
            users.filter { u ->
                u.userId != myUid && !u.incognito && !blocked.contains(u.userId) && (u.latitude != 0.0 && u.longitude != 0.0)
            }.mapNotNull { u ->
                // Apply neighbor's spatial precision settings (APPROXIMATE or NEIGHBORHOOD)
                val adjustedUser = when (u.locationPrecision) {
                    "APPROXIMATE" -> {
                        val offsetLat = ((u.userId.hashCode() % 100) / 100.0) * 0.0018 - 0.0009
                        val offsetLng = (((u.userId.hashCode() / 100) % 100) / 100.0) * 0.0018 - 0.0009
                        u.copy(latitude = u.latitude + offsetLat, longitude = u.longitude + offsetLng)
                    }
                    "NEIGHBORHOOD" -> {
                        val offsetLat = ((u.userId.hashCode() % 50) / 50.0) * 0.003 - 0.0015
                        val offsetLng = (((u.userId.hashCode() / 50) % 50) / 50.0) * 0.003 - 0.0015
                        u.copy(latitude = u.latitude + offsetLat, longitude = u.longitude + offsetLng)
                    }
                    else -> u
                }
                val dist = LocationHelper.calculateDistanceMeters(loc.latitude, loc.longitude, adjustedUser.latitude, adjustedUser.longitude)
                if (dist <= radius) {
                    val latestPost = activePostsByUser[u.userId]?.maxByOrNull { it.createdAt }
                    NearbyNeighbor(
                        user = adjustedUser,
                        distanceMeters = dist,
                        latestPost = latestPost
                    )
                } else {
                    null
                }
            }.sortedBy { it.distanceMeters }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentGeohash = MutableStateFlow("")
    val currentGeohash: StateFlow<String> = _currentGeohash.asStateFlow()

    // 100% Real-time Firestore geohash messages with strict coordinate distance filtering
    val geohashMessages: StateFlow<List<GeohashMessage>> = combine(
        _startupState,
        _currentUser,
        _currentGeohash,
        _currentLocation,
        _uiState
    ) { startup, user, hash, location, state ->
        Triple(startup is FeatureStartupState.Ready && user != null, hash, location to state.discoveryRadiusMeters)
    }.flatMapLatest { (canObserve, hash, locAndRadius) ->
        val (location, radius) = locAndRadius
        val fsRepo = _firestoreRepository
        if (!canObserve || fsRepo == null || hash.isBlank() || location == null) {
            flowOf(emptyList())
        } else {
            val cleanHash = hash.removePrefix("#")
            fsRepo.observeGeohashMessages(cleanHash).map { list ->
                val userLat = location.latitude
                val userLng = location.longitude
                list.filter { msg ->
                    if (msg.latitude != 0.0 && msg.longitude != 0.0 && userLat != 0.0 && userLng != 0.0) {
                        val dist = LocationHelper.calculateDistanceMeters(userLat, userLng, msg.latitude, msg.longitude)
                        dist <= radius
                    } else {
                        val msgPrefix = msg.geohash.removePrefix("#").take(4)
                        val userPrefix = cleanHash.take(4)
                        msgPrefix.isNotBlank() && msgPrefix.equals(userPrefix, ignoreCase = true)
                    }
                }
            }
        }
    }.catch { error ->
        if (error is CancellationException) throw error
        val fsError = error.unwrapFirestoreException()
        android.util.Log.w("NearGridViewModel", "Geohash messages stream error (${fsError?.code}): ${error.message}", error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-time Threaded Post Comments with Offline Room Persistence
    private val _activePostForComments = MutableStateFlow<NeighborPost?>(null)
    val activePostForComments: StateFlow<NeighborPost?> = _activePostForComments.asStateFlow()

    val activePostComments: StateFlow<List<PostComment>> = combine(
        _startupState,
        _activePostForComments
    ) { startup, post ->
        if (startup is FeatureStartupState.Ready) post else null
    }.flatMapLatest { post: NeighborPost? ->
        val fsRepo = _firestoreRepository
        val localRepo = _localRepository
        if (post == null || fsRepo == null || localRepo == null) {
            flowOf(emptyList<PostComment>())
        } else {
            combine(
                fsRepo.observePostComments(post.id)
                    .catch { error ->
                        if (error is CancellationException) throw error
                        val fsError = error.unwrapFirestoreException()
                        android.util.Log.w("NearGridViewModel", "Remote comments error (${fsError?.code}): ${error.message}")
                        emit(emptyList())
                    },
                localRepo.getPostComments(post.id)
            ) { remoteComments: List<PostComment>, localComments: List<PostComment> ->
                if (remoteComments.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                        localRepo.insertPostComments(remoteComments)
                    }
                }
                val map = (localComments + remoteComments).associateBy { it.id }
                map.values.toList().sortedBy { it.timestamp }
            }
        }
    }.catch { error ->
        if (error is CancellationException) throw error
        android.util.Log.w("NearGridViewModel", "Active comments flow error: ${error.message}", error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation chat messages as a stable StateFlow
    val activeChatMessages: StateFlow<List<ChatMessage>> = combine(
        _startupState,
        _uiState
    ) { startup, state ->
        if (startup is FeatureStartupState.Ready) state.activeConversation?.id else null
    }.flatMapLatest { activeId ->
        val fsRepo = _firestoreRepository
        if (activeId.isNullOrBlank() || fsRepo == null) {
            flowOf(emptyList())
        } else {
            fsRepo.observeMessages(activeId)
        }
    }.catch { error ->
        if (error is CancellationException) throw error
        val fsError = error.unwrapFirestoreException()
        android.util.Log.w("NearGridViewModel", "Active chat stream error (${fsError?.code}): ${error.message}", error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Update Status & Notification State
    private val _updateInfo = MutableStateFlow(AppUpdateInfo(currentVersion = BuildConfig.VERSION_NAME))
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _isUpdateBannerDismissed = MutableStateFlow(false)
    val isUpdateBannerDismissed: StateFlow<Boolean> = _isUpdateBannerDismissed.asStateFlow()

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                com.example.data.update.AppUpdateManager.instance.resetBannerDismissed()
                _isUpdateBannerDismissed.value = false
            }
            _updateInfo.value = _updateInfo.value.copy(isChecking = true, errorMessage = null)
            val result = com.example.data.update.AppUpdateService.checkUpdate(BuildConfig.VERSION_NAME)
            _updateInfo.value = result
            _isUpdateBannerDismissed.value = com.example.data.update.AppUpdateManager.instance.isBannerDismissed.value
        }
    }

    fun dismissUpdateBanner() {
        val latestVer = _updateInfo.value.latestVersion
        com.example.data.update.AppUpdateManager.instance.dismissBanner(latestVer)
        _isUpdateBannerDismissed.value = true
    }

    fun openPostComments(post: NeighborPost) {
        _activePostForComments.value = post
    }

    fun closePostComments() {
        _activePostForComments.value = null
    }

    fun sendPostComment(text: String, parentId: String? = null) {
        val post = _activePostForComments.value ?: return
        if (text.isBlank()) return
        val user = auth?.currentUser
        val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
        val initials = author.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "N" }
        val commentId = UUID.randomUUID().toString()
        val localComment = PostComment(
            id = commentId,
            postId = post.id,
            authorId = user?.uid ?: "local_user",
            authorName = author,
            authorInitials = initials,
            text = text.trim(),
            parentId = parentId,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(coroutineExceptionHandler) {
            runCatching { requireLocalRepository().insertPostComment(localComment) }
            try {
                if (user != null) {
                    requireFirestoreRepository().addPostComment(
                        postId = post.id,
                        text = text.trim(),
                        authorName = author,
                        parentId = parentId
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NearGridViewModel", "Failed syncing post comment to Firestore", e)
            }
        }
    }

    private inline fun launchSafely(
        contextLabel: String,
        crossinline block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewModelScope.launch(coroutineExceptionHandler) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                android.util.Log.e("NearGridViewModel", "$contextLabel failed: ${t.message}", t)
            }
        }
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { fbAuth ->
        handleFirebaseUserChanged(fbAuth.currentUser)
    }

    init {
        startInitialization()
    }

    fun retryStartup() {
        startInitialization()
    }

    private fun startInitialization() {
        startupJob?.cancel()
        _startupState.value = FeatureStartupState.Initializing
        startupJob = viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val app = getApplication<Application>()
                val (fsRepo, localRepo) = withContext(Dispatchers.IO) {
                    val override = startupDependencyResolver
                    if (override != null) {
                        override(app)
                    } else {
                        val firestore = FirebaseBootstrap.getFirestore(app)
                            ?: throw IllegalStateException(
                                "Cloud database could not be initialized. Please retry or sign in again."
                            )
                        val db = AppDatabase.getDatabase(app)
                        val local = NearGridRepository(db)
                        runCatching { local.seedInitialDataIfEmpty() }
                            .onFailure { err ->
                                if (err is CancellationException) throw err
                                android.util.Log.w("NearGridViewModel", "Seed initial data skipped: ${err.message}")
                            }
                        Pair(FirestoreRepository(firestore), local)
                    }
                }

                _firestoreRepository = fsRepo
                _localRepository = localRepo

                val firebaseAuth = auth
                val initialUser = runCatching { firebaseAuth?.currentUser }.getOrNull()
                _currentUser.value = initialUser

                if (firebaseAuth != null && !authListenerAttached) {
                    runCatching {
                        firebaseAuth.addAuthStateListener(authStateListener)
                        authListenerAttached = true
                    }.onFailure { err ->
                        android.util.Log.w("NearGridViewModel", "AuthStateListener attachment skipped: ${err.message}")
                    }
                }

                _startupState.value = FeatureStartupState.Ready

                if (initialUser != null) {
                    syncUserSession(initialUser)
                }

                runCatching { refreshGridData() }
                    .onFailure { android.util.Log.w("NearGridViewModel", "Initial refreshGridData failed", it) }

                runCatching { CrashTelemetryManager.initialize(app) }

                runCatching {
                    com.example.data.update.AppUpdateManager.instance.init(app.applicationContext)
                    _isUpdateBannerDismissed.value = com.example.data.update.AppUpdateManager.instance.isBannerDismissed.value
                }.onFailure {
                    android.util.Log.w("NearGridViewModel", "AppUpdateManager initialization failed", it)
                }

                runCatching { checkForUpdates(isManual = false) }
                refreshStorageUsage()

                if (_uiState.value.appSettings.autoTrimCacheEnabled) {
                    viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                        runCatching { StorageCacheManager.trimCache(app) }
                        refreshStorageUsage()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                android.util.Log.e("NearGridViewModel", "Post-login initialization failed", t)
                _startupState.value = FeatureStartupState.Error(
                    t.message?.takeIf { it.isNotBlank() }
                        ?: "Unable to initialize your NearGrid workspace. Please retry or sign out."
                )
            }
        }
    }

    private fun handleFirebaseUserChanged(user: FirebaseUser?) {
        _currentUser.value = user
        if (user != null) {
            if (_startupState.value is FeatureStartupState.Ready) {
                syncUserSession(user)
            }
        } else {
            stopSessionSync()
            clearUserScopedState()
        }
    }

    private fun stopSessionSync() {
        sessionJob?.cancel()
        sessionJob = null
        activeSessionUid = null
    }

    private fun clearUserScopedState() {
        _firestorePosts.value = emptyList()
        _firestoreIssues.value = emptyList()
        _firestoreConversations.value = emptyList()
        _firestoreUsers.value = emptyList()
        _activePostForComments.value = null
        _uiState.value = _uiState.value.copy(
            currentDestination = NavigationDestination.HOME,
            activeConversation = null,
            showCreateDialog = false,
            showUserProfileEditor = false,
            showAppSettingsScreen = false,
            showGridQrScreen = false,
            showQrScannerDialog = false,
            userNickname = "Local Resident",
            userHandle = "@resident",
            userBio = "Civic community member connected via Google Resident Pass.",
            avatarUri = null,
            avatarPresetIndex = 0,
            blockedUserIds = emptyList()
        )
    }

    fun refreshDeviceLocation() {
        refreshGridData()
    }

    fun refreshGridData() {
        viewModelScope.launch(coroutineExceptionHandler) {
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
                if (auth?.currentUser != null) {
                    viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                        try {
                            _firestoreRepository?.updateUserLocationPresence(
                                location.latitude,
                                location.longitude,
                                computedGeohash
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (ignored: Exception) {}
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isLocating = false, isRefreshing = false)
            }
        }
    }

    private fun syncUserSession(user: FirebaseUser?) {
        if (user == null) return
        val fsRepo = _firestoreRepository ?: return
        if (activeSessionUid == user.uid && sessionJob?.isActive == true) {
            return
        }
        sessionJob?.cancel()
        activeSessionUid = user.uid

        sessionJob = viewModelScope.launch(coroutineExceptionHandler) {
            supervisorScope {
                launch {
                    val ensured = fsRepo.ensureUserProfileExists(
                        user = user,
                        cellId = _uiState.value.selectedCell?.id ?: "cell_local",
                        cellName = _currentLocation.value?.displayAreaName ?: "My Neighborhood"
                    )
                    ensured.getOrNull()?.let { profile ->
                        applyProfileToUiState(user, profile)
                    }

                    fsRepo.observeUserProfile(user.uid)
                        .catch { error ->
                            if (error is CancellationException) throw error
                            val fsError = error.unwrapFirestoreException()
                            android.util.Log.w("NearGridViewModel", "User profile stream error (${fsError?.code})", error)
                        }
                        .collect { profile ->
                            if (profile != null) {
                                applyProfileToUiState(user, profile)
                            }
                            val loc = _currentLocation.value
                            val hash = _currentGeohash.value
                            if (loc != null && loc.latitude != 0.0 && loc.longitude != 0.0) {
                                runCatching {
                                    fsRepo.updateUserLocationPresence(
                                        loc.latitude,
                                        loc.longitude,
                                        hash
                                    )
                                }
                            }
                        }
                }

                // Live real-time posts from Firestore filtered by user's current geohash prefix
                launch {
                    _currentGeohash.flatMapLatest { rawGeohash ->
                        val geohashPrefix = rawGeohash.removePrefix("#").take(5)
                        fsRepo.observePosts(geohashPrefix = geohashPrefix)
                    }.catch { error ->
                        if (error is CancellationException) throw error
                        val fsError = error.unwrapFirestoreException()
                        android.util.Log.w("NearGridViewModel", "Posts stream error (${fsError?.code})", error)
                        emit(emptyList())
                    }.collect { posts ->
                        _firestorePosts.value = posts
                    }
                }

                // Live real-time nearby users from Firestore filtered by current geohash prefix
                launch {
                    _currentGeohash.flatMapLatest { rawGeohash ->
                        val geohashPrefix = rawGeohash.removePrefix("#").take(3)
                        fsRepo.observeNearbyUsers(geohashPrefix = geohashPrefix)
                    }.catch { error ->
                        if (error is CancellationException) throw error
                        val fsError = error.unwrapFirestoreException()
                        android.util.Log.w("NearGridViewModel", "Nearby users stream error (${fsError?.code})", error)
                        emit(emptyList())
                    }.collect { users ->
                        _firestoreUsers.value = users
                    }
                }

                // Live real-time community issues from Firestore
                launch {
                    fsRepo.observeIssues()
                        .catch { error ->
                            if (error is CancellationException) throw error
                            val fsError = error.unwrapFirestoreException()
                            android.util.Log.w("NearGridViewModel", "Issues stream error (${fsError?.code})", error)
                            emit(emptyList())
                        }
                        .collect { issuesList ->
                            _firestoreIssues.value = issuesList
                        }
                }

                // Live real-time conversations from Firestore with Push Notification triggers
                launch {
                    var isInitialLoad = true
                    val lastSeenTimes = mutableMapOf<String, Long>()

                    fsRepo.observeConversations()
                        .catch { error ->
                            if (error is CancellationException) throw error
                            val fsError = error.unwrapFirestoreException()
                            android.util.Log.w("NearGridViewModel", "Conversations stream error (${fsError?.code})", error)
                            emit(emptyList())
                        }
                        .collect { convs ->
                            val currentUid = auth?.currentUser?.uid ?: ""
                            val activeConvId = _uiState.value.activeConversation?.id

                            if (!isInitialLoad) {
                                for (conv in convs) {
                                    val previousTime = lastSeenTimes[conv.id] ?: 0L
                                    if (conv.lastMessageTime > previousTime && conv.lastMessage.isNotBlank()) {
                                        val isFromOtherUser = conv.lastMessageSenderId.isNotBlank() &&
                                            conv.lastMessageSenderId != currentUid
                                        val isNotActiveScreen = conv.id != activeConvId

                                        if (isFromOtherUser && isNotActiveScreen) {
                                            com.example.util.ChatNotificationManager.showChatNotification(
                                                context = getApplication(),
                                                conversationId = conv.id,
                                                senderName = conv.participantName,
                                                messageText = conv.lastMessage
                                            )
                                        }
                                    }
                                }
                            } else {
                                isInitialLoad = false
                            }

                            for (conv in convs) {
                                lastSeenTimes[conv.id] = conv.lastMessageTime
                            }

                            _firestoreConversations.value = convs
                        }
                }
            }
        }
    }

    private fun applyProfileToUiState(user: FirebaseUser, profile: UserProfile) {
        _uiState.value = _uiState.value.copy(
            userNickname = profile.displayName.ifBlank { user.displayName ?: "Local Resident" },
            userHandle = profile.handle.ifBlank { "@${user.email?.substringBefore("@") ?: "resident"}" },
            userBio = profile.bio,
            avatarUri = profile.avatarUri ?: user.photoUrl?.toString(),
            avatarPresetIndex = profile.avatarPresetIndex,
            neighborhoodPrivacyRadiusMeters = profile.privacyRadiusMeters,
            fuzzyCoordinateOffset = profile.fuzzyCoords,
            incognitoGridPresence = profile.incognito,
            allowProximityMessaging = profile.allowDMs,
            locationPrecision = profile.locationPrecision,
            hideOnlineStatus = profile.hideOnlineStatus,
            hideExactDistance = profile.hideExactDistance,
            blockedUserIds = profile.blockedUserIds
        )
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

    // 100% Real-Time Live Posts with Strict Geohash & Location Isolation
    val filteredPosts: StateFlow<List<NeighborPost>> = combine(
        _firestorePosts,
        _currentLocation,
        _uiState,
        _currentGeohash
    ) { posts, location, state, geohash ->
        // If GPS is disabled or location is unavailable, return empty list (prompt user to enable location)
        if (!state.gpsModeEnabled || location == null) {
            return@combine emptyList()
        }

        val currentCellId = state.selectedCell?.id ?: ""
        val cleanGeohash = geohash.removePrefix("#").take(5)

        posts.map { post ->
            val distanceM = if (post.lat != 0.0 && post.lng != 0.0) {
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

            // Strict physical coordinate distance filtering
            val matchesLocationCell = if (post.lat != 0.0 && post.lng != 0.0) {
                val dist = LocationHelper.calculateDistanceMeters(
                    location.latitude,
                    location.longitude,
                    post.lat,
                    post.lng
                )
                dist <= state.discoveryRadiusMeters
            } else if (cleanGeohash.isNotBlank() && post.authorCellId.isNotBlank()) {
                post.authorCellId.contains(cleanGeohash, ignoreCase = true)
            } else {
                false
            }

            matchesType && matchesCategory && matchesLocationCell
        }.sortedWith(
            compareByDescending<NeighborPost> { it.urgency == "URGENT" }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topIntentMatches: StateFlow<List<IntentMatchResult>> = combine(
        _startupState,
        _firestorePosts
    ) { startup, posts ->
        val localRepo = _localRepository
        if (startup is FeatureStartupState.Ready && localRepo != null) {
            localRepo.findTopIntentMatches(posts)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-time Firestore issues
    val issues: StateFlow<List<CommunityIssue>> = _firestoreIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<CommunityNotice>> = _startupState.flatMapLatest { startup ->
        val localRepo = _localRepository
        if (startup is FeatureStartupState.Ready && localRepo != null) {
            localRepo.allNotices
        } else {
            flowOf(emptyList())
        }
    }.catch { error ->
        if (error is CancellationException) throw error
        android.util.Log.w("NearGridViewModel", "Notices stream error: ${error.message}", error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 100% Real-time Firestore conversations
    val conversations: StateFlow<List<Conversation>> = _firestoreConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
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

    fun openAppSettings() {
        refreshStorageUsage()
        _uiState.value = _uiState.value.copy(showAppSettingsScreen = true)
    }

    fun closeAppSettings() {
        _uiState.value = _uiState.value.copy(showAppSettingsScreen = false)
    }

    fun updateAppSettings(newSettings: AppSettings) {
        _uiState.value = _uiState.value.copy(appSettings = newSettings)
    }

    fun refreshStorageUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val usage = StorageCacheManager.getStorageUsage(getApplication())
            _uiState.value = _uiState.value.copy(storageUsage = usage)
        }
    }

    fun trimCacheNow() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = StorageCacheManager.trimCache(getApplication())
            refreshStorageUsage()
            CrashTelemetryManager.logEvent("cache_trimmed_manually", mapOf("freedBytes" to result.freedBytes, "filesRemoved" to result.filesRemovedCount))
        }
    }

    fun submitSupportTicket(category: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = _uiState.value.copy(supportTicketStatus = "Submitting ticket...")
            val result = requireFirestoreRepository().submitSupportTicket(
                category = category,
                message = message,
                userNickname = _uiState.value.userNickname,
                appVersion = _uiState.value.appSettings.appVersion
            )
            _uiState.value = _uiState.value.copy(
                supportTicketStatus = if (result.isSuccess) {
                    "Ticket submitted successfully! Our team will respond shortly."
                } else {
                    "Saved locally. Feedback recorded."
                }
            )
        }
    }

    fun clearSupportTicketStatus() {
        _uiState.value = _uiState.value.copy(supportTicketStatus = null)
    }

    private val _selectedNeighborForProfile = MutableStateFlow<NearbyNeighbor?>(null)
    val selectedNeighborForProfile: StateFlow<NearbyNeighbor?> = _selectedNeighborForProfile.asStateFlow()

    fun openNeighborProfile(neighbor: NearbyNeighbor) {
        _selectedNeighborForProfile.value = neighbor
    }

    fun closeNeighborProfile() {
        _selectedNeighborForProfile.value = null
    }

    fun setDestination(dest: NavigationDestination) {
        _uiState.value = _uiState.value.copy(currentDestination = dest)
        if (dest == NavigationDestination.MAP) {
            refreshGridData()
        }
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

    fun joinGridCellByCode(codeOrPayload: String) {
        val parsed = QrCodePayloadHelper.parsePayload(codeOrPayload)
        val cleanCode = parsed.code

        val matchingCell = _uiState.value.allCells.find { it.code.equals(cleanCode, ignoreCase = true) }
            ?: GridCell(
                id = "scanned_${cleanCode.lowercase().replace("#", "")}",
                name = parsed.name,
                code = cleanCode,
                activeNeighborsCount = 18,
                openNeedsCount = 3,
                openOffersCount = 5,
                centerLat = parsed.lat,
                centerLng = parsed.lng,
                description = "Neighborhood spatial grid joined via offline QR grid pass."
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
        return activeChatMessages
    }

    val currentUserId: String
        get() = auth?.currentUser?.uid ?: ""

    fun deletePost(postId: String) {
        launchSafely("deletePost") {
            requireFirestoreRepository().deletePost(postId)
            requireLocalRepository().deletePost(postId)
        }
    }

    fun editPost(postId: String, title: String, description: String, category: String, urgency: String) {
        launchSafely("editPost") {
            requireFirestoreRepository().updatePostContent(postId, title, description, category, urgency)
            requireLocalRepository().updatePostContent(postId, title, description, category, urgency)
        }
    }

    fun sendMessageToActive(text: String, isMeetingPoint: Boolean = false, meetingPoint: String? = null) {
        val active = _uiState.value.activeConversation ?: return
        if (text.isBlank()) return
        launchSafely("sendMessageToActive") {
            requireFirestoreRepository().sendMessage(active.id, text.trim(), isMeetingPoint, meetingPoint)
        }
    }

    fun deleteChatMessage(conversationId: String, messageId: String) {
        launchSafely("deleteChatMessage") {
            requireFirestoreRepository().deleteChatMessage(conversationId, messageId)
            requireLocalRepository().deleteChatMessage(messageId)
        }
    }

    fun editChatMessage(conversationId: String, messageId: String, newText: String) {
        if (newText.isBlank()) return
        launchSafely("editChatMessage") {
            requireFirestoreRepository().editChatMessage(conversationId, messageId, newText.trim())
            requireLocalRepository().editChatMessage(messageId, newText.trim())
        }
    }

    fun connectFromPost(post: NeighborPost) {
        launchSafely("connectFromPost") {
            val result = requireFirestoreRepository().startConversationForPost(post)
            val convId = result.getOrNull() ?: requireLocalRepository().startConversationForPost(post)
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

    fun connectWithNeighbor(neighbor: NearbyNeighbor) {
        launchSafely("connectWithNeighbor") {
            val neighborUser = neighbor.user
            val result = requireFirestoreRepository().startDirectConversationWithNeighbor(
                neighborUser.userId,
                neighborUser.displayName.ifBlank { "Neighbor" }
            )
            val convId = result.getOrNull() ?: "dm_${neighborUser.userId.take(8)}"
            val conv = Conversation(
                id = convId,
                participantName = neighborUser.displayName.ifBlank { "Neighbor" },
                relatedPostTitle = neighbor.latestPost?.title ?: "Nearby Neighbor on Map",
                relatedPostType = neighbor.latestPost?.type ?: "MAP",
                lastMessage = "Connecting nearby (${LocationHelper.formatDistance(neighbor.distanceMeters)} away)",
                distanceBand = LocationHelper.formatDistance(neighbor.distanceMeters)
            )
            _uiState.value = _uiState.value.copy(
                activeConversation = conv,
                currentDestination = NavigationDestination.INBOX
            )
        }
    }

    fun toggleIssueUpvote(issue: CommunityIssue) {
        launchSafely("toggleIssueUpvote") {
            requireFirestoreRepository().toggleIssueUpvote(issue.id, issue.upvotesCount, issue.hasUserUpvoted)
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
        allowDMs: Boolean,
        locationPrecision: String = "EXACT",
        hideOnlineStatus: Boolean = false,
        hideExactDistance: Boolean = false
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
            locationPrecision = locationPrecision,
            hideOnlineStatus = hideOnlineStatus,
            hideExactDistance = hideExactDistance,
            showUserProfileEditor = false
        )

        val user = auth?.currentUser
        if (user != null) {
            launchSafely("updateUserProfile") {
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
                    allowDMs = allowDMs,
                    locationPrecision = locationPrecision,
                    hideOnlineStatus = hideOnlineStatus,
                    hideExactDistance = hideExactDistance,
                    blockedUserIds = _uiState.value.blockedUserIds
                )
                requireFirestoreRepository().saveUserProfile(profile)
            }
        }
    }

    fun toggleIncognitoPresence() {
        val updated = _uiState.value.copy(incognitoGridPresence = !_uiState.value.incognitoGridPresence)
        _uiState.value = updated
        persistQuickProfileState(updated)
    }

    fun toggleAllowProximityMessaging() {
        val updated = _uiState.value.copy(allowProximityMessaging = !_uiState.value.allowProximityMessaging)
        _uiState.value = updated
        persistQuickProfileState(updated)
    }

    fun toggleHideExactDistance() {
        val updated = _uiState.value.copy(hideExactDistance = !_uiState.value.hideExactDistance)
        _uiState.value = updated
        persistQuickProfileState(updated)
    }

    fun setLocationPrecision(precision: String) {
        val updated = _uiState.value.copy(
            locationPrecision = precision,
            fuzzyCoordinateOffset = precision != "EXACT"
        )
        _uiState.value = updated
        persistQuickProfileState(updated)
    }

    private fun persistQuickProfileState(state: NearGridUiState) {
        val user = auth?.currentUser ?: return
        launchSafely("persistQuickProfileState") {
            val loc = _currentLocation.value
            val profile = UserProfile(
                userId = user.uid,
                email = user.email ?: "",
                displayName = state.userNickname.ifBlank { "Neighbor" },
                handle = state.userHandle,
                bio = state.userBio,
                cellId = state.selectedCell?.id ?: "cell_local",
                cellName = loc?.displayAreaName ?: state.selectedCell?.name ?: "Local Area",
                avatarPresetIndex = state.avatarPresetIndex,
                avatarUri = state.avatarUri,
                privacyRadiusMeters = state.neighborhoodPrivacyRadiusMeters,
                fuzzyCoords = state.fuzzyCoordinateOffset,
                incognito = state.incognitoGridPresence,
                allowDMs = state.allowProximityMessaging,
                locationPrecision = state.locationPrecision,
                hideOnlineStatus = state.hideOnlineStatus,
                hideExactDistance = state.hideExactDistance,
                blockedUserIds = state.blockedUserIds
            )
            requireFirestoreRepository().saveUserProfile(profile)
        }
    }

    fun blockUser(targetUserId: String) {
        if (targetUserId.isBlank()) return
        val currentBlocked = _uiState.value.blockedUserIds.toMutableList()
        if (!currentBlocked.contains(targetUserId)) {
            currentBlocked.add(targetUserId)
            _uiState.value = _uiState.value.copy(blockedUserIds = currentBlocked)
            launchSafely("blockUser") {
                requireFirestoreRepository().blockUser(targetUserId)
            }
        }
    }

    fun unblockUser(targetUserId: String) {
        if (targetUserId.isBlank()) return
        val currentBlocked = _uiState.value.blockedUserIds.toMutableList()
        if (currentBlocked.contains(targetUserId)) {
            currentBlocked.remove(targetUserId)
            _uiState.value = _uiState.value.copy(blockedUserIds = currentBlocked)
            launchSafely("unblockUser") {
                requireFirestoreRepository().unblockUser(targetUserId)
            }
        }
    }

    fun reportUser(targetUserId: String, targetDisplayName: String, reason: String, notes: String) {
        launchSafely("reportUser") {
            requireFirestoreRepository().reportUser(targetUserId, targetDisplayName, reason, notes)
        }
    }

    fun togglePostUpvote(post: NeighborPost) {
        launchSafely("togglePostUpvote") {
            requireFirestoreRepository().togglePostUpvote(post.id, post.upvotesCount, post.hasUserUpvoted)
        }
    }

    fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?) {
        launchSafely("updateIssueStatus") {
            requireFirestoreRepository().updateIssueStatus(issueId, newStatus, volunteer)
        }
    }

    fun createNeed(
        title: String,
        description: String,
        category: String,
        urgency: String,
        expiresInHours: Int,
        mediaUris: List<String> = emptyList(),
        mediaTypes: List<String> = emptyList()
    ) {
        launchSafely("createNeed") {
            val processedMediaUris = com.example.util.MediaUploadHelper.processMediaUris(getApplication(), mediaUris)
            val user = auth?.currentUser
            val loc = _currentLocation.value
            if (loc == null || loc.latitude == 0.0 || loc.longitude == 0.0) {
                closeCreateDialog()
                return@launchSafely
            }
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = loc.displayAreaName.ifBlank { "Local Area" }
            val lat = loc.latitude
            val lng = loc.longitude
            val geohash = LocationHelper.computeGeohash(lat, lng, 7).removePrefix("#")
            val authorCellId = "geo_${geohash.take(5)}"

            val post = NeighborPost(
                id = UUID.randomUUID().toString(),
                userId = user?.uid ?: "",
                type = "NEED",
                title = title,
                description = description,
                category = category,
                authorName = author,
                authorInitials = author.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("").ifEmpty { "ME" },
                authorCellId = authorCellId,
                cellName = realLocName,
                lat = lat,
                lng = lng,
                urgency = urgency,
                expiresInHours = expiresInHours,
                approxDistanceBand = "< 50 m",
                status = "ACTIVE",
                mediaUris = processedMediaUris,
                mediaTypes = mediaTypes
            )
            requireFirestoreRepository().createPost(post)
            closeCreateDialog()
        }
    }

    fun createOffer(
        title: String,
        description: String,
        category: String,
        expiresInHours: Int,
        mediaUris: List<String> = emptyList(),
        mediaTypes: List<String> = emptyList()
    ) {
        launchSafely("createOffer") {
            val processedMediaUris = com.example.util.MediaUploadHelper.processMediaUris(getApplication(), mediaUris)
            val user = auth?.currentUser
            val loc = _currentLocation.value
            if (loc == null || loc.latitude == 0.0 || loc.longitude == 0.0) {
                closeCreateDialog()
                return@launchSafely
            }
            val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
            val realLocName = loc.displayAreaName.ifBlank { "Local Area" }
            val lat = loc.latitude
            val lng = loc.longitude
            val geohash = LocationHelper.computeGeohash(lat, lng, 7).removePrefix("#")
            val authorCellId = "geo_${geohash.take(5)}"

            val post = NeighborPost(
                id = UUID.randomUUID().toString(),
                userId = user?.uid ?: "",
                type = "OFFER",
                title = title,
                description = description,
                category = category,
                authorName = author,
                authorInitials = author.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("").ifEmpty { "ME" },
                authorCellId = authorCellId,
                cellName = realLocName,
                lat = lat,
                lng = lng,
                urgency = "NORMAL",
                expiresInHours = expiresInHours,
                approxDistanceBand = "< 50 m",
                status = "ACTIVE",
                mediaUris = processedMediaUris,
                mediaTypes = mediaTypes
            )
            requireFirestoreRepository().createPost(post)
            closeCreateDialog()
        }
    }

    fun createIssue(
        title: String,
        description: String,
        location: String,
        category: String,
        mediaUris: List<String> = emptyList(),
        mediaTypes: List<String> = emptyList()
    ) {
        launchSafely("createIssue") {
            val processedMediaUris = com.example.util.MediaUploadHelper.processMediaUris(getApplication(), mediaUris)
            val user = auth?.currentUser
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
                reporterId = user?.uid ?: "",
                mediaUris = processedMediaUris,
                mediaTypes = mediaTypes
            )
            requireFirestoreRepository().createIssue(issue)
            closeCreateDialog()
            _uiState.value = _uiState.value.copy(
                currentDestination = NavigationDestination.HOME,
                postFilterType = "CIVIC"
            )
        }
    }

    fun createMeetup(
        title: String,
        description: String,
        category: String,
        timeframeHours: Int,
        mediaUris: List<String> = emptyList(),
        mediaTypes: List<String> = emptyList()
    ) {
        launchSafely("createMeetup") {
            val processedMediaUris = com.example.util.MediaUploadHelper.processMediaUris(getApplication(), mediaUris)
            val user = auth?.currentUser
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
                status = "ACTIVE",
                mediaUris = processedMediaUris,
                mediaTypes = mediaTypes
            )
            requireFirestoreRepository().createPost(post)
        }
    }

    fun postGeohashMessage(
        text: String,
        onComplete: ((Result<String>) -> Unit)? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = _uiState.value.copy(isPostingGeohashMessage = true, geohashPostStatus = null)
            try {
                val loc = _currentLocation.value
                if (loc == null || loc.latitude == 0.0 || loc.longitude == 0.0) {
                    _uiState.value = _uiState.value.copy(
                        isPostingGeohashMessage = false,
                        geohashPostStatus = "Error: Live GPS coordinates required to broadcast. Please wait for GPS lock."
                    )
                    onComplete?.invoke(Result.failure(IllegalStateException("Active device GPS required")))
                    return@launch
                }
                val lat = loc.latitude
                val lon = loc.longitude
                val geohash = LocationHelper.computeGeohash(lat, lon, 7).removePrefix("#")
                val user = auth?.currentUser
                val author = _uiState.value.userNickname.ifBlank { user?.displayName ?: "Resident" }
                val locality = loc.displayAreaName.ifBlank { "Local Area" }

                val result = requireFirestoreRepository().postGeohashMessage(
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPostingGeohashMessage = false,
                    geohashPostStatus = "Error: ${e.message ?: "Unable to broadcast message"}"
                )
                onComplete?.invoke(Result.failure(e))
            }
        }
    }

    fun clearGeohashPostStatus() {
        _uiState.value = _uiState.value.copy(geohashPostStatus = null)
    }

    fun resetToFreshGrid() {
        launchSafely("resetToFreshGrid") {
            requireLocalRepository().resetToFreshState()
            refreshDeviceLocation()
        }
    }

    suspend fun prepareForSignOut() {
        withContext(NonCancellable) {
            startupJob?.cancel()
            stopSessionSync()
            withTimeoutOrNull(1200L) {
                runCatching { _firestoreRepository?.markUserOfflineBestEffort() }
            }
            clearUserScopedState()
            _currentUser.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (authListenerAttached) {
            runCatching { auth?.removeAuthStateListener(authStateListener) }
            authListenerAttached = false
        }
        startupJob?.cancel()
        stopSessionSync()
    }
}
