package ru.kostenko.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.kostenko.nework.db.AppDb
import ru.netologia.nmedia.dao.EventDao
import ru.netologia.nmedia.dao.PostDao

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun provideUserDao(appDb: AppDb): UserDao = appDb.userDao()

    @Provides
    fun providePostDao(appDb: AppDb): PostDao = appDb.postDao()

    @Provides
    fun providePostRemoteKeyDao(appDb: AppDb): RemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    fun provideEventDao(appDb: AppDb): EventDao = appDb.eventDao()

    @Provides
    fun provideWallDao(appDb: AppDb): WallDao = appDb.wallDao()

    @Provides
    fun provideJobs(appDb: AppDb): JobDao = appDb.jobDao()
}