package com.example.personalbudgettrackerapp.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class Expense(
    val id: String,
    val amount: Double,
    val date: LocalDate,
    val categoryId: String,
    val description: String
)

data class Category(
    val id: String,
    val name: String,
    val color: Color,
    val icon: String,
    val isDefault: Boolean = false
)

data class Budget(
    val id: String,
    val month: Int,
    val year: Int,
    val totalBudget: Double,
    val categoryBudgets: Map<String, Double>
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconId: String,
    val unlocked: Boolean,
    val unlockedAt: Long? = null,
    val progress: Float = 0f,
    val condition: String
)

enum class TimeRange {
    WEEK, MONTH, YEAR
}

fun getCategoryIcon(iconName: String): String {
    return when (iconName) {
        "shopping-cart" -> "🛒"
        "car" -> "🚗"
        "gamepad-2" -> "🎮"
        "zap" -> "⚡"
        "utensils" -> "🍽️"
        "home" -> "🏠"
        "heart" -> "❤️"
        "briefcase" -> "💼"
        else -> "📦"
    }
}
