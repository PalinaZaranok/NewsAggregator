package com.example.myapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val imageUrl: String? = null,
    val firestoreId: String? = null,
    val userId: String? = null
)