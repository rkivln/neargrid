# NearGrid 🌐🏠

> **Hyper-Local Neighborhood Mesh & Spatial Community Coordination for Android**

NearGrid is a modern, privacy-first Android application that connects nearby neighbors through customizable spatial geohash grids. Residents can broadcast local needs, offer mutual aid, report community issues, organize local meetups, and invite neighbors using QR code passes—all while maintaining precise coordinate privacy.

---

## ✨ Features

- 📍 **Geohash Spatial Grids**: Automatically groups nearby activity based on device GPS or selectable neighborhood cells without revealing exact home addresses.
- 🤝 **Mutual Aid & Community Board**: Post urgent needs, share surplus resources/offers, track community issues with upvotes, and organize local meetups.
- 🏷️ **Grid Pass & QR Invitations**: Generate custom printable resident badges and high-contrast QR codes to invite neighbors to your local grid cell.
- 📷 **Instant QR Scanner**: Scan a neighbor's QR code via CameraX to instantly join their local geohash grid.
- 🛡️ **Privacy-Preserving Radius**: Adjust discovery radius (300m, 1000m, 3000m, 10km) and toggle fuzzy coordinate offsets or incognito mode.
- 💬 **Real-Time Neighborhood Messaging**: Powered by Firebase Firestore for instant live broadcasts and direct neighbor conversations.
- 🎨 **Material 3 UI**: Clean, warm editorial design with smooth Jetpack Compose transitions and responsive layouts.

---

## 🛠️ Built With

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Kotlin Coroutines & Flow
- **Backend & Persistence**: Firebase Firestore & Firebase Authentication (Google Sign-In)
- **Camera & Hardware**: Android CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
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

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
