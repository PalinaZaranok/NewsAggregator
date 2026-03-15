package com.example.myapp.model

import kotlinx.coroutines.flow.Flow

class NewsRepository(private val newsDao: NewsDao) {
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews()

    suspend fun insert(news: NewsEntity) {
        newsDao.insert(news)
    }

    suspend fun update(news: NewsEntity) {
        newsDao.update(news)
    }

    suspend fun delete(news: NewsEntity) {
        newsDao.delete(news)
    }

    suspend fun getNewsById(id: Int): NewsEntity? = newsDao.getNewsById(id)
}