package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GridCell
import com.example.data.remote.FirebaseBootstrap
import com.example.ui.NavigationDestination
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NearbyControlsBar
import com.example.ui.theme.NearGridTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp-xxhdpi")
class HomeAndRecoveryComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseBootstrap.ensureInitialized(context)
    }

    @Test
    fun homeScreen_composesWithoutSelfReferentialBackdropCrash() {
        composeTestRule.setContent {
            NearGridTheme {
                HomeScreen(
                    currentCell = GridCell(
                        id = "cell_test",
                        name = "Downtown Sector",
                        code = "#GPS-11.93,79.83",
                        activeNeighborsCount = 3,
                        openNeedsCount = 1,
                        openOffersCount = 1,
                        centerLat = 11.93,
                        centerLng = 79.83
                    ),
                    discoveryRadiusMeters = 800,
                    gpsEnabled = true,
                    latitude = 11.93,
                    longitude = 79.83,
                    localityName = "Downtown Sector",
                    posts = emptyList(),
                    topIntentMatches = emptyList(),
                    selectedFilterType = "ALL",
                    selectedCategory = "ALL",
                    onCellClick = {},
                    onGpsToggle = {},
                    onRadiusChange = {},
                    onFilterChange = {},
                    onCategoryChange = {},
                    onConnectPost = {},
                    onOpenCreateDialog = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_filter_menu_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_create_post").assertIsDisplayed()
    }

    @Test
    fun homeScreen_burgerSortAndFilterSheetOpensAndSwitchesModesWithoutCrash() {
        var selectedFilter by mutableStateOf("ALL")
        var selectedCategory by mutableStateOf("ALL")
        var selectedRadius by mutableIntStateOf(800)

        composeTestRule.setContent {
            NearGridTheme {
                HomeScreen(
                    currentCell = GridCell(
                        id = "cell_test",
                        name = "Downtown Sector",
                        code = "#GPS-11.93,79.83",
                        activeNeighborsCount = 3,
                        openNeedsCount = 1,
                        openOffersCount = 1,
                        centerLat = 11.93,
                        centerLng = 79.83
                    ),
                    discoveryRadiusMeters = selectedRadius,
                    gpsEnabled = true,
                    latitude = 11.93,
                    longitude = 79.83,
                    localityName = "Downtown Sector",
                    posts = emptyList(),
                    topIntentMatches = emptyList(),
                    selectedFilterType = selectedFilter,
                    selectedCategory = selectedCategory,
                    onCellClick = {},
                    onGpsToggle = {},
                    onRadiusChange = { selectedRadius = it },
                    onFilterChange = { selectedFilter = it },
                    onCategoryChange = { selectedCategory = it },
                    onConnectPost = {},
                    onOpenCreateDialog = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_filter_menu_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_filter_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort & Filter").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feed_mode_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feed_mode_nearby").assertIsDisplayed()

        // Selecting a post filter updates state and closes the sheet cleanly
        composeTestRule.onNodeWithTag("filter_pill_need").performClick()
        composeTestRule.waitForIdle()
        assertEquals("NEED", selectedFilter)
        composeTestRule.onNodeWithTag("home_filter_sheet").assertDoesNotExist()

        // Re-open the burger sheet and verify Reset & Closest First sort switching
        composeTestRule.onNodeWithTag("home_filter_menu_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("filter_reset_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feed_mode_nearby").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Nearby Distance Feed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feed_mode_grid").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Block Feed").assertIsDisplayed()
    }

    @Test
    fun communityScreen_burgerIssueFilterSheetOpensAndFiltersWithoutCrash() {
        composeTestRule.setContent {
            NearGridTheme {
                CommunityScreen(
                    notices = emptyList(),
                    issues = emptyList(),
                    onToggleUpvote = {},
                    onUpdateIssueStatus = { _, _, _ -> },
                    onOpenReportIssue = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Issue tracker (0)").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("community_filter_menu_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("community_filter_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Issue Status").assertIsDisplayed()
        composeTestRule.onNodeWithTag("issue_status_pill_reported").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("community_filter_sheet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Showing: Reported").assertIsDisplayed()
    }

    @Test
    fun nearbyControlsBar_burgerFilterSheetOpensAndTogglesSortWithoutCrash() {
        var sortByDistance by mutableStateOf(true)
        var selectedType by mutableStateOf("ALL")

        composeTestRule.setContent {
            NearGridTheme {
                NearbyControlsBar(
                    selectedRadiusMeters = 3000,
                    onRadiusSelect = {},
                    selectedType = selectedType,
                    onTypeSelect = { selectedType = it },
                    onlyWithPhotos = false,
                    onTogglePhotos = {},
                    sortByDistance = sortByDistance,
                    onToggleSort = { sortByDistance = !sortByDistance },
                    itemCount = 4,
                    isRefreshing = false,
                    onRefresh = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nearby_filter_menu_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nearby_filter_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nearby Filters").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nearby_feed_sort_newest_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals(false, sortByDistance)
        composeTestRule.onNodeWithTag("nearby_filter_sheet").assertDoesNotExist()
    }

    @Test
    fun authenticatedRecoveryScreen_displaysErrorAndInvokesRetryAndSignOut() {
        var retryClicks = 0
        var signOutClicks = 0

        composeTestRule.setContent {
            NearGridTheme {
                AuthenticatedRecoveryScreen(
                    errorMessage = "Cloud database unavailable.",
                    onRetry = { retryClicks++ },
                    onSignOut = { signOutClicks++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("authenticated_recovery_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cloud database unavailable.").assertIsDisplayed()

        composeTestRule.onNodeWithTag("retry_startup_button").performClick()
        composeTestRule.onNodeWithTag("recovery_sign_out_button").performClick()

        assertEquals(1, retryClicks)
        assertEquals(1, signOutClicks)
    }

    @Test
    fun liquidGlassBottomBar_rendersClearActivePillAndSwitchesTabs() {
        var currentDestination by mutableStateOf(NavigationDestination.HOME)

        composeTestRule.setContent {
            NearGridTheme {
                val backdrop = rememberLayerBackdrop()
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                            .background(Color(0xFF0B1020))
                    )
                    LiquidGlassBottomBar(
                        backdrop = backdrop,
                        destinations = listOf(
                            NavigationDestination.HOME,
                            NavigationDestination.MAP,
                            NavigationDestination.RADAR,
                            NavigationDestination.INBOX,
                            NavigationDestination.PROFILE
                        ),
                        currentDestination = currentDestination,
                        onSelectDestination = { currentDestination = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_active_pill").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_radar").performClick()
        composeTestRule.waitForIdle()
        assertEquals(NavigationDestination.RADAR, currentDestination)
        composeTestRule.onNodeWithTag("bottom_nav_active_pill").assertIsDisplayed()
    }
}
