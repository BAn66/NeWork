package ru.kostenko.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.dto.Token
import ru.kostenko.nework.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    val newerPostId: Flow<Int?>
    suspend fun savePost(post: Post)
    suspend fun saveWithAttachment(post: Post, mediaModel: MediaModel)
    suspend fun saveMediaOnServer(mediaModel: MediaModel): Media
    suspend fun removePostById(id: Int)
    suspend fun likePostById(id: Int, likedByMe: Boolean)
}
