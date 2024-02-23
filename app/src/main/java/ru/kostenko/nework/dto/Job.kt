package ru.kostenko.nework.dto


data class Job(
    override val id: Int,
    val name: String,
    val position: String,
    val start: String? = null,
    val finish: String? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,

    ): FeedItem {}