package com.example.personalbudgettrackerapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Montrack", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight(800))
        Text("Start your financial journey", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(40.dp))


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Create account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight(600))
                Text("Sign up to start tracking your budget", style = MaterialTheme.typography.bodyMedium)

            }


            Column() {
                Text("Email")
                OutlinedTextField(
                    value = email,
                    placeholder = { Text("Enter your email") },
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column() {
                Text("Password")
                OutlinedTextField(
                    value = password,
                    placeholder = { Text("Create a password") },
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column() {
                Text("Confirm Password")
                OutlinedTextField(
                    value = confirmPassword,
                    placeholder = { Text("Confirm your password") },
                    onValueChange = { confirmPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        viewModel.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                viewModel.register(email, password, confirmPassword)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = email.trim().isNotEmpty() && password.trim().isNotEmpty() && confirmPassword.trim().isNotEmpty()
        ) {
            Text("Create Account")
        }
        
        TextButton(onClick = { viewModel.setScreen(AuthScreen.Login) }) {
            Text("Already have an account? ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight(400))
            Text("Sign in", fontWeight = FontWeight(600))
        }
    }
}
