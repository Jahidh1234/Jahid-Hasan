package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.service.AdManager
import com.example.service.NativeAdWidget
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: TaskViewModel,
    onNavigateToEditTask: (Int) -> Unit
) {
    val tasks by viewModel.tasksFlow.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0: Analytics, 1: Full History

    val now = System.currentTimeMillis()
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val pendingTasks = remember(tasks, now) { tasks.filter { !it.isCompleted && it.dueDate >= now } }
    val overdueTasks = remember(tasks, now) { tasks.filter { !it.isCompleted && it.dueDate < now } }

    val completionPercentage = remember(tasks, completedTasks) {
        if (tasks.isNotEmpty()) (completedTasks.size * 100 / tasks.size) else 0
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Aesthetic Tab selector Custom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            TabHeaderButton(title = "অগ্রগতি ও পরিসংখ্যান", isActive = activeTab == 0, modifier = Modifier.weight(1f)) {
                activeTab = 0
            }
            TabHeaderButton(title = "সম্পন্ন কাজের ইতিহাস", isActive = activeTab == 1, modifier = Modifier.weight(1f)) {
                activeTab = 1
                // Trigger interstitial ad when viewing historical statistics as requested in spec!
                AdManager.showInterstitialAd()
            }
        }

        if (activeTab == 0) {
            // VIEW STATISTICS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular completion arc
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = "লক্ষ্য অর্জনের হার",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "আপনার সামগ্রিক কর্মক্ষমতা পরিমাপক সূচক।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Native Jetpack Arc
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(70.dp)) {
                                    drawArc(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    drawArc(
                                        color = CompletedColor,
                                        startAngle = -90f,
                                        sweepAngle = (completionPercentage * 3.6f),
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                }
                                Text(
                                    text = "$completionPercentage%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CompletedColor
                                )
                            }
                        }
                    }
                }

                // Native sponsored ad spacer
                item {
                    NativeAdWidget()
                }

                // Categories Breakdown
                item {
                    val categoryBreakdownMap = remember(tasks) {
                        val map = mutableMapOf<String, Int>()
                        tasks.forEach {
                            map[it.category] = (map[it.category] ?: 0) + 1
                        }
                        map.toList().sortedByDescending { it.second }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ক্যাটাগরি ভিত্তিক বন্টন / Categories Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (categoryBreakdownMap.isEmpty()) {
                                Text("কোনো ক্যাটাগরি ডাটা পাওয়া যায়নি।", fontSize = 11.sp, color = Color.Gray)
                            } else {
                                categoryBreakdownMap.forEach { (catName, count) ->
                                    val pct = if (tasks.isNotEmpty()) (count * 100 / tasks.size) else 0
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = getBengaliCategory(catName), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = "$count টি কাজ ($pct%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Most Delayed Tasks List
                item {
                    val delayedTasks = remember(completedTasks) {
                        completedTasks.filter { it.delayMinutes > 0 }.sortedByDescending { it.delayMinutes }.take(5)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "সবচেয়ে বিলম্বিত কাজগুলো / Most Delayed Tasks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HighPriorityColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (delayedTasks.isEmpty()) {
                                Text("অসাধারণ! কোনো কাজে নির্ধারিত দেরির ইতিহাস নেই!", fontSize = 11.sp, color = Color.Gray)
                            } else {
                                delayedTasks.forEach { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(text = task.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = "ক্যাটাগরি: ${getBengaliCategory(task.category)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(HighPriorityColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "${task.delayMinutes} মি. বিলম্ব", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighPriorityColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // VIEW COMPLETED HISTORY permanently
            val historyGroup = remember(completedTasks) {
                val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis
                val yesterday = today - 86400000
                val thisWeek = today - (86400000 * 7)
                val thisMonth = today - (86400000 * 30)

                val todayList = mutableListOf<TaskEntity>()
                val yesterdayList = mutableListOf<TaskEntity>()
                val weekList = mutableListOf<TaskEntity>()
                val monthList = mutableListOf<TaskEntity>()
                val olderList = mutableListOf<TaskEntity>()

                completedTasks.forEach { task ->
                    val dateObj = task.completionDate ?: 0L
                    when {
                        dateObj >= today -> todayList.add(task)
                        dateObj >= yesterday -> yesterdayList.add(task)
                        dateObj >= thisWeek -> weekList.add(task)
                        dateObj >= thisMonth -> monthList.add(task)
                        else -> olderList.add(task)
                    }
                }

                listOf(
                    HistoryGroup("আজকের সম্পন্ন কাজ", todayList),
                    HistoryGroup("গতকালকের সম্পন্ন কাজ", yesterdayList),
                    HistoryGroup("এই সপ্তাহের সম্পন্ন কাজ", weekList),
                    HistoryGroup("এই মাসের সম্পন্ন কাজ", monthList),
                    HistoryGroup("পুরাতন কাজের আর্কাইভ", olderList)
                ).filter { it.items.isNotEmpty() }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (historyGroup.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("আর্কাভিভ সম্পূর্ণ ফাকা!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("কাজ সম্পন্ন করলে তার চিরস্থায়ী বিবরণ এখানে জমা হবে।", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    historyGroup.forEach { group ->
                        item {
                            Text(
                                text = "${group.title} (${group.items.size})",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(group.items) { task ->
                            HistoryItemCard(task = task, onRowClicked = { onNavigateToEditTask(task.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabHeaderButton(
    title: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HistoryItemCard(
    task: TaskEntity,
    onRowClicked: () -> Unit
) {
    val sdfDate = SimpleDateFormat("dd MMM, hh:mm a", Locale("bn", "BD"))
    val complDateStr = task.completionDate?.let { sdfDate.format(Date(it)) } ?: "অজানা সমাপন"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClicked() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CompletedColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = task.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CompletedColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "সম্পন্ন", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CompletedColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Completion stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "সমাপ্তিকাল: $complDateStr", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                // Duration summary
                Text(
                    text = "সময়কাল: ${task.completionDurationMinutes} মিনিট",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            if (task.delayMinutes > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(11.dp), tint = HighPriorityColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "দেরি হয়েছে: ${task.delayMinutes} মিনিট", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighPriorityColor)
                }
            }
        }
    }
}

data class HistoryGroup(
    val title: String,
    val items: List<TaskEntity>
)
