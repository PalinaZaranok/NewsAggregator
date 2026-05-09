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

    @Query("SELECT * FROM news ORDER BY date DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :id")
    suspend fun getNewsById(id: Int): NewsEntity?

    @Query("UPDATE news SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateFirestoreId(id: Int, firestoreId: String)

    @Query("DELETE FROM news")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsList: List<NewsEntity>)

}