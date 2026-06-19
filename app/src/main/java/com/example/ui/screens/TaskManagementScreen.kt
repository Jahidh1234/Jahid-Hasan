package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: TaskViewModel,
    taskId: Int? = null, // If null, we are adding. If not, editing.
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasksFlow.collectAsState()
    
    // Find task if editing
    val editingTask = remember(taskId, tasks) {
        if (taskId != null) tasks.find { it.id == taskId } else null
    }

    // Input States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var customCategoryName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var reminderMinutesBefore by remember { mutableStateOf(15) }

    // Date & Time calendars
    val calendarStart = remember { Calendar.getInstance() }
    val calendarDue = remember { Calendar.getInstance().apply { add(Calendar.HOUR, 2) } } // default safety

    var startDateText by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    // Parse existing task details on launch
    LaunchedEffect(editingTask) {
        if (editingTask != null) {
            title = editingTask.title
            description = editingTask.description
            category = if (listOf("WomenBranch", "MenBranch", "Office", "Personal", "Teacher").contains(editingTask.category)) {
                editingTask.category
            } else {
                customCategoryName = editingTask.category
                "Custom"
            }
            priority = editingTask.priority
            notes = editingTask.notes
            isCompleted = editingTask.isCompleted
            reminderMinutesBefore = editingTask.reminderMinutesBefore

            calendarStart.timeInMillis = editingTask.startDate
            calendarDue.timeInMillis = editingTask.dueDate
        }
        startDateText = dateFormat.format(calendarStart.time)
        dueDateText = dateFormat.format(calendarDue.time)
    }

    // Picker launchers
    fun showDatePicker(isStart: Boolean) {
        val cal = if (isStart) calendarStart else calendarDue
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                // Show TimePicker immediately after Date is chosen for smooth flow
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        if (isStart) {
                            startDateText = dateFormat.format(cal.time)
                        } else {
                            dueDateText = dateFormat.format(cal.time)
                        }
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingTask == null) "নতুন কাজ যোগ করুন" else "কাজ সংশোধন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editingTask != null) {
                        // Quick duplicate
                        IconButton(onClick = {
                            viewModel.duplicateTask(editingTask)
                            Toast.makeText(context, "কাজ অনুলিপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate")
                        }
                        // Delete option
                        IconButton(onClick = {
                            viewModel.deleteTask(editingTask)
                            Toast.makeText(context, "কাজ মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = HighPriorityColor)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title TextField
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("কাজের শিরোনাম / Task Title *") },
                placeholder = { Text("যেমন: বাজার করা, অ্যাসাইনমেন্ট লেখা") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Task Description TextField
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("কাজের বিবরণ / Description") },
                placeholder = { Text("কাজের সহজ ও বিস্তারিত বিবরণ লিখুন") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Selection Row
            Column {
                Text(
                    text = "ক্যাটাগরি নির্বাচন করুন / Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val categories = listOf("WomenBranch", "MenBranch", "Office", "Personal", "Teacher", "Custom")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        CategoryChip(
                            name = getBengaliCategory(cat),
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        CategoryChip(
                            name = getBengaliCategory(cat),
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }

                if (category == "Custom") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customCategoryName,
                        onValueChange = { customCategoryName = it },
                        label = { Text("কাস্টম ক্যাটাগরির নাম লিখুন / Custom Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Priority Selection Row
            Column {
                Text(
                    text = "গুরুত্ব বা অগ্রাধিকার / Priority",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityButton("Low", "নিম্ন (Low)", LowPriorityColor, priority == "Low") { priority = "Low" }
                    PriorityButton("Medium", "মাঝারি (Medium)", MediumPriorityColor, priority == "Medium") { priority = "Medium" }
                    PriorityButton("High", "উচ্চ (High)", HighPriorityColor, priority == "High") { priority = "High" }
                }
            }

            // Date & Time Schedulers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Date Picker Button
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(text = "শুরুর সময় / Start *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker(isStart = true) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(text = startDateText, fontSize = 11.sp)
                        }
                    }
                }

                // Due Date Picker Button
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(text = "শেষ করার তাগিদ / Due *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighPriorityColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker(isStart = false) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighPriorityColor)
                            Text(text = dueDateText, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Reminders setup
            Column {
                Text(
                    text = "স্মার্ট রিমাইন্ডার অ্যালার্ট / Reminders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReminderChip(minutes = 0, label = "কাজের সময়", isSelected = reminderMinutesBefore == 0) { reminderMinutesBefore = 0 }
                    ReminderChip(minutes = 15, label = "১৫ মি. আগে", isSelected = reminderMinutesBefore == 15) { reminderMinutesBefore = 15 }
                    ReminderChip(minutes = 30, label = "৩০ মি. আগে", isSelected = reminderMinutesBefore == 30) { reminderMinutesBefore = 30 }
                    ReminderChip(minutes = 60, label = "১ ঘণ্টা আগে", isSelected = reminderMinutesBefore == 60) { reminderMinutesBefore = 60 }
                }
            }

            // Notes multi-line
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("অতিরিক্ত নোট / Notes & Instructions") },
                placeholder = { Text("অন্যান্য কোনো বিশেষ তথ্য থাকলে লিখুন") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            // Keep status modification if editing
            if (editingTask != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "কাজটি সম্পন্ন করা হয়েছে?", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "অনুগ্রহ করে শিরোনাম লিখুন!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalCategory = if (category == "Custom") {
                        if (customCategoryName.isBlank()) "অন্যান্য" else customCategoryName
                    } else {
                        category
                    }

                    if (editingTask == null) {
                        viewModel.addTask(
                            title = title,
                            description = description,
                            category = finalCategory,
                            priority = priority,
                            startDate = calendarStart.timeInMillis,
                            dueDate = calendarDue.timeInMillis,
                            reminderMinutesBefore = reminderMinutesBefore,
                            notes = notes
                        )
                        Toast.makeText(context, "কাজটি যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.editTask(
                            id = editingTask.id,
                            title = title,
                            description = description,
                            category = finalCategory,
                            priority = priority,
                            startDate = calendarStart.timeInMillis,
                            dueDate = calendarDue.timeInMillis,
                            reminderMinutesBefore = reminderMinutesBefore,
                            notes = notes,
                            isCompleted = isCompleted,
                            completionDate = if (isCompleted) System.currentTimeMillis() else null
                        )
                        Toast.makeText(context, "কাজটি সংরক্ষণ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("কাজের তথ্য সংরক্ষণ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RowScope.PriorityButton(
    value: String,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .height(34.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReminderChip(
    minutes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .height(32.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
