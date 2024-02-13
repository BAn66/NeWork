package ru.kostenko.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kostenko.nework.dao.UserDao
import ru.kostenko.nework.entity.UserEntity

@Database(
    entities =
    [
        UserEntity::class,

    ],
    version = 1,
    exportSchema = false
)

abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao

}