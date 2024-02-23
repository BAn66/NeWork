package ru.kostenko.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.entity.WallEntity

@Dao
interface WallDao {
    @Query("SELECT * FROM WallEntity ORDER BY id DESC")
    fun getAllFromDb(): Flow<List<WallEntity>>

    @Query("SELECT * FROM WallEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, WallEntity>

    @Query("SELECT COUNT(*) == 0 FROM WallEntity")
    suspend fun isEmpty(): Boolean

    @Query("DELETE FROM WallEntity")
    suspend fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для сохранения и редактирования, replace заменяет если есть такой же айди кажется
    suspend fun insert(post: WallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для getall
    suspend fun insert(posts: List<WallEntity>)

    @Query("DELETE FROM WallEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("UPDATE WallEntity SET likedByMe = 1 WHERE id = :id AND likedByMe = 0")
    suspend fun likeById(id: Int)

    @Query("UPDATE WallEntity SET likedByMe = 0 WHERE id = :id AND likedByMe = 1")
    suspend fun unlikeById(id: Int)

    @Query("SELECT * FROM WallEntity WHERE id = :id")
    suspend fun getPostById(id: Int): WallEntity

}