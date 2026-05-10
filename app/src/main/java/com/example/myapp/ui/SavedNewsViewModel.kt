package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.NewsEntity
import com.example.myapp.model.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class SavedNewsViewModel(val repository: NewsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    private val _filterDate = MutableStateFlow<Long?>(null)

    val news: Flow<List<NewsEntity>> = combine(
        repository.allNews,
        _searchQuery,
        _sortType,
        _filterDate
    ) { newsList: List<NewsEntity>, query: String, sort: SortType, filterDate: Long? ->
        var result = newsList

        if (query.isNotBlank()) {
            result = result.filter { item ->
                fuzzyMatch(item.title, query) || fuzzyMatch(item.description, query)
            }
        }

        filterDate?.let { threshold ->
            result = result.filter { item ->
                parseDate(item.date) >= threshold
            }
        }

        when (sort) {
            SortType.DATE_DESC -> result.sortedByDescending { parseDate(it.date) }
            SortType.DATE_ASC -> result.sortedBy { parseDate(it.date) }
            SortType.TITLE_ASC -> result.sortedBy { it.title }
            SortType.TITLE_DESC -> result.sortedByDescending { it.title }
        }
    }.flowOn(Dispatchers.IO)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    fun setFilterDateDays(days: Int) {
        val threshold = if (days == 0) null else System.currentTimeMillis() - days * 24 * 3600 * 1000L
        _filterDate.value = threshold
    }

    fun deleteNews(news: NewsEntity) {
        if (!news.imageUrl.isNullOrEmpty()) {
            val file = File(news.imageUrl)
            if (file.exists()) file.delete()
        }
        viewModelScope.launch {
            repository.delete(news)
        }
    }

    // Вспомогательные методы
    private fun fuzzyMatch(text: String, query: String): Boolean {
        val textLower = text.lowercase()
        val queryLower = query.lowercase()
        if (textLower.contains(queryLower)) return true
        return levenshteinDistance(textLower, queryLower) <= 2
    }

    private fun levenshteinDistance(lhs: String, rhs: String): Int {
        val dp = Array(lhs.length + 1) { IntArray(rhs.length + 1) }
        for (i in 0..lhs.length) dp[i][0] = i
        for (j in 0..rhs.length) dp[0][j] = j
        for (i in 1..lhs.length) {
            for (j in 1..rhs.length) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[lhs.length][rhs.length]
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

enum class SortType {
    DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
}