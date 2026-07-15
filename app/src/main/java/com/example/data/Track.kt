package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val audioDataBase64: String, // Storing base64 encoded audio for simplicity in this prototype
    val isAiGenerated: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
