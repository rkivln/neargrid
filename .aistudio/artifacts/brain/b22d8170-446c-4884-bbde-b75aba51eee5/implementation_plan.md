# Implementation Plan: In-App Version Update Detection via GitHub Releases (Completed)

Automated checking for new versions via GitHub Releases API (`rkivln/neargrid`), showing a dismissible update banner on the Home screen and a dedicated version check & update card in User Profile settings.

---

## Status: COMPLETE

- [x] **1. AppUpdateService (`AppUpdateService.kt`)**: Implemented GitHub Releases API parser (`rkivln/neargrid`) and semantic version comparison (`isNewerVersion`).
- [x] **2. ViewModel Integration (`NearGridViewModel.kt`)**: Added background check on app launch, `updateInfo` and `isUpdateBannerDismissed` StateFlows, and manual check function.
- [x] **3. Dismissible Home Screen Banner (`HomeScreen.kt` & `AppUpdateBanner`)**: Added top alert banner showing latest version number, direct "Update APK" action, release notes link, and dismiss button.
- [x] **4. User Profile Version & Updates Section (`UserProfileScreen.kt`)**: Added Section 6 showing installed version, up-to-date status or new release details, manual "Check for Updates" button, and direct APK download button.
- [x] **5. Verification**: Added unit tests in `ExampleUnitTest.kt` verifying version comparison algorithms; confirmed `compile_applet` succeeds cleanly.

### B. App Update State & Repository Integration
- Add `AppUpdateInfo` data class:
  ```kotlin
  data class AppUpdateInfo(
      val currentVersion: String,
      val latestVersion: String,
      val releaseNotes: String,
      val apkDownloadUrl: String,
      val releaseUrl: String,
      val isUpdateAvailable: Boolean,
      val isChecking: Boolean = false,
      val checkError: String? = null
  )
  ```
- Expose `updateInfo` StateFlow in `NearGridViewModel`.
- Support:
  - Automatic silent background check on app launch.
  - Manual "Check for updates" trigger.
  - Dismissing the home banner for the current session.

---

## 2. User Interface Enhancements

### A. Dismissible Home Screen Banner (`HomeScreen.kt`)
- When `updateInfo.isUpdateAvailable` is true and not dismissed:
  - Display a clean Material 3 banner at the top of the feed:
    - Chip icon (e.g., `Icons.Default.SystemUpdate`)
    - Text: "Update available: v{latestVersion} — Tap to install the latest features"
    - "Update" button: triggers `Intent.ACTION_VIEW` on the direct APK download URL.
    - "Dismiss" close icon to hide the banner for the active session.

### B. Dedicated Update Section in Profile (`UserProfileScreen.kt`)
- Add a new "App Version & Updates" settings section in User Profile:
  - Displays: Current Version (`v1.0`) and Installed Build.
  - If updated: displays green checkmark badge "NearGrid is up to date".
  - If update available: highlights "New version v{latestVersion} available!" with release notes summary and a primary "Download & Install Update" button.
  - Manual "Check for Updates" button with loading spinner indicator.

---

## 3. Verification Plan
- Unit tests for version comparison logic (e.g., comparing `v1.0.3` against `1.0`, handling suffixes, malformed tags).
- Mock/test parsing GitHub Releases response with Moshi.
- Run `compile_applet` to verify compilation.
