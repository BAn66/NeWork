package ru.kostenko.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kostenko.nework.dto.Post


@Entity
data class PostEntity(
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
    val mentionIds:  Set<Int> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds:  Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val ownedByMe: Boolean = false,
    val users: Map<Long, Pair<String, String>>  = mapOf(),
    @Embedded
    val attachment: AttachmentEntity? = null,
) {
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
        ownedByMe,
        users,
        attachment?.toDto(),

        )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
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
            dto.ownedByMe,
            dto.users,
            AttachmentEntity.fromDto(dto.attachment),

            )
    }
}

//@Entity
//data class PostAttachmentEntity(
//    val url: String,
//    val attachmentType: AttachmentType
//) {
//    fun toDto() = Attachment(url, attachmentType)
//
//    companion object {
//        fun fromDto(dto: Attachment?): PostAttachmentEntity? {
//            return if (dto != null) PostAttachmentEntity(dto.url, dto.attachmentType) else null
//        }
//    }
//}
//
//@Entity
//data class PostCoordsEntity(
//    val coordslat: Int = 0,
//    val coordslong: Int = 0,
//) {
//    fun toDto() = Coords(coordslat, coordslong)
//
//    companion object {
//        fun fromDto(dto: Coords?): PostCoordsEntity? {
//            return if (dto != null) PostCoordsEntity(dto.latC, dto.longC) else null
//        }
//    }
//}


fun List<PostEntity>.toDto() = map { it.toDto() }
fun List<Post>.toEntity() = map { PostEntity.fromDto(it) }
