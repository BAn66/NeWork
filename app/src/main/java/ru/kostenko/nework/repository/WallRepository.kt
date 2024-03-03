package ru.kostenko.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Post

interface WallRepository {
    val data: Flow<List<Post>>
    fun getMyWallPosts(): Flow<PagingData<FeedItem>>
    suspend fun likeMyPostById(id: Int, likedByMe: Boolean)
    fun getWallPosts(authorId: Int): Flow<PagingData<FeedItem>>
    suspend fun likePostById(authorId:Int, id: Int, likedByMe: Boolean)
    suspend fun clearDb()
}