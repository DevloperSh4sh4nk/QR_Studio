# QR Studio 📱✨

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose** for instant QR code scanning, custom brand QR code generation, and persistent local history powered by **Room Database**.

---

## 🌟 Features

### 🔍 1. Real-Time QR & Barcode Scanner
- **Live Camera Scanning**: High-speed QR scanning powered by CameraX and ZXing.
- **Smart Payload Parser**: Automatically recognizes and parses:
  - 🌐 **Web URLs** (with 1-tap browser launch)
  - 📶 **Wi-Fi Credentials** (SSID, Password, WPA/WEP/Open encryption)
  - 👤 **Contact Cards (vCard)** (Name, Phone, Email, Organization)
  - 📧 **Emails** (Recipient, Subject, Body)
  - 📞 **Phone Numbers** & 💬 **SMS Messages**
  - 🔗 **Social Media Links** (GitHub, Twitter/X, Instagram, LinkedIn, YouTube, etc.)
- **Flashlight / Torch Toggle**: Clear scanning even in low-light environments.
- **Camera Lens Switching**: Support for back and front-facing cameras.
- **Scan from Gallery**: Pick existing images or screenshots from device storage to decode QR codes.

### 🎨 2. Custom Brand QR Code Generator
- **Multi-Format Creator**: Generate QR codes for URLs, Plain Text, Wi-Fi networks, vCards, Emails, Phone calls, SMS, and Social handles.
- **Branded Design System**:
  - **Color Themes & Gradients**: Solid brand colors, dual-tone linear, radial, and diagonal gradients.
  - **Dot & Module Patterns**: Choose between *Square*, *Rounded*, *Dots*, and *Squircle* matrix styles.
  - **Finder Eye Styling**: Customizable corner finder frames (*Square*, *Rounded*, *Circle*).
  - **Center Logo Embedding**: Choose from built-in vector presets (Tech, Web, Shield, Star, Heart, Shop, Code, Briefcase) or upload custom brand logos from your device photo library.
  - **Brand Header & Subtitle Banners**: Add custom text overlays above or below the QR code for professional print materials.
- **Live Canvas Preview**: Real-time rendering with interactive light/dark canvas toggling.
- **Export & Share**: Save high-resolution PNGs directly to the photo gallery or share across apps via the Android Sharesheet.

### 🗄️ 3. Local Room Database History
- **Offline-First Persistence**: Scanned and generated QR codes are automatically stored in an on-device SQLite database via **Android Jetpack Room**.
- **Search & Filtering**: Instant search across all saved records with quick-filter chips (*All*, *Generated*, *Scanned*, *Starred*).
- **Favorites / Starred**: Bookmark important QR codes for quick access.
- **Studio Re-Load**: Re-open any historical QR code directly in the generator studio with 1 tap to modify styling or regenerate.
- **Management Actions**: Copy raw payload, share record, or delete items.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Programming Language**: [Kotlin](https://kotlinlang.org/) (Coroutines, Flow, StateFlow)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with Kotlin Symbol Processing (KSP)
- **Camera & Scanning**: [CameraX](https://developer.android.com/training/camerax) + [ZXing Core](https://github.com/zxing/zxing)
- **QR Graphics Engine**: Custom multi-layer bitmap generator with Canvas rendering for embedded logos, gradients, and custom module shapes
- **Testing**: Local JVM unit tests using [Robolectric](https://robolectric.org/)

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Application entry point & edge-to-edge setup
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Room Database definition
│   │   ├── dao/
│   │   │   └── QrRecordDao.kt       # Reactive queries with Kotlin Flow
│   │   └── entity/
│   │       └── QrRecord.kt          # Room entity model for QR history
│   └── repository/
│       └── QrRepository.kt          # Data repository layer
├── qr/
│   ├── QrCodeGenerator.kt           # Custom bitmap drawing, gradients & styling
│   ├── QrCodeDecoder.kt             # Gallery image bitmap decoding
│   ├── QrPayloadParser.kt           # Wi-Fi, vCard, URL, SMS & Email parser
│   └── QrStyleConfig.kt             # Design presets & branding models
└── ui/
    ├── components/
    │   ├── CameraScannerView.kt     # CameraX viewfinder with animated scanline
    │   ├── QrCodePreviewCard.kt     # Live QR preview with export & copy actions
    │   └── ScanResultDialog.kt      # Interactive bottom sheet for scanned results
    ├── screens/
    │   ├── GeneratorScreen.kt       # QR studio with multi-tab inputs & styling
    │   ├── HistoryScreen.kt         # Room database history list, search & filters
    │   ├── MainScreen.kt            # Navigation bar & scaffold container
    │   ├── PresetsScreen.kt         # One-tap starter templates for rapid design
    │   └── ScannerScreen.kt         # Camera viewfinder & gallery picker
    ├── theme/
    │   ├── Color.kt                 # Material 3 color palettes
    │   ├── Theme.kt                 # Dynamic dark/light theme composable
    │   └── Type.kt                  # Typography system
    └── viewmodel/
        ├── QrViewModel.kt           # Central ViewModel coordinating UI & database
        └── QrViewModelFactory.kt    # Dependency injection factory for ViewModel
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer
- **JDK**: Java Development Kit 17 or higher
- **Android SDK**: Compile SDK 35, Minimum SDK 26

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/qr-studio.git
   cd qr-studio
   ```

2. Open the project in Android Studio.

3. Sync Gradle and build the debug APK:
   ```bash
   gradle assembleDebug
   ```

4. Run local unit tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🔒 Privacy & Permissions

- **Camera Permission (`android.permission.CAMERA`)**: Used exclusively for real-time QR code scanning on-device.
- **100% Offline-First**: All QR code generation and scanning happens locally on your device. No QR payloads, images, or history records are sent to any external servers.

---

## 📄 License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
