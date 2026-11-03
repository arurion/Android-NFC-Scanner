package com.nfcreader.universal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TagScanEntity::class, CustomKeyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagScanDao(): TagScanDao
    abstract fun customKeyDao(): CustomKeyDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nfc_reader.db"
                )
                    // No migration path is needed yet (pre-release schema change):
                    // recreate the database instead of crashing on version bump.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
