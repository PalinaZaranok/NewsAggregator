package com.example.myapp.model

import android.util.Log
import com.example.myapp.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow

class NewsRepository(
    private val newsDao: NewsDao,
    private val firebaseRepo: FirebaseRepository = FirebaseRepository()
) {
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews()

    /*
    suspend fun insert(news: NewsEntity) {
        newsDao.insert(news)
    }

     */

    suspend fun insert(news: NewsEntity) {
        try {
            newsDao.insert(news)
            Log.d("NewsRepository", "Insert success: $news")
        } catch (e: Exception) {
            Log.e("NewsRepository", "Insert failed", e)
            throw e
        }
    }

    suspend fun update(news: NewsEntity) {
        newsDao.update(news)
    }

    suspend fun delete(news: NewsEntity) {
        newsDao.delete(news)
    }

    suspend fun getNewsById(id: Int): NewsEntity? = newsDao.getNewsById(id)

    /*
    suspend fun syncWithFirebase() {
        val remote = firebaseRepo.getAllArticles()
        newsDao.deleteAll()
        newsDao.insertAll(remote.map { it.copy(id = 0) })
    }

     */

    suspend fun pushToFirebase(article: NewsEntity): String {
        val firestoreId = firebaseRepo.saveArticle(article)
        if (article.id != 0) {
            newsDao.updateFirestoreId(article.id, firestoreId)
        }
        return firestoreId
    }
}