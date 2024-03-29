package ru.kostenko.nework.dto

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType{
    IMAGE, AUDIO, VIDEO
}