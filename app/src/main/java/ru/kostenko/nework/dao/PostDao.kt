package ru.netologia.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.entity.PostEntity


@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllFromDb(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для сохранения и редактирования, replace заменяет если есть такой же айди кажется
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для getall
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("UPDATE PostEntity SET likedByMe = 1 WHERE id = :id AND likedByMe = 0")
    suspend fun likeById(id: Int)

    @Query("UPDATE PostEntity SET likedByMe = 0 WHERE id = :id AND likedByMe = 1")
    suspend fun unlikeById(id: Int)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity

    @Query("SELECT MAX(id) FROM PostEntity")
    fun max(): Flow<Int?>

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}
