package com.example.myapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.databinding.ItemNewsApiBinding
import com.example.myapp.model.ApiNewsEntity

class NewsFeedAdapter : ListAdapter<ApiNewsEntity, NewsFeedAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsApiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: ItemNewsApiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(news: ApiNewsEntity) {
            binding.textTitle.text = news.title
            binding.textDescription.text = news.description ?: ""
            binding.textDate.text = news.publishedAt.take(10)
            Glide.with(binding.imageThumb.context)
                .load(news.urlToImage)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(binding.imageThumb)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ApiNewsEntity>() {
        override fun areItemsTheSame(oldItem: ApiNewsEntity, newItem: ApiNewsEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ApiNewsEntity, newItem: ApiNewsEntity): Boolean =
            oldItem == newItem
    }
}