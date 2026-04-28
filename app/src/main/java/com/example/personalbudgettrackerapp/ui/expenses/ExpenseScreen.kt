package com.example.personalbudgettrackerapp.ui.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import com.example.personalbudgettrackerapp.data.getCategoryIcon
import com.example.personalbudgettrackerapp.ui.components.CustomDatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * The Expense Screen displays a comprehensive list of all recorded expenses.
 * It features searching, filtering by date range, and detailed viewing/deletion of individual expenses.
 */
@Composable
fun ExpenseScreen(viewModel: AppViewModel) {
    val uiState = viewModel.uiState
    val expenses = uiState.expenses
    val categories = uiState.categories

    // State for searching and filtering
    var searchQuery by remember { mutableStateOf("") }
    var dateFrom by remember { mutableStateOf<LocalDate?>(null) }
    var dateTo by remember { mutableStateOf<LocalDate?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableStateOf<String?>(null) }

    // Apply search and date filters to the expense list
    val filteredExpenses = remember(expenses, categories, searchQuery, dateFrom, dateTo) {
        expenses.filter { expense ->
            val matchesSearch = if (searchQuery.isNotBlank()) {
                val cat = categories.find { it.id == expense.categoryId }
                expense.description.contains(searchQuery, ignoreCase = true) ||
                        cat?.name?.contains(searchQuery, ignoreCase = true) == true
            } else true

            val matchesDateFrom = dateFrom?.let { !expense.date.isBefore(it) } ?: true
            val matchesDateTo = dateTo?.let { !expense.date.isAfter(it) } ?: true

            matchesSearch && matchesDateFrom && matchesDateTo
        }.sortedByDescending { it.date }
    }

    // Group expenses by date for categorical display in the list
    val groupedExpenses = remember(filteredExpenses) {
        filteredExpenses.groupBy { it.date }
    }

    val totalFiltered = filteredExpenses.sumOf { it.amount }
    val hasFilters = searchQuery.isNotBlank() || dateFrom != null || dateTo != null

    Scaffold(
        topBar = {
            // Header component containing search and filter controls
            Header(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                showFilters = showFilters,
                onToggleFilters = { showFilters = !showFilters },
                dateFrom = dateFrom,
                onDateFromChange = { dateFrom = it },
                dateTo = dateTo,
                onDateToChange = { dateTo = it },
                onClearFilters = {
                    searchQuery = ""
                    dateFrom = null
                    dateTo = null
                },
                hasFilters = hasFilters
            )
        },
        floatingActionButton = {
            // Floating Action Button to navigate to the Add Expense screen
            FloatingActionButton(
                onClick = { viewModel.setScreen(AppScreen.AddExpense) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary row showing total count and amount for the current view
            if (filteredExpenses.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f),
                    modifier = Modifier.fillMaxWidth()
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredExpenses.size} expense${if (filteredExpenses.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total: R${String.format(Locale.US, "%.2f", totalFiltered)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // List of expenses grouped by date
            if (groupedExpenses.isEmpty()) {
                EmptyState(hasFilters) { viewModel.setScreen(AppScreen.AddExpense) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedExpenses.forEach { (date, dayExpenses) ->
                        item(key = date) {
                            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.Builder().setLanguage("en").setRegion("ZA").build())
                            // Date header for the group
                            Text(
                                text = date.format(formatter).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp),
                                letterSpacing = 1.sp
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    dayExpenses.forEachIndexed { index, expense ->
                                        // Individual expense entry
                                        ExpenseItem(
                                            expense = expense,
                                            category = categories.find { it.id == expense.categoryId },
                                            onClick = { selectedExpenseId = expense.id }
                                        )
                                        // Divider between items in the same date group
                                        if (index < dayExpenses.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog showing detailed information about a selected expense
    selectedExpenseId?.let { id ->
        val expense = expenses.find { it.id == id }
        val category = categories.find { it.id == expense?.categoryId }
        if (expense != null) {
            ExpenseDetailDialog(
                expense = expense,
                category = category,
                onDismiss = { selectedExpenseId = null },
                onDelete = {
                    viewModel.deleteExpense(expense.id)
                    selectedExpenseId = null
                }
            )
        }
    }
}

/**
 * Top bar component for the Expense Screen, housing search and expandable filter options.
 */
@Composable
fun Header(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    dateFrom: LocalDate?,
    onDateFromChange: (LocalDate) -> Unit,
    dateTo: LocalDate?,
    onDateToChange: (LocalDate) -> Unit,
    onClearFilters: () -> Unit,
    hasFilters: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                // Filter toggle button
                if(showFilters){
                    Button(
                        onClick = onToggleFilters,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filter", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggleFilters,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filter", fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search expenses...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth() .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Expandable filters panel
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Date Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    // Date range pickers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomDatePicker(
                                value = dateFrom ?: LocalDate.now(),
                                onValueChange = onDateFromChange,
                                label = "From"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CustomDatePicker(
                                value = dateTo ?: LocalDate.now(),
                                onValueChange = onDateToChange,
                                label = "To"
                            )
                        }
                    }

                    // Clear filters button
                    if (hasFilters) {
                        TextButton(
                            onClick = onClearFilters,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear filters", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single row representing an expense in the list.
 */
@Composable
fun ExpenseItem(expense: Expense, category: Category?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category icon with tinted background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(category?.color?.copy(alpha = 0.15f) ?: Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getCategoryIcon(category?.icon ?: ""), fontSize = 20.sp)
                }
                Column {
                    Text(expense.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(category?.name ?: "No Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Expense amount shown in red
            Text(
                text = "-R${String.format(Locale.US, "%.2f", expense.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * UI state shown when no expenses match the current filters.
 */
@Composable
fun EmptyState(hasFilters: Boolean, onAddExpense: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text("No expenses found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = if (hasFilters) "Try adjusting your filters" else "Start tracking by adding your first expense",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!hasFilters) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddExpense,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Expense")
            }
        }
    }
}

/**
 * Detailed view of a single expense shown in a dialog.
 */
@Composable
fun ExpenseDetailDialog(expense: Expense, category: Category?, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Expense Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("View and manage this expense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(category?.color?.copy(alpha = 0.15f) ?: Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryIcon(category?.icon ?: ""), fontSize = 24.sp)
                    }
                    Column {
                        Text(expense.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(category?.name ?: "No Category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-R${String.format(Locale.US, "%.2f", expense.amount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.Builder().setLanguage("en").setRegion("ZA").build())
                        Text(expense.date.format(formatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Action button to delete the expense
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Expense")
                }
            }
        }
    }
}
