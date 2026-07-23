package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SequencerProjectRepository(private val dao: SequencerProjectDao) {

    val allLocalProjects: Flow<List<SequencerProjectEntity>> = dao.getAllProjects()

    suspend fun saveProject(project: SequencerProjectEntity, saveToCloud: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Save locally first
                dao.insertProject(project)

                var cloudSuccess = false
                if (saveToCloud) {
                    try {
                        val firestore = FirebaseFirestore.getInstance()
                        val projectMap = mapOf(
                            "id" to project.id,
                            "name" to project.name,
                            "bpm" to project.bpm,
                            "masterVolume" to project.masterVolume,
                            "swing" to project.swing,
                            "tracksJson" to project.tracksJson,
                            "updatedAt" to project.updatedAt
                        )
                        firestore.collection("sequencer_projects")
                            .document(project.id)
                            .set(projectMap)
                            .await()

                        cloudSuccess = true
                        dao.insertProject(project.copy(isCloudSynced = true))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cloudSuccess = false
                    }
                }
                Result.success(cloudSuccess)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteProject(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteProjectById(id)
            try {
                FirebaseFirestore.getInstance()
                    .collection("sequencer_projects")
                    .document(id)
                    .delete()
            } catch (_: Exception) {}
        }
    }

    suspend fun syncCloudProjects(): List<SequencerProjectEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("sequencer_projects").get().await()
                val projects = mutableListOf<SequencerProjectEntity>()

                for (doc in snapshot.documents) {
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Unassigned Project"
                    val bpm = doc.getLong("bpm")?.toInt() ?: 120
                    val masterVolume = doc.getDouble("masterVolume")?.toFloat() ?: 0.9f
                    val swing = doc.getDouble("swing")?.toFloat() ?: 0.0f
                    val tracksJson = doc.getString("tracksJson") ?: "[]"
                    val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                    val p = SequencerProjectEntity(
                        id = id,
                        name = name,
                        bpm = bpm,
                        masterVolume = masterVolume,
                        swing = swing,
                        tracksJson = tracksJson,
                        updatedAt = updatedAt,
                        isCloudSynced = true
                    )
                    dao.insertProject(p)
                    projects.add(p)
                }
                projects
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
