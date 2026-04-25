package com.example.personalbudgettrackerapp

data class Budget(
    val month: Int,
    val year: Int,
    val totalBudget: Double,
    val categoryBudgets: Map<String, Double>
)