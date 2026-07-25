package com.example.ui.viewmodels

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.SettingsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LocalTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: String,
    val duration: Int = 0 // in ms
)

@OptIn(FlowPreview::class)
class LocalMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SettingsRepository

    private val _localTracks = MutableStateFlow<List<LocalTrack>>(emptyList())
    val localTracks: StateFlow<List<LocalTrack>> = _localTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow<LocalTrack?>(null)
    val currentTrack: StateFlow<LocalTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isAutoDjEnabled = MutableStateFlow(false)
    val isAutoDjEnabled: StateFlow<Boolean> = _isAutoDjEnabled.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isLoop = MutableStateFlow(false)
    val isLoop: StateFlow<Boolean> = _isLoop.asStateFlow()

    // DJ Loop Machine States
    private val _activeLoopBeats = MutableStateFlow<Int?>(null) // e.g. 4, 8, 12, 16
    val activeLoopBeats: StateFlow<Int?> = _activeLoopBeats.asStateFlow()

    private val _bpm = MutableStateFlow(120) // Default 120 BPM
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private var tapTimes = mutableListOf<Long>()

    private var loopStartPositionMs: Int = 0
    private var loopEndPositionMs: Int = 0

    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _ch1Level = MutableStateFlow(0.8f)
    val ch1Level: StateFlow<Float> = _ch1Level.asStateFlow()

    private val _ch2Level = MutableStateFlow(0.8f)
    val ch2Level: StateFlow<Float> = _ch2Level.asStateFlow()

    private val _masterLevel = MutableStateFlow(0.8f)
    val masterLevel: StateFlow<Float> = _masterLevel.asStateFlow()

    private val _crossfader = MutableStateFlow(0.5f)
    val crossfader: StateFlow<Float> = _crossfader.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0)
    val playbackPosition: StateFlow<Int> = _playbackPosition.asStateFlow()

    private val _playbackDuration = MutableStateFlow(0)
    val playbackDuration: StateFlow<Int> = _playbackDuration.asStateFlow()

    private val _isSampleLibraryLoaded = MutableStateFlow(false)
    val isSampleLibraryLoaded: StateFlow<Boolean> = _isSampleLibraryLoaded.asStateFlow()

    private val _aiQueue = MutableStateFlow<List<LocalTrack>>(emptyList())
    val aiQueue: StateFlow<List<LocalTrack>> = _aiQueue.asStateFlow()

    private val _isAiQueueLoading = MutableStateFlow(false)
    val isAiQueueLoading: StateFlow<Boolean> = _isAiQueueLoading.asStateFlow()

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: android.media.audiofx.Equalizer? = null
    private var visualizer: android.media.audiofx.Visualizer? = null
    private var progressJob: Job? = null
    
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()
    
    private val _beatLevel = MutableStateFlow(0f)
    val beatLevel: StateFlow<Float> = _beatLevel.asStateFlow()

    private val _visualizerBands = MutableStateFlow(FloatArray(16))
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    // Frequency Band Kill States
    private val _isBassKilled = MutableStateFlow(false)
    val isBassKilled: StateFlow<Boolean> = _isBassKilled.asStateFlow()

    private val _isMidKilled = MutableStateFlow(false)
    val isMidKilled: StateFlow<Boolean> = _isMidKilled.asStateFlow()

    private val _isHighKilled = MutableStateFlow(false)
    val isHighKilled: StateFlow<Boolean> = _isHighKilled.asStateFlow()

    // Equalizer state: band frequencies and levels
    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val equalizerBands: StateFlow<List<EqualizerBand>> = _equalizerBands.asStateFlow()

    // DJ Skin States
    private val _availableSkins = MutableStateFlow<List<DjSkin>>(
        listOf(
            DjSkin(
                id = "retro_gold",
                name = "Retro Gold Vinyl",
                primaryColorHex = 0xFFD4AF37, // Gold
                accentColorHex = 0xFFFF7043,  // Warm Coral
                backgroundColorHex = 0xFF0E0D0B, // Deep Warm Black
                surfaceColorHex = 0xFF1C1814,     // Warm Charcoal
                deskType = DeskType.VINYL_TURNTABLE,
                description = "Klassischer analoger Vinyl-Plattenspieler mit warmen Goldtönen und sanftem Drehmoment."
            ),
            DjSkin(
                id = "cyberpunk",
                name = "Neon Cyberpunk",
                primaryColorHex = 0xFFFF007F, // Neon Pink
                accentColorHex = 0xFF00F3FF,  // Neon Cyan
                backgroundColorHex = 0xFF0B0914, // Cyber Midnight
                surfaceColorHex = 0xFF181528,     // Deep Cyber Purple
                deskType = DeskType.DIGITAL_CDJ,
                description = "Modernes CDJ-Jogwheel im futuristischen Neon-Pink und Cyan-Design mit Live-BPM."
            ),
            DjSkin(
                id = "techno_green",
                name = "Techno Launchpad",
                primaryColorHex = 0xFF39FF14, // Neon Lime Green
                accentColorHex = 0xFF00E676,  // Emerald Accent
                backgroundColorHex = 0xFF050505, // Club Pure Black
                surfaceColorHex = 0xFF111E13,     // Matte Forest Black
                deskType = DeskType.MIDI_LAUNCHPAD,
                description = "Inspiriert von Club-Controllern. Ein 16-Tasten Launchpad, das im Takt blinkt."
            ),
            DjSkin(
                id = "cosmic_space",
                name = "Cosmic Studio Mixer",
                primaryColorHex = 0xFF9D4EDD, // Electric Purple
                accentColorHex = 0xFF00F3FF,  // Neon Cyan
                backgroundColorHex = 0xFF080711, // Space Midnight
                surfaceColorHex = 0xFF15122C,     // Deep Indigo Space
                deskType = DeskType.STUDIO_MIXER,
                description = "Professionelles Studio-Mischpult mit gleitenden Pegel-Fadern und VU-Metern."
            )
        )
    )
    val availableSkins: StateFlow<List<DjSkin>> = _availableSkins.asStateFlow()

    private val _currentSkin = MutableStateFlow<DjSkin>(_availableSkins.value[0])
    val currentSkin: StateFlow<DjSkin> = _currentSkin.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SettingsRepository(database.settingsDao())
        
        // Load initial settings
        viewModelScope.launch {
            repository.settings.collect { settings ->
                settings?.let {
                    _volume.value = it.volume
                    _ch1Level.value = it.ch1Level
                    _ch2Level.value = it.ch2Level
                    _masterLevel.value = it.masterLevel
                    _isShuffle.value = it.isShuffle
                    _isLoop.value = it.isLoop
                    _isAutoDjEnabled.value = it.isAutoDjEnabled
                    _crossfader.value = it.crossfader
                    _pitch.value = it.pitch
                    selectSkin(it.currentSkinId)
                    
                    if (it.eqLevels.isNotEmpty()) {
                        try {
                            val levels = it.eqLevels.split(",").map { s -> s.toShort() }
                            _equalizerBands.value = _equalizerBands.value.mapIndexed { index, band ->
                                if (index < levels.size) {
                                    band.copy(currentLevel = levels[index])
                                } else band
                            }
                            // Apply to physical equalizer if already created
                            equalizer?.let { eq ->
                                levels.forEachIndexed { index, level ->
                                    if (index < eq.numberOfBands) {
                                        eq.setBandLevel(index.toShort(), level)
                                    }
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    
                    updateMediaPlayerVolume()
                }
            }
        }

        // Auto-save settings
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                volume, ch1Level, ch2Level, masterLevel, isShuffle, isLoop, isAutoDjEnabled, currentSkin, equalizerBands, crossfader, pitch
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val bands = values[8] as List<EqualizerBand>
                AppSettings(
                    volume = values[0] as Float,
                    ch1Level = values[1] as Float,
                    ch2Level = values[2] as Float,
                    masterLevel = values[3] as Float,
                    isShuffle = values[4] as Boolean,
                    isLoop = values[5] as Boolean,
                    isAutoDjEnabled = values[6] as Boolean,
                    currentSkinId = (values[7] as DjSkin).id,
                    eqLevels = bands.joinToString(",") { it.currentLevel.toString() },
                    crossfader = values[9] as Float,
                    pitch = values[10] as Float
                )
            }
            .debounce(1000)
            .distinctUntilChanged()
            .collect {
                repository.saveSettings(it)
            }
        }
    }

    fun selectSkin(skinId: String) {
        val skin = _availableSkins.value.find { it.id == skinId }
        if (skin != null) {
            _currentSkin.value = skin
        }
    }

    fun setVolume(level: Float) {
        _volume.value = level
        updateMediaPlayerVolume()
    }

    fun setCh1Level(level: Float) {
        _ch1Level.value = level
        updateMediaPlayerVolume()
    }

    fun setCh2Level(level: Float) {
        _ch2Level.value = level
        updateMediaPlayerVolume()
    }

    fun setMasterLevel(level: Float) {
        _masterLevel.value = level
        updateMediaPlayerVolume()
    }

    fun setCrossfader(value: Float) {
        _crossfader.value = value
        updateMediaPlayerVolume()
    }

    fun setPitch(value: Float) {
        _pitch.value = value
        updateMediaPlayerPitch()
    }
    
    private fun updateMediaPlayerVolume() {
        // CH1 is the primary track fader. Master is the global multiplier.
        // Crossfader affects CH1 and CH2. 
        // For CH1: if crossfader < 0.5, it's 1.0. If > 0.5, it fades out.
        val crossfaderFactor = if (_crossfader.value <= 0.5f) 1.0f else (1.0f - _crossfader.value) * 2f
        val finalVolume = _volume.value * _masterLevel.value * _ch1Level.value * crossfaderFactor.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(finalVolume, finalVolume)
    }

    private fun updateMediaPlayerPitch() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying || _isPlaying.value) {
                        val params = player.playbackParams
                        params.speed = _pitch.value
                        player.playbackParams = params
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleShuffle(context: Context? = null) {
        _isShuffle.value = !_isShuffle.value
        if (_isShuffle.value && _aiQueue.value.isNotEmpty()) {
            _aiQueue.value = _aiQueue.value.shuffled()
        } else {
            ensureAiQueue(context)
        }
    }

    fun toggleLoop() {
        _isLoop.value = !_isLoop.value
    }

    fun toggleAutoDj(context: Context? = null) {
        _isAutoDjEnabled.value = !_isAutoDjEnabled.value
        if (_isAutoDjEnabled.value && _aiQueue.value.isEmpty()) {
            regenerateAiQueue(context)
        }
    }

    fun setLoopMachine(beats: Int) {
        if (_activeLoopBeats.value == beats) {
            // Turn off loop
            _activeLoopBeats.value = null
            loopStartPositionMs = 0
            loopEndPositionMs = 0
        } else {
            // Turn on loop
            val currentPos = mediaPlayer?.currentPosition ?: 0
            val currentBpm = _bpm.value
            val msPerBeat = 60000 / currentBpm
            val loopDurationMs = beats * msPerBeat
            loopStartPositionMs = currentPos
            loopEndPositionMs = currentPos + loopDurationMs
            _activeLoopBeats.value = beats
        }
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        if (tapTimes.isNotEmpty() && now - tapTimes.last() > 2000) {
            tapTimes.clear() // Reset if it's been more than 2 seconds since last tap
        }
        tapTimes.add(now)
        
        if (tapTimes.size > 1) {
            // Calculate BPM based on the last few taps (up to 4)
            val tapsToConsider = tapTimes.takeLast(4)
            var totalDuration = 0L
            for (i in 1 until tapsToConsider.size) {
                totalDuration += tapsToConsider[i] - tapsToConsider[i - 1]
            }
            val averageDurationMs = totalDuration / (tapsToConsider.size - 1)
            
            if (averageDurationMs > 0) {
                val calculatedBpm = (60000 / averageDurationMs).toInt()
                // Clamp BPM to reasonable values
                _bpm.value = calculatedBpm.coerceIn(40, 300)
                
                // If a loop is active, recalculate its duration based on new BPM
                val activeBeats = _activeLoopBeats.value
                if (activeBeats != null) {
                    val msPerBeat = 60000 / _bpm.value
                    loopEndPositionMs = loopStartPositionMs + (activeBeats * msPerBeat)
                }
            }
        }
    }

    /**
     * Ensures that _aiQueue always maintains exactly 5 upcoming tracks.
     */
    fun ensureAiQueue(context: Context? = null) {
        val tracks = _localTracks.value
        if (tracks.isEmpty()) return

        val currentQueue = _aiQueue.value.toMutableList()
        if (currentQueue.size >= 5) return

        val current = _currentTrack.value
        val queuedIds = currentQueue.map { it.id }.toSet()

        // Candidate tracks excluding currently playing and already queued items
        var candidates = tracks.filter { it.id != current?.id && !queuedIds.contains(it.id) }
        if (candidates.isEmpty()) {
            candidates = tracks.filter { !queuedIds.contains(it.id) }
        }
        if (candidates.isEmpty()) {
            candidates = tracks
        }

        val needed = 5 - currentQueue.size
        val pool = if (_isShuffle.value) candidates.shuffled() else candidates
        val toAdd = pool.take(needed)
        currentQueue.addAll(toAdd)

        _aiQueue.value = currentQueue.take(5)
    }

    /**
     * Uses Gemini AI to build a smart, energy-matched setlist of 5 upcoming tracks.
     */
    fun regenerateAiQueue(context: Context? = null) {
        val tracks = _localTracks.value
        if (tracks.isEmpty()) return

        val current = _currentTrack.value
        _isAiQueueLoading.value = true

        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val prompt = "Current track: '${current?.title ?: "None"}'. Available library tracks: ${tracks.map { "'${it.title}'" }}. Select a DJ setlist of EXACTLY 5 tracks in ideal transition order. ONLY reply with a JSON array of exact titles e.g. [\"Song A\", \"Song B\", \"Song C\", \"Song D\", \"Song E\"]. No extra markdown."

                val content = com.example.api.Content(parts = listOf(com.example.api.Part(text = prompt)), role = "user")
                val request = com.example.api.GenerateContentRequest(contents = listOf(content), generationConfig = null, tools = null, systemInstruction = null)
                val response = com.example.api.RetrofitClient.service.generateContent("gemini-3.1-flash-lite", apiKey, request)
                val reply = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""

                val newQueue = mutableListOf<LocalTrack>()
                val candidates = tracks.toMutableList()
                if (_isShuffle.value) candidates.shuffle()

                // Extract titles from JSON array response
                val titleMatches = Regex("\"([^\"]+)\"").findAll(reply).map { it.groupValues[1] }.toList()
                for (title in titleMatches) {
                    val found = candidates.find { it.title.contains(title, ignoreCase = true) || title.contains(it.title, ignoreCase = true) }
                    if (found != null && !newQueue.contains(found)) {
                        newQueue.add(found)
                    }
                    if (newQueue.size >= 5) break
                }

                // Fill up to 5 tracks if needed
                for (track in candidates) {
                    if (newQueue.size >= 5) break
                    if (!newQueue.contains(track) && track.id != current?.id) {
                        newQueue.add(track)
                    }
                }

                _aiQueue.value = newQueue.take(5)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback fill
                val candidates = tracks.filter { it.id != current?.id }.let { if (_isShuffle.value) it.shuffled() else it }
                _aiQueue.value = candidates.take(5)
            } finally {
                _isAiQueueLoading.value = false
            }
        }
    }

    fun removeTrackFromAiQueue(trackId: Long, context: Context? = null) {
        _aiQueue.value = _aiQueue.value.filter { it.id != trackId }
        ensureAiQueue(context)
    }

    fun pickNextTrackWithAi(context: Context) {
        val currentQueue = _aiQueue.value
        if (currentQueue.isNotEmpty()) {
            val nextTrack = currentQueue.first()
            _aiQueue.value = currentQueue.drop(1)
            playTrack(context, nextTrack)
            ensureAiQueue(context)
        } else {
            playNext(context)
        }
    }

    fun fetchLyrics() {
        val current = _currentTrack.value ?: return
        if (_lyrics.value != null || _isLoadingLyrics.value) return

        _isLoadingLyrics.value = true
        
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val prompt = "Provide ONLY the lyrics for the song '${current.title}' by '${current.artist}'. Do not add any conversational text. If you don't know it or it's a made up song, generate plausible lyrics for a song with that title. ONLY reply with the lyrics."
                val content = com.example.api.Content(parts = listOf(com.example.api.Part(text = prompt)), role = "user")
                val request = com.example.api.GenerateContentRequest(contents = listOf(content), generationConfig = null, tools = null, systemInstruction = null)
                val response = com.example.api.RetrofitClient.service.generateContent("gemini-3.1-flash-lite", apiKey, request)
                val reply = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "Lyrics not found."
                
                _lyrics.value = reply
            } catch (e: Exception) {
                _lyrics.value = "Failed to load lyrics."
            } finally {
                _isLoadingLyrics.value = false
            }
        }
    }

    fun fetchLocalMusic(context: Context) {
        if (_isSampleLibraryLoaded.value) return // Don't override sample library if user chose it
        _isLoading.value = true
        viewModelScope.launch {
            val tracks = withContext(Dispatchers.IO) {
                val list = mutableListOf<LocalTrack>()
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                
                try {
                    context.contentResolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn) ?: "Unknown Title"
                            val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                            val duration = if (durationColumn != -1) cursor.getInt(durationColumn) else 0
                            val contentUri = "${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/$id"

                            list.add(LocalTrack(id, title, artist, contentUri, duration))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                list
            }
            _localTracks.value = tracks
            _isLoading.value = false
            ensureAiQueue(context)
        }
    }

    fun loadSampleLibrary(context: Context? = null) {
        _isSampleLibraryLoaded.value = true
        _localTracks.value = listOf(
            LocalTrack(
                id = -1,
                title = "SoundHelix Synth Symphony 1",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                duration = 372000
            ),
            LocalTrack(
                id = -2,
                title = "SoundHelix Deep Bass Groove 2",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                duration = 423000
            ),
            LocalTrack(
                id = -3,
                title = "SoundHelix Chillout Ambient 3",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                duration = 302000
            ),
            LocalTrack(
                id = -4,
                title = "SoundHelix Electro Pulse 4",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                duration = 350000
            ),
            LocalTrack(
                id = -5,
                title = "SoundHelix Techno Drive 5",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                duration = 380000
            ),
            LocalTrack(
                id = -6,
                title = "SoundHelix Funk Odyssey 6",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                duration = 310000
            ),
            LocalTrack(
                id = -7,
                title = "SoundHelix Future Bounce 7",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                duration = 340000
            ),
            LocalTrack(
                id = -8,
                title = "SoundHelix Trance Elevation 8",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                duration = 390000
            )
        )
        regenerateAiQueue(context)
    }

    private val _currentLyricsLine = MutableStateFlow(0)
    val currentLyricsLine: StateFlow<Int> = _currentLyricsLine.asStateFlow()

    fun playTrack(context: Context, track: LocalTrack) {
        try {
            _lyrics.value = null
            _currentLyricsLine.value = 0
            visualizer?.release()
            visualizer = null
            equalizer?.release()
            equalizer = null
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            
            _currentTrack.value = track
            removeTrackFromAiQueue(track.id, context)
            fetchLyrics() // Automatically fetch lyrics for the new track
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(track.uri))
                
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _playbackDuration.value = mp.duration
                    setupEqualizer(mp.audioSessionId)
                    setupVisualizer(context, mp.audioSessionId)
                    updateMediaPlayerVolume()
                    updateMediaPlayerPitch()
                    startProgressTracker()
                }
                setOnCompletionListener {
                    if (_isLoop.value) {
                        it.seekTo(0)
                        it.start()
                    } else if (_isAutoDjEnabled.value) {
                        _isPlaying.value = false
                        _playbackPosition.value = 0
                        stopProgressTracker()
                        pickNextTrackWithAi(context)
                    } else {
                        _isPlaying.value = false
                        _playbackPosition.value = 0
                        stopProgressTracker()
                        playNext(context)
                    }
                }
                prepareAsync()
            }
            _playbackPosition.value = 0
            _isPlaying.value = false // Will be set to true once prepared
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEqualizer(sessionId: Int) {
        try {
            equalizer?.release()
            equalizer = android.media.audiofx.Equalizer(0, sessionId).apply {
                enabled = true
            }
            
            val previousBands = _equalizerBands.value
            val bands = mutableListOf<EqualizerBand>()
            equalizer?.let { eq ->
                try {
                    val numBands = eq.numberOfBands
                    val bandRange = eq.bandLevelRange
                    if (bandRange.size >= 2) {
                        val minLevel = bandRange[0]
                        val maxLevel = bandRange[1]
                        for (i in 0 until numBands) {
                            val bandId = i.toShort()
                            val freq = eq.getCenterFreq(bandId) / 1000 // Convert to Hz
                            
                            val previousLevel = previousBands.find { it.band == bandId }?.currentLevel
                            val level = previousLevel ?: eq.getBandLevel(bandId)
                            
                            // apply restored level
                            if (previousLevel != null) {
                                eq.setBandLevel(bandId, previousLevel)
                            }
                            
                            bands.add(EqualizerBand(bandId, freq, minLevel, maxLevel, level))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (bands.isNotEmpty()) {
                _equalizerBands.value = bands
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupVisualizer(context: Context, sessionId: Int) {
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                _audioLevel.value = 0f
                _beatLevel.value = 0f
                return
            }

            visualizer?.release()
            visualizer = android.media.audiofx.Visualizer(sessionId).apply {
                captureSize = android.media.audiofx.Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : android.media.audiofx.Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: android.media.audiofx.Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        waveform?.let {
                            var sum = 0f
                            for (i in it.indices) {
                                val value = it[i].toFloat() - 128f
                                sum += value * value
                            }
                            val rms = Math.sqrt((sum / it.size).toDouble()).toFloat()
                            val normalizedLevel = (rms / 64f).coerceIn(0f, 1f) // More sensitive
                            _audioLevel.value = normalizedLevel
                        }
                    }

                    override fun onFftDataCapture(
                        visualizer: android.media.audiofx.Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let {
                            // Bass frequencies are usually in the first few bins of FFT
                            // Bins are: [real0, imag0, real1, imag1, ...]
                            // real0 is DC, real1 is Nyquist (usually ignored for beat)
                            // Bass is around 20-200Hz.
                            var bassSum = 0f
                            val numBins = 8 // Look at the first 8 frequency components
                            for (i in 1..numBins) {
                                val r = it[i * 2].toFloat()
                                val im = it[i * 2 + 1].toFloat()
                                bassSum += Math.sqrt((r * r + im * im).toDouble()).toFloat()
                            }
                            val avgBass = bassSum / numBins
                            val normalizedBeat = (avgBass / 40f).coerceIn(0f, 1f)
                            _beatLevel.value = normalizedBeat

                            val bandsCount = 16
                            val newBands = FloatArray(bandsCount)
                            for (i in 0 until bandsCount) {
                                val idx = (i + 1) * 2
                                if (idx + 1 < it.size) {
                                    val r = it[idx].toFloat()
                                    val im = it[idx + 1].toFloat()
                                    val mag = Math.sqrt((r * r + im * im).toDouble()).toFloat()
                                    newBands[i] = (mag / 32f).coerceIn(0f, 1f)
                                }
                            }
                            _visualizerBands.value = newBands
                        }
                    }
                }, android.media.audiofx.Visualizer.getMaxCaptureRate() / 2, true, true)
                enabled = true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun toggleBassKill() {
        _isBassKilled.value = !_isBassKilled.value
        applyEqKillsToHardware()
    }

    fun toggleMidKill() {
        _isMidKilled.value = !_isMidKilled.value
        applyEqKillsToHardware()
    }

    fun toggleHighKill() {
        _isHighKilled.value = !_isHighKilled.value
        applyEqKillsToHardware()
    }

    fun resetAllKills() {
        _isBassKilled.value = false
        _isMidKilled.value = false
        _isHighKilled.value = false
        applyEqKillsToHardware()
    }

    private fun applyEqKillsToHardware() {
        val bands = _equalizerBands.value
        if (bands.isEmpty()) return
        val count = bands.size
        bands.forEachIndexed { idx, band ->
            val ratio = idx.toFloat() / (count - 1).coerceAtLeast(1)
            val isKilled = when {
                ratio < 0.35f -> _isBassKilled.value
                ratio < 0.70f -> _isMidKilled.value
                else -> _isHighKilled.value
            }
            val targetLevel = if (isKilled) band.minLevel else 0.toShort()
            try {
                equalizer?.setBandLevel(band.band, targetLevel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setEqualizerBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
            _equalizerBands.value = _equalizerBands.value.map {
                if (it.band == band) it.copy(currentLevel = level) else it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTracker()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs)
            _playbackPosition.value = positionMs
        }
    }

    fun playNext(context: Context) {
        val tracks = _localTracks.value
        if (tracks.isEmpty()) return

        val currentQueue = _aiQueue.value
        if (currentQueue.isNotEmpty()) {
            val nextTrack = currentQueue.first()
            _aiQueue.value = currentQueue.drop(1)
            playTrack(context, nextTrack)
            ensureAiQueue(context)
            return
        }

        val current = _currentTrack.value
        if (_isShuffle.value) {
            val candidates = tracks.filter { it.id != current?.id }.ifEmpty { tracks }
            playTrack(context, candidates.random())
            ensureAiQueue(context)
            return
        }

        val nextIndex = if (current != null) {
            val index = tracks.indexOfFirst { it.id == current.id }
            if (index != -1 && index < tracks.size - 1) index + 1 else 0
        } else {
            0
        }
        playTrack(context, tracks[nextIndex])
        ensureAiQueue(context)
    }

    fun playPrevious(context: Context) {
        val tracks = _localTracks.value
        val current = _currentTrack.value
        if (tracks.isEmpty()) return

        if (_isShuffle.value) {
            playTrack(context, tracks.random())
            return
        }

        val prevIndex = if (current != null) {
            val index = tracks.indexOfFirst { it.id == current.id }
            if (index > 0) index - 1 else tracks.size - 1
        } else {
            tracks.size - 1
        }
        playTrack(context, tracks[prevIndex])
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = player.currentPosition
                        val activeLoop = _activeLoopBeats.value
                        if (activeLoop != null && loopEndPositionMs > 0 && pos >= loopEndPositionMs) {
                            player.seekTo(loopStartPositionMs)
                            _playbackPosition.value = loopStartPositionMs
                        } else {
                            _playbackPosition.value = pos
                        }
                        
                        updateCurrentLyricsLine(pos)

                        // If FFT isn't returning data or visualizer is silent
                        val currentBands = _visualizerBands.value
                        if (currentBands.all { it == 0f } || _audioLevel.value == 0f) {
                            val synthAudio = (0.35f + 0.45f * kotlin.math.sin(pos / 150.0).toFloat()).coerceIn(0.15f, 0.95f)
                            val synthBeat = if ((pos / 380) % 2 == 0) 0.85f else 0.25f
                            _audioLevel.value = synthAudio
                            _beatLevel.value = synthBeat

                            val bands = FloatArray(16)
                            for (i in 0 until 16) {
                                val wave = kotlin.math.sin((pos / 120.0) + i * 0.45).toFloat() * 0.5f + 0.5f
                                bands[i] = (synthAudio * wave).coerceIn(0.1f, 1.0f)
                            }
                            _visualizerBands.value = bands
                        }
                    } else {
                        _audioLevel.value = 0f
                        _beatLevel.value = 0f
                        _visualizerBands.value = FloatArray(16)
                    }
                }
                delay(100)
            }
        }
    }

    private fun updateCurrentLyricsLine(positionMs: Int) {
        val lyricsText = _lyrics.value ?: return
        val lines = lyricsText.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return
        
        // Simple heuristic: divide total duration by number of lines
        val duration = _playbackDuration.value
        if (duration <= 0) return
        
        val msPerLine = (duration.toFloat() / lines.size).coerceAtLeast(1f)
        val currentLine = (positionMs / msPerLine).toInt().coerceIn(0, lines.size - 1)
        _currentLyricsLine.value = currentLine
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        visualizer?.release()
        visualizer = null
        equalizer?.release()
        equalizer = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopProgressTracker()
        super.onCleared()
    }
}

data class EqualizerBand(
    val band: Short,
    val centerFreq: Int,
    val minLevel: Short,
    val maxLevel: Short,
    val currentLevel: Short
)

enum class DeskType {
    VINYL_TURNTABLE,
    DIGITAL_CDJ,
    MIDI_LAUNCHPAD,
    STUDIO_MIXER
}

data class DjSkin(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val backgroundColorHex: Long,
    val surfaceColorHex: Long,
    val deskType: DeskType,
    val description: String
)
