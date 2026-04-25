package com.example.personalbudgettrackerapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.data.Budget
import com.example.personalbudgettrackerapp.data.Category
import com.example.personalbudgettrackerapp.data.Expense
import com.example.personalbudgettrackerapp.data.TimeRange
import com.example.personalbudgettrackerapp.ui.theme.PersonalBudgetTrackerAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * The main screen for displaying financial analytics.
 * It provides insights into spending habits through summaries, charts, and lists.
 */
@Composable
fun AnalyticsScreen(
    expenses: List<Expense>,
    categories: List<Category>,
    budgets: List<Budget>
) {
    // State to track the selected time filter (Week, Month, Year)
    var timeRange by remember { mutableStateOf(TimeRange.MONTH) }
    val now = LocalDate.now()

    // Find the budget for the current month and year
    val currentBudget = remember(budgets, now) {
        budgets.find { it.month == now.monthValue && it.year == now.year }
    }

    // Filter expenses based on the selected time range
    val filteredExpenses = remember(expenses, timeRange) {
        val startDate = when (timeRange) {
            TimeRange.WEEK -> now.minusDays(7)
            TimeRange.MONTH -> now.withDayOfMonth(1)
            TimeRange.YEAR -> now.withDayOfYear(1)
        }
        expenses.filter { it.date >= startDate }
    }

    // Basic calculation for total spent and daily average in the selected range
    val totalSpent = filteredExpenses.sumOf { it.amount }
    val avgPerDay = totalSpent / when (timeRange) {
        TimeRange.WEEK -> 7
        TimeRange.MONTH -> 30
        TimeRange.YEAR -> 365
    }

    // Comparison logic: Compares spending of the current week vs the previous week
    val weekComparison = remember(expenses) {
        val thisWeekStart = now.minusDays(7)
        val lastWeekStart = now.minusDays(14)
        
        val thisWeekTotal = expenses.filter { it.date >= thisWeekStart }.sumOf { it.amount }
        val lastWeekTotal = expenses.filter { it.date in lastWeekStart..<thisWeekStart }.sumOf { it.amount }
        
        // Calculate percentage change
        val change = if (lastWeekTotal > 0) ((thisWeekTotal - lastWeekTotal) / lastWeekTotal) * 100 else 0.0
        Triple(thisWeekTotal, lastWeekTotal, change)
    }

    Scaffold(
        topBar = {
            // Header with screen title and Time Range selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeRangeSelector(selected = timeRange, onSelected = { timeRange = it })
            }
        }
    ) { innerPadding ->
        // Main content area using a scrollable LazyColumn
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards: Displaying Total Spent and Daily Average
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Spent",
                        value = "R${totalSpent.toInt()}",
                        subtext = "${abs(weekComparison.third).toInt()}% vs last week",
                        isPositive = weekComparison.third <= 0 // Positive (green) if spending is down
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Daily Average",
                        value = "R${avgPerDay.toInt()}",
                        subtext = "per day",
                        isPositive = null
                    )
                }
            }

            // Spending Trend: A line chart showing spending over the last 14 days
            item {
                AnalyticsCard(title = "Spending Trend") {
                    SpendingTrendChart(filteredExpenses)
                }
            }

            // Category Breakdown: A ring/donut chart showing spending distribution by category
            item {
                AnalyticsCard(title = "Category Breakdown") {
                    CategoryBreakdownChart(filteredExpenses, categories)
                }
            }

            // Budget vs Actual: Bar indicators showing how much of the budget was used per category
            item {
                AnalyticsCard(title = "Budget vs Actual") {
                    BudgetVsActualChart(filteredExpenses, categories, currentBudget)
                }
            }

            // Top Expenses: A list of the most expensive items in the current selection
            item {
                AnalyticsCard(title = "Top Expenses") {
                    TopExpensesList(filteredExpenses, categories)
                }
            }
            
            // Bottom spacing to ensure content isn't cut off
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}

/**
 * A toggle component to switch between Week, Month, and Year views.
 */
@Composable
fun TimeRangeSelector(selected: TimeRange, onSelected: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = selected == range
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelected(range) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Small card showing a single metric (e.g., Total Spent).
 */
@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    isPositive: Boolean?
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (isPositive != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            } else {
                Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * A reusable wrapper card for different analytics components with a title.
 */
@Composable
fun AnalyticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

/**
 * Custom-drawn line chart using Jetpack Compose Canvas.
 * It visualizes spending trends by connecting data points with lines.
 */
@Composable
fun SpendingTrendChart(expenses: List<Expense>) {
    // Group expenses by date and calculate daily totals for the last 14 days
    val dailyData = remember(expenses) {
        expenses.groupBy { it.date }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toList()
            .sortedBy { it.first }
            .takeLast(14)
    }

    if (dailyData.isEmpty()) {
        Box(Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxAmount = dailyData.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    
    Canvas(modifier = Modifier.height(150.dp).fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val spacing = width / (dailyData.size - 1).coerceAtLeast(1)
        
        // Path for the trend line
        val path = Path().apply {
            dailyData.forEachIndexed { index, data ->
                val x = index * spacing
                val y = height - (data.second.toFloat() / maxAmount * height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        // Path for the gradient fill under the line
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Draw the filled gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.3f), Color.Transparent)
            )
        )
        
        // Draw the actual line
        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Displays a donut chart and a legend showing how much was spent in each category.
 */
@Composable
fun CategoryBreakdownChart(expenses: List<Expense>, categories: List<Category>) {
    // Total spending per category ID
    val spendingByCategory = remember(expenses) {
        expenses.groupBy { it.categoryId }.mapValues { it.value.sumOf { e -> e.amount } }
    }
    
    // Prepare data for the chart: name, amount, and color
    val chartData = categories.map { 
        Triple(it.name, spendingByCategory[it.id] ?: 0.0, it.color)
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        // Draw the ring chart
        Canvas(modifier = Modifier.size(100.dp)) {
            var startAngle = -90f
            val total = chartData.sumOf { it.second }.toFloat()
            chartData.forEach { data ->
                val sweepAngle = (data.second.toFloat() / total) * 360f
                drawArc(
                    color = data.third,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx()) // Use Stroke to make it a ring
                )
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Legend showing the top 4 categories
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            chartData.take(4).forEach { data ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(data.third))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = data.first, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "R${data.second.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Visualizes the current spending against the planned budget for various categories.
 */
@Composable
fun BudgetVsActualChart(expenses: List<Expense>, categories: List<Category>, budget: Budget?) {
    val spendingByCategory = expenses.groupBy { it.categoryId }.mapValues { it.value.sumOf { e -> e.amount } }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Show progress bars for the first 5 categories
        categories.take(5).forEach { category ->
            val spent = spendingByCategory[category.id] ?: 0.0
            val planned = budget?.categoryBudgets?.get(category.id) ?: 0.0
            val max = maxOf(spent, planned).coerceAtLeast(1.0)
            
            Column {
                Text(text = category.name, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                // Progress bar background
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    // Gray bar showing planned budget
                    Box(modifier = Modifier.fillMaxWidth((planned / max).toFloat()).fillMaxHeight().background(Color.LightGray))
                    // Colored bar showing actual spending
                    Box(modifier = Modifier.fillMaxWidth((spent / max).toFloat()).fillMaxHeight().background(category.color))
                }
            }
        }
    }
}

/**
 * Lists the top 5 most expensive transactions.
 */
@Composable
fun TopExpensesList(expenses: List<Expense>, categories: List<Category>) {
    val topExpenses = expenses.sortedByDescending { it.amount }.take(5)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        topExpenses.forEachIndexed { index, expense ->
            val category = categories.find { it.id == expense.categoryId }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Rank number
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Category Icon
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(category?.color?.copy(alpha = 0.1f) ?: Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = getCategoryIcon(category?.icon ?: ""))
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Description and Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = expense.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = expense.date.format(dateFormatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Amount
                Text(text = "R${expense.amount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Maps icon strings to emoji characters.
 */
fun getCategoryIcon(iconName: String): String {
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

/**
 * Preview function to see the Analytics screen in Android Studio's Design tab.
 */
@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    val mockCategories = listOf(
        Category("1", "Food", Color(0xFFE91E63), "utensils"),
        Category("2", "Transport", Color(0xFF2196F3), "car"),
        Category("3", "Entertainment", Color(0xFF9C27B0), "gamepad-2"),
        Category("4", "Shopping", Color(0xFFFF9800), "shopping-cart")
    )
    
    val now = LocalDate.now()
    val mockExpenses = listOf(
        Expense("1", 250.0, now.minusDays(1), "1", "Dinner"),
        Expense("2", 120.0, now.minusDays(2), "2", "Uber"),
        Expense("3", 500.0, now.minusDays(3), "4", "New Shoes"),
        Expense("4", 150.0, now.minusDays(4), "1", "Groceries"),
        Expense("5", 300.0, now.minusDays(5), "3", "Movie Night"),
        Expense("6", 100.0, now.minusDays(6), "2", "Bus fare")
    )
    
    val mockBudgets = listOf(
        Budget(now.monthValue, now.year, mapOf("1" to 2000.0, "2" to 1000.0, "3" to 800.0, "4" to 1500.0))
    )

    PersonalBudgetTrackerAppTheme {
        AnalyticsScreen(
            expenses = mockExpenses,
            categories = mockCategories,
            budgets = mockBudgets
        )
    }
}
