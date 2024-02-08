package ru.kostenko.nework.repossitory

import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dto.Token
import ru.kostenko.nework.error.*

import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
    //Для отслеживания кодов ошибок



) : PostRepository {
    //Для отслеживания кодов ошибок
    private var responseErrMess: Pair<Int, String> = Pair(0, "")
    override fun getErrMess(): Pair<Int, String> {
        TODO("Not yet implemented")
    }

    override suspend fun requestToken(login: String, password: String): Token {
        try {
            val response = apiService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body

        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }


}