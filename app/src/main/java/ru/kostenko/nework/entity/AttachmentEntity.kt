package ru.kostenko.nework.entity

import androidx.room.Entity
import ru.kostenko.nework.dto.Attachment
import ru.kostenko.nework.dto.AttachmentType

@Entity
data class AttachmentEntity(
    val url: String,
    val attachmentType: AttachmentType,
) {
    fun toDto() = Attachment(url, attachmentType)

    companion object {
        fun fromDto(dto: Attachment?): AttachmentEntity? {
            return if (dto != null) AttachmentEntity(dto.url, dto.attachmentType) else null
        }
    }
}