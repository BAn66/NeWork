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
import ru.kostenko.nework.dao.PostDao
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.db.AppDb
import ru.kostenko.nework.dto.Attachment
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.entity.PostEntity
import ru.kostenko.nework.error.ApiError
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    remoteKeyDao: RemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val dataPosts: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            remoteKeyDao = remoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }

    override val newerPostId: Flow<Int?> = postDao.max()

    override suspend fun savePost(post: Post, mediaModel: MediaModel?) {
        try {
            val postWithAttachment = if (mediaModel != null) {
                val media = saveMediaOnServer(mediaModel)
                post.copy(attachment = Attachment(media.url, requireNotNull(mediaModel.type)))
            } else {
                post.copy()
            }

            val response = apiService.savePost(postWithAttachment)

            val body = response.body() ?: throw ApiError(response.message())
            postDao.insert(PostEntity.fromDto(body))
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
                throw ApiError(response.message())
            }
            return response.body() ?: throw ApiError(response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removePostById(id: Int) {
        try {
            postDao.removeById(id)
            val response = apiService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            response.body() ?: throw ApiError(response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likePostById(id: Int, likedByMe: Boolean) {
        try {
            postDao.likeById(id)
            val response =
                apiService.let {
                    if (likedByMe) it.dislikePostById(id) else it.likePostById(id)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            postDao.insert(PostEntity.fromDto(body))
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
                throw ApiError(response.message())
            }
            return response.body() ?: throw ApiError(response.message())
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    suspend fun getPostById(id: Int): Post {
        try {
            val response = apiService.getPostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            return response.body() ?: throw ApiError(response.message())
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw Exception(e)
        }
    }
}