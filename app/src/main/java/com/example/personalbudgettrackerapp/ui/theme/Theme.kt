package com.example.personalbudgettrackerapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = BackgroundDark,
    background = BackgroundDark,
    onBackground = ForegroundDark,
    surface = CardDark,
    onSurface = ForegroundDark,
    secondary = SecondaryDark,
    onSecondary = ForegroundDark,
    outline = BorderDark,
    error = Destructive,
    onError = White,
    surfaceVariant = SecondaryDark,
    onSurfaceVariant = MutedForegroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = White,
    background = BackgroundLight,
    onBackground = ForegroundLight,
    surface = CardLight,
    onSurface = ForegroundLight,
    secondary = SecondaryLight,
    onSecondary = ForegroundLight,
    outline = BorderLight,
    error = Destructive,
    onError = White,
    surfaceVariant = SecondaryLight,
    onSurfaceVariant = MutedForegroundLight
)

@Composable
fun PersonalBudgetTrackerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
