package com.nfcreader.universal.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagScanDao {
    @Query("SELECT * FROM scanned_tags ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<TagScanEntity>>

    @Query("SELECT * FROM scanned_tags WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): TagScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(entity: TagScanEntity): Long

    @Delete
    suspend fun deleteScan(entity: TagScanEntity)

    @Query("DELETE FROM scanned_tags")
    suspend fun clearAll()
}
