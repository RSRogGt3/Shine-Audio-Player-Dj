package com.example.ui.models

enum class PlayerNotificationType(val label: String, val colorHex: Long) {
    INFO("INFO", 0xFF00E5FF),
    SUCCESS("BESTÄTIGT", 0xFF00E676),
    WARNING("WARNUNG", 0xFFFF9100),
    SYNC("BEAT SYNC", 0xFFFFD600),
    RECORDING("AUFNAHME", 0xFFFF1744)
}

data class PlayerNotification(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: PlayerNotificationType = PlayerNotificationType.INFO,
    val timestamp: Long = System.currentTimeMillis()
)
