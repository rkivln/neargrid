package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavigationDestination
import com.example.ui.NearGridViewModel
import com.example.ui.components.AuthVerificationDialog
import com.example.ui.components.CellPickerDialog
import com.example.ui.components.ConversationChatView
import com.example.ui.components.CreateIntentSheet
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.LivingGridScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.NearGridTheme
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.WarmBorder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearGridTheme {
                NearGridApp()
            }
        }
    }
}

@Composable
fun NearGridApp(viewModel: NearGridViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cells by viewModel.cells.collectAsStateWithLifecycle()
    val posts by viewModel.filteredPosts.collectAsStateWithLifecycle()
    val topIntentMatches by viewModel.topIntentMatches.collectAsStateWithLifecycle()
    val issues by viewModel.issues.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val chatMessages by viewModel.getChatMessagesForActive().collectAsStateWithLifecycle()

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
            viewModel.toggleGpsMode()
        }
    }

    val onToggleGps = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission || uiState.gpsModeEnabled) {
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

    // Full-screen Onboarding experience if active
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { showOnboarding = false }
        )
        return
    }

    // Predictive Back Handling
    BackHandler(enabled = uiState.activeConversation != null || uiState.currentDestination != NavigationDestination.HOME) {
        if (uiState.activeConversation != null) {
            viewModel.closeConversation()
        } else if (uiState.currentDestination != NavigationDestination.HOME) {
            viewModel.setDestination(NavigationDestination.HOME)
        }
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
            onBack = { viewModel.closeConversation() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .border(1.dp, WarmBorder)
            ) {
                NavigationDestination.values().forEach { destination ->
                    val isSelected = uiState.currentDestination == destination
                    val (iconFilled, iconOutlined) = when (destination) {
                        NavigationDestination.HOME -> Icons.Filled.Home to Icons.Outlined.Home
                        NavigationDestination.GRID -> Icons.Filled.GridView to Icons.Outlined.GridView
                        NavigationDestination.COMMUNITY -> Icons.Filled.Campaign to Icons.Outlined.Campaign
                        NavigationDestination.INBOX -> Icons.Filled.ChatBubble to Icons.Outlined.ChatBubbleOutline
                        NavigationDestination.PROFILE -> Icons.Filled.Person to Icons.Outlined.Person
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setDestination(destination) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) iconFilled else iconOutlined,
                                contentDescription = destination.label,
                                tint = if (isSelected) PrimaryText else SecondaryText
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontFamily = DMSansFontFamily,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) PrimaryText else SecondaryText
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryText,
                            selectedTextColor = PrimaryText,
                            unselectedTextColor = SecondaryText,
                            unselectedIconColor = SecondaryText,
                            indicatorColor = PastelPeach.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentDestination) {
                NavigationDestination.HOME -> {
                    HomeScreen(
                        currentCell = uiState.selectedCell,
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        gpsEnabled = uiState.gpsModeEnabled,
                        posts = posts,
                        topIntentMatches = topIntentMatches,
                        selectedFilterType = uiState.postFilterType,
                        selectedCategory = uiState.selectedCategory,
                        onCellClick = { showCellPickerDialog = true },
                        onGpsToggle = onToggleGps,
                        onRadiusChange = { viewModel.setDiscoveryRadius(it) },
                        onFilterChange = { viewModel.setPostFilter(it) },
                        onCategoryChange = { viewModel.setCategoryFilter(it) },
                        onConnectPost = { viewModel.connectFromPost(it) },
                        onOpenCreateDialog = { viewModel.openCreateDialog(it) }
                    )
                }

                NavigationDestination.GRID -> {
                    LivingGridScreen(
                        cells = cells,
                        currentCell = uiState.selectedCell,
                        discoveryRadiusMeters = uiState.discoveryRadiusMeters,
                        onSelectCell = { viewModel.selectCell(it) },
                        onRadiusChange = { viewModel.setDiscoveryRadius(it) }
                    )
                }

                NavigationDestination.COMMUNITY -> {
                    CommunityScreen(
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
                        onOpenAuthDialog = { showAuthDialog = true }
                    )
                }
            }
        }
    }

    // Modal Sheet for choosing neighborhood coordinate cells
    if (showCellPickerDialog) {
        CellPickerDialog(
            cells = cells,
            currentCell = uiState.selectedCell,
            onSelectCell = { viewModel.selectCell(it) },
            onDismiss = { showCellPickerDialog = false }
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
            onCreateNeed = { title, desc, cat, urg, exp ->
                viewModel.createNeed(title, desc, cat, urg, exp)
            },
            onCreateOffer = { title, desc, cat, exp ->
                viewModel.createOffer(title, desc, cat, exp)
            },
            onCreateIssue = { title, desc, loc, cat ->
                viewModel.createIssue(title, desc, loc, cat)
            },
            onCreateMeetup = { title, desc, cat, hours ->
                viewModel.createMeetup(title, desc, cat, hours)
            }
        )
    }
}
