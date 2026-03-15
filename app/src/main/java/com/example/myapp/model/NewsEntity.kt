package com.example.myapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "saved_news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: String
)
