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
    val icon: String
)

data class Budget(
    val id: String,
    val month: Int,
    val year: Int,
    val totalBudget: Double,
    val categoryBudgets: Map<String, Double>
)

enum class TimeRange {
    WEEK, MONTH, YEAR
}
