package com.example.personalbudgettrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display to allow content to be drawn under system bars
        enableEdgeToEdge()
        
        setContent {
            // Apply the application theme
            PersonalBudgetTrackerAppTheme {
                // Initialize the shared ViewModel for state management
                val appViewModel: AppViewModel = viewModel()
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
    }
}
