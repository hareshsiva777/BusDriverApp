# 🚌 Bus Driver Android Application

<p align="center">

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Database](https://img.shields.io/badge/Database-Room-success)
![Background](https://img.shields.io/badge/Background-WorkManager-red)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

</p>

An **offline-first Android application** developed as a **Technical Assessment for Hexon Data**.

The application enables bus drivers to operate without internet connectivity by storing all critical information locally and automatically synchronizing completed trips once a connection becomes available.

---

# 📱 Features

### ✅ Offline Driver Login

- Login works without internet
- Driver accounts stored locally using Room Database

---

### ✅ Route Selection

- View available routes
- Select a route before starting a trip
- Refresh routes when online

---

### ✅ Journey Management

- Journey confirmation screen
- Slide-to-start journey
- End journey
- Odometer recording

---

### ✅ GPS Tracking

- Continuous GPS collection
- Foreground Location Service
- Background tracking
- Distance calculation
- Current speed
- Trip duration
- GPS sample counter

---

### ✅ Offline Storage

Trip information is safely stored using Room Database.

Stored information includes:

- Driver
- Route
- Start Time
- End Time
- GPS Locations
- Distance
- Odometer
- Sync Status

---

### ✅ Trip History

Drivers can review completed trips anytime, even when offline.

---

### ✅ Automatic Synchronization

Completed trips remain stored locally until internet connectivity becomes available.

Synchronization is automatically handled using Android WorkManager.

---
# 📸 Application Screenshots

---

## 🔐 Login

<p align="center">
  <img src="https://github.com/user-attachments/assets/29fab3b8-5146-4cc2-81d2-20d39feb4fb8" width="300" alt="Login Screen">
</p>

---

## 🏠 Dashboard

<p align="center">
  <img src="https://github.com/user-attachments/assets/1ca92001-3842-42f8-9403-04a818b1b124" width="300" alt="Dashboard">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/d8d9192d-5f3b-4a78-b57a-66f7b700f628" width="300" alt="Dashboard">
</p>

---

## 🚌 Journey Confirmation

<p align="center">
  <img src="https://github.com/user-attachments/assets/8bd20f38-c532-46c3-8836-81ab9b7cfbed" width="300" alt="Journey Confirmation 1">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/0eef0abe-fd07-4c92-967d-9c12de31cac4" width="300" alt="Journey Confirmation 2">
</p>
---

## 📍 Active Trip

<p align="center">
  <img src="https://github.com/user-attachments/assets/60b63b7d-de32-4b2d-b3e2-b8f316da5135" width="300" alt="Active Trip">
</p>

---

## 🛑 End Trip

<p align="center">
  <img src="https://github.com/user-attachments/assets/36ea5b9b-f026-40ad-a893-55c9918174aa" width="300" alt="End Trip">
</p>

---

## 📜 Trip History

<p align="center">
  <img src="https://github.com/user-attachments/assets/e37da150-aa8f-4e8b-9074-073b7e5f7728" width="300" alt="Synchronization Status">
</p>

# 🎥 Demonstration Video

A complete walkthrough of the application has been provided with the submission.

The video demonstrates:

- Offline Login
- Route Selection
- Journey Start
- GPS Tracking
- Background Tracking
- Journey Completion
- Offline Storage
- Automatic Synchronization

---

# 🏗 Architecture

```
                Jetpack Compose UI
                        │
                        ▼
                 ViewModels (MVVM)
                        │
                        ▼
                  Repository Layer
                        │
        ┌───────────────┴───────────────┐
        ▼                               ▼
   Room Database                 Retrofit API
        │                               │
        └───────────────┬───────────────┘
                        ▼
                 WorkManager Sync
                        │
                        ▼
                  Mock Backend
```

---

# 📂 Project Structure

```
app/

data/
├── api
├── dao
├── database
├── model
└── repository

location/

navigation/

network/

ui/
├── login
├── dashboard
├── routes
├── trip
└── components

viewmodel/

worker/
```

---

# 🛠 Technology Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Database | Room Database |
| Background Tasks | WorkManager |
| Location | Foreground Location Service |
| Networking | Retrofit |
| Build Tool | Gradle |

---

# 🔐 Demo Login

| Driver ID | Password |
|-----------|----------|
| DRIVER001 | 1234 |
| DRIVER002 | 1234 |
| DRIVER003 | 1234 |
| DRIVER004 | 1234 |
| DRIVER005 | 1234 |

---

# 🚀 Getting Started

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 35
- Gradle 8+
- Android Emulator or Physical Device

---

## Installation

Clone the repository

```bash
git clone https://github.com/hareshsiva777/BusDriverApp.git
```

Open using Android Studio.

Allow Gradle Sync.

Run the application.

---

# 💾 Offline Storage

This application follows an **Offline-First Architecture**.

Critical information is stored locally using Room Database.

The application continues functioning even without internet access.

Offline capabilities include:

- Login
- Route selection
- Journey management
- GPS tracking
- Trip history

---

# 📍 GPS Tracking

Location updates are handled through a Foreground Location Service.

Tracking continues while:

- Screen is locked
- Application is minimized
- Driver continues travelling

Every GPS sample is associated with the active trip.

---

# 🔄 Synchronization

Synchronization is performed using Android WorkManager.

```
Start Trip
      │
      ▼
Collect GPS
      │
      ▼
End Trip
      │
      ▼
Save to Room Database
      │
      ▼
Waiting for Internet
      │
      ▼
WorkManager
      │
      ▼
Mock Server
      │
      ▼
Mark Trip as Synced
```

---

# 🔒 Permissions

- Fine Location
- Coarse Location
- Foreground Service
- Internet
- Network State

---

# 📌 Assumptions

For this technical assessment:

- Driver accounts are preloaded into Room Database.
- Route information is simulated.
- Synchronization uses a mock backend.
- GPS accuracy depends on device hardware or emulator settings.

---

# ⚠ Features Not Implemented

Due to the assessment scope:

- Production authentication server
- Live backend integration
- Maps visualization
- Push notifications
- Driver profile management
- Fleet administration

---

# 🚀 Future Enhancements

- Live server integration
- Google Maps support
- Driver attendance
- Vehicle inspection checklist
- Multi-depot support
- Fleet management dashboard

---

# ⭐ Technical Highlights

- Offline-First Design
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database
- WorkManager
- Foreground GPS Service
- Material 3 Design

---

# 👨‍💻 Author

**Hary G**

Technical Assessment Submission

Hexon Data
