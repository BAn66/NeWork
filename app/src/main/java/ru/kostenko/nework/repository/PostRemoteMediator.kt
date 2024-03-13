package ru.kostenko.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dao.PostDao
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.entity.PostEntity
import ru.kostenko.nework.db.AppDb
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.entity.RemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatestPosts(state.config.initialLoadSize)
                }

                LoadType.PREPEND -> {
                    val id = remoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getAfterPost(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = remoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBeforePost(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body() ?: throw Error(result.message())

            if (body.isEmpty()) return MediatorResult.Success(
                endOfPaginationReached = true
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.removeAll()
                        insertMaxKey(body)
                        insertMinKey(body)
                        postDao.removeAll()
                    }

                    LoadType.APPEND -> insertMinKey(body)
                    LoadType.PREPEND -> insertMaxKey(body)
                }
                postDao.insert(body.map(PostEntity.Companion::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun insertMaxKey(body: List<Post>) {
        remoteKeyDao.insert(
            RemoteKeyEntity(
                RemoteKeyEntity.KeyType.AFTER,
                body.first().id
            )
        )
    }

    private suspend fun insertMinKey(body: List<Post>) {
        remoteKeyDao.insert(
            RemoteKeyEntity(
                RemoteKeyEntity.KeyType.BEFORE,
                body.last().id,
            )
        )
    }
}
