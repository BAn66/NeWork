package ru.kostenko.nework.dto

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

interface FeedItem {
    val id: Int
}

class DiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class)
            return false
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return newItem == oldItem
    }
}