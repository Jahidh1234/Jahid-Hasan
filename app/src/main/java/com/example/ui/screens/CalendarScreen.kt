package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: TaskViewModel,
    onNavigateToEditTask: (Int) -> Unit
) {
    val tasks by viewModel.tasksFlow.collectAsState()
    val rawSelectedDate by viewModel.selectedCalendarDate.collectAsState()

    // Interactive calendar settings
    var activeCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val sdfMonthYearBn = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD"))
    val sdfMonthYearEn = SimpleDateFormat("MMMM yyyy", Locale.US)
    val daySdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Compute calendar items
    val monthCalendar = remember(activeCalendar) {
        val cal = activeCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal
    }
    
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfWeekOffset = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) to 6 (Sat)
    
    val calendarDays = remember(activeCalendar) {
        val days = mutableListOf<String?>()
        // Pre-append nulls for empty starts padding
        for (i in 0 until dayOfWeekOffset) {
            days.add(null)
        }
        val cal = monthCalendar.clone() as Calendar
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            days.add(daySdf.format(cal.time))
        }
        days
    }

    // Filter tasks belonging specifically to the selected date
    val tasksForSelectedDate = remember(rawSelectedDate, tasks) {
        tasks.filter { task ->
            val taskDateStr = daySdf.format(Date(task.dueDate))
            taskDateStr == rawSelectedDate
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calendar Title and Month Toggler
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    val cal = activeCalendar.clone() as Calendar
                    cal.add(Calendar.MONTH, -1)
                    activeCalendar = cal
                }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sdfMonthYearBn.format(activeCalendar.time),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = sdfMonthYearEn.format(activeCalendar.time),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = {
                    val cal = activeCalendar.clone() as Calendar
                    cal.add(Calendar.MONTH, 1)
                    activeCalendar = cal
                }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }
        }

        // Days of Week Header
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                val headerDays = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
                headerDays.forEach { head ->
                    Text(
                        text = head,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Calendar Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunks = calendarDays.chunked(7)
                chunks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { dayStr ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayStr != null) {
                                    val isSelected = dayStr == rawSelectedDate
                                    val dateNum = dayStr.split("-").last().toInt().toString()
                                    
                                    // Aggregate status colors for that day
                                    val dayTasks = tasks.filter { daySdf.format(Date(it.dueDate)) == dayStr }
                                    val hasOverdue = dayTasks.any { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
                                    val hasPending = dayTasks.any { !it.isCompleted && it.dueDate >= System.currentTimeMillis() }
                                    val hasCompleted = dayTasks.isNotEmpty() && dayTasks.all { it.isCompleted }

                                    val badgeColor = when {
                                        hasOverdue -> HighPriorityColor
                                        hasPending -> MediumPriorityColor
                                        hasCompleted -> CompletedColor
                                        else -> Color.Transparent
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { viewModel.selectCalendarDate(dayStr) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = dateNum,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            // Tiny dot indicator for state
                                            if (badgeColor != Color.Transparent) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(badgeColor)
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

        // Active List Label
        item {
            val formattedSelected = try {
                val parsed = daySdf.parse(rawSelectedDate) ?: Date()
                val format = SimpleDateFormat("dd MMMM, yyyy (EEEE)", Locale("bn", "BD"))
                format.format(parsed)
            } catch (e: Exception) {
                rawSelectedDate
            }

            Text(
                text = "নির্বাচিত তারিখ: $formattedSelected",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Task items
        if (tasksForSelectedDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.EventNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("এই তারিখে কোনো নির্ধারিত কাজ পাওয়া যায়নি।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(tasksForSelectedDate) { task ->
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
