package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit_completions ORDER BY dateString DESC")
    fun getAllCompletions(): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitType = :habitType ORDER BY dateString DESC")
    fun getCompletionsByHabit(habitType: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE dateString = :dateString")
    fun getCompletionsByDate(dateString: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitType = :habitType AND dateString = :dateString LIMIT 1")
    suspend fun getCompletionByHabitAndDate(habitType: String, dateString: String): HabitCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitType = :habitType AND dateString = :dateString")
    suspend fun deleteCompletionByHabitAndDate(habitType: String, dateString: String)
}
