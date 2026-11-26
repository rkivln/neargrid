package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavigationDestination
import com.example.ui.NearGridViewModel
import com.example.ui.components.AuthVerificationDialog
import com.example.ui.components.CellPickerDialog
import com.example.ui.components.ConversationChatView
import com.example.ui.components.CreateIntentSheet
import com.example.ui.components.GeohashQrInviteDialog
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.PostCommentsSheet
import com.example.ui.components.QrCodeScannerDialog
import com.example.ui.screens.AppSettingsScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.GridQrInviteScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.MeshRadarScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RealtimeMapScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.NearGridTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            )
        } catch (ignored: Exception) {
            // Edge-to-edge fallback for custom OEM ROMs
        }
        try {
            setContent {
                NearGridTheme {
                    NearGridApp()
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Fatal error during Activity setContent", t)
        }
    }
}

@Composable
fun NearGridApp(viewModel: NearGridViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cells by viewModel.cells.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val posts by viewModel.filteredPosts.collectAsStateWithLifecycle()
    val topIntentMatches by viewModel.topIntentMatches.collectAsStateWithLifecycle()
    val issues by viewModel.issues.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val chatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val geohashMessages by viewModel.geohashMessages.collectAsStateWithLifecycle()
    val activeCommentsPost by viewModel.activePostForComments.collectAsStateWithLifecycle()
    val postComments by viewModel.activePostComments.collectAsStateWithLifecycle()
    val nearbyNeighbors by viewModel.nearbyNeighbors.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isUpdateBannerDismissed by viewModel.isUpdateBannerDismissed.collectAsStateWithLifecycle()

    var showCellPickerDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }

    // GPS Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.refreshDeviceLocation()
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Notification permission result */ }

    val activity = context as? android.app.Activity

    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            viewModel.refreshDeviceLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle deep link notification click to open corresponding conversation directly
    LaunchedEffect(activity?.intent, conversations) {
        val convId = activity?.intent?.getStringExtra(com.example.util.ChatNotificationManager.EXTRA_CONVERSATION_ID)
        if (!convId.isNullOrBlank()) {
            val matchingConv = conversations.find { it.id == convId }
            if (matchingConv != null) {
                viewModel.openConversation(matchingConv)
                activity.intent?.removeExtra(com.example.util.ChatNotificationManager.EXTRA_CONVERSATION_ID)
            }
        }
    }

    val onToggleGps = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleGpsMode()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Authentication Gate: Render Google Sign-In when unauthenticated
    if (currentUser == null) {
        SignInScreen(
            onSignInSuccess = { /* Managed reactively by Firebase AuthStateListener */ }
        )
        return
    }

    // Full-screen Onboarding experience if active
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { showOnboarding = false }
        )
        return
    }

    // Predictive Back Handling
    BackHandler(enabled = uiState.showAppSettingsScreen || uiState.showGridQrScreen || uiState.showUserProfileEditor || uiState.activeConversation != null || uiState.currentDestination != NavigationDestination.HOME) {
        if (uiState.showAppSettingsScreen) {
            viewModel.closeAppSettings()
        } else if (uiState.showGridQrScreen) {
            viewModel.closeGridQrScreen()
        } else if (uiState.showUserProfileEditor) {
            viewModel.closeUserProfileEditor()
        } else if (uiState.activeConversation != null) {
            viewModel.closeConversation()
        } else if (uiState.currentDestination != NavigationDestination.HOME) {
            viewModel.setDestination(NavigationDestination.HOME)
        }
    }

    // Full-screen Grid QR Code Pass & Invitation Screen
    if (uiState.showGridQrScreen) {
        GridQrInviteScreen(
            currentCell = uiState.selectedCell,
            currentGeohash = uiState.currentGeohash,
            userNickname = uiState.userNickname,
            onBackClick = { viewModel.closeGridQrScreen() },
            onOpenScanner = { viewModel.openQrScannerDialog() }
        )
        return
    }

    // Full-screen App Settings & Customization Hub
    if (uiState.showAppSettingsScreen) {
        AppSettingsScreen(
            appSettings = uiState.appSettings,
            storageUsage = uiState.storageUsage,
            supportTicketStatus = uiState.supportTicketStatus,
            onUpdateSettings = { viewModel.updateAppSettings(it) },
            onTrimCacheNow = { viewModel.trimCacheNow() },
            onSubmitSupportTicket = { cat, msg -> viewModel.submitSupportTicket(cat, msg) },
            onClearSupportStatus = { viewModel.clearSupportTicketStatus() },
            onBack = { viewModel.closeAppSettings() }
        )
        return
    }

    // Full-screen User Profile & Privacy settings editor
    if (uiState.showUserProfileEditor) {
        UserProfileScreen(
            currentNickname = uiState.userNickname,
            currentHandle = uiState.userHandle,
            currentBio = uiState.userBio,
            currentAvatarUri = uiState.avatarUri,
            currentPresetIndex = uiState.avatarPresetIndex,
            currentPrivacyRadius = uiState.neighborhoodPrivacyRadiusMeters,
            currentFuzzyCoords = uiState.fuzzyCoordinateOffset,
            currentIncognito = uiState.incognitoGridPresence,
            currentAllowDMs = uiState.allowProximityMessaging,
            currentLocationPrecision = uiState.locationPrecision,
            currentHideOnlineStatus = uiState.hideOnlineStatus,
            currentHideExactDistance = uiState.hideExactDistance,
            blockedUserIds = uiState.blockedUserIds,
            onUnblockUser = { viewModel.unblockUser(it) },
            currentCell = uiState.selectedCell,
            onSaveProfile = { nick, handle, bio, uri, preset, radius, fuzzy, incognito, allowDMs, precision, hideOnline, hideDist ->
                viewModel.updateUserProfile(
                    nickname = nick,
                    handle = handle,
                    bio = bio,
                    avatarUri = uri,
                    avatarPresetIndex = preset,
                    privacyRadiusMeters = radius,
                    fuzzyCoords = fuzzy,
                    incognito = incognito,
                    allowDMs = allowDMs,
                    locationPrecision = precision,
                    hideOnlineStatus = hideOnline,
                    hideExactDistance = hideDist
                )
            },
            onBack = { viewModel.closeUserProfileEditor() },
            onOpenSettings = { viewModel.openAppSettings() },
            updateInfo = updateInfo,
            onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) }
        )
        return
    }

    // Active conversation view
    val activeConv = uiState.activeConversation
    if (activeConv != null) {
        ConversationChatView(
            conversation = activeConv,
            messages = chatMessages,
            onSendMessage = { text, isMeetingPoint, meetingPoint ->
                viewModel.sendMessageToActive(text, isMeetingPoint, meetingPoint)
            },
            onEditMessage = { messageId, newText ->
                viewModel.editChatMessage(activeConv.id, messageId, newText)
            },
            onDeleteMessage = { messageId ->
                viewModel.deleteChatMessage(activeConv.id, messageId)
            },
            onBack = { viewModel.closeConversation() }
        )
        return
    }

    val backdrop = rememberLayerBackdrop()

    // Edge-to-edge tab host layered behind the floating Apple Liquid Glass pill dock
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            when (uiState.currentDestination) {
                NavigationDestination.HOME -> {
                    HomeScreen(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshGridData() },
                        currentCell = uiState.selectedCell,
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        gpsEnabled = uiState.gpsModeEnabled,
                        isLocating = uiState.isLocating,
                        currentGeohash = uiState.currentGeohash,
                        latitude = uiState.currentLocation?.latitude ?: uiState.selectedCell?.centerLat ?: 0.0,
                        longitude = uiState.currentLocation?.longitude ?: uiState.selectedCell?.centerLng ?: 0.0,
                        localityName = uiState.currentLocation?.displayAreaName ?: uiState.selectedCell?.name ?: "",
                        isPostingGeohashMessage = uiState.isPostingGeohashMessage,
                        geohashPostStatus = uiState.geohashPostStatus,
                        geohashMessages = geohashMessages,
                        onRefreshLocation = { viewModel.refreshGridData() },
                        onPostGeohashMessage = { viewModel.postGeohashMessage(it) },
                        onDismissGeohashStatus = { viewModel.clearGeohashPostStatus() },
                        posts = posts,
                        notices = notices,
                        issues = issues,
                        topIntentMatches = topIntentMatches,
                        selectedFilterType = uiState.postFilterType,
                        selectedCategory = uiState.selectedCategory,
                        onCellClick = { showCellPickerDialog = true },
                        onGpsToggle = onToggleGps,
                        onOpenMapClick = { viewModel.setDestination(NavigationDestination.MAP) },
                        onShowQrClick = { uiState.selectedCell?.let { viewModel.showCellQrDialog(it) } },
                        onScanQrClick = { viewModel.openQrScannerDialog() },
                        onRadiusChange = { viewModel.setDiscoveryRadius(it) },
                        onFilterChange = { viewModel.setPostFilter(it) },
                        onCategoryChange = { viewModel.setCategoryFilter(it) },
                        onConnectPost = { viewModel.connectFromPost(it) },
                        onToggleUpvote = { viewModel.togglePostUpvote(it) },
                        onOpenComments = { viewModel.openPostComments(it) },
                        onToggleIssueUpvote = { viewModel.toggleIssueUpvote(it) },
                        onUpdateIssueStatus = { id, status, volunteer ->
                            viewModel.updateIssueStatus(id, status, volunteer)
                        },
                        currentUserId = viewModel.currentUserId,
                        onEditPost = { post, title, desc, category, urgency ->
                            viewModel.editPost(post.id, title, desc, category, urgency)
                        },
                        onDeletePost = { post ->
                            viewModel.deletePost(post.id)
                        },
                        onOpenCreateDialog = { viewModel.openCreateDialog(it) },
                        updateInfo = updateInfo,
                        isUpdateBannerDismissed = isUpdateBannerDismissed,
                        onDismissUpdateBanner = { viewModel.dismissUpdateBanner() },
                        nearbyNeighborsCount = nearbyNeighbors.size
                    )
                }

                NavigationDestination.MAP -> {
                    RealtimeMapScreen(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshGridData() },
                        userLocation = currentLocation,
                        currentCell = uiState.selectedCell,
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        nearbyNeighbors = nearbyNeighbors,
                        posts = posts,
                        issues = issues,
                        onRadiusChange = { viewModel.setDiscoveryRadius(it) },
                        onStartChatWithNeighbor = { viewModel.connectWithNeighbor(it) },
                        onBlockNeighbor = { viewModel.blockUser(it) },
                        onReportNeighbor = { targetUid, name, reason, notes ->
                            viewModel.reportUser(targetUid, name, reason, notes)
                        },
                        onPostClick = { viewModel.connectFromPost(it) },
                        onIssueClick = {
                            viewModel.setPostFilter("CIVIC")
                            viewModel.setDestination(NavigationDestination.HOME)
                        }
                    )
                }

                NavigationDestination.RADAR -> {
                    MeshRadarScreen(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshGridData() },
                        currentCell = uiState.selectedCell,
                        currentGeohash = uiState.currentGeohash,
                        latitude = currentLocation?.latitude ?: 0.0,
                        longitude = currentLocation?.longitude ?: 0.0,
                        localityName = currentLocation?.localityName ?: "",
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        nearbyNeighborsCount = nearbyNeighbors.size,
                        isLocating = uiState.isLocating,
                        isPostingGeohashMessage = uiState.isPostingGeohashMessage,
                        geohashPostStatus = uiState.geohashPostStatus,
                        geohashMessages = geohashMessages,
                        onRefreshLocation = { viewModel.refreshDeviceLocation() },
                        onPostGeohashMessage = { viewModel.postGeohashMessage(it) },
                        onDismissGeohashStatus = { viewModel.clearGeohashPostStatus() },
                        onOpenCreateDialog = { viewModel.openCreateDialog(it) }
                    )
                }

                NavigationDestination.COMMUNITY -> {
                    CommunityScreen(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshGridData() },
                        notices = notices,
                        issues = issues,
                        onToggleUpvote = { viewModel.toggleIssueUpvote(it) },
                        onUpdateIssueStatus = { id, status, volunteer ->
                            viewModel.updateIssueStatus(id, status, volunteer)
                        },
                        onOpenReportIssue = { viewModel.openCreateDialog("ISSUE") }
                    )
                }

                NavigationDestination.INBOX -> {
                    InboxScreen(
                        conversations = conversations,
                        onSelectConversation = { viewModel.openConversation(it) }
                    )
                }

                NavigationDestination.PROFILE -> {
                    ProfileScreen(
                        currentCell = uiState.selectedCell,
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        gpsEnabled = uiState.gpsModeEnabled,
                        onRadiusChange = { viewModel.setDiscoveryRadius(it) },
                        onGpsToggle = onToggleGps,
                        onReplayOnboarding = { showOnboarding = true },
                        onOpenAuthDialog = { showAuthDialog = true },
                        userNickname = uiState.userNickname,
                        userHandle = uiState.userHandle,
                        userBio = uiState.userBio,
                        avatarUri = uiState.avatarUri,
                        avatarPresetIndex = uiState.avatarPresetIndex,
                        incognitoGridPresence = uiState.incognitoGridPresence,
                        allowProximityMessaging = uiState.allowProximityMessaging,
                        hideExactDistance = uiState.hideExactDistance,
                        locationPrecision = uiState.locationPrecision,
                        blockedUserIds = uiState.blockedUserIds,
                        onToggleIncognito = { viewModel.toggleIncognitoPresence() },
                        onToggleAllowDMs = { viewModel.toggleAllowProximityMessaging() },
                        onToggleHideExactDistance = { viewModel.toggleHideExactDistance() },
                        onSetLocationPrecision = { viewModel.setLocationPrecision(it) },
                        onSaveProfile = { nick, handle, bio, uri, preset, radius ->
                            viewModel.updateUserProfile(
                                nickname = nick,
                                handle = handle,
                                bio = bio,
                                avatarUri = uri,
                                avatarPresetIndex = preset,
                                privacyRadiusMeters = radius,
                                fuzzyCoords = uiState.fuzzyCoordinateOffset,
                                incognito = uiState.incognitoGridPresence,
                                allowDMs = uiState.allowProximityMessaging,
                                locationPrecision = uiState.locationPrecision,
                                hideOnlineStatus = uiState.hideOnlineStatus,
                                hideExactDistance = uiState.hideExactDistance
                            )
                        },
                        onUnblockUser = { viewModel.unblockUser(it) },
                        onEditProfile = { viewModel.openUserProfileEditor() },
                        onOpenGridQrScreen = { viewModel.openGridQrScreen() },
                        onOpenSettings = { viewModel.openAppSettings() },
                        onResetToFreshGrid = { viewModel.resetToFreshGrid() },
                        userEmail = currentUser?.email,
                        onSignOut = { viewModel.signOut(context) }
                    )
                }
            }
        }

        LiquidGlassBottomBar(
            backdrop = backdrop,
            destinations = listOf(
                NavigationDestination.HOME,
                NavigationDestination.MAP,
                NavigationDestination.RADAR,
                NavigationDestination.INBOX,
                NavigationDestination.PROFILE
            ),
            currentDestination = uiState.currentDestination,
            onSelectDestination = { viewModel.setDestination(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }

    // Modal Sheet for choosing neighborhood coordinate cells
    if (showCellPickerDialog) {
        CellPickerDialog(
            cells = cells,
            currentCell = uiState.selectedCell,
            onSelectCell = { viewModel.selectCell(it) },
            onShowCellQr = { cell ->
                viewModel.showCellQrDialog(cell)
            },
            onScanQrClick = {
                viewModel.openQrScannerDialog()
            },
            onDismiss = { showCellPickerDialog = false }
        )
    }

    // Modal Dialog for QR Code cell invitations
    val qrCell = uiState.selectedQrCell
    if (qrCell != null) {
        GeohashQrInviteDialog(
            cell = qrCell,
            onDismiss = { viewModel.dismissCellQrDialog() }
        )
    }

    // Modal Dialog for QR Code scanner
    if (uiState.showQrScannerDialog) {
        QrCodeScannerDialog(
            sampleCells = cells,
            onScannedGeohash = { geohash ->
                viewModel.joinGridCellByCode(geohash)
            },
            onDismiss = { viewModel.closeQrScannerDialog() }
        )
    }

    // Modal Sheet for resident pass verification
    if (showAuthDialog) {
        AuthVerificationDialog(
            currentCell = uiState.selectedCell,
            onDismiss = { showAuthDialog = false },
            onVerified = { /* Verified */ }
        )
    }

    // Modal Sheet for creating intent posts (Need, Offer, Issue, Meetup)
    if (uiState.showCreateDialog) {
        CreateIntentSheet(
            initialType = uiState.createDialogInitialType,
            onDismiss = { viewModel.closeCreateDialog() },
            onCreateNeed = { title, desc, cat, urg, exp, uris, types ->
                viewModel.createNeed(title, desc, cat, urg, exp, uris, types)
            },
            onCreateOffer = { title, desc, cat, exp, uris, types ->
                viewModel.createOffer(title, desc, cat, exp, uris, types)
            },
            onCreateIssue = { title, desc, loc, cat, uris, types ->
                viewModel.createIssue(title, desc, loc, cat, uris, types)
            },
            onCreateMeetup = { title, desc, cat, hours, uris, types ->
                viewModel.createMeetup(title, desc, cat, hours, uris, types)
            }
        )
    }

    // Modal Sheet for Threaded Post Comments
    val activeCommentsPostVal = activeCommentsPost
    if (activeCommentsPostVal != null) {
        PostCommentsSheet(
            post = activeCommentsPostVal,
            comments = postComments,
            onSendComment = { text, parentId ->
                viewModel.sendPostComment(text, parentId)
            },
            onDismiss = { viewModel.closePostComments() }
        )
    }
}
