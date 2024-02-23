package ru.kostenko.nework.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.dao.WallDao
import ru.kostenko.nework.db.AppDb
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.entity.WallEntity
import ru.kostenko.nework.error.ApiError
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    private val wallDao: WallDao,
    private val apiService: ApiService,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
) : WallRepository {

    val wallList = mutableListOf<Post>()
    private val _wall = MutableLiveData<List<Post>>()
    override val data = _wall.asFlow().flowOn(Dispatchers.Default)

    @OptIn(ExperimentalPagingApi::class)
    override fun getWallPosts(authorId: Int): Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { wallDao.getPagingSource() },
        remoteMediator = WallRemoteMediator(
                apiService = apiService,
                wallDao = wallDao,
                remoteKeyDao = remoteKeyDao,
                appDb = appDb,
                authorId = authorId
            )
    ).flow
        .map { pagingData ->
            pagingData.map(WallEntity::toDto)
        }

    override suspend fun likePostById(authorId: Int, id: Int, likedByMe: Boolean) {
        try {
            wallDao.likeById(id)
            val response =
                apiService.let {
                    if (likedByMe) it.dislikePostByIdOnWall(authorId, id) else it.likePostByIdOnWall(authorId, id)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            wallDao.insert(WallEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun clearDb(){
        wallDao.removeAll()
    }
}