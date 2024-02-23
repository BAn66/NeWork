package ru.kostenko.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.dao.WallDao
import ru.kostenko.nework.db.AppDb
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.entity.RemoteKeyEntity
import ru.kostenko.nework.entity.WallEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator(
    private val apiService: ApiService,
    private val wallDao: WallDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val authorId: Int
) : RemoteMediator<Int, WallEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, WallEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    if (authorId == 0) apiService.getLatestPostsOnMyWall(state.config.initialLoadSize)
                    else  apiService.getLatestPostsOnWall(authorId, state.config.initialLoadSize)
                }

                LoadType.PREPEND -> {
                    val id = remoteKeyDao.max() ?: return MediatorResult.Success(false)
                    if (authorId == 0) apiService.getAfterPostOnMyWall(id, state.config.pageSize)
                    else apiService.getAfterPostOnWall(authorId, id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = remoteKeyDao.min() ?: return MediatorResult.Success(false)
                    if (authorId == 0) apiService.getBeforePostOnMyWall(id, state.config.pageSize)
                    apiService.getBeforePostOnWall(authorId, id, state.config.pageSize)
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
                        wallDao.removeAll()
                        insertMaxKey(body)
                        insertMinKey(body)
                        wallDao.removeAll()
                    }

                    LoadType.APPEND -> insertMinKey(body)
                    LoadType.PREPEND -> insertMaxKey(body)
                }
                wallDao.insert(body.map(WallEntity.Companion::fromDto))
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