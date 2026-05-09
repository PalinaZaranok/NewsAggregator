package com.example.myapp

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapp.databinding.ActivityAddEditBinding
import com.example.myapp.model.AppDatabase
import com.example.myapp.model.NewsEntity
import com.example.myapp.model.NewsRepository
import com.example.myapp.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var repository: NewsRepository
    private lateinit var firebaseRepo: FirebaseRepository

    private var newsId: Int = 0
    private var existingImageUrl: String? = null
    private var existingFirestoreId: String? = null
    private var selectedImageUri: Uri? = null


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imagePreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getInstance(this)
        repository = NewsRepository(database.newsDao())
        firebaseRepo = FirebaseRepository()

        newsId = intent.getIntExtra("news_id", 0)

        if (newsId != 0) {
            loadNews()
        }

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveNews()
        }
    }

    private fun loadNews() {
        lifecycleScope.launch {
            val news = repository.getNewsById(newsId)
            if (news != null) {
                binding.etTitle.setText(news.title)
                binding.etDescription.setText(news.description)
                binding.etDate.setText(news.date)
                existingImageUrl = news.imageUrl
                existingFirestoreId = news.firestoreId
                if (!news.imageUrl.isNullOrEmpty()) {
                    Glide.with(this@AddEditActivity)
                        .load(news.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.imagePreview)
                }
            }
        }
    }

    /*
    private fun saveNews() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var imagePath = existingImageUrl

            selectedImageUri?.let { uri ->
                deleteOldImage(existingImageUrl)
                imagePath = saveImageLocally(uri)
            }

            val article = NewsEntity(
                id = newsId,
                title = title,
                description = description,
                date = date,
                imageUrl = imagePath,
                firestoreId = existingFirestoreId
            )

            if (newsId == 0) {
                repository.insert(article)
            } else {
                repository.update(article)
            }

            Toast.makeText(this@AddEditActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }


     */
    private fun saveNews() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                var imagePath = existingImageUrl

                // Сохраняем изображение, если выбрано
                selectedImageUri?.let { uri ->
                    deleteOldImage(existingImageUrl)
                    imagePath = saveImageLocally(uri)
                    if (imagePath == null) {
                        Toast.makeText(this@AddEditActivity, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
                    }
                }

                val article = NewsEntity(
                    id = newsId,
                    title = title,
                    description = description,
                    date = date,
                    imageUrl = imagePath,
                    firestoreId = existingFirestoreId
                )

                // Сохраняем в Room
                if (newsId == 0) {
                    repository.insert(article)
                    Log.d("AddEditActivity", "Inserted new article")
                } else {
                    repository.update(article)
                    Log.d("AddEditActivity", "Updated article")
                }

                Toast.makeText(this@AddEditActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("AddEditActivity", "Error saving news", e)
                Toast.makeText(this@AddEditActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun uriToFile(uri: Uri): File? {
        val contentResolver: ContentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        return tempFile
    }

    private fun saveImageLocally(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName) // папка app's private storage
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("AddEditActivity", "Failed to save image", e)
            null
        }
    }


    private fun deleteOldImage(path: String?) {
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }
}