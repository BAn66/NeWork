package ru.kostenko.nework.dto

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import ru.kostenko.nework.R

data class Event(
    override val id: Int,
    val authorId: Int = 0,
    val author: String = "",
    val authorJob: String? = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val datetime: String = "",
    val published: String = "",
    val coords: Coords? = null,
    val type: EventType = EventType.OFFLINE,
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Int> = emptySet(),
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val users: Map<Long, Pair<String, String>>  = mapOf(),
    val ownedByMe: Boolean = false,
    ): FeedItem

enum class EventType(val str: String){
    OFFLINE("Offline"),
    ONLINE("Online")
//OFFLINE(R.string.offline as String),
//        ONLINE(R.string.online as String);
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


