package com.example.personalbudgettrackerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalbudgettrackerapp.ui.analytics.AnalyticsScreen
import com.example.personalbudgettrackerapp.ui.auth.LoginScreen
import com.example.personalbudgettrackerapp.ui.auth.RegisterScreen
import com.example.personalbudgettrackerapp.ui.catagory.CategoryScreen
import com.example.personalbudgettrackerapp.ui.components.BottomNav
import com.example.personalbudgettrackerapp.ui.expenses.AddExpense
import com.example.personalbudgettrackerapp.ui.expenses.ExpenseScreen
import com.example.personalbudgettrackerapp.ui.home.HomeScreen
import com.example.personalbudgettrackerapp.ui.rewards.RewardsScreen
import com.example.personalbudgettrackerapp.ui.settings.SettingsScreen
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme

/**
 * The main activity of the application.
 * This activity serves as the entry point and manages the overall UI structure,
 * including navigation between different screens.
 */

private const val PERMISSION_REQUEST_CODE = 101
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display to allow content to be drawn under system bars
        enableEdgeToEdge()

        setContent {
            // Request notification permission for Android 13+
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Handle permission result if needed
            }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Initialize the shared ViewModel for state management
            val appViewModel: AppViewModel = viewModel()

            // Calcula o darkTheme com base na preferência do utilizador
            val darkTheme = when (appViewModel.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Apply the application theme
            PersonalBudgetTrackerAppTheme(darkTheme = darkTheme) {
                val currentScreen = appViewModel.uiState.currentScreen

                // Scaffold provides the basic material design visual layout structure
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Display the bottom navigation bar only for specific screens
                        if (currentScreen == AppScreen.Home ||
                            currentScreen == AppScreen.Rewards ||
                            currentScreen == AppScreen.Analytics ||
                            currentScreen == AppScreen.Settings ||
                            currentScreen == AppScreen.Expense) {
                            BottomNav(appViewModel)
                        }
                    }
                ) { innerPadding ->
                    // Main content area where screens are swapped based on the current state
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            AppScreen.Login -> LoginScreen(appViewModel)
                            AppScreen.Register -> RegisterScreen(appViewModel)
                            AppScreen.Home -> HomeScreen(appViewModel)
                            AppScreen.Rewards -> RewardsScreen(appViewModel)
                            AppScreen.Analytics -> AnalyticsScreen(appViewModel)
                            AppScreen.Expense -> ExpenseScreen(appViewModel)
                            AppScreen.AddExpense -> AddExpense(appViewModel)
                            AppScreen.Categories -> CategoryScreen(appViewModel)
                            AppScreen.Settings -> SettingsScreen(appViewModel)
                        }
                    }
                }
            }
        }


        askNotificationPermission()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificações Gerais"
            val descriptionText = "Canal para notificações do sistema"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }


}