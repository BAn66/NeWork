package ru.kostenko.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kostenko.nework.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Query("SELECT max(`key`) FROM RemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT min(`key`) FROM RemoteKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKeyEntity: RemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKeyEntity: List<RemoteKeyEntity>)
}
