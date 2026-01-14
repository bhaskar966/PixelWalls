# PixelWalls

**Cross-Platform Creative Wallpaper Studio (Android, iOS & Desktop)**

PixelWalls brings a “Pixel-like” creative wallpaper experience to more devices using **Kotlin Multiplatform** + **Compose Multiplatform**. It includes a 3D pop-out wallpaper editor and an optional AI wallpaper generator, with a unified gallery to manage creations.

> Built for **KotlinConf Kotlin Multiplatform Contest 2026**

---

## 📹 Demo

**Demo video (Android + iOS + Desktop):**

![PixelWalls Demo](https://bhaskar-dump-files.fra1.cdn.digitaloceanspaces.com/PixelWallsFramThumb.png)


Watch the video here: **[Video Link](https://www.youtube.com/watch?v=DVE64bHyx7Y)**

---

## ✨ Features

### 🎨 3D Pop‑Out Editor
Create wallpapers where the subject appears to “pop out” of a shape with depth.

- Intelligent subject segmentation (platform-optimized)
- Material-style shapes (circle, rounded square, squircle, pill, etc.)
- Fine-grained controls: position, scale, pop-out intensity, and colors
- Real-time preview with gestures (pan/zoom)

### 🤖 AI Wallpaper Generator (Gemini)
Generate wallpapers from text prompts (AI feature is optional).

- Prompt templates to help get consistent results
- Interactive prompt variables (tap to swap styles/keywords)
- “Surprise me” randomization
- Dynamic aspect ratios: 9:16 for phones, 16:9 for desktop

### 🖼️ My Creations Gallery
Manage wallpapers created inside the app.

- Cross-platform storage (native picture/document locations)
- Actions: share, locate file, apply/set wallpaper (where supported)

---

## 💡 Motivation

I’m a phone customization enthusiast (and a final-year Computer Science & Engineering student) who has spent years personalizing Android devices. When Google introduced a new wallpaper editor and AI wallpaper features (mostly limited to Pixel devices), the idea was simple: why not make that creative experience available to more users?

Then Kotlin Multiplatform made the goal even more interesting! why stop at Android when the same experience can be built for Android, iOS, and Desktop with a shared codebase?

---

## 🏗 Architecture & Tech Stack

PixelWalls shares around **90–92%** of the codebase across Android, iOS, and Desktop.

| Category | Library / Tool | Purpose |
|---|---|---|
| UI | [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)| Shared UI across Android, iOS, Desktop |
| Architecture | MVVM + MVI | UDF-style state management with shared ViewModels |
| DI | [Koin](https://insert-koin.io/) | Dependency management |
| Networking | [Ktor](https://github.com/ktorio/ktor) | Gemini API requests |
| Serialization | [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON parsing |
| Image Loading | [Coil 3](https://github.com/coil-kt/coil) | Async image loading/caching |
| Color Picker | [colorpicker-compose](https://github.com/skydoves/colorpicker-compose) | Color wheel + image color picker |
| File Handling | [FileKit](https://github.com/vinceglb/FileKit) | Cross-platform file pick/save |
| Config | [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) | Inject config like API keys from `local.properties` |
| Desktop ML | [ONNX Runtime](https://github.com/microsoft/onnxruntime) | Background removal on JVM (u2netp model) |
| Android ML | [ML Kit](https://developers.google.com/ml-kit) | On-device subject segmentation |

---

## 🔧 Platform Implementation Notes

PixelWalls uses `expect/actual` where platform-specific behavior is needed.

### Background removal
- Android: ML Kit subject segmentation
- Desktop (JVM): ONNX Runtime (u2netp)
- iOS: placeholder (planned CoreML / Vision integration; not fully tested due to hardware constraints)

### Wallpaper setting
- Android: `WallpaperManager` (Home Screen, Lock Screen and  Both)
- macOS: AppleScript
- Windows: JNA calling `SystemParametersInfo` (`user32.dll`)
- Linux: commands for GNOME / KDE Plasma / XFCE (currently untested)
- iOS: saves to Photos and shows instructions (iOS restrictions)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (recent version recommended)
- JDK 17+
- Xcode (for iOS)
- Gemini API key (optional; image generation is a **paid** service)

### Clone
```bash
git clone https://github.com/bhaskar966/PixelWalls.git
cd PixelWalls
```

### Configure Gemini (Optional)
- Create local.properties in the project root:
```text
GEMINI_API_KEY=your_api_key_here
```

> **You must have a pay as you go project to generate images.**


> If the key is missing, the project still builds and runs; the AI feature will show as unavailable in the UI while other features work.

### Run
- After Gradle sync and project build, click Run in Android Studio.
- Android Studio should automatically generate run configurations for available targets (Android / Desktop / iOS) if your environment is set up correctly.
- To run on a different platform, switch the run configuration from the dropdown and run again.
- If any configuration is missing, you can add a Gradle run configuration manually from **Run → Edit Configurations**.

---

## 🚀 Technical Challenges

### Composable pop‑out effect
The main challenge was layering shapes, subject, and background in a way that looks 3D while keeping movements synchronized (background and subject move together) even with a solid separation color layer in between.

A shape mask creates a “hollow” window, and a top clipping layer prevents the subject from overflowing beyond the mask while still allowing the pop-out look.

Since segmentation can fail for cluttered or abstract images, the editor also supports turning off the pop‑out effect so the subject layer is not shown.

### Native features across platforms
Wallpaper setting, saving, caching, and file path/URI handling differ across platforms, so shared logic needed platform-specific implementations behind a common API.

Desktop wallpaper setting required different approaches (AppleScript on macOS, JNA/Windows APIs on Windows, and per-desktop-environment commands on Linux), and Linux is currently untested due to limited hardware access.

| Feature                  | Android | iOS                               | Windows | macOS | Linux       |
|--------------------------|---------|-----------------------------------|---------|-------|-------------|
| 3D Pop‑Out Editor        | ✅       | ❌ (Hardware limitation)        | ✅       | ✅     | ⚠️ Untested |
| AI Wallpaper             | ✅       | ✅                                          | ✅       | ✅     | ⚠️ Untested |
| Direct wallpaper setting | ✅       | ⏸️ Manual (Photos + instructions) | ✅       | ✅     | ⚠️ Untested |
| Creations gallery        | ✅       | ✅                                 | ✅       | ✅     | ⚠️ Untested |

> Linux is expected to work but hasn’t been physically tested yet.

## 🤝 Feedback
Issues and suggestions are welcome, please use the GitHub Issues tab on this repository.

## 📜 License
Licensed under the Apache 2.0 License.

