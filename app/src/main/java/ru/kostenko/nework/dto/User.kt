package ru.kostenko.nework.dto

data class User(
    override val id: Long = 0L,
    val login: String = "",
    val name: String = "",
    val avatar: String? = null
) : FeedItem{}

