# Implementation Plan: Post Creation with Photos and Videos

We will add full support for attaching photos and videos to posts (Needs, Offers, Community Issues, and Meetups) using Android's zero-permission Photo Picker (`PickMultipleVisualMedia`), along with an interactive media carousel on post cards in the feed.

## User Requirements & Choices
- **Selection Method**: Android Photo Picker (`ActivityResultContracts.PickMultipleVisualMedia`) for safe, zero-permission gallery photo and video selection.
- **Feed Display**: Interactive horizontal media carousel preview with Coil for photos and video preview indicators on post cards.

---

## Technical Strategy

### 1. Data Model Enhancements
- **`NeighborPost.kt`**: Add `mediaUris: List<String> = emptyList()` and `mediaTypes: List<String> = emptyList()` (e.g. `"IMAGE"`, `"VIDEO"`).
- **`CommunityIssue.kt`**: Add `mediaUris: List<String> = emptyList()` and `mediaTypes: List<String> = emptyList()`.

### 2. ViewModel & Repository Updates (`NearGridViewModel.kt`)
- Update `createNeed`, `createOffer`, `createIssue`, and `createMeetup` functions to receive attached media URIs/types.
- Pass media arrays when saving documents to Firestore and local Room database repository.

### 3. Media Picker & Creation UI (`CreateIntentSheet.kt`)
- Add `rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia())` launcher for selecting up to 5 photos/videos.
- Display a horizontal scrollable thumbnail row with badges (image vs video indicator) and delete ("X") buttons to manage attachments before publishing.

### 4. Interactive Media Carousel Component (`PostMediaCarousel.kt`)
- Create a dedicated Composable `PostMediaCarousel` rendered via `HorizontalPager` or `LazyRow`.
- Render images using Coil's `AsyncImage` with rounded corners and aspect ratio scaling.
- Render videos with thumbnail preview, duration/video badge, and full media viewer modal on tap.

### 5. Feed Integration (`PostCard.kt` & `CommunityScreen.kt`)
- Insert `PostMediaCarousel` directly inside `PostCard` for home feed, search, and neighborhood cell views.
- Display media attachments on community issue cards.

---

## Verification Plan
1. Execute `compile_applet` to ensure zero compilation or build errors.
2. Verify Kotlin DSL dependencies and Material 3 design alignment.
