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
import com.example.personalbudgettrackerapp.ui.components.BottomNav
import com.example.personalbudgettrackerapp.ui.expenses.AddExpense
import com.example.personalbudgettrackerapp.ui.home.HomeScreen
import com.example.personalbudgettrackerapp.ui.rewards.RewardsScreen
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBudgetTrackerAppTheme {
                val appViewModel: AppViewModel = viewModel()
                val currentScreen = appViewModel.uiState.currentScreen
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == AppScreen.Home ||
                            currentScreen == AppScreen.Rewards ||
                            currentScreen == AppScreen.Analytics ||
                            currentScreen == AppScreen.Settings) {
                            BottomNav(appViewModel)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            AppScreen.Login -> LoginScreen(appViewModel)
                            AppScreen.Register -> RegisterScreen(appViewModel)
                            AppScreen.Home -> HomeScreen(appViewModel)
                            AppScreen.Rewards -> RewardsScreen(appViewModel)
                            AppScreen.Analytics -> AnalyticsScreen(appViewModel)
                            AppScreen.AddExpense -> AddExpense(appViewModel)
                            AppScreen.Settings -> com.example.personalbudgettrackerapp.ui.settings.SettingsScreen(appViewModel)
                            AppScreen.CategoryManagement -> com.example.personalbudgettrackerapp.ui.settings.CategoryManagementScreen(
                                viewModel = appViewModel,
                                onBack = { appViewModel.setScreen(AppScreen.Settings) }
                            )
                        }
                    }
                }
            }
        }
    }
}
