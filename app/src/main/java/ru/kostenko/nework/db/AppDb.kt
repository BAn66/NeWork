package ru.kostenko.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.kostenko.nework.dao.PostRemoteKeyDao
import ru.kostenko.nework.dao.UserDao
import ru.kostenko.nework.entity.PostEntity
import ru.kostenko.nework.entity.PostRemoteKeyEntity
import ru.kostenko.nework.entity.UserEntity
import ru.kostenko.nework.util.Converters
import ru.netologia.nmedia.dao.PostDao

@Database(
    entities =
    [
        UserEntity::class,
        PostEntity::class,
        PostRemoteKeyEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

}