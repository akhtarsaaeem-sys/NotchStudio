# Notch Studio 🎨

A sleek, premium Android utility app built in Kotlin that enables users to place a fully customizable, persistent floating "pill" overlay directly over their device's physical camera cutout (notch). Tailor your status bar area with dynamic text, custom colors, or cropped graphics seamlessly.

---

## ✨ Features

* **🎯 Live Position Fine-Tuning:** Dynamic X and Y sliders featuring negative margin bounds to let you push layouts deep into the device status bar without snapping bugs.
* **📐 Intuitive Drag & Resize Sync:** Drag the bubble anywhere freely; sliders dynamically update positions in real-time to prevent layout "jumping".
* **🌈 Custom Appearance Palettes:** Fine-tune both the background card color and typography colors instantly via circular dashboard swatches.
* **📱 Orientation Responsiveness:** Auto-hides the overlay completely during landscape mode (e.g., full-screen videos or gaming) and smoothly restores visibility in portrait.
* **✂️ Flexible Framing:** Supports structured 3:1 proportions or custom unlocked aspect ratios for cropping gallery images or animations directly into the overlay view.
* **⚡ Battery Mirroring Mode:** Dynamically mirrors live device battery percentages alongside color-coded critical status warnings.

---

## 🛠️ Tech Stack & Requirements

* **Language:** 100% Kotlin
* **Minimum SDK:** Android 9 (API 28)
* **Target SDK:** Android 14 (API 34)
* **Core APIs:** Foreground Services, `WindowManager` overlay structures (`TYPE_APPLICATION_OVERLAY`), system configuration broadcast channels.

---

## 🚀 Getting Started

1. Clone this repository into Android Studio.
2. Build and install the APK onto an Android device running API 28+.
3. Grant the **"Display over other apps"** (System Alert Window) permission when prompted.
4. Customize your layout parameters and hit **Launch**!

---

 ## 📥 Direct Download (APK)

You can download the ready-to-install application directly to your Android device without needing Android Studio:

1. On your phone, go to the [Notch Studio Releases](https://github.com/akhtarsaaeem-sys/NotchStudio/releases) page.
2. Under the latest release version, tap on **Assets**.
3. Download the `app-debug.apk` or `NotchStudio.apk` file.
4. Open the file on your device and install it (you may need to allow "Install from Unknown Sources" in your browser/file manager settings).
