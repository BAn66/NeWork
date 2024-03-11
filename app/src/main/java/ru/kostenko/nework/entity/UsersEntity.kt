package ru.kostenko.nework.entity

import ru.kostenko.nework.dto.UserPreview

data class UsersPreviewEntity (
    val name: String,
    val avatar: String?,
) {
    fun toDto() = UserPreview(name, avatar)

    companion object {
        fun fromDto(dto: UserPreview?) = dto?.let {
            UsersPreviewEntity(dto.name,dto.avatar)
        }
    }
}