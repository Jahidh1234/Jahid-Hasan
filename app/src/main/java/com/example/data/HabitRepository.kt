package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HabitRepository(private val habitDao: HabitDao) {
    val allCompletions: Flow<List<HabitCompletion>> = habitDao.getAllCompletions()

    fun getCompletionsByHabit(habitType: String): Flow<List<HabitCompletion>> {
        return habitDao.getCompletionsByHabit(habitType)
    }

    fun getCompletionsByDate(dateString: String): Flow<List<HabitCompletion>> {
        return habitDao.getCompletionsByDate(dateString)
    }

    suspend fun toggleHabitCompletion(habitType: String, dateString: String) {
        val existing = habitDao.getCompletionByHabitAndDate(habitType, dateString)
        if (existing != null) {
            habitDao.deleteCompletion(existing)
        } else {
            val element = HabitCompletion(
                habitType = habitType,
                dateString = dateString,
                isCompleted = true,
                intakeValue = if (habitType == "WATER_INTAKE") 1 else 1
            )
            habitDao.insertCompletion(element)
        }
    }

    suspend fun incrementWaterIntake(dateString: String) {
        val existing = habitDao.getCompletionByHabitAndDate("WATER_INTAKE", dateString)
        if (existing != null) {
            val updated = existing.copy(intakeValue = existing.intakeValue + 1)
            habitDao.insertCompletion(updated)
        } else {
            val element = HabitCompletion(
                habitType = "WATER_INTAKE",
                dateString = dateString,
                isCompleted = true,
                intakeValue = 1
            )
            habitDao.insertCompletion(element)
        }
    }

    suspend fun decrementWaterIntake(dateString: String) {
        val existing = habitDao.getCompletionByHabitAndDate("WATER_INTAKE", dateString)
        if (existing != null) {
            if (existing.intakeValue <= 1) {
                habitDao.deleteCompletion(existing)
            } else {
                val updated = existing.copy(intakeValue = existing.intakeValue - 1)
                habitDao.insertCompletion(updated)
            }
        }
    }

    suspend fun incrementQuranPages(dateString: String) {
        val existing = habitDao.getCompletionByHabitAndDate("QURAN_RECITE", dateString)
        if (existing != null) {
            val updated = existing.copy(intakeValue = existing.intakeValue + 1, isCompleted = true)
            habitDao.insertCompletion(updated)
        } else {
            val element = HabitCompletion(
                habitType = "QURAN_RECITE",
                dateString = dateString,
                isCompleted = true,
                intakeValue = 1
            )
            habitDao.insertCompletion(element)
        }
    }

    suspend fun decrementQuranPages(dateString: String) {
        val existing = habitDao.getCompletionByHabitAndDate("QURAN_RECITE", dateString)
        if (existing != null) {
            if (existing.intakeValue <= 1) {
                // If 1 or 0, we can reset to 0 but keep it completed, or delete it depending on isCompleted
                val updated = existing.copy(intakeValue = 0)
                habitDao.insertCompletion(updated)
            } else {
                val updated = existing.copy(intakeValue = existing.intakeValue - 1)
                habitDao.insertCompletion(updated)
            }
        }
    }

    // Helper: calculate consecutive streak for a habit
    fun calculateStreak(completions: List<HabitCompletion>): Int {
        if (completions.isEmpty()) return 0
        
        val dates = completions
            .filter { it.isCompleted }
            .map { it.dateString }
            .distinct()
            .toSet()

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()
        val todayStr = format.format(today.time)
        
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = format.format(yesterday.time)

        // If today or yesterday is not in the set, streak is broken
        if (!dates.contains(todayStr) && !dates.contains(yesterdayStr)) {
            return 0
        }

        var streak = 0
        val checkCalendar = Calendar.getInstance()
        
        // Start checking from today (or yesterday if today is not completed yet)
        if (!dates.contains(todayStr)) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateToCheck = format.format(checkCalendar.time)
            if (dates.contains(dateToCheck)) {
                streak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}
