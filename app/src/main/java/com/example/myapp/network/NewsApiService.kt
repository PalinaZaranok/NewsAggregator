package com.example.myapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = "7d3c565382e446afbd4c1441349a1672"
    ): NewsApiResponse
}

data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val source: Source?,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
) {

    fun generateId(): String = url
}

data class Source(
    val id: String?,
    val name: String
)