package com.example.personalbudgettrackerapp.ui.rewards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalbudgettrackerapp.AppViewModel
import com.example.personalbudgettrackerapp.data.Achievement
import java.text.SimpleDateFormat
import java.util.*

/**
 * The Rewards Screen displays user achievements and badges.
 * It uses gamification to encourage users to maintain their financial tracking habits.
 */
@Composable
fun RewardsScreen(viewModel: AppViewModel) {
    val uiState = viewModel.uiState
    val achievements = uiState.achievements

    // Calculate overall progress stats
    val unlockedCount = achievements.count { it.unlocked }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) (unlockedCount.toFloat() / totalCount) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary header with overall progress bar
        item {
            HeaderSection(unlockedCount, totalCount, progress)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Break down of unlocked vs locked achievements
                StatsSection(unlockedCount, totalCount)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ALL BADGES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // List of individual achievement badges
        items(achievements) { achievement ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                AchievementItem(achievement)
            }
        }

        // Footer card with encouraging message
        item {
            MotivationalCard()
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Renders the top section of the rewards screen, showing the overall achievement progress.
 */
@Composable
fun HeaderSection(unlockedCount: Int, totalCount: Int, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary, 
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 48.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), 
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$unlockedCount of $totalCount unlocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Progress Card providing a visual summary of completion
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )
                Text(
                    text = "Keep tracking your expenses to unlock more badges!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * A row of small cards displaying numeric achievement statistics.
 */
@Composable
fun StatsSection(unlockedCount: Int, totalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.EmojiEvents,
            value = unlockedCount.toString(),
            label = "Unlocked",
            iconColor = MaterialTheme.colorScheme.primary,
            iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Lock,
            value = (totalCount - unlockedCount).toString(),
            label = "Locked",
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            iconBg = MaterialTheme.colorScheme.surfaceVariant
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            value = totalCount.toString(),
            label = "Total",
            iconColor = Color(0xFFFACC15),
            iconBg = Color(0xFFFACC15).copy(alpha = 0.1f)
        )
    }
}

/**
 * A small reusable card for displaying a single achievement-related statistic.
 */
@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String, iconColor: Color, iconBg: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Handles the display of a single achievement, swapping between unlocked and locked states.
 */
@Composable
fun AchievementItem(achievement: Achievement) {
    if (achievement.unlocked) {
        AchievementUnlocked(achievement)
    } else {
        AchievementLocked(achievement)
    }
}

/**
 * Visual representation of an achievement that has already been earned.
 */
@Composable
fun AchievementUnlocked(achievement: Achievement) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(0.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        border = BorderStroke(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), width = 1.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAchievementIcon(achievement.iconId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "UNLOCKED",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (achievement.unlockedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(achievement.unlockedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Visual representation of an achievement that is yet to be earned, showing a lock and current progress.
 */
@Composable
fun AchievementLocked(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with a lock overlay
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAchievementIcon(achievement.iconId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant )
                    Text(
                        text = "${achievement.progress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Shows how close the user is to unlocking this badge
                LinearProgressIndicator(
                    progress = { achievement.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

/**
 * An encouraging card to motivate the user to keep using the app.
 */
@Composable
fun MotivationalCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Keep Going!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Track your expenses daily and stay within budget to unlock more achievements.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Maps achievement icon IDs to their corresponding ImageVector icons.
 */
fun getAchievementIcon(iconId: String): ImageVector {
    return when (iconId) {
        "trophy" -> Icons.Default.EmojiEvents
        "calendar-check" -> Icons.Default.CalendarMonth
        "piggy-bank" -> Icons.Default.Savings
        "footprints" -> Icons.Default.Star
        "folder-plus" -> Icons.Default.CreateNewFolder
        else -> Icons.Default.EmojiEvents
    }
}
