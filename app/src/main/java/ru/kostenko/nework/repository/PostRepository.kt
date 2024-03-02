package ru.kostenko.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.dto.Post

interface PostRepository {
    val dataPosts: Flow<PagingData<FeedItem>>
    val newerPostId: Flow<Int?>
    suspend fun savePost(post: Post, mediaModel: MediaModel?)
    suspend fun saveMediaOnServer(mediaModel: MediaModel): Media
    suspend fun removePostById(id: Int)
    suspend fun likePostById(id: Int, likedByMe: Boolean)
}
