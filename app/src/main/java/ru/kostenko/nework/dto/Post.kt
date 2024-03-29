package ru.kostenko.nework.dto

data class Post(
    override val id: Int,
    val author: String = "",
    val authorId: Int = 0,
    val authorJob: String? = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val published: String = "",
    val coords: Coords? = null,
    val link: String? = null,
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val users: Map<Long, UserPreview> = mapOf(),
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    ): FeedItem

