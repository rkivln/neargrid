# NearGrid: 100% Real-Time Proximity & GPS-Driven Civic Mesh

NearGrid connects verified local neighbors through live Firestore synchronization and physical device GPS coordinates, eliminating all synthetic seed data. Every post, issue, and direct neighbor connection operates against real live data in your physical neighborhood or municipality.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Architectural Directives (From Clarifying Questions)**:
> 1. **Exact GPS Coordinates**: The app will capture and store exact device coordinates (`lat`, `lng`) when publishing posts or issues, calculating precise straight-line proximity (e.g., `120 m away`, `1.4 km away`) rather than coarse grid-cell approximations.
> 2. **Android Geocoder Locality Detection**: Rather than synthetic cell IDs like `#H3-8826 Oakwood Grove`, the app will query Android's native `Geocoder` to dynamically resolve your real neighborhood, sub-locality, street corridor, and city name directly from device coordinates.
> 3. **Clean Zero-State Onboarding**: If no neighbors have posted in your physical radius yet, the app presents a clean, welcoming empty state inviting you to be the pioneer neighbor and post the very first local request or offer.
> 4. **100% Mock Data Elimination**: Remove all synthetic Room database seeds (`seedInitialDataIfEmpty()`, mock Oakwood Grove items, hardcoded San Francisco coordinates) so the application runs entirely on live, authentic cloud data.

---

### 1. Overview & Core Concept

- **What It Does**: NearGrid transforms your actual physical location into a live civic mesh. Neighbors request aid, lend tools, report community infrastructure hazards, and coordinate safe public meetups using real-time Firestore synchronization.
- **Target Audience / Persona**: Local residents, neighborhood volunteers, and civic organizers who want trusted, hyper-local mutual aid grounded in real geographic proximity.
- **Key Value**: 100% authentic local signal. Every post is authored by an authenticated neighbor within your physical surroundings, with genuine distance measurements and real local neighborhood names.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Location Fix & Reverse Geocoding**: On launch, NearGrid requests location permission (`ACCESS_FINE_LOCATION`), queries the device's current GPS fix via `FusedLocationProviderClient`, and resolves the address via Android `Geocoder` (e.g., "Mission District, San Francisco" or "Indiranagar, Bengaluru").
  2. **Live Neighborhood Feed**: The board displays active posts ordered by physical proximity to the user's current GPS coordinates. Each card features an exact calculated distance pill (e.g., "85 m away", "450 m away").
  3. **Zero-State Discovery**: When opening the app in a new neighborhood or town with no existing posts, users see a prominent empty state card with localized badge: `"No posts yet in [Your Neighborhood]"` with an instant `[+ Create First Post]` action.
  4. **Publishing with Real GPS**: Creating a post or reporting an issue automatically attaches the device's real-time latitude, longitude, and geocoded locality name, syncing immediately to Firestore across all listening devices.
  5. **Live Neighbor Coordination**: Tapping "Connect" initiates a real-time Firestore conversation between authenticated Google users with safe public meeting point recommendations.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Utilitarian civic design with crisp Material 3 typography, high-contrast borders, and spacious card layouts.
  - *Colors & Atmosphere*:
    - Deep slate and warm terracotta accent (`#D95D39` / primary M3 container)
    - Emerald green badges for verified GPS precision and active neighbor status
    - Amber/Orange highlight borders for urgent mutual aid requests
  - *Typography*: Clean geometric sans-serif with tabular numeric figures for distance and coordinates.

- **Interactive Feedback & Empty States**:
  - Animated location acquisition indicator (pulsing GPS satellite chip) showing fix status.
  - Pull-to-refresh / location re-calibration button in the top status bar.
  - Clean vector illustrations for zero-state screens with encouraging copy.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Exact GPS Distance Calculation over Coarse Grid Buckets**
  - *Chosen Approach*: Store exact double precision `lat` and `lng` for each post, and compute real-time distance using `android.location.Location.distanceBetween()`.
  - *Why*: Delivers unambiguous distance indicators that reflect true walking distance between neighbors.
  - *Trade-Off*: Relies on device GPS availability, gracefully falling back to coarse network location if fine GPS is temporarily unavailable.

- **Decision 2: Native Android Geocoder for Dynamic Cell Naming**
  - *Chosen Approach*: Use Android's `Geocoder.getFromLocation()` with coroutine suspension to fetch `subLocality`, `thoroughfare`, or `locality`.
  - *Why*: Allows the app to work anywhere globally without hardcoding static neighborhood lists or paying for third-party mapping APIs.
  - *Alternatives Considered*: Hardcoded neighborhood polygons or fixed bounding boxes were rejected because they fail outside preset test cities.

- **Decision 3: Complete Removal of Synthetic Seed Data**
  - *Chosen Approach*: Strip out `seedInitialDataIfEmpty()`, remove synthetic fallback branches in ViewModels, and source all posts, issues, and conversations strictly from Firestore.
  - *Why*: Fulfills the explicit user mandate that the application run 100% on real-time data without simulated artifacts.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Device GPS & Sensors                             │
│                  (FusedLocationProviderClient + Geocoder)                   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Real Lat/Lng + Locality Name
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              LocationManager                                │
│        Provides StateFlow<LocationState> (Coords, Locality, Accuracy)       │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             NearGridViewModel                               │
│       • Computes dynamic distances (Location.distanceBetween)               │
│       • Subscribes to real-time Firestore collections (posts, issues)       │
│       • Zero mock fallbacks: empty list yields genuine zero-state           │
└──────────────────────┬───────────────────────────────┬──────────────────────┘
                       │                               │
                       ▼                               ▼
       ┌───────────────────────────────┐ ┌───────────────────────────┐
       │      FirestoreRepository      │ │       UI Composables      │
       │   • posts (real coords & loc) │ │  • NeighborhoodBoard      │
       │   • issues (hazard reports)   │ │  • Dynamic Proximity Feed │
       │   • conversations (1:1 chat)  │ │  • Clean First-Post Empty │
       └───────────────────────────────┘ └───────────────────────────┘
```

#### Core Data Entities & GPS Schema
- **NeighborPost**:
  - `lat`: Double (Device GPS latitude)
  - `lng`: Double (Device GPS longitude)
  - `cellName`: String (Dynamically geocoded neighborhood/city, e.g. "Mission District")
  - `approxDistanceBand`: String (Dynamically computed distance formatted as "150 m away")
  - `userId`, `authorName`, `title`, `description`, `category`, `urgency`, `status`, `createdAt`
- **LocationState**:
  - `latitude`: Double?
  - `longitude`: Double?
  - `localityName`: String ("Locating..." -> "Mission District, San Francisco")
  - `isAccurate`: Boolean
  - `permissionGranted`: Boolean

#### Implementation Steps Post-Approval
1. **Location Helper Service (`LocationHelper.kt`)**: Implement reactive location observer leveraging `FusedLocationProviderClient` with `Geocoder` reverse geocoding on `Dispatchers.IO`.
2. **Purge Synthetic Data**: Remove `seedInitialDataIfEmpty` and mock posts from `NearGridRepository.kt` and `NearGridViewModel.kt`.
3. **Dynamic Distance Sorting**: Hook up real-time distance calculations in `NearGridViewModel` to sort posts by proximity to the device.
4. **Clean Empty States**: Add high-polish zero-state UI in `HomeScreen.kt`, `LivingGridScreen.kt`, and `CommunityScreen.kt` when no nearby posts exist.
5. **Compilation & Verification**: Verify build passes cleanly via `compile_applet`.
