package com.example.personalbudgettrackerapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val categories by viewModel.categories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val currentBudget by viewModel.currentBudget.collectAsState(initial = null)
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState(initial = emptyList())
    val totalSpent by viewModel.totalSpent.collectAsState(initial = 0.0)
    val categorySpending by viewModel.categorySpending.collectAsState(initial = emptyList())
    val chartData by viewModel.chartData.collectAsState(initial = emptyList())

    DashboardScreenContent(
        currentUser = currentUser,
        currentBudget = currentBudget,
        monthlyExpenses = monthlyExpenses,
        totalSpent = totalSpent,
        categorySpending = categorySpending,
        chartData = chartData,
        categories = categories,
        onLogout = { viewModel.logout() },
        onNavigate = { viewModel.setScreen(it) }
    )
}

@Composable
fun DashboardScreenContent(
    currentUser: User?,
    currentBudget: Budget?,
    monthlyExpenses: List<Expense>,
    totalSpent: Double,
    categorySpending: List<CategorySpending>,
    chartData: List<ChartPoint>,
    categories: List<Category>,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val totalBudget = currentBudget?.totalBudget ?: 0.0
    val remaining = totalBudget - totalSpent
    val percentUsed = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val isOverBudget = remaining < 0

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                HeaderSection(
                    username = currentUser?.username ?: "User",
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    percentUsed = percentUsed,
                    remaining = remaining,
                    isOverBudget = isOverBudget,
                    currencyFormatter = currencyFormatter,
                    onLogout = onLogout
                )
            }

            // Quick Stats
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        label = "Transactions",
                        value = "${monthlyExpenses.size} expenses",
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Adjust,
                        label = "Status",
                        value = if (isOverBudget) "Over Budget" else "On Track",
                        iconColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Spending Trend
            item {
                SpendingTrendCard(chartData = chartData, onNavigate = { onNavigate("graphs") })
            }

            // Category Spending
            item {
                CategorySpendingCard(
                    categorySpending = categorySpending,
                    currencyFormatter = currencyFormatter,
                    onNavigate = { onNavigate("categories") }
                )
            }

            // Recent Expenses
            item {
                RecentExpensesCard(
                    expenses = monthlyExpenses.take(4),
                    categories = categories,
                    currencyFormatter = currencyFormatter,
                    onNavigate = { onNavigate("expense-list") }
                )
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { onNavigate("add-expense") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add expense")
        }

        // Simple Bottom Nav Placeholder
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            tonalElevation = 8.dp
        ) {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Expenses") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                    label = { Text("Stats") },
                    selected = false,
                    onClick = { }
                )
            }
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
                    "Welcome back,",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    username,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onLogout,
                modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Monthly Budget",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        currencyFormatter.format(totalBudget),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        currencyFormatter.format(totalSpent),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "spent",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { percentUsed.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(percentUsed * 100).toInt()}% used",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        "${currencyFormatter.format(abs(remaining))} ${if (isOverBudget) "over" else "left"}",
                        color = if (isOverBudget) Color(0xFFFFCDD2) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SpendingTrendCard(chartData: List<ChartPoint>, onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spending Trend", fontWeight = FontWeight.Bold)
                Text(
                    "View all",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chartData.isNotEmpty()) {
                val maxAmount = chartData.maxOfOrNull { it.amount } ?: 1.0
                Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (chartData.size - 1).coerceAtLeast(1)

                    val path = Path()
                    chartData.forEachIndexed { index, point ->
                        val x = index * spacing
                        val y = height - (point.amount.toFloat() / maxAmount.toFloat() * height)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF4CAF50), // Standard Green
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
                            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.3f), Color.Transparent)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("By Category", fontWeight = FontWeight.Bold)
                Text(
                    "Manage",
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
                                Text(item.category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    currencyFormatter.format(item.spent),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                if (item.budget > 0) {
                                    Text(
                                        " / ${currencyFormatter.format(item.budget)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (item.budget > 0) {
                            LinearProgressIndicator(
                                progress = { catPercent.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
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
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Expenses", fontWeight = FontWeight.Bold)
                Text(
                    "View all",
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
                                    .background(Color.Gray.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getCategoryEmoji(cat?.icon ?: ""))
                            }
                            Column {
                                Text(expense.description, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            "-${currencyFormatter.format(expense.amount)}",
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val sampleCategories = listOf(
        Category("1", "Shopping", "shopping-cart", "#FF9800"),
        Category("2", "Transport", "car", "#2196F3"),
        Category("3", "Entertainment", "gamepad-2", "#E91E63"),
        Category("4", "Utilities", "zap", "#FFEB3B"),
        Category("5", "Food", "utensils", "#4CAF50"),
        Category("6", "Home", "home", "#9C27B0")
    )
    val sampleUser = User("1", "John Doe", "john@example.com")
    val sampleBudget = Budget(10, 2023, 5000.0, mapOf("1" to 1000.0, "2" to 500.0, "5" to 1500.0))
    val sampleExpenses = listOf(
        Expense("1", 250.0, "1", "Groceries", System.currentTimeMillis()),
        Expense("2", 100.0, "2", "Fuel", System.currentTimeMillis() - 86400000),
        Expense("3", 50.0, "5", "Coffee", System.currentTimeMillis() - 172800000),
        Expense("4", 1200.0, "5", "Dinner", System.currentTimeMillis() - 3600000),
        Expense("5", 300.0, "3", "Movie", System.currentTimeMillis() - 432000000)
    )
    val sampleCategorySpending = listOf(
        CategorySpending(sampleCategories[0], 250.0, 1000.0),
        CategorySpending(sampleCategories[1], 100.0, 500.0),
        CategorySpending(sampleCategories[4], 1250.0, 1500.0)
    )
    val sampleChartData = listOf(
        ChartPoint(1, 100.0),
        ChartPoint(2, 200.0),
        ChartPoint(3, 150.0),
        ChartPoint(4, 300.0),
        ChartPoint(5, 250.0)
    )

    PersonalBudgetTrackerAppTheme {
        DashboardScreenContent(
            currentUser = sampleUser,
            currentBudget = sampleBudget,
            monthlyExpenses = sampleExpenses,
            totalSpent = 1600.0,
            categorySpending = sampleCategorySpending,
            chartData = sampleChartData,
            categories = sampleCategories,
            onLogout = {},
            onNavigate = {}
        )
    }
}
