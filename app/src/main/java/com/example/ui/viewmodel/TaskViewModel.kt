package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepository(database.taskDao())
    private val habitRepository = HabitRepository(database.habitDao())

    // Direct flows from repositories
    val tasksFlow: StateFlow<List<TaskEntity>> = taskRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitCompletionsFlow: StateFlow<List<HabitCompletion>> = habitRepository.allCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<String?>(null)
    val selectedPriorityFilter = _selectedPriorityFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null) // PENDING, COMPLETED, OVERDUE
    val selectedStatusFilter = _selectedStatusFilter.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow<String>(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    )
    val selectedCalendarDate = _selectedCalendarDate.asStateFlow()

    // AI Insight states
    private val _aiInsights = MutableStateFlow<String>("")
    val aiInsights = _aiInsights.asStateFlow()

    private val _isAnalyzingTask = MutableStateFlow(false)
    val isAnalyzingTask = _isAnalyzingTask.asStateFlow()

    // Backup notification messages
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage = _backupMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setPriorityFilter(priority: String?) {
        _selectedPriorityFilter.value = priority
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun selectCalendarDate(dateString: String) {
        _selectedCalendarDate.value = dateString
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    // Filter tasks live
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        tasksFlow, searchQuery, selectedCategoryFilter, selectedPriorityFilter, selectedStatusFilter
    ) { list, query, category, priority, status ->
        val now = System.currentTimeMillis()
        list.filter { task ->
            val matchQuery = query.isEmpty() || task.title.contains(query, ignoreCase = true) || task.description.contains(query, ignoreCase = true)
            val matchCategory = category == null || task.category == category
            val matchPriority = priority == null || task.priority == priority
            
            val matchStatus = when (status) {
                "PENDING" -> !task.isCompleted && task.dueDate >= now
                "COMPLETED" -> task.isCompleted
                "OVERDUE" -> !task.isCompleted && task.dueDate < now
                else -> true
            }
            matchQuery && matchCategory && matchPriority && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- TASK OPERATIONS ---

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        startDate: Long,
        dueDate: Long,
        reminderMinutesBefore: Int,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = title,
                description = description,
                category = category,
                priority = priority,
                startDate = startDate,
                dueDate = dueDate,
                reminderMinutesBefore = reminderMinutesBefore,
                notes = notes
            )
            taskRepository.insertTask(task)
        }
    }

    fun editTask(
        id: Int,
        title: String,
        description: String,
        category: String,
        priority: String,
        startDate: Long,
        dueDate: Long,
        reminderMinutesBefore: Int,
        notes: String,
        isCompleted: Boolean,
        completionDate: Long?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val original = taskRepository.getTaskById(id) ?: return@launch
            val updated = original.copy(
                title = title,
                description = description,
                category = category,
                priority = priority,
                startDate = startDate,
                dueDate = dueDate,
                reminderMinutesBefore = reminderMinutesBefore,
                notes = notes,
                isCompleted = isCompleted,
                completionDate = completionDate
            )
            taskRepository.updateTask(updated)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val isCompleting = !task.isCompleted
            val completedDate = if (isCompleting) now else null
            
            // Metrics calculation
            val delayMinutes = if (isCompleting && now > task.dueDate) {
                (now - task.dueDate) / 60000
            } else {
                0L
            }
            val completionDurationMinutes = if (isCompleting && now > task.startDate) {
                (now - task.startDate) / 60000
            } else {
                0L
            }

            val updated = task.copy(
                isCompleted = isCompleting,
                completionDate = completedDate,
                delayMinutes = delayMinutes,
                completionDurationMinutes = completionDurationMinutes
            )
            taskRepository.updateTask(updated)
        }
    }

    fun duplicateTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val duplicated = TaskEntity(
                title = "${task.title} (অনুলিপি)",
                description = task.description,
                category = task.category,
                priority = task.priority,
                startDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (task.dueDate - task.startDate),
                reminderMinutesBefore = task.reminderMinutesBefore,
                notes = task.notes,
                isCompleted = false
            )
            taskRepository.insertTask(duplicated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(task)
        }
    }

    fun deleteTaskById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTaskById(id)
        }
    }

    // --- HABIT TRACTION ---

    fun toggleHabit(habitType: String, dateString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            habitRepository.toggleHabitCompletion(habitType, dateString)
        }
    }

    fun changeWaterIntake(dateString: String, isIncrement: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isIncrement) {
                habitRepository.incrementWaterIntake(dateString)
            } else {
                habitRepository.decrementWaterIntake(dateString)
            }
        }
    }

    fun changeQuranPages(dateString: String, isIncrement: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isIncrement) {
                habitRepository.incrementQuranPages(dateString)
            } else {
                habitRepository.decrementQuranPages(dateString)
            }
        }
    }

    // Getter helper for dynamic habit stats
    fun getHabitStreak(habitType: String): Int {
        val completions = habitCompletionsFlow.value.filter { it.habitType == habitType }
        return habitRepository.calculateStreak(completions)
    }

    // --- BACKUP & RESTORE ---

    fun exportBackup(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tasks = tasksFlow.value
                val completions = habitCompletionsFlow.value
                
                val rootJson = JSONObject()
                val tasksArray = JSONArray()
                tasks.forEach {
                    val j = JSONObject().apply {
                        put("id", it.id)
                        put("title", it.title)
                        put("description", it.description)
                        put("category", it.category)
                        put("priority", it.priority)
                        put("startDate", it.startDate)
                        put("dueDate", it.dueDate)
                        put("reminderMinutes", it.reminderMinutesBefore)
                        put("notes", it.notes)
                        put("isCompleted", it.isCompleted)
                        put("completionDate", it.completionDate ?: -1L)
                        put("delayMinutes", it.delayMinutes)
                        put("duration", it.completionDurationMinutes)
                    }
                    tasksArray.put(j)
                }
                
                val completionsArray = JSONArray()
                completions.forEach {
                    val j = JSONObject().apply {
                        put("habitType", it.habitType)
                        put("dateString", it.dateString)
                        put("isCompleted", it.isCompleted)
                        put("intakeValue", it.intakeValue)
                    }
                    completionsArray.put(j)
                }
                
                rootJson.put("tasks", tasksArray)
                rootJson.put("habitCompletions", completionsArray)
                rootJson.put("exportedAt", System.currentTimeMillis())

                val file = File(context.filesDir, "task_master_bd_backup.json")
                file.writeText(rootJson.toString(2))
                
                _backupMessage.value = "সফলভাবে স্থানীয়ভাবে ডাটা ব্যাকআপ সংরক্ষণ করা হয়েছে! পথ: ${file.name}"
            } catch (e: Exception) {
                Log.e("Backup", "Error: ", e)
                _backupMessage.value = "ব্যাকআপ নিতে ত্রুটি হয়েছে: ${e.localizedMessage}"
            }
        }
    }

    fun importBackup(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "task_master_bd_backup.json")
                if (!file.exists()) {
                    _backupMessage.value = "কোনো পূর্বে ব্যাকআপ রাখা ফাইল পাওয়া যায়নি!"
                    return@launch
                }
                
                val raw = file.readText()
                val rootJson = JSONObject(raw)
                
                val tasksArray = rootJson.optJSONArray("tasks")
                if (tasksArray != null) {
                    for (i in 0 until tasksArray.length()) {
                        val o = tasksArray.getJSONObject(i)
                        val entity = TaskEntity(
                            title = o.optString("title", ""),
                            description = o.optString("description", ""),
                            category = o.optString("category", "Personal"),
                            priority = o.optString("priority", "Medium"),
                            startDate = o.optLong("startDate", System.currentTimeMillis()),
                            dueDate = o.optLong("dueDate", System.currentTimeMillis() + 86400000),
                            reminderMinutesBefore = o.optInt("reminderMinutes", 0),
                            notes = o.optString("notes", ""),
                            isCompleted = o.optBoolean("isCompleted", false),
                            completionDate = o.optLong("completionDate")?.let { if (it == -1L) null else it },
                            delayMinutes = o.optLong("delayMinutes", 0),
                            completionDurationMinutes = o.optLong("duration", 0)
                        )
                        taskRepository.insertTask(entity)
                    }
                }

                val completionsArray = rootJson.optJSONArray("habitCompletions")
                if (completionsArray != null) {
                    for (i in 0 until completionsArray.length()) {
                        val o = completionsArray.getJSONObject(i)
                        val habitType = o.optString("habitType", "")
                        val dateString = o.optString("dateString", "")
                        val isCompleted = o.optBoolean("isCompleted", true)
                        val intakeValue = o.optInt("intakeValue", 1)
                        if (habitType.isNotEmpty() && dateString.isNotEmpty()) {
                            val element = HabitCompletion(
                                habitType = habitType,
                                dateString = dateString,
                                isCompleted = isCompleted,
                                intakeValue = intakeValue
                            )
                            database.habitDao().insertCompletion(element)
                        }
                    }
                }
                
                _backupMessage.value = "আপনার ডাটা সফলভাবে রিস্টোর (পুনরুদ্ধার) করা হয়েছে!"
            } catch (e: Exception) {
                Log.e("Backup", "Error during import: ", e)
                _backupMessage.value = "ডাটা পুনরুদ্ধার ব্যর্থ: ${e.localizedMessage}"
            }
        }
    }

    // --- AI ASSISTANT TRIGGERS ---

    fun loadAiInsightsLog() {
        if (_aiInsights.value.isNotEmpty()) return
        triggerAiInsights()
    }

    fun triggerAiInsights() {
        viewModelScope.launch {
            _isAnalyzingTask.value = true
            val tasks = tasksFlow.value
            val now = System.currentTimeMillis()
            
            val total = tasks.size
            val completed = tasks.filter { it.isCompleted }
            val pending = tasks.filter { !it.isCompleted && it.dueDate >= now }
            val overdue = tasks.filter { !it.isCompleted && it.dueDate < now }
            val compRate = if (total > 0) (completed.size * 100 / total) else 0

            val report = GeminiService.generateAnalyticsInsights(
                tasks = tasks,
                completedCount = completed.size,
                pendingCount = pending.size,
                overdueCount = overdue.size,
                completionRate = compRate
            )
            _aiInsights.value = report
            _isAnalyzingTask.value = false
        }
    }

    private val _customAiQueryResponse = MutableStateFlow<String>("")
    val customAiQueryResponse = _customAiQueryResponse.asStateFlow()

    private val _isQueryingCustomAi = MutableStateFlow(false)
    val isQueryingCustomAi = _isQueryingCustomAi.asStateFlow()

    fun askGeminiCustom(question: String) {
        viewModelScope.launch {
            _isQueryingCustomAi.value = true
            val response = GeminiService.askGeminiQuestion(question)
            _customAiQueryResponse.value = response
            _isQueryingCustomAi.value = false
        }
    }

    fun clearCustomAiQuery() {
        _customAiQueryResponse.value = ""
    }
}
