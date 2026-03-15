package com.example.myapp.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.NewsEntity
import com.example.myapp.model.NewsRepository
import kotlinx.coroutines.launch

class SavedNewsViewModel(private val repository: NewsRepository) : ViewModel() {
    val allNews = repository.allNews

    fun insertNews(title: String, description: String, date: String) {
        viewModelScope.launch {
            repository.insert(NewsEntity(title = title, description = description, date = date))
        }
    }

    fun updateNews(id: Int, title: String, description: String, date: String) {
        viewModelScope.launch {
            repository.update(NewsEntity(id = id, title = title, description = description, date = date))
        }
    }

    fun deleteNews(news: NewsEntity) {
        viewModelScope.launch {
            repository.delete(news)
        }
    }
}