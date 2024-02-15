package ru.kostenko.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.kostenko.nework.db.AppDb
import ru.netologia.nmedia.dao.PostDao

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun provideUserDao(appDb: AppDb): UserDao = appDb.userDao()

    @Provides
    fun providePostDao(appDb: AppDb): PostDao = appDb.postDao()

    @Provides
    fun providePostRemoteKeyDao(appDb: AppDb): PostRemoteKeyDao = appDb.postRemoteKeyDao()
}