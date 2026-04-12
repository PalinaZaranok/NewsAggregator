package com.example.myapp.model

import android.content.Context
import com.example.myapp.network.NewsApiService
import com.example.myapp.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiNewsRepository(private val context: Context) {
    private val apiService: NewsApiService
    private val dao: ApiNewsDao

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(NewsApiService::class.java)
        dao = AppDatabase.getInstance(context).apiNewsDao()
    }

    fun getNews(): Flow<List<ApiNewsEntity>> = dao.getAll()

    suspend fun refreshNews() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val response = apiService.getTopHeadlines()
                if (response.status == "ok") {
                    val newsEntities = response.articles.map { article ->
                        ApiNewsEntity(
                            id = article.generateId(),
                            title = article.title,
                            description = article.description,
                            url = article.url,
                            urlToImage = article.urlToImage,
                            publishedAt = article.publishedAt
                        )
                    }
                    dao.clearAll()
                    dao.insertAll(newsEntities)
                }
            } catch (e: Exception) {
                // Ошибка сети или API – игнорируем, остаются старые данные из кэша
                e.printStackTrace()
            }
        }
        // Если сети нет – ничего не делаем, данные остаются из БД (кэш)
    }
}