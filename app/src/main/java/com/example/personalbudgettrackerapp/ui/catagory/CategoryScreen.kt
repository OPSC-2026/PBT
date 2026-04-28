package com.example.personalbudgettrackerapp.ui.catagory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.getCategoryIcon
import java.util.UUID

/**
 * Data class representing an icon selection option in the category editor.
 */
data class IconOption(val id: String, val emoji: String, val label: String)

/**
 * Predefined list of icon options for categories.
 */
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

/**
 * Predefined list of color options for categories.
 */
val COLOR_OPTIONS = listOf(
    Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFFF59E0B),
    Color(0xFFEF4444), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF84CC16),
)

/**
 * The Category Screen allows users to manage their spending categories.
 * Users can view, add, edit, or delete categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: AppViewModel) {
    val uiState = viewModel.uiState
    val categories = uiState.categories

    // State for managing the edit/add category dialog
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .drawBehind {
                        // Draw a thin separator line at the bottom of the top bar
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 20.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.setScreen(AppScreen.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Button to open the dialog for adding a new category
                    Button(
                        onClick = {
                            editingCategory = null
                            showDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "add Category", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 14.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display each category in a list
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onEdit = {
                        editingCategory = category
                        showDialog = true
                    },
                    onDelete = { viewModel.deleteCategory(category.id) }
                )
            }

            // A prominent button at the end of the list to add a new category
            item {
                AddButton(onClick = {
                    editingCategory = null
                    showDialog = true
                })
            }

            // Bottom spacer to ensure content isn't hidden by navigation or floating elements
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Category editor dialog
    if (showDialog) {
        CategoryEditDialog(
            category = editingCategory,
            onDismiss = { showDialog = false },
            onSave = { name, icon, color ->
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.copy(name = name, icon = icon, color = color))
                } else {
                    viewModel.addCategory(Category(UUID.randomUUID().toString(), name, color, icon, false))
                }
                showDialog = false
            }
        )
    }
}

/**
 * A single category item in the list showing its icon, name, and actions.
 */
@Composable
fun CategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with tinted background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryIcon(category.icon), fontSize = 20.sp)
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

            // Only allow deletion for custom categories
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * A large, clickable box used to trigger the "Add New Category" action.
 */
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

/**
 * A dialog for creating or editing a category.
 * Provides inputs for name, icon selection, and color selection.
 */
@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, Color) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "shopping-cart") }
    var selectedColor by remember { mutableStateOf(category?.color ?: COLOR_OPTIONS[0]) }
    var error by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Header section
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (category != null) "Edit Category" else "New Category",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (category != null) "Update your category details" else "Create a custom spending category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Category Name Input
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Name",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; error = "" },
                            placeholder = { Text("e.g., Subscriptions") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = error.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Icon Selection Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Icon",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ICON_OPTIONS.chunked(4).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { opt ->
                                        val isSelected = selectedIcon == opt.id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF15803D)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                )
                                                .clickable { selectedIcon = opt.id }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(opt.emoji, fontSize = 20.sp)
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    opt.label,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isSelected) Color.White
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Color Selection Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Color",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            COLOR_OPTIONS.forEach { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) Modifier.border(
                                                width = 2.dp,
                                                color = Color.Black,
                                                shape = CircleShape
                                            ).padding(2.dp) else Modifier
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }
                }

                // Preview section to see how the category will look
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(20.dp)
                    ) {
                        Text(
                            "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(selectedColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getCategoryIcon(selectedIcon), fontSize = 22.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = name.ifBlank { "Category Name" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Display validation error if any
                if (error.isNotEmpty()) {
                    item {
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Action buttons: Cancel and Save/Create
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    error = "Please enter a category name"
                                } else {
                                    onSave(name, selectedIcon, selectedColor)
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                if (category != null) "Save" else "Create Category",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
