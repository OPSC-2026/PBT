package com.example.personalbudgettrackerapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel
import com.example.personalbudgettrackerapp.CategorySpending
import com.example.personalbudgettrackerapp.ChartPoint
import com.example.personalbudgettrackerapp.data.Budget
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val auth = FirebaseAuth.getInstance()
    val uiState = viewModel.uiState
    val now = LocalDate.now()
    
    val currentBudget = remember(uiState.budgets, now) {
        uiState.budgets.find { (it.month == now.monthValue) && (it.year == now.year) }
    }
    
    val monthlyExpenses = remember(uiState.expenses, now) {
        uiState.expenses.filter { (it.date.monthValue == now.monthValue) && (it.date.year == now.year) }
    }
    
    val totalSpent = remember(monthlyExpenses) {
        monthlyExpenses.sumOf { it.amount }
    }
    
    val totalBudget = currentBudget?.totalBudget ?: 0.0
    val remaining = totalBudget - totalSpent
    val percentUsed = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val isOverBudget = remaining < 0

    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("ZA").build()) 
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Section
            item {
                HeaderSection(
                    username = auth.currentUser?.displayName ?: "User",
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    percentUsed = percentUsed,
                    remaining = remaining,
                    isOverBudget = isOverBudget,
                    currencyFormatter = currencyFormatter
                ) { viewModel.logout() }
            }

            // Quick Stats Section
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        label = "This Month",
                        value = "${monthlyExpenses.size} expenses",
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Adjust,
                        label = "Budget Status",
                        value = if (isOverBudget) "Over Budget" else "On Track",
                        iconColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Spending Trend Section
            item {
                val chartData = remember(monthlyExpenses, now) {
                    val days = monthlyExpenses.groupBy { it.date.dayOfMonth }
                        .mapValues { it.value.sumOf { e -> e.amount } }
                    (1..now.dayOfMonth).map { day ->
                        ChartPoint(day, days[day] ?: 0.0)
                    }
                }
                SpendingTrendCard(
                    chartPoints = chartData,
                    onNavigate = { viewModel.setScreen(AppScreen.Analytics) }
                )
            }

            // Category Spending Section
            item {
                val categorySpending = remember(monthlyExpenses, uiState.categories, currentBudget) {
                    val spendingMap = monthlyExpenses.groupBy { it.categoryId }
                        .mapValues { it.value.sumOf { e -> e.amount } }
                    uiState.categories.map { cat ->
                        CategorySpending(
                            category = cat,
                            spent = spendingMap[cat.id] ?: 0.0,
                            budget = currentBudget?.categoryBudgets?.get(cat.id) ?: 0.0
                        )
                    }
                }
                CategorySpendingCard(
                    categorySpending = categorySpending,
                    currencyFormatter = currencyFormatter,
                    onNavigate = { /* Manage categories screen */ }
                )
            }

            // Recent Expenses Section
            item {
                RecentExpensesCard(
                    expenses = uiState.expenses.asSequence().sortedByDescending { it.date }.take(4).toList(),
                    categories = uiState.categories,
                    currencyFormatter = currencyFormatter,
                    onNavigate = { /* All expenses screen */ }
                )
            }

            item{
                Spacer(modifier = Modifier.height(40.dp))
            }

        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.setScreen(AppScreen.AddExpense) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add expense")
        }
    }
}

@Composable
fun HeaderSection(
    username: String,
    totalBudget: Double,
    totalSpent: Double,
    percentUsed: Float,
    remaining: Double,
    isOverBudget: Boolean,
    currencyFormatter: NumberFormat,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 48.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = username,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onLogout,
                modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Budget",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = currencyFormatter.format(totalBudget),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currencyFormatter.format(totalSpent),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "spent",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { percentUsed.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(percentUsed * 100).toInt()}% used",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${currencyFormatter.format(abs(remaining))} ${if (isOverBudget) "over" else "left"}",
                        color = if (isOverBudget) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Button(onClick = { viewModel.setScreen(AppScreen.Categories) }) {
                Text("Manage Categories")
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun SpendingTrendCard(chartPoints: List<ChartPoint>, onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Spending Trend", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "View all",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chartPoints.isNotEmpty()) {
                val maxAmount = chartPoints.maxOfOrNull { it.amount }?.toFloat()?.coerceAtLeast(1f) ?: 1f
                Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (chartPoints.size - 1).coerceAtLeast(1)

                    val path = Path()
                    chartPoints.forEachIndexed { index, point ->
                        val x = index * spacing
                        val y = height - (point.amount.toFloat() / maxAmount * height)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF00A368), 
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Fill area
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF00A368).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySpendingCard(
    categorySpending: List<CategorySpending>,
    currencyFormatter: NumberFormat,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "By Category", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Manage",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categorySpending.forEach { item ->
                    val catPercent = if (item.budget > 0) (item.spent / item.budget).toFloat() else 0f
                    val isOver = item.budget > 0 && item.spent > item.budget

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(getCategoryEmoji(item.category.icon), fontSize = 18.sp)
                                Text(text = item.category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currencyFormatter.format(item.spent),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                if (item.budget > 0) {
                                    Text(
                                        text = " / ${currencyFormatter.format(item.budget)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (item.budget > 0) {
                            LinearProgressIndicator(
                                progress = { catPercent.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentExpensesCard(
    expenses: List<Expense>,
    categories: List<Category>,
    currencyFormatter: NumberFormat,
    onNavigate: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Expenses", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "View all",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                expenses.forEach { expense ->
                    val cat = categories.find { it.id == expense.categoryId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getCategoryEmoji(cat?.icon ?: ""))
                            }
                            Column {
                                Text(
                                    text = expense.description, 
                                    fontSize = 14.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = expense.date.format(dateFormatter),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "-${currencyFormatter.format(expense.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryEmoji(iconName: String): String {
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
