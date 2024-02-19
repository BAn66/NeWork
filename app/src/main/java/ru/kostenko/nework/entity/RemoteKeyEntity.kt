package ru.kostenko.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Int,
) {
    enum class KeyType {
        AFTER,
        BEFORE,
    }
}

