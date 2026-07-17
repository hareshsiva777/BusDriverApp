# 🚌 Bus Driver Android Application

An offline Android application developed as a technical assessment for Hexon Data.

The application allows bus drivers to log in without internet connectivity, manage trips, continuously collect GPS location data, store trip information locally, and automatically synchronize completed trips when the network becomes available.

---

# Features

✅ Offline Driver Login

- Login works without internet.
- Driver accounts are stored locally using Room Database.

---

✅ Route Selection

- View available routes.
- Select a route before starting a journey.
- Route information can be refreshed when online.

---

✅ Journey Management

- Journey confirmation screen
- Slide-to-start functionality
- Start and end trips
- Record vehicle odometer

---

✅ GPS Tracking

- Continuous GPS tracking
- Foreground Location Service
- Location recording while the app runs in the background
- Live trip duration
- Distance travelled
- Current speed
- GPS sample counter

---

✅ Offline Storage

All trip information is stored locally using Room Database.

Stored information includes:

- Driver
- Route
- Start Time
- End Time
- GPS Locations
- Distance
- Odometer
- Synchronization Status

---<img width="1912" height="975" alt="image" src="https://github.com/user-attachments/assets/410e7f98-127d-4f9c-85f4-a2c72d5a5a01" />


✅ Trip History

Drivers can review completed trips even without internet connectivity.

---

✅ Background Synchronization

Synchronization is handled using Android WorkManager.

Completed trips remain stored locally until synchronization succeeds.

---

# Technology Stack

Language

- Kotlin

UI

- Jetpack Compose
- Material 3

Architecture

- MVVM

Database

- Room Database

Background Processing

- WorkManager

Location Tracking

- Foreground Location Service
- Fused Location Provider

Networking

- Retrofit (Mock API)

---

# Architecture

```
Presentation Layer
│
├── Jetpack Compose UI
│
├── ViewModels
│
├── Repository Layer
│
├── Room Database
│
└── WorkManager
        │
        ▼
 Mock Server
```

---

# Project Structure

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

viewmodel/

worker/
```

---

# Demo Login Credentials

| Driver ID | Password |
|-----------|----------|
| DRIVER001 | 1234 |
| DRIVER002 | 1234 |
| DRIVER003 | 1234 |
| DRIVER004 | 1234 |
| DRIVER005 | 1234 |

---

# How to Run

## Requirements

Android Studio Hedgehog or newer

Android SDK 35

Gradle 8+

Android Emulator or Physical Device

---

## Steps

1. Clone repository

```
git clone https://github.com/YOUR_USERNAME/BusDriverApp.git
```

2. Open in Android Studio

3. Sync Gradle

4. Run the application

---

# Offline Storage

The application follows an Offline-First architecture.

Instead of requiring internet connectivity, all critical data is stored locally using Room Database.

This allows drivers to:

- Login offline
- Continue trips offline
- Store completed trips locally
- View trip history offline

---

# GPS Tracking

GPS tracking is implemented using a Foreground Location Service.

This ensures location updates continue while:

- Screen is locked
- App is minimized
- Driver continues travelling

Collected GPS points are associated with the active trip.

---

# Synchronization

Synchronization is handled using Android WorkManager.

Workflow:

Driver Ends Trip

↓

Trip Saved in Room Database

↓

Waiting for Internet

↓

WorkManager Executes

↓

Mock Server

↓

Trip Marked Synced

---

# Permissions

The application requires:

- Fine Location
- Coarse Location
- Foreground Service
- Internet
- Network State

---

# Assumptions

For this assessment:

- Driver accounts are preloaded into Room Database.
- Route data is simulated.
- Synchronization uses a mock backend.
- GPS depends on emulator/device location.

---

# Features Not Implemented

Due to assessment scope:

- Production authentication API
- Real backend server
- Push notifications
- Google Maps visualization
- Route optimization
- Driver profile management

---

# Future Improvements

- Live backend integration
- Maps integration
- Multiple depots
- Driver attendance
- Digital inspection checklist
- Fleet management dashboard

---

# Technical Highlights

✔ Offline-First Design

✔ MVVM Architecture

✔ Room Database

✔ WorkManager

✔ Foreground GPS Service

✔ Jetpack Compose

✔ Material 3

---

# Author

Hary G

Technical Assessment

Hexon Data
