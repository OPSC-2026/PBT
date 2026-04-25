package com.example.personalbudgettrackerapp

data class Expense(
    val id: String,
    val amount: Double,
    val categoryId: String,
    val description: String,
    val date: Long
)