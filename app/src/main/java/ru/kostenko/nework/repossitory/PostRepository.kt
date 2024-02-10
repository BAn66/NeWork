package ru.kostenko.nework.repossitory

import ru.kostenko.nework.model.PhotoModel

interface PostRepository {
    suspend fun authentication(login: String, password: String): AuthResultCode
    suspend fun registration(login: String, password: String, name: String, avatar: PhotoModel): AuthResultCode
}