package com.example.personalbudgettrackerapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome!", style = MaterialTheme.typography.headlineLarge)
            Text(text = "${auth.currentUser?.displayName}", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { viewModel.logout() }) {
                Text("Logout")
            }
            Button(onClick = { viewModel.setScreen(AppScreen.AddExpense) }) {
                Text("Add Expense")
            }
        }
    }
}
