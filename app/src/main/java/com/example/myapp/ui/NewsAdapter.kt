package com.example.myapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.model.NewsEntity
import java.io.File

class NewsAdapter(
    private val onEditClick: (NewsEntity) -> Unit,
    private val onDeleteClick: (NewsEntity) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var items = listOf<NewsEntity>()

    fun submitList(list: List<NewsEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage) // Убедитесь, что ID совпадает
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: NewsEntity) {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvDate.text = item.date

            // Загружаем изображение через Glide
            val imagePath = item.imageUrl
            if (!imagePath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(File(imagePath))
                    .placeholder(android.R.drawable.ic_menu_gallery) // теперь ресурс корректен
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnEdit.setOnClickListener { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}