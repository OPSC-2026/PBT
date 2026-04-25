package com.example.personalbudgettrackerapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*

class DashboardViewModel : ViewModel() {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(User("1", "John Doe", "john@example.com"))
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)

    val currentBudget = budgets.map { list ->
        list.find { it.month == currentMonth && it.year == currentYear }
    }

    val monthlyExpenses = expenses.map { list ->
        list.filter { e ->
            val cal = Calendar.getInstance().apply { timeInMillis = e.date }
            cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }

    val totalSpent = monthlyExpenses.map { list ->
        list.sumOf { it.amount }
    }

    val categorySpending = combine(monthlyExpenses, categories, currentBudget) { expenses, categories, budget ->
        val spendingMap = expenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        categories.map { cat ->
            CategorySpending(
                category = cat,
                spent = spendingMap[cat.id] ?: 0.0,
                budget = budget?.categoryBudgets?.get(cat.id) ?: 0.0
            )
        }
    }

    val chartData = monthlyExpenses.map { expenses ->
        val days = expenses.groupBy { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.DAY_OF_MONTH)
        }.mapValues { it.value.sumOf { it.amount } }
        
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        (1..today).map { day ->
            ChartPoint(day, days[day] ?: 0.0)
        }
    }

    init {
        // Mock data
        _categories.value = listOf(
            Category("1", "Shopping", "shopping-cart", "#FF9800"),
            Category("2", "Transport", "car", "#2196F3"),
            Category("3", "Entertainment", "gamepad-2", "#E91E63"),
            Category("4", "Utilities", "zap", "#FFEB3B"),
            Category("5", "Food", "utensils", "#4CAF50"),
            Category("6", "Home", "home", "#9C27B0")
        )

        _budgets.value = listOf(
            Budget(currentMonth, currentYear, 5000.0, mapOf(
                "1" to 1000.0, 
                "2" to 500.0,
                "5" to 1500.0
            ))
        )

        _expenses.value = listOf(
            Expense("1", 250.0, "1", "Groceries", System.currentTimeMillis()),
            Expense("2", 100.0, "2", "Fuel", System.currentTimeMillis() - 86400000),
            Expense("3", 50.0, "5", "Coffee", System.currentTimeMillis() - 172800000),
            Expense("4", 1200.0, "5", "Dinner", System.currentTimeMillis() - 3600000),
            Expense("5", 300.0, "3", "Movie", System.currentTimeMillis() - 432000000)
        )
    }

    fun logout() {
        _currentUser.value = null
    }

    fun setScreen(screen: String) {
        // Navigation logic would go here
    }
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