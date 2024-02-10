package ru.kostenko.nework.di


import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
//    @Singleton
//    @Binds
//    fun bindsPostRepository(
//        impl: PostRepositoryImpl
//    ): PostRepository
}