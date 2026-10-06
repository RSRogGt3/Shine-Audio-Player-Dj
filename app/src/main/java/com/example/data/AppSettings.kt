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
    val pitch: Float = 1.0f,
    val bpm: Int = 120,
    val isBassKilled: Boolean = false,
    val isMidKilled: Boolean = false,
    val isHighKilled: Boolean = false,
    val isAiBassBoostEnabled: Boolean = true,
    val aiBassBoostStrength: Float = 0.7f,
    val aiBassBoostPreset: String = "DYNAMIC_AI",
    val visualizerMode: String = "SPECTRUM_BARS",
    val isHiRes192kHzEnabled: Boolean = true,
    val visualizerSensitivity: Float = 1.0f,
    val eqLaneCount: Int = 5,
    val eqBassGain: Float = 0f,
    val eqMidGain: Float = 0f,
    val eqTrebleGain: Float = 0f,
    val eqPresetName: String = "Flat",
    val isEqEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
