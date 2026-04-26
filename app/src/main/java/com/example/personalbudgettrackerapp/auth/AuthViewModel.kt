package com.example.personalbudgettrackerapp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

sealed interface AuthScreen {
    data object Login : AuthScreen
    data object Register : AuthScreen
    data object Home : AuthScreen
    data object Rewards : AuthScreen

    data object Analytics : AuthScreen

    data object Settings : AuthScreen
}

data class AuthUiState(
    val currentScreen: AuthScreen = AuthScreen.Login,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    var uiState by mutableStateOf(
        AuthUiState(
           // currentScreen = if (auth.currentUser != null) AuthScreen.Home else AuthScreen.Login
            currentScreen = AuthScreen.Settings
        )
    )
        private set

    fun setScreen(screen: AuthScreen) {
        uiState = uiState.copy(currentScreen = screen, error = null)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all fields")
            return
        }
        
        uiState = uiState.copy(isLoading = true, error = null)
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val newState = if (task.isSuccessful) {
                    uiState.copy(currentScreen = AuthScreen.Home, isLoading = false)
                } else {
                    uiState.copy(
                        error = task.exception?.localizedMessage ?: "Invalid email or password",
                        isLoading = false
                    )
                }
                uiState = newState
            }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all fields")
            return
        }
        
        if (password != confirm) {
            uiState = uiState.copy(error = "Passwords do not match")
            return
        }

        if (password.length < 6) {
            uiState = uiState.copy(error = "Password must be at least 6 characters")
            return
        }
        
        uiState = uiState.copy(isLoading = true, error = null)
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                    }
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            uiState = uiState.copy(
                                currentScreen = AuthScreen.Home,
                                isLoading = false
                            )
                        }
                } else {
                    uiState = uiState.copy(
                        error = task.exception?.localizedMessage ?: "Registration failed",
                        isLoading = false
                    )
                }
            }
    }

    fun logout() {
        auth.signOut()
        uiState = uiState.copy(currentScreen = AuthScreen.Login)
    }
}
