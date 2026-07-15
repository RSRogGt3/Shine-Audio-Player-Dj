package com.example.data

import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    val settings: Flow<AppSettings?> = settingsDao.getSettings()

    suspend fun saveSettings(settings: AppSettings) {
        settingsDao.saveSettings(settings)
    }
}
