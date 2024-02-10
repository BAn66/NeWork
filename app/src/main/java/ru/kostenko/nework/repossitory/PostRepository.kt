package ru.kostenko.nework.repossitory

interface PostRepository {
    suspend fun auth(login: String, password: String): AuthResultCode
}