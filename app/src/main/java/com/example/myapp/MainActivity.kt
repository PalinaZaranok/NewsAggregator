package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
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
import com.example.myapp.ui.SortType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var spinnerSort: Spinner
    private lateinit var btnFilter: Button
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
        searchView = findViewById(R.id.searchView)
        spinnerSort = findViewById(R.id.spinnerSort)
        btnFilter = findViewById(R.id.btnFilter)

        setupRecyclerView()
        observeData()
        setupListeners()
        setupSearchView()
        setupSortSpinner()
        setupFilterButton()

        testFirestoreConnection()


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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.news.collect { list ->
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

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
        searchView.clearFocus()
    }


    private fun setupSortSpinner() {
        val sortOptions = arrayOf(
            getString(R.string.sort_date_desc),
            getString(R.string.sort_date_asc),
            getString(R.string.sort_title_asc),
            getString(R.string.sort_title_desc)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = adapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortType = when (position) {
                    0 -> SortType.DATE_DESC
                    1 -> SortType.DATE_ASC
                    2 -> SortType.TITLE_ASC
                    else -> SortType.TITLE_DESC
                }
                viewModel.setSortType(sortType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }


    private fun setupFilterButton() {
        btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val days = arrayOf("7 days", "30 days", "All")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Filter by date")
            .setItems(days) { _, which ->
                when (which) {
                    0 -> viewModel.setFilterDateDays(7)
                    1 -> viewModel.setFilterDateDays(30)
                    2 -> viewModel.setFilterDateDays(0)
                }
            }
            .show()
    }

    private fun testFirestoreConnection() {
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf("testMessage" to "Привет, Firebase! Это тестовая запись!")

        db.collection("test_collection")
            .document("test_document")
            .set(testData)
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ УСПЕХ! Документ успешно создан!")
                Toast.makeText(this, "Firestore работает!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "❌ ОШИБКА: ${e.message}")
                Toast.makeText(this, "Ошибка Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}