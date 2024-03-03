package ru.kostenko.nework.repository

import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.MediaModel

interface MediaRepository {
    suspend fun saveMediaOnServer(mediaModel: MediaModel): Media
}