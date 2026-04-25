package com.example.personalbudgettrackerapp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class AuthScreen {
    object Login : AuthScreen()
    object Register : AuthScreen()
    object Home : AuthScreen()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    var currentScreen by mutableStateOf<AuthScreen>(
        if (auth.currentUser != null) AuthScreen.Home else AuthScreen.Login
    )
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun setScreen(screen: AuthScreen) {
        error = null
        currentScreen = screen
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            error = "email and password required"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentScreen = AuthScreen.Home
                } else {
                    error = task.exception?.localizedMessage ?: "Erro ao fazer login"
                }
            }
    }

    fun register(email: String, password: String, confirm: String) {
        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            error = "Preencha todos os campos"
            return
        }
        
        if (password != confirm) {
            error = "Senhas não coincidem"
            return
        }
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentScreen = AuthScreen.Home
                } else {
                    error = task.exception?.localizedMessage ?: "Erro ao registrar"
                }
            }
    }

    fun logout() {
        auth.signOut()
        currentScreen = AuthScreen.Login
    }

}


