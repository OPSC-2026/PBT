package com.example.personalbudgettrackerapp.data

import androidx.compose.ui.graphics.Color

/**
 * Data model for categories with extended properties for UI management.
 */
data class CategoryExtended(
    val id: String,
    val name: String,
    val color: Color,
    val icon: String,
    val isDefault: Boolean = false
)
