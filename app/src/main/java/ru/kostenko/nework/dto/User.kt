package ru.kostenko.nework.dto

data class User(
    override val id: Int = 0,
    val login: String = "",
    val name: String = "",
    val avatar: String? = null,
    var isTaken: Boolean = false,
    var isChecked: Boolean = false
) : FeedItem


