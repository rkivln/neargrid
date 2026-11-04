# Implementation Plan: Strict Coordinate-Based Geolocation & Geohash Isolation

Eliminate cross-city content leakage (e.g., Chennai vs Puducherry ~150km) and enforce strict coordinate-based isolation for all Community Posts, Broadcasts, and Neighborhood Grids.

---

## 1. Robust GPS Location Engine (`LocationHelper.kt`) [COMPLETED]
- **Multi-Source Location Resolution**:
  - Enhanced `LocationHelper.getFreshDeviceLocation()`:
    1. Checks `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)`.
    2. Falls back to `FusedLocationProviderClient.lastLocation`.
    3. Falls back to standard Android system `LocationManager` with `GPS_PROVIDER`, `NETWORK_PROVIDER`, and `PASSIVE_PROVIDER` so devices always obtain genuine physical coordinates.
    4. Explicitly rejects `(0.0, 0.0)` coordinate fixes.
- **Accurate Geohash Encoding & Distance**:
  - Calculates Haversine distance in meters accurately via `LocationHelper.calculateDistanceMeters`.
  - Ensures geohash calculations use uniform lowercase base32 format with sanitized `#` stripping in query prefixes.

---

## 2. Guarded Firestore Real-Time Queries (`FirestoreRepository.kt` & `NearGridViewModel.kt`) [COMPLETED]
- **No Unrestricted Queries**:
  - `observeGeohashMessages`: Returns empty flow immediately when `geohash` is null or blank. Never queries whole database without geohash bounds.
  - Queries Firestore with clean 4-character geohash prefix range (`whereGreaterThanOrEqualTo("geohash", prefix).whereLessThanOrEqualTo("geohash", prefix + "\uf8ff")`).
  - `observePosts`: Rejects empty `geohashPrefix` and `cellId` to ensure unlocated devices do not trigger collection-wide dumps.
- **Sanitized Data Storage**:
  - `postGeohashMessage`: Stores lowercase sanitized geohash without leading `#`.

---

## 3. Strict Coordinate Distance Filtering (`NearGridViewModel.kt`) [COMPLETED]
- **Strict Haversine Distance Check for Posts**:
  - Updated `filteredPosts`: Requires actual physical distance check `LocationHelper.calculateDistanceMeters(userLat, userLng, post.lat, post.lng) <= discoveryRadiusMeters` (default 1,000m, max 10,000m).
  - Removed loose fallback cell name/ID matching that allowed generic cells to cross city boundaries.
- **Strict Distance & Prefix Check for Broadcasts**:
  - In `NearGridViewModel.kt`, combined `geohashMessages` with user location and radius. Rejects all broadcasts where physical distance exceeds discovery radius or where geohash prefixes diverge.
- **GPS-Required Creation**:
  - `createNeed` and `createOffer` require verified non-zero GPS lock before saving.

---

## 4. UI GPS Lock Enforcement (`GeohashMessageComposer.kt`) [COMPLETED]
- **Disable Broadcast Posting Without Valid GPS**:
  - Removed hardcoded `"9q8yyk8"` and San Francisco `(37.7749, -122.4194)` fallback coordinates.
  - Added live GPS lock check (`latitude != 0.0 && longitude != 0.0`).
  - When GPS fix is pending, disabled broadcast button with indicator: "Acquiring GPS fix to broadcast...".
- **Real-Time Live Distance Badges**:
  - `GeohashMessageItem` displays live physical distance (e.g. `85 m away`, `1.2 km away`) based on relative user coordinates.

---

## 5. Verification [COMPLETED]
- **Unit Tests (`ExampleUnitTest.kt`)**:
  - `coordinateFiltering_rejectsChennaiPostFromPuducherry`: Validated that Puducherry coordinates (11.9416, 79.8083) and Chennai coordinates (13.0827, 80.2707) are > 130 km apart and rejected by the 10 km discovery radius and geohash prefix.
  - `coordinateFiltering_acceptsNearbyLocalPost`: Validated that nearby neighbors within 200m share geohash prefixes and are accepted within discovery radius.
- **Applet Compilation**:
  - Ran `compile_applet` to verify clean build and compatibility.
