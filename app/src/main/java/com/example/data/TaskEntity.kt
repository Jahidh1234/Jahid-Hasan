package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Personal, Business, Study, Family, Health, Custom Category
    val priority: String, // High, Medium, Low
    val startDate: Long, // Epoch millis
    val dueDate: Long, // Epoch millis
    val reminderMinutesBefore: Int = 0, // Reminder setting: e.g. 0, 15, 30, 60 minutes before
    val notes: String = "",
    val isCompleted: Boolean = false,
    val completionDate: Long? = null, // Epoch millis when completed
    val delayMinutes: Long = 0, // Delay minutes calculated against due date
    val completionDurationMinutes: Long = 0 // Duration in minutes from start to complete
) : Serializable
