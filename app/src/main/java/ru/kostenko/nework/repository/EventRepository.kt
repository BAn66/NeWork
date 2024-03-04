package ru.kostenko.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel


interface EventRepository {
    val dataEvents: Flow<PagingData<FeedItem>>
    suspend fun saveEvent(event: Event, mediaModel: MediaModel?)
    suspend fun saveMediaOnServer(mediaModel: MediaModel): Media
    suspend fun removeEventById(id: Int)
    suspend fun likeEventById(id: Int, likedByMe: Boolean)
    suspend fun participateById(id: Int, participatedByMe: Boolean)
}