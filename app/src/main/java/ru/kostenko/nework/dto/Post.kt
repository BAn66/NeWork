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
    val mentionIds: Set<Int> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val users: Map<Long, Pair<String, String>>  = mapOf(),
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,

    ): FeedItem

