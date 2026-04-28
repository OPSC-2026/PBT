package com.example.personalbudgettrackerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalbudgettrackerapp.data.Achievement
import com.example.personalbudgettrackerapp.data.Budget
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Represents the different screens available in the application for navigation.
 */
sealed interface AppScreen {
    data object Login : AppScreen
    data object Register : AppScreen
    data object Home : AppScreen
    data object Rewards : AppScreen
    data object Analytics : AppScreen
    data object AddExpense : AppScreen
    data object Categories : AppScreen
    data object Settings : AppScreen
    data object Expense : AppScreen
}

/**
 * Data class to represent spending details for a specific category.
 */
data class CategorySpending(
    val category: Category,
    val spent: Double,
    val budget: Double
)

/**
 * Data class representing a single point on a chart.
 */
data class ChartPoint(
    val day: Int,
    val amount: Double
)

/**
 * Encapsulates the entire UI state for the application.
 */
data class AppUiState(
    val currentScreen: AppScreen = AppScreen.Login,
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

/**
 * The central ViewModel for the application, managing state and data synchronization with Firebase.
 * It handles authentication, expense tracking, budget management, and achievement processing.
 */
class AppViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Predefined achievements available in the app
    private val defaultAchievements = listOf(
        Achievement("1", "Budget Master", "Stay within budget for a month", "trophy", false, null, 0f, "budget_master"),
        Achievement("2", "Expense Tracker", "Log expenses 7 days in a row", "calendar-check", false, null, 0f, "expense_tracker"),
        Achievement("3", "Saving Champion", "Use less than 80% of your budget", "piggy-bank", false, null, 0f, "saving_champion"),
        Achievement("4", "First Step", "Add your first expense", "footprints", false, null, 0f, "first_expense"),
        Achievement("5", "Category Creator", "Create a custom category", "folder-plus", false, null, 0f, "category_creator")
    )

    // Publicly exposed UI state using Compose state for reactivity
    var uiState by mutableStateOf(
        AppUiState(
            currentScreen = if (auth.currentUser != null) AppScreen.Home else AppScreen.Login,
            achievements = defaultAchievements
        )
    )
        private set

    init {
        // If a user is already logged in, start observing data immediately
        if (auth.currentUser != null) {
            observeData()
        }
    }

    /**
     * Sets up listeners for all relevant data collections in Firestore.
     */
    private fun observeData() {
        observeCategories()
        observeExpenses()
        observeBudgets()
        observeAchievements()
    }

    /**
     * Listens for changes in the user's categories.
     */
    private fun observeCategories() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            Category(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                color = androidx.compose.ui.graphics.Color(doc.getLong("color")?.toInt() ?: 0),
                                icon = doc.getString("icon") ?: "",
                                isDefault = doc.getBoolean("isDefault") ?: false
                            )
                        } catch (_: Exception) { null }
                    }
                    uiState = uiState.copy(categories = categories)
                    checkAchievements()
                }
            }
    }

    /**
     * Listens for changes in the user's expense logs.
     */
    private fun observeExpenses() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { doc ->
                        try {
                            Expense(
                                id = doc.id,
                                amount = doc.getDouble("amount") ?: 0.0,
                                date = LocalDate.parse(doc.getString("date") ?: LocalDate.now().toString()),
                                categoryId = doc.getString("categoryId") ?: "",
                                description = doc.getString("description") ?: ""
                            )
                        } catch (_: Exception) { null }
                    }
                    uiState = uiState.copy(expenses = expenses.sortedByDescending { it.date })
                    checkAchievements()
                }
            }
    }

    /**
     * Listens for changes in the user's budget configurations.
     */
    private fun observeBudgets() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("budgets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val budgets = snapshot.documents.mapNotNull { doc ->
                        try {
                            val categoryBudgetsRaw = doc.get("categoryBudgets") as? Map<String, Any> ?: emptyMap()
                            val categoryBudgets = categoryBudgetsRaw.mapValues { (_, v) ->
                                when (v) { is Double -> v; is Long -> v.toDouble(); else -> 0.0 }
                            }
                            Budget(
                                id = doc.id,
                                month = doc.getLong("month")?.toInt() ?: 1,
                                year = doc.getLong("year")?.toInt() ?: 2026,
                                totalBudget = doc.getDouble("totalBudget") ?: 0.0,
                                categoryBudgets = categoryBudgets
                            )
                        } catch (_: Exception) { null }
                    }
                    uiState = uiState.copy(budgets = budgets)
                    checkAchievements()
                }
            }
    }

    /**
     * Listens for changes in the user's unlocked achievements.
     */
    private fun observeAchievements() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("achievements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val firestoreData = snapshot.documents.associate { doc ->
                        val unlocked = doc.getBoolean("unlocked") ?: false
                        val unlockedAt = doc.getLong("unlockedAt")
                        doc.id to (unlocked to unlockedAt)
                    }

                    val updatedAchievements = uiState.achievements.map { ach ->
                        val data = firestoreData[ach.id]
                        if (data != null) {
                            ach.copy(unlocked = data.first, unlockedAt = data.second)
                        } else ach
                    }
                    uiState = uiState.copy(achievements = updatedAchievements)
                    checkAchievements()
                }
            }
    }

    /**
     * Evaluates the current state of expenses, categories, and budgets to update achievement progress.
     */
    private fun checkAchievements() {
        val expenses = uiState.expenses
        val categories = uiState.categories
        val budgets = uiState.budgets
        val now = LocalDate.now()
        val currentBudget = budgets.find { it.month == now.monthValue && it.year == now.year }
        val monthlyExpenses = expenses.filter { it.date.monthValue == now.monthValue && it.date.year == now.year }
        val totalSpent = monthlyExpenses.sumOf { it.amount }

        var anyUpdated = false
        val updatedList = uiState.achievements.map { achievement ->
            if (achievement.unlocked) return@map achievement

            var progress = 0f
            var shouldUnlock = false

            when (achievement.condition) {
                "first_expense" -> {
                    shouldUnlock = expenses.isNotEmpty()
                    progress = if (shouldUnlock) 100f else 0f
                }
                "category_creator" -> {
                    shouldUnlock = categories.any { !it.isDefault }
                    progress = if (shouldUnlock) 100f else 0f
                }
                "saving_champion" -> {
                    if (currentBudget != null && currentBudget.totalBudget > 0) {
                        shouldUnlock = totalSpent > 0 && totalSpent <= currentBudget.totalBudget * 0.8
                        progress = ((totalSpent / (currentBudget.totalBudget * 0.8)) * 100).toFloat().coerceIn(0f, 100f)
                    }
                }
                "budget_master" -> {
                    if (currentBudget != null && currentBudget.totalBudget > 0) {
                        shouldUnlock = totalSpent > 0 && totalSpent <= currentBudget.totalBudget
                        progress = ((totalSpent / currentBudget.totalBudget) * 100).toFloat().coerceIn(0f, 100f)
                    }
                }
                "expense_tracker" -> {
                    // Check for consecutive days of logging expenses
                    val dates = expenses.map { it.date }.distinct().sortedDescending()
                    var consecutive = 0
                    if (dates.isNotEmpty()) {
                        var current = LocalDate.now()
                        for (date in dates) {
                            if (date == current) {
                                consecutive++
                                current = current.minusDays(1)
                            } else if (date.isBefore(current)) {
                                break
                            }
                        }
                    }
                    shouldUnlock = consecutive >= 7
                    progress = (consecutive.toFloat() / 7f * 100f).coerceIn(0f, 100f)
                }
            }

            if (shouldUnlock) {
                unlockAchievement(achievement.id)
                anyUpdated = true
                achievement.copy(unlocked = true, unlockedAt = System.currentTimeMillis(), progress = 100f)
            } else if (progress != achievement.progress) {
                anyUpdated = true
                achievement.copy(progress = progress)
            } else {
                achievement
            }
        }

        if (anyUpdated) {
            uiState = uiState.copy(achievements = updatedList)
        }
    }

    /**
     * Persists an unlocked achievement to Firestore.
     */
    private fun unlockAchievement(id: String) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "unlocked" to true,
            "unlockedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId).collection("achievements").document(id)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
    }

    /**
     * Changes the current active screen.
     */
    fun setScreen(screen: AppScreen) {
        uiState = uiState.copy(currentScreen = screen, error = null)
    }

    /**
     * Handles user login with Firebase Authentication.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all fields")
            return
        }
        uiState = uiState.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    observeData()
                    uiState = uiState.copy(currentScreen = AppScreen.Home, isLoading = false)
                } else {
                    uiState = uiState.copy(error = task.exception?.localizedMessage ?: "Invalid email or password", isLoading = false)
                }
            }
    }

    /**
     * Handles user registration and profile initialization.
     */
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
                        observeData()
                        uiState = uiState.copy(currentScreen = AppScreen.Home, isLoading = false)
                    }
                } else {
                    uiState = uiState.copy(error = task.exception?.localizedMessage ?: "Registration failed", isLoading = false)
                }
            }
    }

    /**
     * Logs the user out and clears the current UI state.
     */
    fun logout() {
        auth.signOut()
        uiState = uiState.copy(currentScreen = AppScreen.Login, expenses = emptyList(), categories = emptyList(), budgets = emptyList())
    }

    /**
     * Adds a new expense entry for the authenticated user.
     */
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
     * Deletes a specific expense entry.
     */
    fun deleteExpense(expenseId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("expenses").document(expenseId)
            .delete()
            .addOnFailureListener { e ->
                uiState = uiState.copy(error = e.localizedMessage)
            }
    }

    /**
     * Updates or creates a budget configuration for a specific month and year.
     */
    fun updateBudget(month: Int, year: Int, total: Double, categoryLimits: Map<String, Double>) {
        val userId = auth.currentUser?.uid ?: return
        val budgetId = "${year}_${String.format("%02d", month)}"
        val budgetData = hashMapOf(
            "month" to month,
            "year" to year,
            "totalBudget" to total,
            "categoryBudgets" to categoryLimits
        )
        db.collection("users").document(userId).collection("budgets").document(budgetId)
            .set(budgetData)
            .addOnFailureListener { e ->
                uiState = uiState.copy(error = e.localizedMessage)
            }
    }

    /**
     * Deletes a custom category created by the user.
     */
    fun deleteCategory(categoryId: String, onSuccess: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return
        uiState = uiState.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("categories").document(categoryId).delete().await()
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    /**
     * Updates an existing category's details.
     */
    fun updateCategory(category: Category, onSuccess: () -> Unit = {}) {
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
                db.collection("users").document(userId).collection("categories").document(category.id).set(categoryData).await()
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    /**
     * Adds a new custom category for the user.
     */
    fun addCategory(category: Category, onSuccess: () -> Unit = {}) {
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
                db.collection("users").document(userId).collection("categories").add(categoryData).await()
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}