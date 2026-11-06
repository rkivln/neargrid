# NearGrid — Complete Feature Breakdown & Full-Stack Architecture

> **Purpose**: A comprehensive product and engineering blueprint of NearGrid, detailing every user flow, screen structure, state management pattern, and backend data model for redesigning or expanding the application with ChatGPT.

---

## 1. Product Overview & Core Philosophy

**NearGrid** is an Android hyper-local spatial community and mutual aid coordination platform built with Kotlin and Jetpack Compose. Unlike traditional social networks that rely on personal identities, friend graphs, or exact street addresses, NearGrid organizes all interactions into **privacy-preserving spatial geohash grid cells** (typically 300m to 10km).

### Core Pillars
1. **Zero Address Disclosure**: Coordinates are masked into discrete geohash zones and 3 privacy tiers (`EXACT`, `APPROXIMATE`, `NEIGHBORHOOD`).
2. **Mutual Aid & Intent Coordination**: Quick reciprocal actions (Need, Offer, Meetup, Civic Issue) matching nearby residents.
3. **Seamless Offline & Mesh Handoff**: Printable/sharable QR Grid Passes and CameraX scanner for immediate neighborhood joining.
4. **Clean Editorial UI**: Warm, welcoming Material 3 aesthetic pairing `DMSerifFontFamily` headers with readable `DMSansFontFamily` / `Manrope` typography.

---

## 2. 6-Tab Navigation & Screen Hierarchy

The app uses a 6-destination bottom navigation bar with single-line labels and smart active-state pill indicators:

```
┌────────────────────────────────────────────────────────────────────────┐
│                              NearGrid                                  │
├───────────┬─────────┬─────────────┬───────────┬──────────┬─────────────┤
│   Home    │   Map   │ Mesh Radar  │ Community │  Inbox   │   Profile   │
│  (Feed)   │ (Radar) │  (Intents)  │ (Civic)   │ (Direct) │ (Settings)  │
└───────────┴─────────┴─────────────┴───────────┴──────────┴─────────────┘
```

### 1. Home Tab (`HomeScreen.kt`)
- **Top Bar (`TopNeighborhoodBar.kt`)**: Minimalist header showing current locality name, geohash badge (`#GPS-LIVE`), and a collapsible quick actions toggle (GPS toggle, Live Map, Scan QR, Share Grid Pass).
- **Update Alert Banner (`UpdateBanner.kt`)**: Appears only when a newer GitHub release is detected; automatically and permanently disappears when the installed version is up to date.
- **Hero Card**: Community illustration with quick stats (nearby neighbors, active requests).
- **Discovery Radius Selector (`DiscoveryRadiusSelector.kt`)**: 300m (Hyperlocal), 1 km (Neighborhood), 3 km (District), 10 km (City).
- **Filter Chips**: All, Needs (`HelpOutline`), Offers (`VolunteerActivism`), Meetups (`Groups`), Issues (`Handyman`).
- **Post Feed (`PostCard.kt`)**: Displays author avatar preset, fuzzy distance (`< 50 m`, `350 m`, `1.4 km`), timestamp, intent category badge, upvote counter, and threaded comments trigger (`PostCommentsSheet.kt`).
- **FAB**: Quick-create modal for new neighborhood posts.

### 2. Map Tab (`RealtimeMapScreen.kt`)
- **Interactive OpenStreetMap Canvas**: Dynamic tile-rendered map showing current user pin, discovery radius circle, and neighborhood pins.
- **Privacy Obfuscation Layer**: Random offset radius applied to prevent pinpointing residential addresses.
- **Bottom Sheet Drawer**: Quick preview of posts originating within the visible viewport bounds.
- **Controls**: Recenter GPS, toggle satellite/standard layer, and adjust proximity radius.

### 3. Mesh Radar Tab (`MeshRadarScreen.kt`)
- **Hyperlocal Broadcast Panel**: View and broadcast ephemeral geohash messages to neighbors within the active cell.
- **Intent Creation Tiles**:
  - `01 Need Aid`: Ask for emergency help, tools, or supplies.
  - `02 Offer Resource`: Lend tools, offer spare food, or volunteer skills.
  - `03 Host Meetup`: Organize block parties, park cleanups, or safety walks.
- **Intent Matching Engine**: Real-time algorithmic card displaying automated matches between complementary needs and offers.

### 4. Community Tab (`CommunityScreen.kt`)
- **Municipal Bulletin & Civic Board**: Civic issue tracking (potholes, street lighting, lost pets, zoning notices).
- **Civic Voting**: Upvoting mechanism for prioritizing unresolved neighborhood grievances.
- **Resolution Tracking**: Status badges (`OPEN`, `IN_PROGRESS`, `RESOLVED`) with neighbor comments.

### 5. Inbox Tab (`InboxScreen.kt` & `ConversationChatView.kt`)
- **Direct Neighbor Messaging**: Real-time 1-on-1 conversations secured via Firestore rules.
- **Safe Exchange Protocols**: Integrated safety guidelines for in-person item handoffs.
- **Chat Actions**: In-line message editing, deletion, text copying, and media image attachments (Base64 JPEG).

### 6. Profile & Settings Tab (`ProfileScreen.kt`, `UserProfileScreen.kt`, `AppSettingsScreen.kt`)
- **Resident Identity**: Custom nickname, handle (`@username`), bio, and avatar preset selection.
- **Grid Pass Generator (`GridQrInviteScreen.kt`)**: Generates printable QR pass for neighbors to scan and join the exact cell.
- **3-Tier Privacy Controls**:
  - `EXACT`: Full GPS precision for trusted closed communities.
  - `APPROXIMATE`: 100m–300m randomized coordinate fuzzing.
  - `NEIGHBORHOOD`: Geohash centroid masking without coordinates.
- **Safety Settings**: Conceal online presence, hide exact distance meters, and blocked users management.
- **App Settings & Diagnostics**:
  - Theme palette customization (Standard, Forest, Ocean, Sunset).
  - Layout density adjustments.
  - Storage & Cache Meter: Visual memory breakdown with one-tap cache clearing.
  - Crash Telemetry: View local diagnostic logs and error reports.
  - Check for Updates: Manual trigger to check GitHub release updates.

---

## 3. Full-Stack Technical Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                           Presentation Layer                           │
│  Jetpack Compose M3 UI Screens & Components (State-Driven, Pure UI)    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Collects StateFlow / Triggers Events
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            ViewModel Layer                             │
│  NearGridViewModel (Coordinates UI State, Coroutines, Business Logic)  │
│   ├─ Device Location & Geohash Math (LocationHelper, GeohashUtil)      │
│   ├─ AppUpdateManager (Release Version Checker & Dismissal Logic)      │
│   └─ StorageCacheManager & CrashTelemetryManager                       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Calls Repositories / Flow Streams
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                              Data Layer                                │
│   ├─ Firebase Authentication (CredentialManager + Google Sign-In)      │
│   ├─ Firestore SDK (Real-time Snapshot Listeners & Scoped Queries)     │
│   ├─ Retrofit & Moshi (GitHub Releases API Client)                     │
│   └─ Android SharedPreferences (Settings, Dismissed Versions, Cache)   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. State Management & ViewModel Design

### `NearGridViewModel.kt`
- **Main State Flow (`uiState: StateFlow<NearGridUiState>`)**:
  - `currentLocation: UserLocation?` (Latitude, Longitude, Locality, DisplayArea)
  - `currentGeohash: String` (Standard Base32 Geohash, e.g., `tdr1v4s`)
  - `discoveryRadiusMeters: Int` (300, 1000, 3000, 10000)
  - `locationPrecision: LocationPrecision` (`EXACT`, `APPROXIMATE`, `NEIGHBORHOOD`)
  - `fuzzyCoordinateOffset: Boolean`
  - `hideExactDistance: Boolean`
  - `incognitoGridPresence: Boolean`
  - `blockedUserIds: Set<String>`
- **Real-Time Data Streams**:
  - `posts: StateFlow<List<NeighborPost>>` (Filtered by geohash prefix and blocked users)
  - `conversations: StateFlow<List<Conversation>>` (Filtered by participant UID)
  - `activeChatMessages: StateFlow<List<ChatMessage>>` (Real-time messages flow for active chat)
  - `civicIssues: StateFlow<List<CivicIssue>>` (Municipal complaints and resolutions)
  - `updateInfo: StateFlow<AppUpdateInfo>` (Latest release tag, APK URL, release notes)
  - `isUpdateBannerDismissed: StateFlow<Boolean>` (Synced with SharedPreferences)

---

## 5. Firebase Firestore Schema & Security Rules

### Collections Architecture

1. **`/posts/{postId}`**
   - `id`: String (Document ID)
   - `authorId`: String (Firebase Auth UID)
   - `authorName`: String
   - `content`: String
   - `intentType`: String (`NEED`, `OFFER`, `MEETUP`, `GENERAL`)
   - `geohash`: String (e.g. `tdr1v4s`)
   - `latitude`: Double
   - `longitude`: Double
   - `upvotes`: Long
   - `upvoterIds`: List<String>
   - `commentCount`: Long
   - `createdAt`: Timestamp
   - **Subcollection `/posts/{postId}/comments/{commentId}`**: Threaded neighbor replies.

2. **`/conversations/{conversationId}`**
   - `id`: String
   - `participantIds`: List<String> (Size = 2, contains both user UIDs)
   - `participantNames`: Map<String, String>
   - `lastMessage`: String
   - `lastMessageTimestamp`: Timestamp
   - **Subcollection `/conversations/{conversationId}/messages/{messageId}`**:
     - `senderId`: String
     - `text`: String
     - `mediaUrl`: String? (Base64 JPEG data URL)
     - `timestamp`: Timestamp
     - `isEdited`: Boolean

3. **`/civic_issues/{issueId}`**
   - `title`: String, `description`: String
   - `category`: String (`INFRASTRUCTURE`, `SAFETY`, `ENVIRONMENT`, `EVENT`)
   - `status`: String (`OPEN`, `IN_PROGRESS`, `RESOLVED`)
   - `upvotes`: Long, `upvoterIds`: List<String>
   - `geohash`: String, `createdAt`: Timestamp

4. **`/telemetry_logs/{logId}`**
   - Device model, app version, error message, stack trace snippet, timestamp.

### Security Rules Contract (`firestore.rules`)
- **Zero-Trust**: Read and write access strictly requires `request.auth != null`.
- **Author Guard**: `create` requires `request.auth.uid == request.resource.data.authorId`.
- **Edit/Delete Guard**: `update` and `delete` require `request.auth.uid == resource.data.authorId` (except for atomic upvote increments).
- **Conversation Isolation**: Only participants in `resource.data.participantIds` can read or write messages.

---

## 6. How to Use This in ChatGPT for Redesign

To redesign NearGrid with ChatGPT, you can provide the following prompt:

> "I have an existing Android Kotlin + Jetpack Compose app named NearGrid. It is a hyper-local spatial mutual aid and neighborhood mesh platform. Here is the full feature list, 6-tab navigation structure, ViewModel architecture, and Firestore data schema: [Paste sections 2, 3, 4, and 5 above]. 
> 
> Please help me redesign:
> 1. [Specify your focus: e.g., Visual aesthetic / Animation system / New feature / Refactored architecture]
> 2. Provide updated Jetpack Compose composables adhering to Material Design 3."
