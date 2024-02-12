package ru.kostenko.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kostenko.nework.dao.UserDao
import ru.kostenko.nework.dao.UserRemoteKeyDao
import ru.kostenko.nework.entity.UserEntity
import ru.kostenko.nework.entity.UserRemoteKeyEntity

@Database(
    entities =
    [UserEntity::class, UserRemoteKeyEntity::class],
    version = 3,
    exportSchema = false
)

abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): UserDao
    abstract fun postRemoteKeyDao(): UserRemoteKeyDao
}