package com.example.personalbudgettrackerapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Defines the color palette for the application in both Light and Dark modes.
 * These colors are used to build the Material3 ColorScheme.
 */

// Light Mode Colors
val BackgroundLight = Color(0xFFFAFAFA) // Neutral off-white for main backgrounds
val ForegroundLight = Color(0xFF252525) // Dark gray for primary text
val CardLight = Color(0xFFFFFFFF)       // Pure white for elevated surfaces
val PrimaryLight = Color(0xFF00A368)    // Emerald green for primary branding and actions
val PrimaryLight5 = Color(0x0D00A368)   // 5% opacity version of primary green
val SecondaryLight = Color(0xFFF2F2F2)  // Light gray for secondary elements
val BorderLight = Color(0xFFE5E5E5)     // Subtle gray for dividers and borders
val MutedForegroundLight = Color(0xFF808080) // Medium gray for descriptive/secondary text

// Dark Mode Colors
val BackgroundDark = Color(0xFF1A1A1A)  // Deep charcoal for main backgrounds
val ForegroundDark = Color(0xFFFAFAFA)  // Off-white for primary text
val CardDark = Color(0xFF262626)        // Slightly lighter charcoal for elevated surfaces
val PrimaryDark = Color(0xFF00B875)     // Vibrant green for visibility in dark mode
val SecondaryDark = Color(0xFF383838)   // Dark gray for secondary elements
val BorderDark = Color(0xFF424242)      // Muted gray for dividers and borders
val MutedForegroundDark = Color(0xFFA6A6A6) // Light gray for descriptive/secondary text

// Global Semantic Colors
val Destructive = Color(0xFFE5484D) // Red for errors or destructive actions
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Yellow = Color(0xFFFACC15)     // For highlights or awards
