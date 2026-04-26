package com.example.personalbudgettrackerapp.ui.Catagory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.personalbudgettrackerapp.data.CategoryExtended
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme
import java.util.UUID

/**
 * Icon Options for categories.
 */
data class IconOption(val id: String, val emoji: String, val label: String)

val ICON_OPTIONS = listOf(
    IconOption("shopping-cart", "🛒", "Shopping"),
    IconOption("car", "🚗", "Transport"),
    IconOption("gamepad-2", "🎮", "Games"),
    IconOption("zap", "⚡", "Utilities"),
    IconOption("utensils", "🍽️", "Food"),
    IconOption("home", "🏠", "Home"),
    IconOption("heart", "❤️", "Health"),
    IconOption("briefcase", "💼", "Work"),
)

val COLOR_OPTIONS = listOf(
    Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFFF59E0B),
    Color(0xFFEF4444), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF84CC16),
)

/**
 * The Category Screen allows users to manage their spending categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categories: List<CategoryExtended>,
    onAddCategory: (CategoryExtended) -> Unit,
    onUpdateCategory: (CategoryExtended) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryExtended?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            editingCategory = null
                            showDialog = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onEdit = {
                        editingCategory = category
                        showDialog = true
                    },
                    onDelete = { onDeleteCategory(category.id) }
                )
            }

            item {
                AddButton(onClick = {
                    editingCategory = null
                    showDialog = true
                })
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        CategoryEditDialog(
            category = editingCategory,
            onDismiss = { showDialog = false },
            onSave = { name, icon, color ->
                if (editingCategory != null) {
                    onUpdateCategory(editingCategory!!.copy(name = name, icon = icon, color = color))
                } else {
                    onAddCategory(CategoryExtended(UUID.randomUUID().toString(), name, color, icon, false))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun CategoryItem(category: CategoryExtended, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryEmoji(category.icon), fontSize = 20.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, fontWeight = FontWeight.Medium)
                Text(
                    if (category.isDefault) "Default category" else "Custom category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
            }
            
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Add New Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: CategoryExtended?,
    onDismiss: () -> Unit,
    onSave: (String, String, Color) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "shopping-cart") }
    var selectedColor by remember { mutableStateOf(category?.color ?: COLOR_OPTIONS[0]) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (category != null) "Edit Category" else "New Category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = "" },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotEmpty()
                )
                
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ICON_OPTIONS.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { opt ->
                                val isSelected = selectedIcon == opt.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedIcon = opt.id }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(opt.emoji, fontSize = 20.sp)
                                        Text(opt.label, fontSize = 10.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    COLOR_OPTIONS.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(if (selectedColor == color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }
                
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isBlank()) errorMessage = "Please enter a name"
                            else onSave(name, selectedIcon, selectedColor)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (category != null) "Save" else "Create") }
                }
            }
        }
    }
}

fun getCategoryEmoji(iconId: String): String = ICON_OPTIONS.find { it.id == iconId }?.emoji ?: "📦"

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    val mockCategories = listOf(
        CategoryExtended("1", "Food", Color(0xFFEF4444), "utensils", true),
        CategoryExtended("2", "Transport", Color(0xFF3B82F6), "car", true),
        CategoryExtended("3", "Entertainment", Color(0xFFA855F7), "gamepad-2", false),
        CategoryExtended("4", "Shopping", Color(0xFFF59E0B), "shopping-cart", false)
    )

    PersonalBudgetTrackerAppTheme {
        CategoryScreen(
            categories = mockCategories,
            onAddCategory = {},
            onUpdateCategory = {},
            onDeleteCategory = {},
            onBack = {}
        )
    }
}
