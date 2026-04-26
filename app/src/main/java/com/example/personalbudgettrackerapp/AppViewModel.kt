package com.example.personalbudgettrackerapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalbudgettrackerapp.data.CategoryExtended
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.compose.ui.graphics.Color

import com.example.personalbudgettrackerapp.data.Budget
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import java.time.LocalDate

private const val TAG = "AppViewModel"

sealed interface AppScreen {
    data object Login : AppScreen
    data object Register : AppScreen
    data object Home : AppScreen
    data object Rewards : AppScreen
    data object Analytics : AppScreen
    data object AddExpense : AppScreen
    data object Categories : AppScreen
    data object Settings : AppScreen
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
    val categories: List<CategoryExtended> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList()
)

class AppViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(
        AppUiState(
            currentScreen = if (auth.currentUser != null) AppScreen.Home else AppScreen.Login,
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

    init {
        if (auth.currentUser != null) {
            observeCategories()
        }
    }

    private fun observeCategories() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            CategoryExtended(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                color = androidx.compose.ui.graphics.Color(doc.getLong("color")?.toInt() ?: 0),
                                icon = doc.getString("icon") ?: "",
                                isDefault = doc.getBoolean("isDefault") ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing category", e)
                            null
                        }
                    }
                    uiState = uiState.copy(categories = categories)
                }
            }
    }

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
                if (task.isSuccessful) {
                    observeCategories()
                    uiState = uiState.copy(currentScreen = AppScreen.Home, isLoading = false)
                } else {
                    Log.e(TAG, "Login failed", task.exception)
                    uiState = uiState.copy(
                        error = task.exception?.localizedMessage ?: "Invalid email or password",
                        isLoading = false
                    )
                }
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

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { _ ->
                            observeCategories()
                            uiState = uiState.copy(
                                currentScreen = AppScreen.Home,
                                isLoading = false
                            )
                        }
                } else {
                    Log.e(TAG, "Registration failed", task.exception)
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

    /**
     * Deletes a specific category from the user's Firestore collection.
     * Updates [uiState] to reflect loading and potential error states.
     *
     * @param categoryId The unique identifier of the category to be deleted.
     * @param onSuccess Callback triggered after successful deletion.
     */
    fun deleteCategory(categoryId: String, onSuccess: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .collection("categories").document(categoryId)
                    .delete()
                    .await()

                Log.d(TAG, "Category deleted: $categoryId")
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category", e)
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    /**
     * Updates an existing category's information in Firestore.
     * Converts the category's color to an ARGB integer for storage.
     *
     * @param category The [CategoryExtended] object containing the updated data.
     * @param onSuccess Callback triggered after the update is successfully persisted.
     */
    fun updateCategory(category: CategoryExtended, onSuccess: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        uiState = uiState.copy(isLoading = true, error = null)

        val categoryData = hashMapOf(
            "name" to category.name,
            "color" to category.color.toArgb(),
            "icon" to category.icon,
            "isDefault" to category.isDefault
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .collection("categories").document(category.id)
                    .set(categoryData)
                    .await()

                Log.d(TAG, "Category updated: ${category.id}")
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating category", e)
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    /**
     * Adds a new category to the user's Firestore collection.
     * The Firestore document ID is automatically generated upon addition.
     *
     * @param category The [CategoryExtended] data to be saved as a new category.
     * @param onSuccess Callback triggered after the new category is successfully added.
     */
    fun addCategory(category: CategoryExtended, onSuccess: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        uiState = uiState.copy(isLoading = true, error = null)

        val categoryData = hashMapOf(
            "name" to category.name,
            "color" to category.color.toArgb(),
            "icon" to category.icon,
            "isDefault" to category.isDefault
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .collection("categories")
                    .add(categoryData)
                    .await()

                Log.d(TAG, "Category added: ${category.name}")
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding category", e)
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
