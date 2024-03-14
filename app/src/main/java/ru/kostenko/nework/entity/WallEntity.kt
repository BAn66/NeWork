package ru.kostenko.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.dto.UserPreview

@Entity
data class WallEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String = "",
    val authorId: Int = 0,
    val authorJob: String? = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val published: String = "",
    @Embedded
    val coords: CoordsEntity? = null,
    val link: String? = null,
    val mentionIds:  Set<Long> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds:  Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val users: Map<Long, UserPreview>  = mapOf(),
    @Embedded
    val attachment: AttachmentEntity? = null,
){
    fun toDto() = Post(
        id,
        author,
        authorId,
        authorJob,
        authorAvatar,
        content,
        published,
        coords?.toDto(),
        link,
        mentionIds,
        mentionedMe,
        likeOwnerIds,
        likedByMe,
        users,
        attachment?.toDto(),
    )

    companion object {
        fun fromDto(dto: Post) = WallEntity(
            dto.id,
            dto.author,
            dto.authorId,
            dto.authorJob,
            dto.authorAvatar,
            dto.content,
            dto.published,
            CoordsEntity.fromDto(dto.coords),
            dto.link,
            dto.mentionIds,
            dto.mentionedMe,
            dto.likeOwnerIds,
            dto.likedByMe,
            dto.users,
            AttachmentEntity.fromDto(dto.attachment),
        )
    }
}

fun List<WallEntity>.toDto() = map { it.toDto() }
