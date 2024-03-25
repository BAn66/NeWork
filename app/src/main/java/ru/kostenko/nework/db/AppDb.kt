package ru.kostenko.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.kostenko.nework.dao.EventDao
import ru.kostenko.nework.dao.JobDao
import ru.kostenko.nework.dao.PostDao
import ru.kostenko.nework.dao.RemoteKeyDao
import ru.kostenko.nework.dao.UserDao
import ru.kostenko.nework.dao.WallDao
import ru.kostenko.nework.entity.EventEntity
import ru.kostenko.nework.entity.JobEntity
import ru.kostenko.nework.entity.PostEntity
import ru.kostenko.nework.entity.RemoteKeyEntity
import ru.kostenko.nework.entity.UserEntity
import ru.kostenko.nework.entity.WallEntity
import ru.kostenko.nework.util.Converters

@Database(
    entities =
    [
        UserEntity::class,
        PostEntity::class,
        RemoteKeyEntity::class,
        EventEntity::class,
        WallEntity::class,
        JobEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): RemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun wallDao(): WallDao
    abstract fun jobDao(): JobDao
}