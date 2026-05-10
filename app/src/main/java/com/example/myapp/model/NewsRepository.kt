package com.example.myapp.model

import android.util.Log
import com.example.myapp.repository.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class NewsRepository(
    private val newsDao: NewsDao,
    private val firebaseRepo: FirebaseRepository = FirebaseRepository(),
    private val scope: CoroutineScope
) {
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews()

    private var snapshotListenerRegistration: ListenerRegistration? = null

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

    suspend fun pushToFirebase(article: NewsEntity): String {
        val firestoreId = firebaseRepo.saveArticle(article)
        if (article.id != 0) {
            newsDao.updateFirestoreId(article.id, firestoreId)
        }
        return firestoreId
    }

    fun startRealtimeUpdates(userId: String) {
        snapshotListenerRegistration = firebaseRepo.observeArticles(userId) { remoteArticles ->
            scope.launch {
                newsDao.deleteAll()
                newsDao.insertAll(remoteArticles.map { it.copy(id = 0) })
            }
        }
    }

    fun stopRealtimeUpdates() {
        snapshotListenerRegistration?.remove()
        snapshotListenerRegistration = null
    }
}