package com.nfcreader.universal.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomKeyDao {
    @Query("SELECT * FROM custom_mifare_keys ORDER BY createdAt DESC")
    fun getAllKeys(): Flow<List<CustomKeyEntity>>

    @Query("SELECT * FROM custom_mifare_keys ORDER BY createdAt DESC")
    suspend fun getAllKeysOnce(): List<CustomKeyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(entity: CustomKeyEntity): Long

    @Delete
    suspend fun deleteKey(entity: CustomKeyEntity)

    @Query("DELETE FROM custom_mifare_keys")
    suspend fun clearAll()
}
