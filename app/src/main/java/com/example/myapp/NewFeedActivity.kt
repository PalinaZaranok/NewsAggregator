package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.ActivityNewFeedBinding
import com.example.myapp.model.ApiNewsRepository
import com.example.myapp.ui.NewsFeedAdapter
import com.example.myapp.ui.NewsFeedViewModel

class NewFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewFeedBinding
    private lateinit var viewModel: NewsFeedViewModel
    private lateinit var adapter: NewsFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = ApiNewsRepository(applicationContext)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return NewsFeedViewModel(repository) as T
            }
        })[NewsFeedViewModel::class.java]

        adapter = NewsFeedAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.news.observe(this) { newsList ->
            adapter.submitList(newsList)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshNews()
        }
    }
}