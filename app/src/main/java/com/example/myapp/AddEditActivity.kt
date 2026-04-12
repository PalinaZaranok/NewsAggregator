package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapp.model.AppDatabase
import com.example.myapp.model.NewsEntity
import com.example.myapp.model.NewsRepository
import kotlinx.coroutines.launch

class AddEditActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSave: Button

    private var newsId: Int = 0
    private lateinit var repository: NewsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        btnSave = findViewById(R.id.btnSave)

        val database = AppDatabase.getInstance(this)
        repository = NewsRepository(database.newsDao())

        newsId = intent.getIntExtra("news_id", 0)

        if (newsId != 0) {
            loadNews()
        }

        btnSave.setOnClickListener {
            saveNews()
        }
    }

    private fun loadNews() {
        lifecycleScope.launch {
            val news = repository.getNewsById(newsId)
            if (news != null) {
                etTitle.setText(news.title)
                etDescription.setText(news.description)
                etDate.setText(news.date)
            }
        }
    }

    private fun saveNews() {
        val title = etTitle.text.toString()
        val description = etDescription.text.toString()
        val date = etDate.text.toString()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (newsId == 0) {
                repository.insert(NewsEntity(title = title, description = description, date = date))
            } else {
                repository.update(NewsEntity(id = newsId, title = title, description = description, date = date))
            }
            finish()
        }
    }
}