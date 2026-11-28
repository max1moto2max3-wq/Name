package com.mediapro.studio.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface ProjectDao {
    @Query("SELECT * FROM media_projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)
    @Query("DELETE FROM media_projects WHERE id = :id")
    suspend fun deleteById(id: String)
}
