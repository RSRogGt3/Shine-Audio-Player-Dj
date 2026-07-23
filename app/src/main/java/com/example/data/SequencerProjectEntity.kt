package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequencer_projects")
data class SequencerProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bpm: Int,
    val masterVolume: Float,
    val swing: Float,
    val tracksJson: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = false
)
