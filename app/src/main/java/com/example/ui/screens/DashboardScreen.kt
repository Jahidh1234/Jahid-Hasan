package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.service.BannerAdWidget
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Int) -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val tasks by viewModel.tasksFlow.collectAsState()
    
    // Calculate statistics
    val now = System.currentTimeMillis()
    val totalTasks = tasks.size
    val completedTasks = tasks.filter { it.isCompleted }
    val completedCount = completedTasks.size
    val pendingCount = tasks.count { !it.isCompleted && it.dueDate >= now }
    val overdueCount = tasks.count { !it.isCompleted && it.dueDate < now }
    
    val completionRate = if (totalTasks > 0) (completedCount * 100 / totalTasks) else 0
    
    // Formatting today's date in Bengali & English
    val sdfBengali = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("bn", "BD"))
    val sdfEnglish = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
    val formattedBengaliDate = sdfBengali.format(Date())
    val formattedEnglishDate = sdfEnglish.format(Date())

    // Get today's tasks
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val todayEnd = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val todayTasks = tasks.filter { task ->
        (task.dueDate in todayStart..todayEnd) || (!task.isCompleted && task.dueDate < todayEnd)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Task Master ",
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            color = EditorialDeepBlue,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "BD",
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            color = EditorialBlue,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Text(
                        text = "টাস্ক মাস্টার বিডি — $formattedBengaliDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EditorialLightBlue)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EditorialDeepBlue
                    )
                }
            }
        }

        // Productivity Score Card (Editorial Aesthetic)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(EditorialDeepBlue)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "PRODUCTIVITY SCORE",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${getBengaliNumber(completionRate)}%",
                            fontSize = 48.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                        Text(
                            text = "আপনার কাজ এগিয়েছে",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            style = androidx.compose.ui.text.TextStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(completionRate / 100f)
                                .clip(CircleShape)
                                .background(EditorialProgressBlue)
                        )
                    }
                }
            }
        }

        // Editorial KPI Stats grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Pending (⏳)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(EditorialGray)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏳", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = getBengaliNumber(pendingCount),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialDeepBlue
                                )
                                Text(
                                    text = "বাকি কাজ (Pending)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                    // Card 2: Completed (✅)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(EditorialLightBlue)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = getBengaliNumber(completedCount),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialDeepBlue
                                )
                                Text(
                                    text = "সম্পন্ন (Completed)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // In-app banner advertisement option
        item {
            BannerAdWidget()
        }

        // Today's / Overdue Tasks Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "আজকের বিশেষ কাজ (${todayTasks.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EditorialDeepBlue
                )
                Text(
                    text = "সবগুলো",
                    fontSize = 12.sp,
                    color = EditorialBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToAnalytics() }
                        .padding(4.dp)
                )
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Success",
                            tint = CompletedColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "অসাধারণ! আজকের সব কাজ শেষ!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "কোনো চলমান বা বকেয়া কাজ অবশিষ্ট নেই।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayTasks) { task ->
                TaskItemRow(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onDuplicate = { viewModel.duplicateTask(task) },
                    onEdit = { onNavigateToEditTask(task.id) }
                )
            }
        }
    }
}

// Stats Card translation wrapper helper
fun getBengaliNumber(number: Int): String {
    val englishDigits = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return number.toString().map { char ->
        val index = englishDigits.indexOf(char)
        if (index != -1) bengaliDigits[index] else char
    }.joinToString("")
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit
) {
    val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (task.isCompleted) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onEdit() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Container matching Spec HTML style
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, EditorialBlue, RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) EditorialBlue else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Formatted details
                val sdf = SimpleDateFormat("h:mm a, dd MMM", Locale("bn", "BD"))
                val timeStr = sdf.format(Date(task.dueDate))
                Text(
                    text = "$timeStr • Priority: ${task.priority}",
                    fontSize = 11.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Special Category Badge / Priority Check
            val (badgeBg, badgeText, badgeLabel) = when (task.priority) {
                "High" -> Triple(HighPriorityBg, HighPriorityText, "অপেক্ষিত")
                else -> Triple(NormalBadgeBg, NormalBadgeText, getBengaliCategory(task.category))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action options
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// Translations for category tags
fun getBengaliCategory(category: String): String {
    return when (category) {
        "WomenBranch" -> "মহিলা শাখার কাজ"
        "MenBranch" -> "পুরুষ শাখার কাজ"
        "Office" -> "অফিসের কাজ"
        "Personal" -> "ব্যক্তিগত কাজ"
        "Teacher" -> "শিক্ষকদের কাজ"
        "Business" -> "ব্যবসা"
        "Study" -> "পড়াশোনা"
        "Family" -> "পারিবারিক"
        "Health" -> "স্বাস্থ্য"
        else -> category
    }
}
