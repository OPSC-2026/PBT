# Personal Budget Tracker (Montrack)

## Overview
Montrack is a professional personal finance management application designed to facilitate rigorous tracking of spending habits and the achievement of financial goals. The platform provides users with an intuitive interface to monitor expenses, manage budgets, and gain insights into their financial health through detailed analytics.

## Core Features
- **User Authentication:** Secure account creation and login using Firebase Authentication.
- **Financial Tracking:** Log expenses with amount, category, date, and description.
- **Budget Management:** Monthly budgets with category-based limits and real-time tracking.
- **Analytical Insights:** Visual reports of spending trends, category breakdowns, and budget performance.
- **Gamified Achievements:** Unlock milestones based on financial tracking consistency.
- **Category Management:** Create and customize spending categories with unique visuals.
- **Push Notifications (FCM):** Firebase Cloud Messaging integration using `MyFirebaseMessagingService` to handle notification and data payloads, delivering real-time updates to users.
- **Theme Switching 🌗:** Manual in-app toggle between Light and Dark mode using Material 3 dynamic theming.
- **Adaptive UI**: Built with Jetpack Compose for a responsive, modern, and reactive user experience.

## Technical Architecture
- **Language:** Kotlin  
- **UI:** Jetpack Compose (Material 3)  
- **Architecture:** MVVM (Model-View-ViewModel)  
- **Asynchronous Handling:** Kotlin Coroutines + Flow  
- **Backend Services:**
  - Firebase Authentication  
  - Cloud Firestore  
  - Firebase Cloud Messaging (FCM)  
- **Theme System:** Runtime Compose-based theme switching (Light/Dark)

## Project Directory Structure
- `app/src/main/java/com/example/personalbudgettrackerapp/`
    - `ui/`: Implementation of all Composable screens and reusable components.
    - `data/`: Definitions for data models and persistence logic.
    - `notifications/`: Firebase Messaging service (FCM)
    - `AppViewModel.kt`: Core application logic, state management, and external service integration.
    - `MainActivity.kt`: Primary activity responsible for navigation orchestration.

## Implementation Details
The application utilizes a reactive state management approach, where the central ViewModel maintains the `AppUiState`. Real-time synchronization with Cloud Firestore ensures that user data remains consistent across multiple sessions. The achievement engine evaluates user data against predefined criteria to provide immediate feedback on financial milestones.

## Setup Requirements
1. Android Studio Ladybug or later.
2. A configured Firebase project with Authentication and Firestore enabled.
3. The `google-services.json` configuration file placed in the `app/` directory.
4. JDK 17 or higher.

## Video Presentation
[Watch Here](https://youtu.be/eiMg7ZuNves)

## Download do App (APK)
[Download Here](https://github.com/OPSC-2026/PBT/blob/main/buildActionResult/app-release-unsigned.apk)
