package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 0,
    val volume: Float = 1.0f,
    val ch1Level: Float = 0.8f,
    val ch2Level: Float = 0.8f,
    val masterLevel: Float = 0.8f,
    val isShuffle: Boolean = false,
    val isLoop: Boolean = false,
    val isAutoDjEnabled: Boolean = false,
    val currentSkinId: String = "retro_gold",
    val eqLevels: String = "", // Comma separated levels like "0,0,0,0,0"
    val crossfader: Float = 0.5f,
    val pitch: Float = 1.0f
)
