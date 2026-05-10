package com.example.myapp.repository

import android.net.Uri
import android.util.Log
import com.example.myapp.model.NewsEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var articlesListenerRegistration: ListenerRegistration? = null

    suspend fun saveArticle(article: NewsEntity): String {
        val docRef = if (article.firestoreId.isNullOrEmpty()) {
            firestore.collection("articles").document()
        } else {
            firestore.collection("articles").document(article.firestoreId!!)
        }
        val data = mapOf(
            "title" to article.title,
            "description" to article.description,
            "date" to article.date,
            "imageUrl" to (article.imageUrl ?: ""),
            "updatedAt" to System.currentTimeMillis()
        )
        docRef.set(data).await()
        return docRef.id
    }


    suspend fun getAllArticles(): List<NewsEntity> {
        val snapshot = firestore.collection("articles").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            NewsEntity(
                id = 0,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                date = data["date"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String,
                firestoreId = doc.id
            )
        }
    }



    fun observeArticles(userId: String, onUpdate: (List<NewsEntity>) -> Unit): ListenerRegistration {
        return firestore.collection("articles")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val articles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NewsEntity::class.java)?.copy(firestoreId = doc.id)
                } ?: emptyList()
                onUpdate(articles)
            }
    }

    fun stopObserving() {
        articlesListenerRegistration?.remove()
        articlesListenerRegistration = null
    }
}