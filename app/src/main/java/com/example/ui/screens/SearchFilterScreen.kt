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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.theme.*

@Composable
fun SearchFilterScreen(
    viewModel: TaskViewModel,
    onNavigateToEditTask: (Int) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val priorityFilter by viewModel.selectedPriorityFilter.collectAsState()
    val statusFilter by viewModel.selectedStatusFilter.collectAsState()
    val results by viewModel.filteredTasks.collectAsState()

    val categoriesList = remember { listOf("WomenBranch", "MenBranch", "Office", "Personal", "Teacher") }
    val prioritiesList = remember { listOf("High", "Medium", "Low") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input TextField
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("কাজ খুঁজুন (নাম বা বর্ণনা...)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_tasks_input"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Filter Chips
        Text(text = "অবস্থা অনুযায়ী ফিল্টার / Status Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip("চলমান", statusFilter == "PENDING", modifier = Modifier.weight(1f)) {
                viewModel.setStatusFilter(if (statusFilter == "PENDING") null else "PENDING")
            }
            StatusChip("সম্পন্ন", statusFilter == "COMPLETED", modifier = Modifier.weight(1f)) {
                viewModel.setStatusFilter(if (statusFilter == "COMPLETED") null else "COMPLETED")
            }
            StatusChip("বকেয়া", statusFilter == "OVERDUE", modifier = Modifier.weight(1f)) {
                viewModel.setStatusFilter(if (statusFilter == "OVERDUE") null else "OVERDUE")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Priority Filter chips
        Text(text = "অগ্রাধিকার / Priority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prioritiesList.forEach { prio ->
                val active = priorityFilter == prio
                val prioColor = when (prio) {
                    "High" -> HighPriorityColor
                    "Medium" -> MediumPriorityColor
                    else -> LowPriorityColor
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) prioColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (active) prioColor else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setPriorityFilter(if (active) null else prio) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (prio == "High") "উচ্চ" else if (prio == "Medium") "মাঝারি" else "নিম্ন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) prioColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter
        Text(text = "ক্যাটাগরি ফিল্টার / Categories", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categoriesList.take(3).forEach { cat ->
                CategoryFilterWidget(name = getBengaliCategory(cat), isActive = categoryFilter == cat) {
                    viewModel.setCategoryFilter(if (categoryFilter == cat) null else cat)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categoriesList.drop(3).forEach { cat ->
                CategoryFilterWidget(name = getBengaliCategory(cat), isActive = categoryFilter == cat) {
                    viewModel.setCategoryFilter(if (categoryFilter == cat) null else cat)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heading for lists
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ফলাফল তালিকা / Results (${results.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (categoryFilter != null || priorityFilter != null || statusFilter != null || query.isNotEmpty()) {
                Text(
                    text = "সব মুছুন",
                    fontSize = 11.sp,
                    color = HighPriorityColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.setSearchQuery("")
                        viewModel.setCategoryFilter(null)
                        viewModel.setPriorityFilter(null)
                        viewModel.setStatusFilter(null)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Container
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (results.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("দুঃখিত, কোনো মিল পাওয়া যায়নি!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("অন্য শব্দ বা ফিল্টার পরিবর্তন করে পুনরায় খুঁজুন।", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            } else {
                items(results) { task ->
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
}

@Composable
fun StatusChip(
    title: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RowScope.CategoryFilterWidget(
    name: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
            .clickable { onClick() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
