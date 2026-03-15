package com.example.myapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: NewsEntity)

    @Update
    suspend fun update(news: NewsEntity)

    @Delete
    suspend fun delete(news: NewsEntity)

    @Query("SELECT * FROM saved_news ORDER BY date DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM saved_news WHERE id = :id")
    suspend fun getNewsById(id: Int): NewsEntity?
}