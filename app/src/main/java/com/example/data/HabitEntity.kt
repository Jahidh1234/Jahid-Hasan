package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitType: String, // EXERCISE, STUDY, READING, PRAYER, WATER_INTAKE
    val dateString: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = true,
    val intakeValue: Int = 1 // For water intake, e.g. glasses. For others, 1.
) : Serializable
