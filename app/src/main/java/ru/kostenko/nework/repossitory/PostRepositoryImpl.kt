package ru.kostenko.nework.repossitory


import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.error.*

import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth
) : PostRepository {
//    override suspend fun requestToken(login: String, password: String): Token {
//        try {
//            val response = apiService.updateUser(login, password)
//            statusCode = response.code()
//            Log.d("APIerrCODE", "requestToken Repository: ${statusCode}")
//            AuthResult.Success
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            return body
//
//        } catch (e: IOException) {
//            throw NetworkError
//
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }

    override suspend fun auth(login: String, password: String): AuthResultCode {
        try {
            val response = apiService.updateUser(login, password)
            return when (response.code()) {
                404 -> AuthResultCode.UserNotFound
                400 -> AuthResultCode.IncorrectPassword
                200 -> {
                    response.body()?.let {
                        appAuth.setAuth(id = it.id, token = it.token)
                    }
                    AuthResultCode.Success
                }

                else -> AuthResultCode.UnknownError
            }
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownError
        }
    }


}

sealed interface AuthResultCode {
    data object Success : AuthResultCode
    data object IncorrectPassword : AuthResultCode
    data object UserNotFound : AuthResultCode
    data object UnknownError : AuthResultCode
}