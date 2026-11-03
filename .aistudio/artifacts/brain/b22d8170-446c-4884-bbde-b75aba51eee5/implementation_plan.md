# Implementation Plan: Persistent App Update Banner Dismissal (Completed)

Ensured the app update banner is displayed only when a new release is actually found and has not been previously dismissed by the user for that specific release version.

---

## Status: COMPLETE

- [x] **1. Dismissed Version Tracking (`AppUpdateManager.kt`)**: Added `lastDismissedVersion` tracking to `AppUpdateManager`. When `checkForUpdate()` runs, if `latestVersion == lastDismissedVersion`, `_isBannerDismissed` is set to `true` automatically.
- [x] **2. ViewModel Integration (`NearGridViewModel.kt`)**: Connected `dismissUpdateBanner()` to pass the current release tag to `AppUpdateManager.dismissBanner(latestVer)`. Configured manual update checks (`checkForUpdates(isManual = true)`) to reset the dismissal state when explicitly requested from Profile settings.
- [x] **3. UI Banner Display (`UpdateBanner.kt` & `HomeScreen.kt`)**: Ensured update banner visibility evaluates `updateInfo?.isUpdateAvailable == true && !isDismissed`.
- [x] **4. Verification**: Added unit test `appUpdateManager_remembersDismissedVersion` passing all 11 unit tests and verified successful compilation via `compile_applet`.

---

## 2. Manual Update Check Behavior (`NearGridViewModel.kt`)
- In `checkForUpdates(isManual: Boolean)`:
  - When `isManual = true` (triggered from Profile settings), invoke `AppUpdateManager.instance.resetBannerDismissed()` so the latest release details and banner appear if an update is available.

---

## 3. UI Banner Visibility (`UpdateBanner.kt` & `HomeScreen.kt`)
- Ensure `UpdateBanner` visibility condition strictly checks:
  `updateInfo?.isUpdateAvailable == true && !isDismissed`
- Pass `AppUpdateManager.instance` dismissal flow consistently through `NearGridViewModel`.

---

## 4. Verification Plan
- Add unit test verifying that dismissing version `X` keeps `isBannerDismissed = true` until version `Y` (where `Y > X`) is detected.
- Verify project compilation using `compile_applet`.
