package com.example.personalbudgettrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.AppScreen
import com.example.personalbudgettrackerapp.AppViewModel

/**
 * Data class representing an item in the bottom navigation bar.
 */
data class NavItem(
    val icon: ImageVector,
    val label: String
)

/**
 * A custom bottom navigation component that manages main application navigation.
 * It highlights the active screen and provides quick access to major app features.
 */
@Composable
fun BottomNav(viewModel: AppViewModel) {
    val currentScreen = viewModel.uiState.currentScreen
    
    // Define the list of navigation items
    val navItems = listOf(
        NavItem(Icons.Default.Dashboard, "Home"),        // index 0
        NavItem(Icons.AutoMirrored.Filled.List, "Expenses"), // index 1
        NavItem(Icons.Default.PieChart, "Analytics"),   // index 2
        NavItem(Icons.Default.EmojiEvents, "Rewards"),   // index 3
        NavItem(Icons.Default.Settings, "Settings")      // index 4
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Subtle divider at the top of the navigation bar
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    // Determine if the current navigation item is selected
                    val isSelected = when (index) {
                        0 -> currentScreen == AppScreen.Home
                        1 -> currentScreen == AppScreen.Expense
                        2 -> currentScreen == AppScreen.Analytics
                        3 -> currentScreen == AppScreen.Rewards
                        4 -> currentScreen == AppScreen.Settings
                        else -> false
                    }
                    
                    val isEnabled = index == 0 || index == 1 || index == 2 || index == 3 || index == 4

                    // Navigation button for each item
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable(enabled = isEnabled) {
                                // Navigate to the corresponding screen
                                when(index) {
                                    0 -> viewModel.setScreen(AppScreen.Home)
                                    1 -> viewModel.setScreen(AppScreen.Expense)
                                    2 -> viewModel.setScreen(AppScreen.Analytics)
                                    3 -> viewModel.setScreen(AppScreen.Rewards)
                                    4 -> viewModel.setScreen(AppScreen.Settings)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
