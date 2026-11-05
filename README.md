# NearGrid 

> **Hyper-Local Neighborhood Mesh & Spatial Community Coordination for Android**

NearGrid is a modern, privacy-first Android application that connects nearby neighbors through customizable spatial geohash grids. Residents can broadcast local needs, offer mutual aid, report community issues, organize local meetups, and invite neighbors using QR code passes—all while maintaining precise coordinate privacy.

---

## ✨ Key Features & Architecture

- 📍 **6-Tab Ergonomic Navigation**: Clean separation into **Home**, **Map**, **Mesh Radar**, **Community**, **Inbox**, and **Profile** tabs.
- 📡 **Dedicated Mesh Radar Tab**: Hyperlocal geohash area broadcasts and quick intent action tiles (Ask Need, Offer/Lend, Host Meetup).
- 🗺️ **Neighborhood Map & Radar**: Spatial discovery with 3-tier location precision (`EXACT`, `APPROXIMATE`, `NEIGHBORHOOD`), online status concealment, and distance masking.
- 🤝 **Mutual Aid & Community Board**: Post urgent needs, share surplus resources/offers, track community issues with upvotes, and organize local meetups.
- 🏷️ **Grid Pass & QR Invitations**: Generate custom printable resident badges and high-contrast QR codes to invite neighbors to your local grid cell.
- 📷 **Instant QR Scanner**: Scan a neighbor's QR code via CameraX to instantly join their local geohash grid.
- 💬 **Real-Time Neighborhood Messaging**: Powered by Firebase Firestore for instant live broadcasts and direct neighbor conversations.
- ⚙️ **Production Settings & Telemetry**: Theme accent palettes, layout density, storage usage meter, and silent Firestore telemetry logging.
- 🎨 **Material 3 UI**: Clean editorial design with `DMSerifFontFamily` headers, smooth Jetpack Compose transitions, and responsive layouts.

---

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Kotlin Coroutines & Flow
- **Backend & Persistence**: Firebase Firestore & Firebase Authentication (Google Sign-In)
- **Camera & Hardware**: Android CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Testing**: Robolectric & JUnit local JVM testing suite covering all 11 bug categories (UI, Functional, Performance, Crash, Network, Database, Security, Navigation, Compatibility, Battery, Memory)
- **Build System**: Gradle (Kotlin DSL `.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 35 (Minimum SDK 26)

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/NearGrid.git
   cd NearGrid
   ```

2. Open the project in **Android Studio**.

3. Build and run on a device or emulator:
   ```bash
   gradle assembleDebug
   ```

---

## 🧪 Running Tests

To run the comprehensive local Robolectric unit test suite:
```bash
gradle :app:testDebugUnitTest
```

---

## 📱 Publishing & GitHub Releases

To host APK releases on GitHub:
1. Generate an APK in Android Studio via `Build -> Build APK(s)`.
2. Navigate to your repository's **Releases** page on GitHub.
3. Draft a new release (e.g., `v1.0.0`) and upload the `.apk` file.

---

## 🔒 Privacy & Safety

NearGrid is designed around spatial privacy:
- Exact latitude and longitude coordinates are masked into geohash zones.
- Direct messaging and proximity presence can be toggled on or off anytime in settings.
- Blocked user management and online status concealment.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
