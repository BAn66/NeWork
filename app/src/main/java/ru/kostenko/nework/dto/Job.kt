package ru.kostenko.nework.dto


data class Job(
    override val id: Int,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long? = null,
    val link: String? = null,

    ): FeedItem {}