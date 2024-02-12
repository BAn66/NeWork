package ru.kostenko.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.entity.*

interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM UserEntity ORDER BY name ASC")
    fun getAll(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(users: List<UserEntity>)

    @Query("DELETE FROM UserEntity WHERE id = :id")
    suspend fun removeById(id: Int)
}

@Dao
interface UserRemoteKeyDao {
    @Query("SELECT max(`key`) FROM UserRemoteKeyEntity") //поиск максимального айди среди постов
    suspend fun max(): Long? //Если данных может не быть в базе, можно сделать их опциональными поставив знак "?"

    @Query("SELECT min(`key`) FROM UserRemoteKeyEntity") //поиск минимального айди среди постов
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE ) //Запись одного энтети
    suspend fun insert(postRemoteKeyEntity: UserRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE ) //Запись в бд листа энтети
    suspend fun insert(postRemoteKeyEntityList: List<UserRemoteKeyEntity>)

    @Query("DELETE FROM UserRemoteKeyEntity")
    suspend fun clear()

}