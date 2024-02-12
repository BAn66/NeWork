package ru.kostenko.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kostenko.nework.dto.User

@Entity
class UserEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null
){
    fun toDto() = User(id, login, name, avatar)
    companion object {
        fun fromDto(dto: User) = UserEntity(dto.id, dto.login, dto.name, dto.avatar)
    }
}

fun List<UserEntity>.toDto() = map(UserEntity::toDto)
fun List<User>.toUserEntity() = map(UserEntity.Companion::fromDto)

@Entity
data class UserRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Long,
) {
    enum class KeyType{
        AFTER, //Вверху пост
        BEFORE //В самом низу пост
    }
}