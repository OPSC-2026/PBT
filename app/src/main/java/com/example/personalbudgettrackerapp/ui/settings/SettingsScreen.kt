package com.example.personalbudgettrackerapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personalbudgettrackerapp.auth.AuthViewModel

@Composable
fun SettingsScreen(authViewModel: AuthViewModel) {

    var totalBudget by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Manage your budget settings",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TOTAL BUDGET INPUT (safe version)
        Card {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = totalBudget,
                    onValueChange = { totalBudget = it },
                    label = { Text("Enter budget (R)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAVE BUTTON
        Button(
            onClick = {
                saved = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saved) "Saved!" else "Save Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT
        Button(
            onClick = {
                authViewModel.logout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.error
            )
        ) {
            Text("Log Out")
        }
    }
}