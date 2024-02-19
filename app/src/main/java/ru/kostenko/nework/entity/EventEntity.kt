package ru.kostenko.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.EventType



@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int = 0,
    val author: String = "",
    val authorJob: String? = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val datetime: String = "",
    val published: String = "",
    @Embedded
    val coords: CoordsEntity? = null,
    val type: EventType = EventType.OFFLINE,
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Int> = emptySet(),
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEntity? = null,
    val link: String? = null,
    val users: Map<Long, Pair<String, String>> = mapOf(),


    ) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        content,
        datetime,
        published,
        coords?.toDto(),
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment?.toDto(),
        link,
        users,
        )

    companion object {
        fun fromDto(dto: Event) = EventEntity(
            dto.id,
            dto.authorId,
            dto.author,
            dto.authorJob,
            dto.authorAvatar,
            dto.content,
            dto.datetime,
            dto.published,
            CoordsEntity.fromDto(dto.coords),
            dto.type,
            dto.likeOwnerIds,
            dto.likedByMe,
            dto.speakerIds,
            dto.participantsIds,
            dto.participatedByMe,
            AttachmentEntity.fromDto(dto.attachment),
            dto.link,
            dto.users,
            )
    }
}





fun List<EventEntity>.toDto() = map { it.toDto() }
fun List<Event>.toEntity() = map { EventEntity.fromDto(it) }
