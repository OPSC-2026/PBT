package com.example.personalbudgettrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBudgetTrackerAppTheme {
                DashboardScreen()
            }
        }
    }
}
