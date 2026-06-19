package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.TaskEntity
import com.example.service.AdManager
import com.example.service.InterstitialAdOverlay
import com.example.service.RewardedAdOverlay
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize simulated AdMob environment
        AdManager.initAdMob(this)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = viewModel()
    val tasks by viewModel.tasksFlow.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "dashboard"

    // Reminder system calculations (In-App Smart Reminders)
    val now = System.currentTimeMillis()
    val pendingCount = tasks.count { !it.isCompleted && it.dueDate >= now }
    val overdueCount = tasks.count { !it.isCompleted && it.dueDate < now }
    
    // Determine dynamic reminder banner text
    val reminderWarning = remember(overdueCount, pendingCount) {
        when {
            overdueCount > 0 -> "আপনি এই কাজটি এখনো সম্পন্ন করেননি! কাজের সময় ইতিমধ্যে অতিক্রম করেছে।"
            pendingCount > 0 -> "আজকের কাজ এখনো বাকি আছে! কাজের সময় শেষ হয়ে যাচ্ছে।"
            else -> null
        }
    }

    // Controls whether bottom navigator & FAB should be displayed on the active route
    val showChrome = currentRoute in listOf("dashboard", "calendar", "habits", "analytics", "ai_insights")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showChrome) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "টাস্ক মাস্টার বিডি",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        // Quick Search button
                        IconButton(onClick = { navController.navigate("search") }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Tasks")
                        }
                        // Backup-restore options button
                        IconButton(onClick = { navController.navigate("backup") }) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Backup Data")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar(
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        NavTabItem("dashboard", "হোম", Icons.Default.Home),
                        NavTabItem("calendar", "ক্যালেন্ডার", Icons.Default.CalendarMonth),
                        NavTabItem("habits", "অভ্যাস", Icons.Default.DirectionsRun),
                        NavTabItem("analytics", "অ্যানালিটিক্স", Icons.Default.BarChart),
                        NavTabItem("ai_insights", "এআই কোচ", Icons.Default.AutoAwesome)
                    )

                    items.forEach { item ->
                        val selected = currentBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
                            label = { Text(text = item.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showChrome) {
                ExtendedFloatingActionButton(
                    text = { Text("কাজ যোগ", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task") },
                    onClick = { navController.navigate("add_task") },
                    modifier = Modifier
                        .testTag("add_task_fab")
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Reminders alert box (Displays if there are overdue/pending items)
            if (showChrome && reminderWarning != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reminderWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Navigation Host
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.weight(1f)
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAddTask = { navController.navigate("add_task") },
                        onNavigateToEditTask = { id -> navController.navigate("edit_task/$id") },
                        onNavigateToAnalytics = { navController.navigate("analytics") }
                    )
                }

                composable("calendar") {
                    CalendarScreen(
                        viewModel = viewModel,
                        onNavigateToEditTask = { id -> navController.navigate("edit_task/$id") }
                    )
                }

                composable("habits") {
                    HabitScreen(viewModel = viewModel)
                }

                composable("analytics") {
                    AnalyticsScreen(
                        viewModel = viewModel,
                        onNavigateToEditTask = { id -> navController.navigate("edit_task/$id") }
                    )
                }

                composable("ai_insights") {
                    AiInsightsScreen(viewModel = viewModel)
                }

                composable("search") {
                    SearchFilterScreen(
                        viewModel = viewModel,
                        onNavigateToEditTask = { id -> navController.navigate("edit_task/$id") }
                    )
                }

                composable("backup") {
                    BackupRestoreScreen(viewModel = viewModel)
                }

                composable("add_task") {
                    TaskManagementScreen(
                        viewModel = viewModel,
                        taskId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "edit_task/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("id")
                    TaskManagementScreen(
                        viewModel = viewModel,
                        taskId = taskId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // Interstitial overlays and rewarded ad containers
    InterstitialAdOverlay()
    RewardedAdOverlay(onRewardClaimed = {
        // Trigger VIP unlocks or state bonuses
        viewModel.triggerAiInsights()
    })
}

// Navigation structure tab item
data class NavTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
