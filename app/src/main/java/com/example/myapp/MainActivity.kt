package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.model.AppDatabase
import com.example.myapp.model.NewsRepository
import com.example.myapp.ui.NewsAdapter
import com.example.myapp.ui.SavedNewsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var adapter: NewsAdapter

    private val viewModel: SavedNewsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getInstance(applicationContext)
                val repository = NewsRepository(database.newsDao())
                return SavedNewsViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnAdd = findViewById(R.id.btnAdd)

        setupRecyclerView()
        observeData()
        setupListeners()

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnNewsFeed).setOnClickListener {
            startActivity(Intent(this, NewFeedActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onEditClick = { news ->
                val intent = Intent(this, AddEditActivity::class.java).apply {
                    putExtra("news_id", news.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { news ->
                viewModel.deleteNews(news)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        // Подписка на Flow с учётом жизненного цикла
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allNews.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }

    private fun setupListeners() {
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }
}