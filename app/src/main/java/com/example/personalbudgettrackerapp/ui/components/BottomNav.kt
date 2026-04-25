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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.auth.AuthScreen
import com.example.personalbudgettrackerapp.auth.AuthViewModel

data class NavItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNav(viewModel: AuthViewModel) {
    val currentScreen = viewModel.uiState.currentScreen
    
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = when (index) {
                        0 -> currentScreen == AuthScreen.Home
                        2 -> currentScreen == AuthScreen.Analytics
                        3 -> currentScreen == AuthScreen.Rewards
                        else -> false
                    }
                    
                    val isEnabled = index == 0 || index == 2 || index == 3

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable(enabled = isEnabled) {
                                when(index) {
                                    0 -> viewModel.setScreen(AuthScreen.Home)
                                    2 -> viewModel.setScreen(AuthScreen.Analytics)
                                    3 -> viewModel.setScreen(AuthScreen.Rewards)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(min = 64.dp),
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
                            fontSize = 11.sp,
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
