package ru.kostenko.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.model.PhotoModel

interface UserRepository {
    val data: Flow<List<User>>
    suspend fun authentication(login: String, password: String): AuthResultCode
    suspend fun registration(login: String, password: String, name: String, avatar: PhotoModel): AuthResultCode
    suspend fun getUsers()
    suspend fun getUserById(id: Long): User
}