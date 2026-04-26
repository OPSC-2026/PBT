package com.example.personalbudgettrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalbudgettrackerapp.auth.*
import com.example.personalbudgettrackerapp.ui.analytics.AnalyticsScreen
import com.example.personalbudgettrackerapp.ui.components.BottomNav
import com.example.personalbudgettrackerapp.ui.home.HomeScreen
import com.example.personalbudgettrackerapp.ui.rewards.RewardsScreen
import com.example.personalbudgettrackerapp.ui.settings.SettingsScreen
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBudgetTrackerAppTheme {
                val authViewModel: AuthViewModel = viewModel()
                val currentScreen = authViewModel.uiState.currentScreen
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == AuthScreen.Home || 
                            currentScreen == AuthScreen.Rewards || 
                            currentScreen == AuthScreen.Analytics) {
                            BottomNav(authViewModel)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            AuthScreen.Login -> LoginScreen(authViewModel)
                            AuthScreen.Register -> RegisterScreen(authViewModel)
                            AuthScreen.Home -> HomeScreen(authViewModel)
                            AuthScreen.Rewards -> RewardsScreen(authViewModel)
                            AuthScreen.Analytics -> AnalyticsScreen(authViewModel)
                            AuthScreen.Settings -> SettingsScreen(authViewModel)
                        }
                    }
                }
            }
        }
    }
}
