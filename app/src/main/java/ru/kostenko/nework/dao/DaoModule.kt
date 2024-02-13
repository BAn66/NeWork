package ru.kostenko.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.kostenko.nework.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun providePostDao(appDb: AppDb): UserDao = appDb.userDao()
}