package com.mediapro.studio.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [ProjectEntity::class], version = 1, exportSchema = true)
abstract class MediaProDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
