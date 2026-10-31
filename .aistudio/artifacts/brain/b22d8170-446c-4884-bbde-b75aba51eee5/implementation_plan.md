# Implementation Plan: Redefine QR Code Mechanism to Offline Structured Payload (Completed)

Replaced non-existent external URLs (`neargrid.com` / `neargrid.app`) with a pure offline structured text payload that encodes the cell geohash code, location area name, and GPS coordinates, enabling instant offline scanning and cell joining within NearGrid.

---

## Status: COMPLETE

- [x] **1. Dedicated QR Code Utility (`QrCodePayloadHelper.kt`)**: Implemented standardized offline protocol `NEARGRID:v1;code=<geohash>;name=<area_name>;lat=<lat>;lng=<lng>` with bi-directional parser and share message builder.
- [x] **2. `GeohashQrInviteDialog.kt`**: Replaced dead web URLs with `QrCodePayloadHelper.buildPayload(...)` for the canvas and `buildShareMessage(...)` for clipboard & sharing.
- [x] **3. `GridQrInviteScreen.kt`**: Updated full-screen grid pass canvas, share actions, and resident badge copy to use the offline structured payload.
- [x] **4. `QrCodeScannerDialog.kt`**: Added dynamic parsing of offline passes with live area name and geohash code previews.
- [x] **5. `NearGridViewModel.kt`**: Updated `joinGridCellByCode` to decode `NEARGRID:v1` passes, accurately populating cell name, coordinates, and geohash.
- [x] **6. Verification**: Verified encoding, decoding, and fallback handling via unit tests, followed by successful `compile_applet` compilation.

---

## 2. Changes to Proposed Modules

### A. Dedicated QR Code Utility (`QrCodePayloadHelper.kt`)
- Create `com.example.util.QrCodePayloadHelper`:
  - `buildPayload(code: String, name: String, lat: Double, lng: Double): String`
  - `parsePayload(raw: String): ParsedGridInvite` with data class `ParsedGridInvite(code: String, name: String?, lat: Double?, lng: Double?)`
  - Helper to build clean shareable text for clipboard and Android Share Sheet without fake web links.

### B. `GeohashQrInviteDialog.kt`
- Replace `https://neargrid.app/join?cell=...` with `QrCodePayloadHelper.buildPayload(...)`.
- Update dialog copy to clarify: "Offline Grid Pass: Neighbors can scan this QR code with NearGrid to join directly without internet."
- Update Share and Copy actions to format formatted offline pass code and cell details.

### C. `GridQrInviteScreen.kt`
- Update the hero resident pass canvas to render the structured offline payload.
- Replace external URL references in the UI descriptions and instructions.
- Update Share Intent text to send the offline grid pass code and mesh instructions.

### D. `QrCodeScannerDialog.kt` & `NearGridViewModel.kt`
- Update scanner detection label and parsing logic in `QrCodeScannerDialog`:
  - Recognizes `NEARGRID:v1` payloads instantly and previews the extracted area name and geohash.
- Update `joinGridCellByCode` (or add `joinGridCellFromPayload`) in `NearGridViewModel`:
  - Extracts code, name, lat, and lng.
  - Finds existing cell or creates a new `GridCell` populated with the decoded name, geohash, and exact GPS coordinates.
  - Automatically switches the selected grid cell and dismisses the scanner.

---

## 3. Verification Plan
- Run unit/Robolectric tests (`gradle :app:testDebugUnitTest`) verifying payload serialization and parsing.
- Run `compile_applet` to confirm zero compilation errors.
- Test round-trip: generating QR payload for a cell and parsing it back in the scanner.
