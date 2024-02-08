package ru.kostenko.nework.repossitory

import ru.kostenko.nework.dto.Token

interface PostRepository {
    suspend fun requestToken(login: String, password: String): Token
    fun getErrMess(): Pair<Int, String>
}