package ru.kostenko.nework.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.error.ApiError
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MediaRepository {

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
}