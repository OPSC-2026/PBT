package com.example.personalbudgettrackerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed interface AppScreen {
    data object Login : AppScreen
    data object Register : AppScreen
    data object Home : AppScreen
    data object Rewards : AppScreen
    data object Analytics : AppScreen
    data object AddExpense : AppScreen
    data object Categories : AppScreen
}

data class AppUiState(
    val currentScreen: AppScreen = AppScreen.Login,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AppViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    var uiState by mutableStateOf(
        AppUiState(
            currentScreen = if (auth.currentUser != null) AppScreen.Home else AppScreen.Login
        )
    )
        private set

    fun setScreen(screen: AppScreen) {
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
                    uiState.copy(currentScreen = AppScreen.Home, isLoading = false)
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
                        ?.addOnCompleteListener { _ ->
                            uiState = uiState.copy(
                                currentScreen = AppScreen.Home,
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
        uiState = uiState.copy(currentScreen = AppScreen.Login)
    }

    fun addExpense(amount: Double, date: java.time.LocalDate, categoryId: String, description: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        
        uiState = uiState.copy(isLoading = true, error = null)
        
        val expense = hashMapOf(
            "amount" to amount,
            "date" to date.toString(),
            "categoryId" to categoryId,
            "description" to description,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("users").document(userId).collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
    }

    fun deleteCategory(categoryId: String) {
        // these dont do anything
    }

    fun updateCategory(category: com.example.personalbudgettrackerapp.data.CategoryExtended) {
        // Mock implementation for now
    }

    fun addCategory(category: com.example.personalbudgettrackerapp.data.CategoryExtended) {
        // Mock implementation for now
    }
}
