<div align="center">

# 🧘 ZenMode

**A minimalist Android launcher that transforms your phone from an attention trap into a deliberate utility.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-orange)](https://apilevels.com)

</div>

---

## 🧩 The Problem

Every time you unlock your phone, you're greeted by a minefield of colorful, distracting icons. Even with the best intentions — checking a calendar event, replying to a critical message, looking up directions — the home screen hijacks your attention before you get there.

**The result:** accidental browsing sessions, lost focus, and a habit loop that's hard to break.

---

## 💡 The Solution

ZenMode replaces your home screen with a **clean, low-stimulation interface** that forces intentional selection over passive tap-reflexes — and backs it up with a **human accountability layer**.

### Core Pillars

| Pillar | Description |
|---|---|
| 🖤 **Minimalist Interface** | Strips away colorful icons. Text-first, distraction-free home screen that breaks instant gratification. |
| 👥 **Buddy Accountability** | Sync focus states or habit goals with a trusted peer. Social friction is stronger than willpower. |
| ⏸️ **The Resistance Screen** | A mindful pause (looping video or brief delay) instead of a hard block — lets you reclaim your true intent. |

---

## 🔄 How It Works

1. **You unlock your phone** → ZenMode's minimal home screen greets you, not a wall of icons.
2. **You tap a distracting app** → A 7-second Resistance Screen plays, giving your brain time to reconsider.
3. **Your buddy sees your focus state** → Social accountability kicks in, reinforcing good habits.
4. **You open what you actually needed** → Mission accomplished, intentionally.

> *"ZenMode didn't just block me from my phone — it rewired my habit loop."*

---

## 🛠️ Technical Architecture

```
zenmode-main/
├── app/                  # Main Android application (Kotlin + Compose)
├── core-api/             # Public interfaces (Open Core contract)
└── core-mock/            # Open-source mock implementation
```

This project follows an **Open Core** architecture. The `core-api` module defines the service contracts that power ZenMode's backend features (auth, analytics, remote config). Contributors use `core-mock` — a fully functional, standalone implementation.

### Stack

| Technology | Purpose |
|---|---|
| **Kotlin & Jetpack Compose** | Core Android UI — fast, battery-efficient, text-based |
| **Room Database** | Local on-device storage for preferences, hidden apps, and configs |
| **RoleManager & LauncherApps** | Deep OS integration as the default home screen |
| **UsageStats & Accessibility Services** | Screen time monitoring, app blocking, Resistance Screen |
| **NotificationListenerService** | Mutes/batches notifications during deep-focus sessions |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 35, Min SDK 28

### Build & Run

```bash
# Clone the repo
git clone https://github.com/your-org/zenmode-main.git
cd zenmode-main

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Setting ZenMode as Your Launcher

After installing, press your **Home button** → select **ZenMode** → tap **Always**.

---

## 🤝 Contributing

ZenMode is an open-core project. The `core-api` interfaces and `core-mock` implementation are fully open-source.

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes and open a pull request

---

## 📄 License

[MIT License](LICENSE) © ZenMode Contributors
