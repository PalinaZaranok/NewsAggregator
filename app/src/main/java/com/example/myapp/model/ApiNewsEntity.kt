package com.example.myapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_news")
data class ApiNewsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String
)