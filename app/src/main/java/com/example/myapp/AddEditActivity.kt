package com.example.myapp

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var repository: NewsRepository
    private lateinit var firebaseRepo: FirebaseRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
        repository = NewsRepository(
            newsDao = database.newsDao(),
            firebaseRepo = FirebaseRepository(),
            scope = lifecycleScope
        )
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
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

    private fun getCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val locText = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                binding.etDescription.append("\nLocation: $locText")
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }
}