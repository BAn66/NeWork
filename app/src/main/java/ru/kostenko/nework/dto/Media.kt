package ru.kostenko.nework.dto

import android.net.Uri

import java.io.InputStream

data class Media(val url: String)

data class MediaModel(
    val uri: Uri? = null,
    val inputStream: InputStream? = null,
    val type: AttachmentType? = null
)