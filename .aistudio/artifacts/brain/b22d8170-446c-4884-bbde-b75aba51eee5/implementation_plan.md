# Implementation Plan: Cross-Device Post Media Attachment Fix (Completed)

Fixed post image/video media visibility across devices by compressing attached local content URIs to 1024px Base64 Data URLs (`data:image/jpeg;base64,...`) before storing to Firestore, allowing Coil on any user's device to instantly render uploaded post media.

---

## Status: COMPLETE

- [x] **1. Media Encoding Helper (`MediaUploadHelper.kt`)**: Created downscaling (1024px max dimension) and JPEG 80% compression pipeline converting local `content://` URIs to portable `data:image/jpeg;base64,...` Data URLs.
- [x] **2. Post & Issue Creation (`NearGridViewModel.kt`)**: Updated `createNeed`, `createOffer`, `createIssue`, and `createMeetup` to process media attachments through `MediaUploadHelper.processMediaUris()` before saving to Firestore.
- [x] **3. Media Carousel & Render Polish (`PostMediaCarousel.kt`)**: Configured Coil `AsyncImage` error fallback painter to `R.drawable.img_neighborhood_connection` for clean rendering.
- [x] **4. Verification**: Added unit test `mediaUploadHelper_formatsBase64DataUrlCorrectly` passing all 9 unit tests and confirmed successful build via `compile_applet`.

---

## 2. Integration in Post & Issue Creation (`NearGridViewModel.kt`)
- Update `createNeedPost()`, `createOfferPost()`, `createMeetupPost()`, and `reportIssue()` in `NearGridViewModel`:
  - Process attached `mediaUris` through `MediaUploadHelper.processMediaUris(getApplication(), mediaUris)` before writing to `firestoreRepository`.
  - Save the resulting portable Base64 Data URLs in the post document in Firestore.

---

## 3. Media Carousel Component Polish (`PostMediaCarousel.kt`)
- Verify Coil `AsyncImage` configuration in `PostMediaCarousel.kt` and `PostCard` to render Base64 Data URLs smoothly.
- Ensure error states display a clean, elegant image fallback rather than an empty dark container.

---

## 4. Verification Plan
- Add unit test in `ExampleUnitTest.kt` verifying bitmap scaling calculation and data URI formatting logic.
- Verify project compilation using `compile_applet`.
