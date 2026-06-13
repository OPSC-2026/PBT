# Personal Budget Tracker (Montrack)

## Overview
Montrack is a professional personal finance management application designed to facilitate rigorous tracking of spending habits and the achievement of financial goals. The platform provides users with an intuitive interface to monitor expenses, manage budgets, and gain insights into their financial health through detailed analytics.

## Core Features
- **User Authentication**: Secure account creation and management utilizing Firebase Authentication services.
- **Financial Tracking**: Detailed logging of individual expenses with attributes for amount, category, date, and description.
- **Budget Management**: Advanced monthly budget configuration including the ability to allocate specific limits to distinct spending categories.
- **Analytical Insights**: Comprehensive data visualization featuring spending trend analysis, category-based distribution, and budget performance metrics.
- **Gamified Achievements**: A reward system designed to encourage consistent financial tracking through the unlocking of milestone-based badges.
* **Category Management**: Tools for defining and customizing spending categories with unique visual identifiers.
- **Adaptive UI**: High-performance user interface built with Jetpack Compose, featuring full support for both light and dark system themes.

## Technical Architecture
- **Programming Language**: Kotlin
- **User Interface**: Jetpack Compose (Material 3)
- **Database and Auth**: Firebase Cloud Firestore and Firebase Authentication
- **Design Pattern**: MVVM (Model-View-ViewModel)
- **Asynchronous Processing**: Kotlin Coroutines and Flow

## Project Directory Structure
- `app/src/main/java/com/example/personalbudgettrackerapp/`
    - `ui/`: Implementation of all Composable screens and reusable components.
    - `data/`: Definitions for data models and persistence logic.
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
[Watch Here](https://youtu.be/HCPA5yylDG0)

## Download do App (APK)
[Download Here](https://github.com/OPSC-2026/PBT/blob/main/buildActionResult/app-release-unsigned.apk)
