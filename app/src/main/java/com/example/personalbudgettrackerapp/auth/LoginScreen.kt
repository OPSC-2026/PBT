package com.example.personalbudgettrackerapp.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        Text("Montrack", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight(800))
        Text("Your personal budget tracker", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(40.dp))


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Welcome back", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight(600))
                Text("Sign in to continue tracking your finances", style = MaterialTheme.typography.bodyMedium)

            }

            Column(
            ) {
                Text("Email")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column() {
                Text("Password")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password") },
    //            visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        
        viewModel.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = email.trim().isNotEmpty() && password.trim().isNotEmpty()
        ) {
            Text("Sign In")
        }
        
        TextButton(onClick = { viewModel.setScreen(AuthScreen.Register) }) {
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight(400))
            Text("Sign up", fontWeight = FontWeight(600))
        }
    }
}
