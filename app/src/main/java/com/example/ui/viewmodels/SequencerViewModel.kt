package com.example.ui.viewmodels

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.InstrumentType
import com.example.audio.SequencerAudioEngine
import com.example.data.AppDatabase
import com.example.data.SequencerProjectEntity
import com.example.data.SequencerProjectRepository
import com.example.utils.TrackSerializer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SequencerTrack(
    val id: String,
    val name: String,
    val instrumentType: InstrumentType,
    val color: Color,
    val steps: BooleanArray = BooleanArray(16) { false },
    val volume: Float = 0.8f,
    val pan: Float = 0.0f,
    val pitch: Float = 1.0f,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val appliedStylePreset: String? = null,
    val distortion: Float = 0.0f,
    val delayMix: Float = 0.0f,
    val reverbMix: Float = 0.0f,
    val filterCutoff: Float = 1.0f,
    val customSamplePath: String? = null,
    val isBassKilled: Boolean = false,
    val isMidKilled: Boolean = false,
    val isHighKilled: Boolean = false,
    val sampleOriginalBpm: Int = 120,
    val isTempoSynced: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SequencerTrack

        if (id != other.id) return false
        if (name != other.name) return false
        if (instrumentType != other.instrumentType) return false
        if (color != other.color) return false
        if (!steps.contentEquals(other.steps)) return false
        if (volume != other.volume) return false
        if (pan != other.pan) return false
        if (pitch != other.pitch) return false
        if (isMuted != other.isMuted) return false
        if (isSoloed != other.isSoloed) return false
        if (appliedStylePreset != other.appliedStylePreset) return false
        if (distortion != other.distortion) return false
        if (delayMix != other.delayMix) return false
        if (reverbMix != other.reverbMix) return false
        if (filterCutoff != other.filterCutoff) return false
        if (isBassKilled != other.isBassKilled) return false
        if (isMidKilled != other.isMidKilled) return false
        if (isHighKilled != other.isHighKilled) return false
        if (customSamplePath != other.customSamplePath) return false
        if (sampleOriginalBpm != other.sampleOriginalBpm) return false
        if (isTempoSynced != other.isTempoSynced) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + instrumentType.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + steps.contentHashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + pan.hashCode()
        result = 31 * result + pitch.hashCode()
        result = 31 * result + isMuted.hashCode()
        result = 31 * result + isSoloed.hashCode()
        result = 31 * result + (appliedStylePreset?.hashCode() ?: 0)
        result = 31 * result + distortion.hashCode()
        result = 31 * result + delayMix.hashCode()
        result = 31 * result + reverbMix.hashCode()
        result = 31 * result + filterCutoff.hashCode()
        result = 31 * result + isBassKilled.hashCode()
        result = 31 * result + isMidKilled.hashCode()
        result = 31 * result + isHighKilled.hashCode()
        result = 31 * result + (customSamplePath?.hashCode() ?: 0)
        result = 31 * result + sampleOriginalBpm.hashCode()
        result = 31 * result + isTempoSynced.hashCode()
        return result
    }
}

enum class CloudSyncState {
    SAVED,
    SAVING,
    NEEDS_ATTENTION
}

class SequencerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = SequencerAudioEngine()
    private val database = AppDatabase.getDatabase(application)
    private val projectRepository = SequencerProjectRepository(database.sequencerProjectDao())

    val savedProjects: StateFlow<List<SequencerProjectEntity>> = projectRepository.allLocalProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _currentProjectName = MutableStateFlow<String>("My House Beat")
    val currentProjectName: StateFlow<String> = _currentProjectName.asStateFlow()

    private val _tracks = MutableStateFlow<List<SequencerTrack>>(emptyList())
    val tracks: StateFlow<List<SequencerTrack>> = _tracks.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStep = MutableStateFlow(-1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _currentBar = MutableStateFlow(1)
    val currentBar: StateFlow<Int> = _currentBar.asStateFlow()

    private val _isSynced = MutableStateFlow(true)
    val isSynced: StateFlow<Boolean> = _isSynced.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.9f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _swing = MutableStateFlow(0.0f)
    val swing: StateFlow<Float> = _swing.asStateFlow()

    private val _activePresetName = MutableStateFlow("4-on-the-Floor House")
    val activePresetName: StateFlow<String> = _activePresetName.asStateFlow()

    // Master EQ Kill States
    private val _isMasterBassKilled = MutableStateFlow(false)
    val isMasterBassKilled: StateFlow<Boolean> = _isMasterBassKilled.asStateFlow()

    private val _isMasterMidKilled = MutableStateFlow(false)
    val isMasterMidKilled: StateFlow<Boolean> = _isMasterMidKilled.asStateFlow()

    private val _isMasterHighKilled = MutableStateFlow(false)
    val isMasterHighKilled: StateFlow<Boolean> = _isMasterHighKilled.asStateFlow()

    private val _isGeneratingAiBeat = MutableStateFlow(false)
    val isGeneratingAiBeat: StateFlow<Boolean> = _isGeneratingAiBeat.asStateFlow()

    // Firestore & Drive Sync States
    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.SAVED)
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow<Long?>(System.currentTimeMillis())
    val lastCloudSyncTime: StateFlow<Long?> = _lastCloudSyncTime.asStateFlow()

    private val _isDriveBackupEnabled = MutableStateFlow(true)
    val isDriveBackupEnabled: StateFlow<Boolean> = _isDriveBackupEnabled.asStateFlow()

    private val _lastDriveBackupTime = MutableStateFlow<Long?>(null)
    val lastDriveBackupTime: StateFlow<Long?> = _lastDriveBackupTime.asStateFlow()

    private var sequencerJob: Job? = null

    init {
        loadPreset("4-on-the-Floor House")
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun markUnsavedChanges() {
        if (_cloudSyncState.value == CloudSyncState.SAVED) {
            _cloudSyncState.value = CloudSyncState.NEEDS_ATTENTION
        }
    }

    fun saveCurrentProject(projectName: String, saveToCloud: Boolean) {
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.SAVING
            val tracksJson = TrackSerializer.serializeTracks(_tracks.value)
            val id = "proj_${System.currentTimeMillis()}"
            val entity = SequencerProjectEntity(
                id = id,
                name = projectName.ifBlank { "Untitled Project" },
                bpm = _bpm.value,
                masterVolume = _masterVolume.value,
                swing = _swing.value,
                tracksJson = tracksJson,
                updatedAt = System.currentTimeMillis(),
                isCloudSynced = saveToCloud
            )

            _currentProjectName.value = entity.name
            val result = projectRepository.saveProject(entity, saveToCloud)
            result.fold(
                onSuccess = { cloudSynced ->
                    _cloudSyncState.value = CloudSyncState.SAVED
                    _lastCloudSyncTime.value = System.currentTimeMillis()
                    if (saveToCloud && cloudSynced) {
                        _statusMessage.value = "Project saved & synced to Firestore!"
                    } else if (saveToCloud) {
                        _statusMessage.value = "Saved locally. Cloud sync pending."
                    } else {
                        _statusMessage.value = "Project saved locally."
                    }
                },
                onFailure = { err ->
                    _cloudSyncState.value = CloudSyncState.NEEDS_ATTENTION
                    _statusMessage.value = "Failed to save project: ${err.message}"
                }
            )
        }
    }

    fun loadProject(project: SequencerProjectEntity) {
        viewModelScope.launch {
            try {
                val deserializedTracks = TrackSerializer.deserializeTracks(project.tracksJson)
                if (deserializedTracks.isNotEmpty()) {
                    _tracks.value = deserializedTracks
                    _bpm.value = project.bpm
                    _masterVolume.value = project.masterVolume
                    _swing.value = project.swing
                    _currentProjectName.value = project.name
                    _activePresetName.value = project.name
                    _cloudSyncState.value = CloudSyncState.SAVED
                    _lastCloudSyncTime.value = System.currentTimeMillis()
                    _statusMessage.value = "Arrangement '${project.name}' loaded!"
                } else {
                    _statusMessage.value = "Failed to parse project arrangement."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading project: ${e.message}"
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
            _statusMessage.value = "Project deleted."
        }
    }

    fun syncCloudProjects() {
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.SAVING
            _statusMessage.value = "Syncing with Firestore..."
            val fetched = projectRepository.syncCloudProjects()
            _cloudSyncState.value = CloudSyncState.SAVED
            _lastCloudSyncTime.value = System.currentTimeMillis()
            if (fetched.isNotEmpty()) {
                _statusMessage.value = "Synced ${fetched.size} project(s) from Firestore!"
            } else {
                _statusMessage.value = "Firestore sync complete."
            }
        }
    }

    fun backupToGoogleDrive() {
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.SAVING
            _statusMessage.value = "Creating Google Drive cloud backup..."
            val result = com.example.utils.GoogleDriveBackupManager.createDriveBackup(
                context = getApplication(),
                projectName = _currentProjectName.value,
                bpm = _bpm.value,
                masterVolume = _masterVolume.value,
                swing = _swing.value,
                tracks = _tracks.value
            )
            result.fold(
                onSuccess = { file ->
                    _lastDriveBackupTime.value = System.currentTimeMillis()
                    _cloudSyncState.value = CloudSyncState.SAVED
                    _statusMessage.value = "Exported '${_currentProjectName.value}' & samples to Google Drive (${file.name})!"
                },
                onFailure = { err ->
                    _cloudSyncState.value = CloudSyncState.NEEDS_ATTENTION
                    _statusMessage.value = "Drive backup error: ${err.message}"
                }
            )
        }
    }

    fun toggleDriveBackup(enabled: Boolean) {
        _isDriveBackupEnabled.value = enabled
        if (enabled) {
            _statusMessage.value = "Google Drive Auto-Backup activated."
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopSequencer()
        } else {
            startSequencer()
        }
    }

    fun startSequencer() {
        _isPlaying.value = true
        _isSynced.value = true
        sequencerJob?.cancel()
        sequencerJob = viewModelScope.launch {
            var step = 0
            var bar = 1
            _currentBar.value = bar
            while (_isPlaying.value) {
                _currentStep.value = step
                _currentBar.value = bar

                // Play sound for active tracks on this step
                val currentTracks = _tracks.value
                val hasAnySolo = currentTracks.any { it.isSoloed }

                currentTracks.forEach { track ->
                    val isTrackActive = if (hasAnySolo) track.isSoloed else !track.isMuted
                    if (isTrackActive && track.steps[step]) {
                        val effectiveVol = track.volume * _masterVolume.value
                        val effectivePitch = getEffectivePitch(track)
                        audioEngine.playSound(
                            type = track.instrumentType,
                            volume = effectiveVol,
                            pitch = effectivePitch,
                            pan = track.pan,
                            distortion = track.distortion,
                            delayMix = track.delayMix,
                            reverbMix = track.reverbMix,
                            filterCutoff = track.filterCutoff,
                        
                            isBassKilled = _isMasterBassKilled.value || track.isBassKilled,
                            isMidKilled = _isMasterMidKilled.value || track.isMidKilled,
                            isHighKilled = _isMasterHighKilled.value || track.isHighKilled,
                            samplePath = track.customSamplePath
                        )
                    }
                }

                // BPM calculation: 60,000ms / BPM / 4 (for 16th notes)
                val baseDelay = (60_000f / _bpm.value / 4f).toLong()
                
                // Swing timing adjustment
                val swingAdj = if (step % 2 == 1) (baseDelay * _swing.value).toLong() else -((baseDelay * _swing.value).toLong())
                val actualDelay = (baseDelay + swingAdj).coerceAtLeast(20)

                delay(actualDelay)
                step = (step + 1) % 16
                if (step == 0) {
                    bar++
                }
            }
        }
    }

    fun stopSequencer() {
        _isPlaying.value = false
        _currentStep.value = -1
        _currentBar.value = 1
        sequencerJob?.cancel()
        sequencerJob = null
    }

    fun syncTracks() {
        _isSynced.value = true
        _statusMessage.value = "Tracks synchronisiert! Phasen-Lock & Quantisierung auf Downbeat Takt 1 / Step 0 ausgeführt."
        audioEngine.playSound(InstrumentType.HIHAT, 0.7f, 2.0f, 0.0f)
        if (_isPlaying.value) {
            startSequencer()
        } else {
            _currentStep.value = 0
            _currentBar.value = 1
        }
    }

    fun getEffectivePitch(track: SequencerTrack): Float {
        return if (track.instrumentType == InstrumentType.SAMPLE && track.isTempoSynced && track.customSamplePath != null && track.sampleOriginalBpm > 0) {
            track.pitch * (_bpm.value.toFloat() / track.sampleOriginalBpm.toFloat())
        } else {
            track.pitch
        }
    }

    fun setTrackSamplePath(trackId: String, path: String) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(customSamplePath = path) else it
        }
    }

    fun setTrackSampleBpm(trackId: String, sampleBpm: Int) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(sampleOriginalBpm = sampleBpm.coerceIn(40, 240)) else track
        }
    }

    fun toggleTrackTempoSync(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isTempoSynced = !track.isTempoSynced) else track
        }
    }

    fun toggleStep(trackId: String, stepIndex: Int) {
        markUnsavedChanges()
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) {
                val newSteps = track.steps.clone()
                newSteps[stepIndex] = !newSteps[stepIndex]
                
                // Audio preview when turning step ON
                if (newSteps[stepIndex]) {
                    audioEngine.playSound(
                        type = track.instrumentType,
                        volume = track.volume * _masterVolume.value,
                        pitch = getEffectivePitch(track),
                        pan = track.pan,
                        distortion = track.distortion,
                        delayMix = track.delayMix,
                        reverbMix = track.reverbMix,
                        filterCutoff = track.filterCutoff,
                        samplePath = track.customSamplePath
                    )
                }
                
                track.copy(steps = newSteps)
            } else {
                track
            }
        }
    }

    fun setTrackVolume(trackId: String, volume: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(volume = volume) else track
        }
    }

    fun setTrackPan(trackId: String, pan: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(pan = pan) else track
        }
    }

    fun setTrackPitch(trackId: String, pitch: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(pitch = pitch) else track
        }
    }

    fun setTrackDistortion(trackId: String, distortion: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(distortion = distortion.coerceIn(0f, 1f)) else track
        }
    }

    fun setTrackDelay(trackId: String, delayMix: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(delayMix = delayMix.coerceIn(0f, 1f)) else track
        }
    }

    fun setTrackReverb(trackId: String, reverbMix: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(reverbMix = reverbMix.coerceIn(0f, 1f)) else track
        }
    }

    fun setTrackFilter(trackId: String, cutoff: Float) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(filterCutoff = cutoff.coerceIn(0.1f, 1f)) else track
        }
    }

    fun resetTrackFx(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(
                distortion = 0.0f,
                delayMix = 0.0f,
                reverbMix = 0.0f,
                filterCutoff = 1.0f,
                isBassKilled = false,
                isMidKilled = false,
                isHighKilled = false
            ) else track
        }
    }

    fun toggleMasterBassKill() {
        _isMasterBassKilled.value = !_isMasterBassKilled.value
    }

    fun toggleMasterMidKill() {
        _isMasterMidKilled.value = !_isMasterMidKilled.value
    }

    fun toggleMasterHighKill() {
        _isMasterHighKilled.value = !_isMasterHighKilled.value
    }

    fun resetMasterKills() {
        _isMasterBassKilled.value = false
        _isMasterMidKilled.value = false
        _isMasterHighKilled.value = false
    }

    fun toggleTrackBassKill(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isBassKilled = !track.isBassKilled) else track
        }
    }

    fun toggleTrackMidKill(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isMidKilled = !track.isMidKilled) else track
        }
    }

    fun toggleTrackHighKill(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isHighKilled = !track.isHighKilled) else track
        }
    }

    fun generateAiBeat(prompt: String) {
        viewModelScope.launch {
            _isGeneratingAiBeat.value = true
            _statusMessage.value = "🤖 KI verarbeitet Beat-Request: '$prompt'..."
            val result = com.example.ai.GeminiAiManager.generateBeatPattern(prompt)
            
            _bpm.value = result.bpm
            _activePresetName.value = "✨ AI: ${result.title}"

            val currentTracksList = _tracks.value.toMutableList()
            result.patterns.forEach { (instName, patternSteps) ->
                val matchingType = when (instName.uppercase()) {
                    "KICK" -> InstrumentType.KICK
                    "SNARE" -> InstrumentType.SNARE
                    "HIHAT" -> InstrumentType.HIHAT
                    "CLAP" -> InstrumentType.CLAP
                    "BASS" -> InstrumentType.BASS
                    "SYNTH" -> InstrumentType.SYNTH
                    "PERC" -> InstrumentType.PERC
                    "FX" -> InstrumentType.FX
                    else -> null
                }

                if (matchingType != null) {
                    val trackIdx = currentTracksList.indexOfFirst { it.instrumentType == matchingType }
                    val boolArray = BooleanArray(16) { i ->
                        if (i < patternSteps.size) patternSteps[i] else false
                    }
                    if (trackIdx != -1) {
                        currentTracksList[trackIdx] = currentTracksList[trackIdx].copy(
                            steps = boolArray,
                            appliedStylePreset = "✨ AI Pattern"
                        )
                    }
                }
            }

            _tracks.value = currentTracksList
            _isGeneratingAiBeat.value = false
            _statusMessage.value = "✨ KI Beat '${result.title}' geladen! ${result.advice}"
        }
    }

    fun toggleTrackMute(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isMuted = !track.isMuted) else track
        }
    }

    fun toggleTrackSolo(trackId: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(isSoloed = !track.isSoloed) else track
        }
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(40, 240)
        if (_bpm.value != clamped) {
            _bpm.value = clamped
            markUnsavedChanges()
        }
    }

    fun setMasterVolume(vol: Float) {
        _masterVolume.value = vol.coerceIn(0f, 1f)
        markUnsavedChanges()
    }

    fun setSwing(swingVal: Float) {
        _swing.value = swingVal.coerceIn(0f, 0.5f)
        markUnsavedChanges()
    }

    fun applyTrackStylePreset(trackId: String, presetStyle: String) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id != trackId) return@map track

            val newSteps = BooleanArray(16)
            var targetPitch = track.pitch
            var targetVolume = track.volume
            var targetPan = track.pan

            when (presetStyle) {
                "Lo-Fi Beats" -> {
                    targetPitch = 0.9f
                    targetVolume = 0.78f
                    targetPan = -0.1f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0, 7, 10).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(4, 12).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT -> listOf(2, 6, 10, 14).forEach { newSteps[it] = true }
                        InstrumentType.BASS -> listOf(0, 3, 7, 10, 14).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH, InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(2, 5, 8, 11, 14).forEach { newSteps[it] = true }
                    }
                }

                "Cinematic Synth" -> {
                    targetPitch = 1.25f
                    targetVolume = 0.88f
                    targetPan = 0.2f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0, 8).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(4, 12).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT -> listOf(0, 4, 8, 12).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH -> listOf(0, 2, 4, 6, 8, 10, 12, 14).forEach { newSteps[it] = true }
                        InstrumentType.BASS -> listOf(0, 4, 8, 12).forEach { newSteps[it] = true }
                        InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(3, 7, 11, 15).forEach { newSteps[it] = true }
                    }
                }

                "Jazz Piano" -> {
                    targetPitch = 1.05f
                    targetVolume = 0.82f
                    targetPan = -0.15f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0, 10).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(4, 12).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT -> listOf(2, 5, 8, 11, 14).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH, InstrumentType.BASS -> listOf(0, 3, 6, 8, 11, 14).forEach { newSteps[it] = true }
                        InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(2, 6, 9, 13).forEach { newSteps[it] = true }
                    }
                }

                "808 Trap Bounce" -> {
                    targetPitch = if (track.instrumentType == InstrumentType.BASS || track.instrumentType == InstrumentType.KICK) 0.82f else 1.15f
                    targetVolume = 0.95f
                    targetPan = 0.0f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0, 3, 8, 11).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(8).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT -> (0..15).forEach { newSteps[it] = true }
                        InstrumentType.BASS -> listOf(0, 3, 8).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH, InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(0, 6, 12).forEach { newSteps[it] = true }
                    }
                }

                "Afrobeat Funk" -> {
                    targetPitch = 1.0f
                    targetVolume = 0.85f
                    targetPan = 0.15f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0, 3, 6, 10, 12).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(4, 10, 14).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT, InstrumentType.PERC -> listOf(0, 2, 5, 7, 10, 12, 15).forEach { newSteps[it] = true }
                        InstrumentType.BASS -> listOf(0, 3, 6, 10, 12).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(2, 6, 10, 14).forEach { newSteps[it] = true }
                    }
                }

                "Ambient Space Pad" -> {
                    targetPitch = 1.35f
                    targetVolume = 0.65f
                    targetPan = -0.25f
                    when (track.instrumentType) {
                        InstrumentType.KICK -> listOf(0).forEach { newSteps[it] = true }
                        InstrumentType.SNARE, InstrumentType.CLAP -> listOf(12).forEach { newSteps[it] = true }
                        InstrumentType.HIHAT -> listOf(0, 8).forEach { newSteps[it] = true }
                        InstrumentType.SYNTH, InstrumentType.BASS -> listOf(0, 6, 14).forEach { newSteps[it] = true }
                        InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE -> listOf(4, 12).forEach { newSteps[it] = true }
                    }
                }
            }

            // Preview sound of track with applied style
            audioEngine.playSound(
                type = track.instrumentType,
                volume = targetVolume * _masterVolume.value,
                pitch = targetPitch,
                pan = targetPan,
                samplePath = track.customSamplePath
            )

            track.copy(
                steps = newSteps,
                volume = targetVolume,
                pitch = targetPitch,
                pan = targetPan,
                
                appliedStylePreset = presetStyle
            )
        }
    }

    fun addTrack(type: InstrumentType) {
        val color = when (type) {
            InstrumentType.KICK -> Color(0xFFFF3366) // Neon Red/Pink
            InstrumentType.SNARE -> Color(0xFF33CCFF) // Cyan
            InstrumentType.HIHAT -> Color(0xFFFFCC00) // Amber/Yellow
            InstrumentType.CLAP -> Color(0xFFFF9900) // Orange
            InstrumentType.BASS -> Color(0xFFCC33FF) // Purple
            InstrumentType.SYNTH -> Color(0xFF33FF99) // Mint Green
            InstrumentType.PERC -> Color(0xFFFF66CC) // Light Pink
            InstrumentType.FX -> Color(0xFF00FFCC) // Bright Teal
            InstrumentType.SAMPLE -> Color(0xFFE040FB) // Electric Purple
        }

        val newTrack = SequencerTrack(
            id = "track_${System.currentTimeMillis()}_${_tracks.value.size}",
            name = "${type.displayName} ${_tracks.value.count { it.instrumentType == type } + 1}",
            instrumentType = type,
            color = color
        )

        _tracks.value = _tracks.value + newTrack
    }

    fun removeTrack(trackId: String) {
        _tracks.value = _tracks.value.filterNot { it.id == trackId }
    }

    fun clearAllSteps() {
        _tracks.value = _tracks.value.map { track ->
            track.copy(steps = BooleanArray(16) { false })
        }
        _activePresetName.value = "Custom Pattern"
    }

    fun loadPreset(presetName: String) {
        _activePresetName.value = presetName

        val defaultTracks = listOf(
            SequencerTrack("t1", "Kick Drum", InstrumentType.KICK, Color(0xFFFF3366)),
            SequencerTrack("t2", "Snare Drum", InstrumentType.SNARE, Color(0xFF33CCFF)),
            SequencerTrack("t3", "Hi-Hat", InstrumentType.HIHAT, Color(0xFFFFCC00)),
            SequencerTrack("t4", "Hand Clap", InstrumentType.CLAP, Color(0xFFFF9900)),
            SequencerTrack("t5", "Sub Bass", InstrumentType.BASS, Color(0xFFCC33FF)),
            SequencerTrack("t6", "Synth Lead", InstrumentType.SYNTH, Color(0xFF33FF99))
        )

        when (presetName) {
            "4-on-the-Floor House" -> {
                _bpm.value = 124
                _swing.value = 0.05f
                
                // Kick
                val kickSteps = BooleanArray(16)
                listOf(0, 4, 8, 12).forEach { kickSteps[it] = true }
                
                // Snare
                val snareSteps = BooleanArray(16)
                listOf(4, 12).forEach { snareSteps[it] = true }

                // HiHat
                val hihatSteps = BooleanArray(16)
                listOf(2, 6, 10, 14).forEach { hihatSteps[it] = true }

                // Clap
                val clapSteps = BooleanArray(16)
                listOf(4, 12).forEach { clapSteps[it] = true }

                // Bass
                val bassSteps = BooleanArray(16)
                listOf(0, 3, 6, 8, 11, 14).forEach { bassSteps[it] = true }

                // Synth
                val synthSteps = BooleanArray(16)
                listOf(2, 7, 10, 15).forEach { synthSteps[it] = true }

                _tracks.value = listOf(
                    defaultTracks[0].copy(steps = kickSteps, volume = 0.9f),
                    defaultTracks[1].copy(steps = snareSteps, volume = 0.8f),
                    defaultTracks[2].copy(steps = hihatSteps, volume = 0.7f),
                    defaultTracks[3].copy(steps = clapSteps, volume = 0.75f),
                    defaultTracks[4].copy(steps = bassSteps, volume = 0.85f),
                    defaultTracks[5].copy(steps = synthSteps, volume = 0.65f)
                )
            }

            "Trap Beat" -> {
                _bpm.value = 140
                _swing.value = 0.0f

                // Kick
                val kickSteps = BooleanArray(16)
                listOf(0, 3, 8, 10).forEach { kickSteps[it] = true }

                // Snare
                val snareSteps = BooleanArray(16)
                listOf(8).forEach { snareSteps[it] = true }

                // HiHat
                val hihatSteps = BooleanArray(16) { true } // Full roll

                // Clap
                val clapSteps = BooleanArray(16)
                listOf(8).forEach { clapSteps[it] = true }

                // Bass
                val bassSteps = BooleanArray(16)
                listOf(0, 3, 10).forEach { bassSteps[it] = true }

                // Synth
                val synthSteps = BooleanArray(16)
                listOf(0, 6, 12).forEach { synthSteps[it] = true }

                _tracks.value = listOf(
                    defaultTracks[0].copy(steps = kickSteps, volume = 0.95f),
                    defaultTracks[1].copy(steps = snareSteps, volume = 0.85f),
                    defaultTracks[2].copy(steps = hihatSteps, volume = 0.65f),
                    defaultTracks[3].copy(steps = clapSteps, volume = 0.8f),
                    defaultTracks[4].copy(steps = bassSteps, volume = 0.9f),
                    defaultTracks[5].copy(steps = synthSteps, volume = 0.7f)
                )
            }

            "Funk Groove" -> {
                _bpm.value = 110
                _swing.value = 0.15f

                // Kick
                val kickSteps = BooleanArray(16)
                listOf(0, 5, 8, 10, 14).forEach { kickSteps[it] = true }

                // Snare
                val snareSteps = BooleanArray(16)
                listOf(4, 12, 15).forEach { snareSteps[it] = true }

                // HiHat
                val hihatSteps = BooleanArray(16)
                listOf(0, 2, 4, 6, 8, 10, 12, 14).forEach { hihatSteps[it] = true }

                // Clap
                val clapSteps = BooleanArray(16)
                listOf(4, 12).forEach { clapSteps[it] = true }

                // Bass
                val bassSteps = BooleanArray(16)
                listOf(0, 3, 5, 8, 11, 14).forEach { bassSteps[it] = true }

                // Synth
                val synthSteps = BooleanArray(16)
                listOf(2, 6, 10, 14).forEach { synthSteps[it] = true }

                _tracks.value = listOf(
                    defaultTracks[0].copy(steps = kickSteps, volume = 0.85f),
                    defaultTracks[1].copy(steps = snareSteps, volume = 0.8f),
                    defaultTracks[2].copy(steps = hihatSteps, volume = 0.7f),
                    defaultTracks[3].copy(steps = clapSteps, volume = 0.7f),
                    defaultTracks[4].copy(steps = bassSteps, volume = 0.85f),
                    defaultTracks[5].copy(steps = synthSteps, volume = 0.75f)
                )
            }

            "Cyberpunk Synthwave" -> {
                _bpm.value = 128
                _swing.value = 0.02f

                // Kick
                val kickSteps = BooleanArray(16)
                listOf(0, 4, 8, 12).forEach { kickSteps[it] = true }

                // Snare
                val snareSteps = BooleanArray(16)
                listOf(4, 12).forEach { snareSteps[it] = true }

                // HiHat
                val hihatSteps = BooleanArray(16) { true }

                // Clap
                val clapSteps = BooleanArray(16)
                listOf(4, 12).forEach { clapSteps[it] = true }

                // Bass
                val bassSteps = BooleanArray(16)
                listOf(0, 2, 4, 6, 8, 10, 12, 14).forEach { bassSteps[it] = true }

                // Synth
                val synthSteps = BooleanArray(16)
                listOf(3, 7, 11, 15).forEach { synthSteps[it] = true }

                _tracks.value = listOf(
                    defaultTracks[0].copy(steps = kickSteps, volume = 0.9f),
                    defaultTracks[1].copy(steps = snareSteps, volume = 0.85f),
                    defaultTracks[2].copy(steps = hihatSteps, volume = 0.6f),
                    defaultTracks[3].copy(steps = clapSteps, volume = 0.75f),
                    defaultTracks[4].copy(steps = bassSteps, volume = 0.9f),
                    defaultTracks[5].copy(steps = synthSteps, volume = 0.8f)
                )
            }
        }
    }

    override fun onCleared() {
        stopSequencer()
        super.onCleared()
    }
}
