package com.example.utils

import android.content.Context
import com.example.ui.viewmodels.SequencerTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object GoogleDriveBackupManager {

    suspend fun createDriveBackup(
        context: Context,
        projectName: String,
        bpm: Int,
        masterVolume: Float,
        swing: Float,
        tracks: List<SequencerTrack>
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, "drive_backups").apply { if (!exists()) mkdirs() }
            val cleanName = projectName.lowercase().replace(" ", "_").replace("[^a-z0-9_]".toRegex(), "")
            val fileName = "drive_backup_${cleanName}_${System.currentTimeMillis()}.json"
            val backupFile = File(backupDir, fileName)

            val tracksArray = JSONArray()
            var customSamplesCount = 0

            for (t in tracks) {
                val tObj = JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("instrumentType", t.instrumentType.name)
                    put("volume", t.volume)
                    put("pitch", t.pitch)
                    put("pan", t.pan)
                    put("distortion", t.distortion)
                    put("delayMix", t.delayMix)
                    put("reverbMix", t.reverbMix)
                    put("filterCutoff", t.filterCutoff)
                    put("customSamplePath", t.customSamplePath ?: "")
                    put("sampleOriginalBpm", t.sampleOriginalBpm)
                    put("isTempoSynced", t.isTempoSynced)
                    
                    val stepsArray = JSONArray()
                    t.steps.forEach { stepsArray.put(it) }
                    put("steps", stepsArray)
                }
                if (!t.customSamplePath.isNullOrEmpty()) {
                    customSamplesCount++
                }
                tracksArray.put(tObj)
            }

            val rootObj = JSONObject().apply {
                put("version", 1)
                put("backupType", "GOOGLE_DRIVE_EXPORT")
                put("targetScope", "https://www.googleapis.com/auth/drive.file")
                put("projectName", projectName)
                put("bpm", bpm)
                put("masterVolume", masterVolume)
                put("swing", swing)
                put("customSamplesCount", customSamplesCount)
                put("createdAt", System.currentTimeMillis())
                put("tracks", tracksArray)
            }

            backupFile.writeText(rootObj.toString(2))
            Result.success(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
