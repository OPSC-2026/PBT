package com.example.personalbudgettrackerapp.ui.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.getCategoryIcon
import com.example.personalbudgettrackerapp.ui.components.CustomDatePicker
import java.time.LocalDate

/**
 * The Add Expense screen provides a form to record new spending.
 * It includes an amount input, category selection, date picker, and description field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpense(viewModel: AppViewModel) {
    // Local state for the expense form
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var categoryId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    // Display success animation and message upon successful addition
    if (success) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Expense Added!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Redirecting to dashboard...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Delay and then navigate back to the Home Screen
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            viewModel.setScreen(AppScreen.Home)
        }
        return
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .drawBehind {
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 40.dp, bottom = 20.dp, start = 8.dp, end = 16.dp)

                ) {
                    IconButton(onClick = { viewModel.setScreen(AppScreen.Home) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),

            ) {
            item {
                // Large amount input area with currency symbol
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "R",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Box(contentAlignment = Alignment.Center) {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = amount,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() || char == '.' }) amount =
                                            it
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        fontSize = 45.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.widthIn(min = 100.dp)
                                )
                            }
                        }
                    }
                }
            }



            item {
                // Category grid for easy selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(viewModel.uiState.categories) { category ->
                            CardCategory(
                                category,
                                isSelected = categoryId == category.id,
                                onSelect = { categoryId = category.id }
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date input utilizing the custom date picker
                    CustomDatePicker(
                        value = date,
                        onValueChange = { date = it },
                        label = "Date"
                    )

                    // Text input for expense description
                    Column {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("What was this expense for?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            item {
                // Optional action button for receipts
                OutlinedButton(
                    onClick = { /* Optional functionality */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add receipt (optional)", fontWeight = FontWeight.Medium)
                }
            }
            item {
                // Display localized or ViewModel-level errors
                val combinedError = error.ifEmpty { viewModel.uiState.error ?: "" }
                if (combinedError.isNotEmpty()) {
                    Text(
                        text = combinedError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                // Main action button to save the expense
                Button(
                    onClick = {
                        error = ""
                        val amountVal = amount.toDoubleOrNull()
                        // Input validation
                        if (amountVal == null || amountVal <= 0) {
                            error = "Please enter a valid amount"
                        } else if (categoryId.isEmpty()) {
                            error = "Please select a category"
                        } else if (description.trim().isEmpty()) {
                            error = "Please enter a description"
                        } else {
                            viewModel.addExpense(
                                amount = amountVal,
                                date = date,
                                categoryId = categoryId,
                                description = description.trim(),
                                onSuccess = { success = true }
                            )
                        }
                    },
                    enabled = !viewModel.uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Save Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/**
 * A selectable category card for the Add Expense form.
 */
@Composable
fun CardCategory(
    category: Category,
    isSelected: Boolean,
    onSelect: () -> Unit
){
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 0.5.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = getCategoryIcon(category.icon),
            fontSize = 24.sp
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}