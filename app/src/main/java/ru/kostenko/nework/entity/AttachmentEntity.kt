package ru.kostenko.nework.entity

import ru.kostenko.nework.dto.Attachment
import ru.kostenko.nework.dto.AttachmentType

data class AttachmentEntity(
    val url: String,
    val type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEntity(dto.url,dto.type)
        }
    }
}