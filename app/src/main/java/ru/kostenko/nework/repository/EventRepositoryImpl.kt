package ru.kostenko.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.db.AppDb
import ru.kostenko.nework.dto.Attachment
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.entity.EventEntity
import ru.kostenko.nework.error.ApiError
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import ru.netologia.nmedia.dao.EventDao
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    remoteKeyDao: RemoteKeyDao,
    appDb: AppDb
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val dataEvents: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(
            apiService = apiService,
            eventDao = eventDao,
            remoteKeyDao = remoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { pagingData ->
            pagingData.map(EventEntity::toDto)
        }

    override suspend fun saveEvent(event: Event) {
        try {
            eventDao.insert(EventEntity.fromDto(event))
            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, mediaModel: MediaModel) {
        try {
            val media = saveMediaOnServer(mediaModel)
            val eventWithAttachment = event.copy(attachment = mediaModel.type?.let { type ->
                Attachment(media.url, type)
            })
            saveEvent(eventWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveMediaOnServer(mediaModel: MediaModel): Media {
        try {
            val data = MultipartBody.Part.createFormData(
                "file", "name", mediaModel.inputStream!!.readBytes()
                    .toRequestBody("*/*".toMediaTypeOrNull())
            )
            val response = apiService.saveMediaOnServer(data)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeEventById(id: Int) {
        try {
            eventDao.removeById(id)
            val response = apiService.removeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeEventById(id: Int, likedByMe: Boolean) {
        try {
            eventDao.likeById(id)
            val response =
                apiService.let {
                    if (likedByMe) it.dislikeEventById(id) else it.likeEventById(id)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getUserById(id: Int): User {
        try {
            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    suspend fun getEventById(id: Int): Event {
        try {
            val response = apiService.getEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw Exception(e)
        }
    }
}