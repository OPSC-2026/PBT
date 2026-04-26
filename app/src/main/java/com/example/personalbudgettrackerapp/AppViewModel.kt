package com.example.personalbudgettrackerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.personalbudgettrackerapp.data.Budget
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

sealed interface AppScreen {
    data object Login : AppScreen
    data object Register : AppScreen
    data object Home : AppScreen
    data object Rewards : AppScreen
    data object Analytics : AppScreen
    data object AddExpense : AppScreen
}

data class CategorySpending(
    val category: Category,
    val spent: Double,
    val budget: Double
)

data class ChartPoint(
    val day: Int,
    val amount: Double
)

data class AppUiState(
    val currentScreen: AppScreen = AppScreen.Login,
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList()
)

class AppViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    var uiState by mutableStateOf(
        AppUiState(
            currentScreen = if (auth.currentUser != null) AppScreen.Home else AppScreen.Login,
            categories = listOf(
                Category("1", "Shopping", Color(0xFFFF9800), "shopping-cart"),
                Category("2", "Transport", Color(0xFF2196F3), "car"),
                Category("3", "Entertainment", Color(0xFFE91E63), "gamepad-2"),
                Category("4", "Utilities", Color(0xFFFFEB3B), "zap"),
                Category("5", "Food", Color(0xFF4CAF50), "utensils"),
                Category("6", "Home", Color(0xFF9C27B0), "home")
            ),
            expenses = listOf(
                Expense("1", 250.0, LocalDate.now(), "1", "Groceries"),
                Expense("2", 100.0, LocalDate.now().minusDays(1), "2", "Fuel"),
                Expense("3", 50.0, LocalDate.now().minusDays(2), "5", "Coffee"),
                Expense("4", 1200.0, LocalDate.now(), "5", "Dinner"),
                Expense("5", 300.0, LocalDate.now().minusDays(4), "3", "Movie")
            ),
            budgets = listOf(
                Budget("b1", LocalDate.now().monthValue, LocalDate.now().year, 5000.0, mapOf(
                    "1" to 1000.0, "2" to 500.0, "5" to 1500.0
                ))
            )
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
                    uiState.copy(error = task.exception?.localizedMessage ?: "Invalid email or password", isLoading = false)
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
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest { displayName = name }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { _ ->
                        uiState = uiState.copy(currentScreen = AppScreen.Home, isLoading = false)
                    }
                } else {
                    uiState = uiState.copy(error = task.exception?.localizedMessage ?: "Registration failed", isLoading = false)
                }
            }
    }

    fun logout() {
        auth.signOut()
        uiState = uiState.copy(currentScreen = AppScreen.Login)
    }

    fun addExpense(amount: Double, date: LocalDate, categoryId: String, description: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        uiState = uiState.copy(isLoading = true, error = null)
        val expenseData = hashMapOf(
            "amount" to amount,
            "date" to date.toString(),
            "categoryId" to categoryId,
            "description" to description,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(userId).collection("expenses")
            .add(expenseData)
            .addOnSuccessListener {
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
    }
}
