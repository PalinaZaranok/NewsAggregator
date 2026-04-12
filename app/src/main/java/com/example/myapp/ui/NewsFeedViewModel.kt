package com.example.myapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.ApiNewsEntity
import com.example.myapp.model.ApiNewsRepository
import kotlinx.coroutines.launch

class NewsFeedViewModel(private val repository: ApiNewsRepository) : ViewModel() {
    private val _news = MutableLiveData<List<ApiNewsEntity>>()
    val news: LiveData<List<ApiNewsEntity>> = _news

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadNewsFromCache()
        refreshNews()
    }

    private fun loadNewsFromCache() {
        viewModelScope.launch {
            repository.getNews().collect { cachedNews ->
                _news.postValue(cachedNews)
            }
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshNews()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}