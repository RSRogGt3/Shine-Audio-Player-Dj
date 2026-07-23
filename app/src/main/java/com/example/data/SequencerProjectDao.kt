package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SequencerProjectDao {
    @Query("SELECT * FROM sequencer_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<SequencerProjectEntity>>

    @Query("SELECT * FROM sequencer_projects WHERE id = :id")
    suspend fun getProjectById(id: String): SequencerProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: SequencerProjectEntity)

    @Query("DELETE FROM sequencer_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
