package com.example.myapp.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiNewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsList: List<ApiNewsEntity>)

    @Query("SELECT * FROM api_news ORDER BY publishedAt DESC")
    fun getAll(): Flow<List<ApiNewsEntity>>

    @Query("DELETE FROM api_news")
    suspend fun clearAll()
}