package com.example.personalbudgettrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalbudgettrackerapp.auth.*
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBudgetTrackerAppTheme {
                val authViewModel: AuthViewModel = viewModel()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (authViewModel.uiState.currentScreen) {
                            AuthScreen.Login -> LoginScreen(authViewModel)
                            AuthScreen.Register -> RegisterScreen(authViewModel)
                            AuthScreen.Home -> HomeScreen(authViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: AuthViewModel) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome!", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
            Text(text = "${auth.currentUser?.displayName}", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
            Button(onClick = { viewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}
