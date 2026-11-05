# Reorganizing App Architecture into 6 Clean Navigation Tabs & Decluttering Home

Reorganize the application into **6 distinct, well-defined navigation destinations** and declutter the Home screen by creating a dedicated **Mesh Radar** tab (`MeshRadarScreen.kt`).

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the clarifying interview:
> - **6 Bottom Navigation Tabs**:
>   1. 🏠 **Home**: Clean, spacious feed of AI intent matches, filter chips, and neighbor posts.
>   2. 🗺️ **Map**: Interactive OSM map with live neighbor pins, distance radius filters, and layer toggles.
>   3. 📡 **Mesh Radar**: Dedicated new screen for Hyperlocal Geohash Broadcasts and Quick Intent Creation (Need/Offer/Meetup).
>   4. 📢 **Community**: Community notice board, municipal issue reporting, and upvoting.
>   5. 💬 **Inbox**: Direct neighbor messages and private chat threads.
>   6. 👤 **Profile**: User profile, spatial perimeter preferences, and App Settings.
> - **Home Screen Decluttering**: Moved the heavy Geohash Broadcast Composer and Quick Intent Action Grid out of the Home screen and into the dedicated **Mesh Radar** screen.

---

### 1. Overview & Visual Redesign

```
┌─────────────────────────────────────────────────────────────────┐
│                   6 Navigation Destinations                     │
│  Home  │  Map  │  Mesh Radar  │  Community  │  Inbox  │ Profile │
└────────────────────────────────┬────────────────────────────────┘
                                 │
            ┌────────────────────┴────────────────────┐
            ▼                                         ▼
┌───────────────────────────────┐         ┌───────────────────────────────┐
│          HomeScreen           │         │       MeshRadarScreen         │
│ - Top Neighborhood Bar        │         │ - Hyperlocal Geohash Composer │
│ - Subtle Hero Editorial       │         │ - Live Area Broadcast Feed    │
│ - Radius & Category Filters   │         │ - Quick Intent Action Grid    │
│ - AI Intent Matches           │         │   (Need, Offer, Meetup)       │
│ - Neighbor Needs & Offers     │         └───────────────────────────────┘
└───────────────────────────────┘
```

---

### 2. Proposed Changes

#### A. Navigation Enum & ViewModel (`NearGridViewModel.kt`)
- Update `enum class NavigationDestination`:
  ```kotlin
  enum class NavigationDestination(val label: String) {
      HOME("Home"),
      MAP("Map"),
      RADAR("Mesh Radar"),
      COMMUNITY("Community"),
      INBOX("Inbox"),
      PROFILE("Profile")
  }
  ```

#### B. Dedicated Mesh Radar Screen (`MeshRadarScreen.kt`)
- Create `MeshRadarScreen.kt`:
  - Top header with local geohash code badge and location refresh button.
  - Hyperlocal Geohash Message Composer card with live broadcast feed.
  - Quick Intent Creation action grid (Numbered tiles: 01 Need, 02 Offer, 03 Meetup).

#### C. Home Screen Decluttering (`HomeScreen.kt`)
- Remove `GeohashMessageComposer` and Quick Intent Action Grid from `HomeScreen.kt`.
- Keep the subtle hero banner, radius selector, category chips, AI intent matches, and post list for a clean, spacious feed.

#### D. Bottom Navigation Bar Wiring (`MainActivity.kt`)
- Wire `NavigationDestination.RADAR -> Icons.Filled.Radar to Icons.Outlined.Radar`.
- Render `MeshRadarScreen` when `uiState.currentDestination == NavigationDestination.RADAR`.
- Keep `alwaysShowLabel = false`, `maxLines = 1`, `fontSize = 10.sp`, and `overflow = TextOverflow.Ellipsis` on all 6 items.

#### E. Unit Test Suite (`app/src/test/java/com/example/ExampleUnitTest.kt`)
- Update `testCategory08_NavigationBugs_verifiesNavigationDestinationsAndBackStackState` to assert all 6 destinations (`Home`, `Map`, `Mesh Radar`, `Community`, `Inbox`, `Profile`).

---

### 3. Verification & Completion Status

1. **Unit Testing Execution (`app/src/test/java/com/example/ExampleUnitTest.kt`) - PASSED**:
   - `testCategory08_NavigationBugs_verifiesNavigationDestinationsAndBackStackState`: Verified all 6 clean destinations (`Home`, `Map`, `Mesh Radar`, `Community`, `Inbox`, `Profile`).
   - Ran `gradle :app:testDebugUnitTest --tests "com.example.ExampleUnitTest"` -> **All 35 unit tests passed**.

2. **Compilation - PASSED**:
   - Executed `compile_applet` -> **Build succeeded - the applet is compiled**.

3. **Completed Improvements**:
   - [x] Updated `NavigationDestination` enum in `NearGridViewModel.kt` to include 6 distinct destinations (`HOME`, `MAP`, `RADAR`, `COMMUNITY`, `INBOX`, `PROFILE`).
   - [x] Created `MeshRadarScreen.kt` for dedicated Geohash Broadcast Area messaging, live area broadcasts, and quick intent creation tiles (Need, Offer, Meetup).
   - [x] Decluttered `HomeScreen.kt`, leaving a clean, spacious feed of AI matches, category filters, and neighbor posts.
   - [x] Updated `MainActivity.kt`'s bottom navigation bar with active-only single-line label formatting (`alwaysShowLabel = false`, `maxLines = 1`, `fontSize = 10.sp`) across all 6 navigation items.
