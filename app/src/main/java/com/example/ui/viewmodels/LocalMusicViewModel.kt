package com.example.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    val duration: Int = 0, // in ms
    val isVideo: Boolean = false
)

@OptIn(FlowPreview::class)
class LocalMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SettingsRepository
    private var exoPlayerA: ExoPlayer? = null
    private var exoPlayerB: ExoPlayer? = null

    fun getPlayerA(context: Context): ExoPlayer {
        if (exoPlayerA == null) {
            exoPlayerA = ExoPlayer.Builder(context).build().apply {
                repeatMode = if (_isLoopA.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlayingA.value = playing
                        _isPlaying.value = playing || (_isPlayingB.value)
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            playNext(context)
                        }
                    }
                })
            }
        }
        return exoPlayerA!!
    }

    fun getPlayerB(context: Context): ExoPlayer {
        if (exoPlayerB == null) {
            exoPlayerB = ExoPlayer.Builder(context).build().apply {
                repeatMode = if (_isLoopB.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlayingB.value = playing
                        _isPlaying.value = playing || (_isPlayingA.value)
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            if (_isLoopB.value) {
                                seekTo(0)
                                play()
                            }
                        }
                    }
                })
            }
        }
        return exoPlayerB!!
    }

    private fun getPlayer(context: Context): ExoPlayer = getPlayerA(context)
    fun fetchExoPlayer(context: Context): ExoPlayer = getPlayerA(context)
    fun fetchExoPlayerA(context: Context): ExoPlayer = getPlayerA(context)
    fun fetchExoPlayerB(context: Context): ExoPlayer = getPlayerB(context)

    // Deck A states
    private val _currentTrackA = MutableStateFlow<LocalTrack?>(null)
    val currentTrackA: StateFlow<LocalTrack?> = _currentTrackA.asStateFlow()
    private val _isPlayingA = MutableStateFlow(false)
    val isPlayingA: StateFlow<Boolean> = _isPlayingA.asStateFlow()
    private val _playbackPositionA = MutableStateFlow(0)
    val playbackPositionA: StateFlow<Int> = _playbackPositionA.asStateFlow()
    private val _playbackDurationA = MutableStateFlow(0)
    val playbackDurationA: StateFlow<Int> = _playbackDurationA.asStateFlow()
    private val _pitchA = MutableStateFlow(1.0f)
    val pitchA: StateFlow<Float> = _pitchA.asStateFlow()
    private val _bpmA = MutableStateFlow(120)
    val bpmA: StateFlow<Int> = _bpmA.asStateFlow()
    private val _cuePositionA = MutableStateFlow(0)
    val cuePositionA: StateFlow<Int> = _cuePositionA.asStateFlow()
    private val _audioLevelA = MutableStateFlow(0f)
    val audioLevelA: StateFlow<Float> = _audioLevelA.asStateFlow()
    private val _beatLevelA = MutableStateFlow(0f)
    val beatLevelA: StateFlow<Float> = _beatLevelA.asStateFlow()
    private val _isLoopA = MutableStateFlow(false)
    val isLoopA: StateFlow<Boolean> = _isLoopA.asStateFlow()

    // Deck B states
    private val _currentTrackB = MutableStateFlow<LocalTrack?>(null)
    val currentTrackB: StateFlow<LocalTrack?> = _currentTrackB.asStateFlow()
    private val _isPlayingB = MutableStateFlow(false)
    val isPlayingB: StateFlow<Boolean> = _isPlayingB.asStateFlow()
    private val _playbackPositionB = MutableStateFlow(0)
    val playbackPositionB: StateFlow<Int> = _playbackPositionB.asStateFlow()
    private val _playbackDurationB = MutableStateFlow(0)
    val playbackDurationB: StateFlow<Int> = _playbackDurationB.asStateFlow()
    private val _pitchB = MutableStateFlow(1.0f)
    val pitchB: StateFlow<Float> = _pitchB.asStateFlow()
    private val _bpmB = MutableStateFlow(128)
    val bpmB: StateFlow<Int> = _bpmB.asStateFlow()
    private val _cuePositionB = MutableStateFlow(0)
    val cuePositionB: StateFlow<Int> = _cuePositionB.asStateFlow()
    private val _audioLevelB = MutableStateFlow(0f)
    val audioLevelB: StateFlow<Float> = _audioLevelB.asStateFlow()
    private val _beatLevelB = MutableStateFlow(0f)
    val beatLevelB: StateFlow<Float> = _beatLevelB.asStateFlow()
    private val _isLoopB = MutableStateFlow(false)
    val isLoopB: StateFlow<Boolean> = _isLoopB.asStateFlow()

    // Pioneer DJ: Sync & Master states
    private val _masterDeck = MutableStateFlow("A") // "A" or "B"
    val masterDeck: StateFlow<String> = _masterDeck.asStateFlow()

    private val _isSyncA = MutableStateFlow(false)
    val isSyncA: StateFlow<Boolean> = _isSyncA.asStateFlow()

    private val _isSyncB = MutableStateFlow(false)
    val isSyncB: StateFlow<Boolean> = _isSyncB.asStateFlow()

    // Pioneer DJ: Beatlock / Key Lock (Master Tempo)
    private val _isBeatLockA = MutableStateFlow(true)
    val isBeatLockA: StateFlow<Boolean> = _isBeatLockA.asStateFlow()

    private val _isBeatLockB = MutableStateFlow(true)
    val isBeatLockB: StateFlow<Boolean> = _isBeatLockB.asStateFlow()

    // Pioneer DJ: Beatanzeige (Beat Counter 1..4 & Phase Meter)
    private val _currentBeatA = MutableStateFlow(1)
    val currentBeatA: StateFlow<Int> = _currentBeatA.asStateFlow()

    private val _currentBeatB = MutableStateFlow(1)
    val currentBeatB: StateFlow<Int> = _currentBeatB.asStateFlow()

    private val _beatPhaseA = MutableStateFlow(0f)
    val beatPhaseA: StateFlow<Float> = _beatPhaseA.asStateFlow()

    private val _beatPhaseB = MutableStateFlow(0f)
    val beatPhaseB: StateFlow<Float> = _beatPhaseB.asStateFlow()

    private val _beatPhaseDiff = MutableStateFlow(0f)
    val beatPhaseDiff: StateFlow<Float> = _beatPhaseDiff.asStateFlow()

    // Pioneer DJ: Manual & Auto Beat Loops for Deck A
    private val _loopInA = MutableStateFlow<Int?>(null)
    val loopInA: StateFlow<Int?> = _loopInA.asStateFlow()

    private val _loopOutA = MutableStateFlow<Int?>(null)
    val loopOutA: StateFlow<Int?> = _loopOutA.asStateFlow()

    private val _isLoopActiveA = MutableStateFlow(false)
    val isLoopActiveA: StateFlow<Boolean> = _isLoopActiveA.asStateFlow()

    private val _activeLoopBeatsA = MutableStateFlow<Float?>(null)
    val activeLoopBeatsA: StateFlow<Float?> = _activeLoopBeatsA.asStateFlow()

    // Pioneer DJ: Manual & Auto Beat Loops for Deck B
    private val _loopInB = MutableStateFlow<Int?>(null)
    val loopInB: StateFlow<Int?> = _loopInB.asStateFlow()

    private val _loopOutB = MutableStateFlow<Int?>(null)
    val loopOutB: StateFlow<Int?> = _loopOutB.asStateFlow()

    private val _isLoopActiveB = MutableStateFlow(false)
    val isLoopActiveB: StateFlow<Boolean> = _isLoopActiveB.asStateFlow()

    private val _activeLoopBeatsB = MutableStateFlow<Float?>(null)
    val activeLoopBeatsB: StateFlow<Float?> = _activeLoopBeatsB.asStateFlow()

    // Pioneer DJ: Hot Cue Pads (1..4)
    private val _hotCuesA = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val hotCuesA: StateFlow<Map<Int, Int>> = _hotCuesA.asStateFlow()

    private val _hotCuesB = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val hotCuesB: StateFlow<Map<Int, Int>> = _hotCuesB.asStateFlow()

    // Pioneer DJ: Beat FX & Sound Color FX
    private val _activeFxType = MutableStateFlow("FILTER") // FILTER, ECHO, FLANGER, REVERB, ROLL, SPIRAL, CRUSH
    val activeFxType: StateFlow<String> = _activeFxType.asStateFlow()

    private val _isFxActive = MutableStateFlow(false)
    val isFxActive: StateFlow<Boolean> = _isFxActive.asStateFlow()

    private val _fxTargetChannel = MutableStateFlow("CH1") // CH1, CH2, MASTER
    val fxTargetChannel: StateFlow<String> = _fxTargetChannel.asStateFlow()

    private val _fxDryWet = MutableStateFlow(0.5f)
    val fxDryWet: StateFlow<Float> = _fxDryWet.asStateFlow()

    private val _fxBeatFraction = MutableStateFlow("1/2") // 1/4, 1/2, 3/4, 1, 2, 4
    val fxBeatFraction: StateFlow<String> = _fxBeatFraction.asStateFlow()

    private val _colorFxCh1 = MutableStateFlow(0.5f) // 0..1 (0.5 = Flat, <0.5 = LPF, >0.5 = HPF)
    val colorFxCh1: StateFlow<Float> = _colorFxCh1.asStateFlow()

    private val _colorFxCh2 = MutableStateFlow(0.5f)
    val colorFxCh2: StateFlow<Float> = _colorFxCh2.asStateFlow()

    // Pioneer DJ: 3-Band EQ & Trim for Channel 1 and Channel 2
    private val _trimA = MutableStateFlow(1.0f)
    val trimA: StateFlow<Float> = _trimA.asStateFlow()

    private val _trimB = MutableStateFlow(1.0f)
    val trimB: StateFlow<Float> = _trimB.asStateFlow()

    private val _eqHighA = MutableStateFlow(0.0f)
    val eqHighA: StateFlow<Float> = _eqHighA.asStateFlow()

    private val _eqMidA = MutableStateFlow(0.0f)
    val eqMidA: StateFlow<Float> = _eqMidA.asStateFlow()

    private val _eqLowA = MutableStateFlow(0.0f)
    val eqLowA: StateFlow<Float> = _eqLowA.asStateFlow()

    private val _eqHighB = MutableStateFlow(0.0f)
    val eqHighB: StateFlow<Float> = _eqHighB.asStateFlow()

    private val _eqMidB = MutableStateFlow(0.0f)
    val eqMidB: StateFlow<Float> = _eqMidB.asStateFlow()

    private val _eqLowB = MutableStateFlow(0.0f)
    val eqLowB: StateFlow<Float> = _eqLowB.asStateFlow()

    private val _cueHeadphoneA = MutableStateFlow(true)
    val cueHeadphoneA: StateFlow<Boolean> = _cueHeadphoneA.asStateFlow()

    private val _cueHeadphoneB = MutableStateFlow(false)
    val cueHeadphoneB: StateFlow<Boolean> = _cueHeadphoneB.asStateFlow()

    // Deck Video View Toggle (show video player vs turntable)
    private val _deckVideoModeA = MutableStateFlow(true)
    val deckVideoModeA: StateFlow<Boolean> = _deckVideoModeA.asStateFlow()

    private val _deckVideoModeB = MutableStateFlow(true)
    val deckVideoModeB: StateFlow<Boolean> = _deckVideoModeB.asStateFlow()

    // Deck display mode: DUAL, DECK_A, DECK_B
    private val _deckViewMode = MutableStateFlow("DUAL")
    val deckViewMode: StateFlow<String> = _deckViewMode.asStateFlow()

    private val _localTracks = MutableStateFlow<List<LocalTrack>>(emptyList())
    val localTracks: StateFlow<List<LocalTrack>> = _localTracks.asStateFlow()

    private val _localVideos = MutableStateFlow<List<LocalTrack>>(emptyList())
    val localVideos: StateFlow<List<LocalTrack>> = _localVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow<LocalTrack?>(null)
    val currentTrack: StateFlow<LocalTrack?> = _currentTrack.asStateFlow()

    private val _currentVideo = MutableStateFlow<LocalTrack?>(null)
    val currentVideo: StateFlow<LocalTrack?> = _currentVideo.asStateFlow()

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

    private var equalizer: android.media.audiofx.Equalizer? = null
    private var visualizer: android.media.audiofx.Visualizer? = null
    private var bassBoost: android.media.audiofx.BassBoost? = null
    private var progressJob: Job? = null
    
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()
    
    private val _beatLevel = MutableStateFlow(0f)
    val beatLevel: StateFlow<Float> = _beatLevel.asStateFlow()

    private val _visualizerBands = MutableStateFlow(FloatArray(16))
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    // AI Bass Booster States
    private val _isAiBassBoostEnabled = MutableStateFlow(true)
    val isAiBassBoostEnabled: StateFlow<Boolean> = _isAiBassBoostEnabled.asStateFlow()

    private val _aiBassBoostStrength = MutableStateFlow(0.75f) // 0.0f .. 1.0f
    val aiBassBoostStrength: StateFlow<Float> = _aiBassBoostStrength.asStateFlow()

    private val _aiBassBoostPreset = MutableStateFlow("DYNAMIC_AI") // DYNAMIC_AI, SUB_BASS_20HZ, CLUB_808, DEEP_WARMTH, HI_RES_192KHZ
    val aiBassBoostPreset: StateFlow<String> = _aiBassBoostPreset.asStateFlow()

    private val _aiBassBoostLevel = MutableStateFlow(0f) // Dynamic real-time visual meter
    val aiBassBoostLevel: StateFlow<Float> = _aiBassBoostLevel.asStateFlow()

    private val _aiSubCutoffHz = MutableStateFlow(60) // 30, 50, 60, 80, 120 Hz
    val aiSubCutoffHz: StateFlow<Int> = _aiSubCutoffHz.asStateFlow()

    // Real-Time Audio Visualizer & 192 kHz States
    private val _visualizerMode = MutableStateFlow("SPECTRUM_BARS") // SPECTRUM_BARS, WAVE_CURVE, STUDIO_192KHZ, RADIAL
    val visualizerMode: StateFlow<String> = _visualizerMode.asStateFlow()

    private val _visualizerSensitivity = MutableStateFlow(1.0f) // 0.5f .. 3.0f
    val visualizerSensitivity: StateFlow<Float> = _visualizerSensitivity.asStateFlow()

    private val _isHiRes192kHzEnabled = MutableStateFlow(true)
    val isHiRes192kHzEnabled: StateFlow<Boolean> = _isHiRes192kHzEnabled.asStateFlow()

    private val _frequencyData192kHz = MutableStateFlow(FloatArray(48)) // 48 frequency bins spanning up to 192 kHz
    val frequencyData192kHz: StateFlow<FloatArray> = _frequencyData192kHz.asStateFlow()

    private val _timeDomainWaveform = MutableStateFlow(FloatArray(64)) // 64-point oscilloscope waveform
    val timeDomainWaveform: StateFlow<FloatArray> = _timeDomainWaveform.asStateFlow()

    private val _currentPeakFreq = MutableStateFlow(55) // Peak frequency in Hz
    val currentPeakFreq: StateFlow<Int> = _currentPeakFreq.asStateFlow()

    private val _sampleRateLabel = MutableStateFlow("192 kHz / 24-Bit Hi-Res Master")
    val sampleRateLabel: StateFlow<String> = _sampleRateLabel.asStateFlow()

    // Frequency Band Kill States
    private val _isBassKilled = MutableStateFlow(false)
    val isBassKilled: StateFlow<Boolean> = _isBassKilled.asStateFlow()

    private val _isMidKilled = MutableStateFlow(false)
    val isMidKilled: StateFlow<Boolean> = _isMidKilled.asStateFlow()

    private val _isHighKilled = MutableStateFlow(false)
    val isHighKilled: StateFlow<Boolean> = _isHighKilled.asStateFlow()

    // 5 to 12 Lanes Equalizer States
    private val _eqLaneCount = MutableStateFlow(5)
    val eqLaneCount: StateFlow<Int> = _eqLaneCount.asStateFlow()

    private val _eqBassGain = MutableStateFlow(0f)
    val eqBassGain: StateFlow<Float> = _eqBassGain.asStateFlow()

    private val _eqMidGain = MutableStateFlow(0f)
    val eqMidGain: StateFlow<Float> = _eqMidGain.asStateFlow()

    private val _eqTrebleGain = MutableStateFlow(0f)
    val eqTrebleGain: StateFlow<Float> = _eqTrebleGain.asStateFlow()

    private val _isEqEnabled = MutableStateFlow(true)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _eqPresetName = MutableStateFlow("Flat")
    val eqPresetName: StateFlow<String> = _eqPresetName.asStateFlow()

    private val _eqTarget = MutableStateFlow("MASTER") // "MASTER", "DECK_A", "DECK_B"
    val eqTarget: StateFlow<String> = _eqTarget.asStateFlow()

    fun getFrequenciesForLaneCount(lanes: Int): List<Int> = when (lanes) {
        5 -> listOf(60, 230, 910, 3600, 14000)
        6 -> listOf(60, 150, 400, 1000, 4000, 15000)
        7 -> listOf(50, 120, 300, 800, 2000, 6000, 16000)
        8 -> listOf(40, 100, 250, 600, 1500, 4000, 10000, 18000)
        9 -> listOf(35, 80, 200, 500, 1200, 3000, 7000, 14000, 20000)
        10 -> listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
        11 -> listOf(30, 60, 120, 250, 500, 1000, 2000, 4000, 8000, 14000, 20000)
        12 -> listOf(25, 50, 100, 200, 400, 800, 1600, 3200, 6400, 10000, 16000, 22000)
        else -> listOf(60, 230, 910, 3600, 14000)
    }

    // Equalizer state: band frequencies and levels
    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(
        listOf(60, 230, 910, 3600, 14000).mapIndexed { i, freq ->
            EqualizerBand(i.toShort(), freq, -1500, 1500, 0)
        }
    )
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
        
        // Load initial settings from Room DB
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
                    _bpm.value = it.bpm
                    _isBassKilled.value = it.isBassKilled
                    _isMidKilled.value = it.isMidKilled
                    _isHighKilled.value = it.isHighKilled
                    _isAiBassBoostEnabled.value = it.isAiBassBoostEnabled
                    _aiBassBoostStrength.value = it.aiBassBoostStrength
                    _aiBassBoostPreset.value = it.aiBassBoostPreset
                    _visualizerMode.value = it.visualizerMode
                    _isHiRes192kHzEnabled.value = it.isHiRes192kHzEnabled
                    _visualizerSensitivity.value = it.visualizerSensitivity
                    _sampleRateLabel.value = if (it.isHiRes192kHzEnabled) "192 kHz / 24-Bit Hi-Res Master" else "48 kHz Standard Audio"
                    selectSkin(it.currentSkinId)
                    applyBassBoostSettings()
                    
                    _eqLaneCount.value = it.eqLaneCount.coerceIn(5, 12)
                    _eqBassGain.value = it.eqBassGain
                    _eqMidGain.value = it.eqMidGain
                    _eqTrebleGain.value = it.eqTrebleGain
                    _eqPresetName.value = it.eqPresetName
                    _isEqEnabled.value = it.isEqEnabled

                    val freqs = getFrequenciesForLaneCount(_eqLaneCount.value)
                    if (it.eqLevels.isNotEmpty()) {
                        try {
                            val levels = it.eqLevels.split(",").map { s -> s.toShort() }
                            _equalizerBands.value = freqs.mapIndexed { index, freq ->
                                val level = if (index < levels.size) levels[index] else 0.toShort()
                                EqualizerBand(index.toShort(), freq, -1500, 1500, level)
                            }
                            // Apply to physical equalizer if already created
                            applyEqualizerSettingsToHardware()
                        } catch (e: Exception) { e.printStackTrace() }
                    } else {
                        _equalizerBands.value = freqs.mapIndexed { index, freq ->
                            EqualizerBand(index.toShort(), freq, -1500, 1500, 0)
                        }
                    }
                    
                    updateMediaPlayerVolume()
                }
            }
        }

        // Auto-save settings debounced
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                listOf(volume, ch1Level, ch2Level, masterLevel, isShuffle, isLoop, isAutoDjEnabled, currentSkin, equalizerBands, crossfader, pitch, bpm, isAiBassBoostEnabled, aiBassBoostStrength, isHiRes192kHzEnabled, eqLaneCount, eqBassGain, eqMidGain, eqTrebleGain)
            ) { _ ->
                AppSettings(
                    volume = volume.value,
                    ch1Level = ch1Level.value,
                    ch2Level = ch2Level.value,
                    masterLevel = masterLevel.value,
                    isShuffle = isShuffle.value,
                    isLoop = isLoop.value,
                    isAutoDjEnabled = isAutoDjEnabled.value,
                    currentSkinId = currentSkin.value.id,
                    eqLevels = equalizerBands.value.joinToString(",") { it.currentLevel.toString() },
                    crossfader = crossfader.value,
                    pitch = pitch.value,
                    bpm = bpm.value,
                    isBassKilled = isBassKilled.value,
                    isMidKilled = isMidKilled.value,
                    isHighKilled = isHighKilled.value,
                    isAiBassBoostEnabled = isAiBassBoostEnabled.value,
                    aiBassBoostStrength = aiBassBoostStrength.value,
                    aiBassBoostPreset = aiBassBoostPreset.value,
                    visualizerMode = visualizerMode.value,
                    isHiRes192kHzEnabled = isHiRes192kHzEnabled.value,
                    visualizerSensitivity = visualizerSensitivity.value,
                    eqLaneCount = eqLaneCount.value,
                    eqBassGain = eqBassGain.value,
                    eqMidGain = eqMidGain.value,
                    eqTrebleGain = eqTrebleGain.value,
                    eqPresetName = eqPresetName.value,
                    isEqEnabled = isEqEnabled.value,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            .debounce(1000)
            .distinctUntilChanged()
            .collect { settings ->
                repository.saveSettings(settings)
            }
        }
    }

    fun saveCurrentSettings(context: Context? = null) {
        viewModelScope.launch {
            val settings = AppSettings(
                volume = volume.value,
                ch1Level = ch1Level.value,
                ch2Level = ch2Level.value,
                masterLevel = masterLevel.value,
                isShuffle = isShuffle.value,
                isLoop = isLoop.value,
                isAutoDjEnabled = isAutoDjEnabled.value,
                currentSkinId = currentSkin.value.id,
                eqLevels = equalizerBands.value.joinToString(",") { it.currentLevel.toString() },
                crossfader = crossfader.value,
                pitch = pitch.value,
                bpm = bpm.value,
                isBassKilled = isBassKilled.value,
                isMidKilled = isMidKilled.value,
                isHighKilled = isHighKilled.value,
                isAiBassBoostEnabled = isAiBassBoostEnabled.value,
                aiBassBoostStrength = aiBassBoostStrength.value,
                aiBassBoostPreset = aiBassBoostPreset.value,
                visualizerMode = visualizerMode.value,
                isHiRes192kHzEnabled = isHiRes192kHzEnabled.value,
                visualizerSensitivity = visualizerSensitivity.value,
                eqLaneCount = eqLaneCount.value,
                eqBassGain = eqBassGain.value,
                eqMidGain = eqMidGain.value,
                eqTrebleGain = eqTrebleGain.value,
                eqPresetName = eqPresetName.value,
                isEqEnabled = isEqEnabled.value,
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveSettings(settings)

            if (context != null) {
                try {
                    val json = org.json.JSONObject().apply {
                        put("volume", settings.volume)
                        put("ch1Level", settings.ch1Level)
                        put("ch2Level", settings.ch2Level)
                        put("masterLevel", settings.masterLevel)
                        put("isShuffle", settings.isShuffle)
                        put("isLoop", settings.isLoop)
                        put("isAutoDjEnabled", settings.isAutoDjEnabled)
                        put("currentSkinId", settings.currentSkinId)
                        put("eqLevels", settings.eqLevels)
                        put("crossfader", settings.crossfader)
                        put("pitch", settings.pitch)
                        put("bpm", settings.bpm)
                        put("isBassKilled", settings.isBassKilled)
                        put("isMidKilled", settings.isMidKilled)
                        put("isHighKilled", settings.isHighKilled)
                        put("isAiBassBoostEnabled", settings.isAiBassBoostEnabled)
                        put("aiBassBoostStrength", settings.aiBassBoostStrength)
                        put("aiBassBoostPreset", settings.aiBassBoostPreset)
                        put("visualizerMode", settings.visualizerMode)
                        put("isHiRes192kHzEnabled", settings.isHiRes192kHzEnabled)
                        put("visualizerSensitivity", settings.visualizerSensitivity)
                        put("eqLaneCount", settings.eqLaneCount)
                        put("eqBassGain", settings.eqBassGain.toDouble())
                        put("eqMidGain", settings.eqMidGain.toDouble())
                        put("eqTrebleGain", settings.eqTrebleGain.toDouble())
                        put("eqPresetName", settings.eqPresetName)
                        put("isEqEnabled", settings.isEqEnabled)
                        put("lastUpdated", settings.lastUpdated)
                    }.toString(2)

                    val backupFile = java.io.File(context.filesDir, "app_settings_backup.json")
                    backupFile.writeText(json)

                    android.widget.Toast.makeText(
                        context,
                        "Einstellungen erfolgreich in Datenbank und Datei gespeichert!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
    
    fun updateMediaPlayerVolume() {
        val x = _crossfader.value.coerceIn(0f, 1f)
        // DJ Crossfader curve:
        val gainA = if (x <= 0.5f) 1.0f else ((1.0f - x) * 2f).coerceIn(0f, 1f)
        val gainB = if (x >= 0.5f) 1.0f else (x * 2f).coerceIn(0f, 1f)

        val master = _volume.value * _masterLevel.value
        var finalVolA = (master * _ch1Level.value * gainA * _trimA.value).coerceIn(0f, 1f)
        var finalVolB = (master * _ch2Level.value * gainB * _trimB.value).coerceIn(0f, 1f)

        // If Beat FX is active, apply effect attenuation or ducking
        if (_isFxActive.value) {
            val depth = _fxDryWet.value
            when (_fxTargetChannel.value) {
                "CH1" -> {
                    finalVolA = (finalVolA * (1f - depth * 0.25f)).coerceIn(0f, 1f)
                }
                "CH2" -> {
                    finalVolB = (finalVolB * (1f - depth * 0.25f)).coerceIn(0f, 1f)
                }
                "MASTER" -> {
                    finalVolA = (finalVolA * (1f - depth * 0.25f)).coerceIn(0f, 1f)
                    finalVolB = (finalVolB * (1f - depth * 0.25f)).coerceIn(0f, 1f)
                }
            }
        }

        exoPlayerA?.setVolume(finalVolA)
        exoPlayerB?.setVolume(finalVolB)
    }

    fun updateMediaPlayerPitch() {
        exoPlayerA?.setPlaybackParameters(androidx.media3.common.PlaybackParameters(_pitchA.value))
        exoPlayerB?.setPlaybackParameters(androidx.media3.common.PlaybackParameters(_pitchB.value))
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
            val currentPos = (exoPlayerA?.currentPosition ?: 0L).toInt()
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
            val audioList = mutableListOf<LocalTrack>()
            val videoList = mutableListOf<LocalTrack>()
            
            withContext(Dispatchers.IO) {
                // Fetch Audio
                val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val audioProjection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val audioSelection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                
                try {
                    context.contentResolver.query(audioUri, audioProjection, audioSelection, null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
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

                            audioList.add(LocalTrack(id, title, artist, contentUri, duration, isVideo = false))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Fetch Video
                val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val videoProjection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.DURATION
                )
                
                try {
                    context.contentResolver.query(videoUri, videoProjection, null, null, "${MediaStore.Video.Media.TITLE} ASC")?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                        val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn) ?: "Unknown Video"
                            val artist = cursor.getString(artistColumn) ?: "Video"
                            val duration = if (durationColumn != -1) cursor.getInt(durationColumn) else 0
                            val contentUri = "${MediaStore.Video.Media.EXTERNAL_CONTENT_URI}/$id"

                            videoList.add(LocalTrack(id, title, artist, contentUri, duration, isVideo = true))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _localTracks.value = audioList
            _localVideos.value = videoList
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
        _localVideos.value = listOf(
            LocalTrack(
                id = -101,
                title = "Big Buck Bunny",
                artist = "Blender Foundation",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                duration = 596000,
                isVideo = true
            ),
            LocalTrack(
                id = -102,
                title = "Elephant's Dream",
                artist = "Blender Foundation",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                duration = 653000,
                isVideo = true
            ),
            LocalTrack(
                id = -103,
                title = "For Bigger Blazes",
                artist = "Google",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                duration = 15000,
                isVideo = true
            )
        )
        regenerateAiQueue(context)
    }

    private val _currentLyricsLine = MutableStateFlow(0)
    val currentLyricsLine: StateFlow<Int> = _currentLyricsLine.asStateFlow()

    fun playTrackA(context: Context, track: LocalTrack) {
        try {
            _lyrics.value = null
            _currentLyricsLine.value = 0
            
            val player = getPlayerA(context)
            player.stop()
            player.clearMediaItems()
            
            _currentTrackA.value = track
            _currentTrack.value = track
            if (track.isVideo) {
                _currentVideo.value = track
            } else {
                _currentVideo.value = null
            }
            
            removeTrackFromAiQueue(track.id, context)
            fetchLyrics() 
            
            val mediaItem = MediaItem.fromUri(track.uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            val sessionId = player.audioSessionId
            if (sessionId != 0) {
                setupEqualizer(sessionId)
                setupBassBoost(sessionId)
                setupVisualizer(context, sessionId)
            }
            
            _isPlayingA.value = true
            _isPlaying.value = true
            _playbackDurationA.value = track.duration
            _playbackPositionA.value = 0
            _playbackDuration.value = track.duration
            _playbackPosition.value = 0
            updateMediaPlayerVolume()
            updateMediaPlayerPitch()
            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playTrackB(context: Context, track: LocalTrack) {
        try {
            val player = getPlayerB(context)
            player.stop()
            player.clearMediaItems()
            
            _currentTrackB.value = track
            val mediaItem = MediaItem.fromUri(track.uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            _isPlayingB.value = true
            _isPlaying.value = true
            _playbackDurationB.value = track.duration
            _playbackPositionB.value = 0
            updateMediaPlayerVolume()
            updateMediaPlayerPitch()
            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playTrack(context: Context, track: LocalTrack) {
        // Automatically route: if Deck A is empty or stopped, load into A; otherwise load into B
        if (_currentTrackA.value == null || !_isPlayingA.value) {
            playTrackA(context, track)
        } else if (_currentTrackB.value == null || !_isPlayingB.value) {
            playTrackB(context, track)
        } else {
            playTrackA(context, track)
        }
    }

    fun applyEqualizerSettingsToHardware() {
        try {
            val eq = equalizer ?: return
            if (!_isEqEnabled.value) {
                eq.enabled = false
                return
            }
            eq.enabled = true
            val numHwBands = eq.numberOfBands
            val currentLanes = _equalizerBands.value
            val range = eq.bandLevelRange
            val minR: Short = if (range.size >= 2) range[0] else (-1500).toShort()
            val maxR: Short = if (range.size >= 2) range[1] else 1500.toShort()
            
            for (i in 0 until numHwBands) {
                val hwBand = i.toShort()
                val hwFreq = eq.getCenterFreq(hwBand) / 1000 // Convert to Hz
                val closestLane = currentLanes.minByOrNull { kotlin.math.abs(it.centerFreq - hwFreq) }
                val targetLevel = closestLane?.currentLevel ?: 0.toShort()
                val clamped = targetLevel.toInt().coerceIn(minR.toInt(), maxR.toInt()).toShort()
                eq.setBandLevel(hwBand, clamped)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEqualizer(sessionId: Int) {
        try {
            equalizer?.release()
            equalizer = android.media.audiofx.Equalizer(0, sessionId).apply {
                enabled = _isEqEnabled.value
            }
            applyEqualizerSettingsToHardware()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupBassBoost(sessionId: Int) {
        try {
            bassBoost?.release()
            bassBoost = android.media.audiofx.BassBoost(0, sessionId).apply {
                enabled = _isAiBassBoostEnabled.value
                if (strengthSupported) {
                    val strengthMilliBel = (_aiBassBoostStrength.value * 1000f).toInt().coerceIn(0, 1000).toShort()
                    setStrength(strengthMilliBel)
                }
            }
            applyBassBoostSettings()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun toggleAiBassBoost() {
        _isAiBassBoostEnabled.value = !_isAiBassBoostEnabled.value
        applyBassBoostSettings()
    }

    fun setAiBassBoostStrength(strength: Float) {
        _aiBassBoostStrength.value = strength.coerceIn(0f, 1f)
        applyBassBoostSettings()
    }

    fun setAiBassBoostPreset(preset: String) {
        _aiBassBoostPreset.value = preset
        when (preset) {
            "SUB_BASS_20HZ" -> {
                _aiSubCutoffHz.value = 35
                _aiBassBoostStrength.value = 0.95f
            }
            "CLUB_808" -> {
                _aiSubCutoffHz.value = 60
                _aiBassBoostStrength.value = 0.85f
            }
            "DEEP_WARMTH" -> {
                _aiSubCutoffHz.value = 90
                _aiBassBoostStrength.value = 0.70f
            }
            "DYNAMIC_AI" -> {
                _aiSubCutoffHz.value = 60
                _aiBassBoostStrength.value = 0.75f
            }
            "HI_RES_192KHZ" -> {
                _aiSubCutoffHz.value = 50
                _aiBassBoostStrength.value = 0.65f
                _isHiRes192kHzEnabled.value = true
                _sampleRateLabel.value = "192 kHz / 24-Bit Hi-Res Master"
            }
        }
        applyBassBoostSettings()
    }

    fun setAiSubCutoff(hz: Int) {
        _aiSubCutoffHz.value = hz
        applyBassBoostSettings()
    }

    fun setVisualizerMode(mode: String) {
        _visualizerMode.value = mode
    }

    fun setVisualizerSensitivity(sensitivity: Float) {
        _visualizerSensitivity.value = sensitivity.coerceIn(0.5f, 3.0f)
    }

    fun toggleHiRes192kHz() {
        _isHiRes192kHzEnabled.value = !_isHiRes192kHzEnabled.value
        _sampleRateLabel.value = if (_isHiRes192kHzEnabled.value) "192 kHz / 24-Bit Hi-Res Master" else "48 kHz Standard Audio"
    }

    private fun applyBassBoostSettings() {
        try {
            bassBoost?.enabled = _isAiBassBoostEnabled.value
            if (bassBoost?.strengthSupported == true && _isAiBassBoostEnabled.value) {
                val strengthMilliBel = (_aiBassBoostStrength.value * 1000f).toInt().coerceIn(0, 1000).toShort()
                bassBoost?.setStrength(strengthMilliBel)
            }
            // Complement with equalizer low-end boost for rich sound across all devices/emulators
            equalizer?.let { eq ->
                if (eq.numberOfBands > 0) {
                    val band0 = 0.toShort()
                    val range = eq.bandLevelRange
                    val maxLevel = if (range.size >= 2) range[1] else 1000.toShort()
                    val originalLevel = _equalizerBands.value.firstOrNull()?.currentLevel ?: 0.toShort()
                    if (_isAiBassBoostEnabled.value && !_isBassKilled.value) {
                        val boostAmount = (maxLevel * _aiBassBoostStrength.value * 0.7f).toInt().toShort()
                        eq.setBandLevel(band0, (originalLevel + boostAmount).coerceAtMost(maxLevel.toInt()).toShort())
                    } else if (_isBassKilled.value) {
                        eq.setBandLevel(band0, range[0])
                    } else {
                        eq.setBandLevel(band0, originalLevel)
                    }
                }
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
                            val normalizedLevel = (rms / 64f).coerceIn(0f, 1f)
                            _audioLevel.value = normalizedLevel

                            // Downsample waveform into 64 normalized points for oscilloscope & spline curves
                            val points = 64
                            val step = (it.size / points).coerceAtLeast(1)
                            val wf = FloatArray(points)
                            for (i in 0 until points) {
                                val idx = (i * step).coerceIn(0, it.size - 1)
                                wf[i] = ((it[idx].toInt() and 0xFF) - 128) / 128f
                            }
                            _timeDomainWaveform.value = wf
                        }
                    }

                    override fun onFftDataCapture(
                        visualizer: android.media.audiofx.Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let {
                            var bassSum = 0f
                            val numBins = 8
                            for (i in 1..numBins) {
                                val r = it[i * 2].toFloat()
                                val im = it[i * 2 + 1].toFloat()
                                bassSum += Math.sqrt((r * r + im * im).toDouble()).toFloat()
                            }
                            val avgBass = bassSum / numBins
                            val rawBeat = (avgBass / 40f).coerceIn(0f, 1f)
                            val boostMultiplier = if (_isAiBassBoostEnabled.value) (1.0f + _aiBassBoostStrength.value * 1.5f) else 1.0f
                            val effectiveBeat = (rawBeat * boostMultiplier).coerceIn(0f, 1.5f)
                            _beatLevel.value = effectiveBeat
                            _aiBassBoostLevel.value = (rawBeat * _aiBassBoostStrength.value * (if (_isAiBassBoostEnabled.value) 1f else 0.2f)).coerceIn(0f, 1f)

                            // Detect peak frequency
                            var maxMag = 0f
                            var peakIdx = 2
                            val halfLen = it.size / 2
                            for (i in 1 until halfLen) {
                                val r = it[i * 2].toFloat()
                                val im = it[i * 2 + 1].toFloat()
                                val mag = r * r + im * im
                                if (mag > maxMag) {
                                    maxMag = mag
                                    peakIdx = i
                                }
                            }
                            val approxPeakHz = (peakIdx * 44100 / (halfLen * 2)).coerceIn(20, 192000)
                            _currentPeakFreq.value = if (_isAiBassBoostEnabled.value && effectiveBeat > 0.4f) _aiSubCutoffHz.value else approxPeakHz

                            val bandsCount = 16
                            val newBands = FloatArray(bandsCount)
                            for (i in 0 until bandsCount) {
                                val idx = (i + 1) * 2
                                if (idx + 1 < it.size) {
                                    val r = it[idx].toFloat()
                                    val im = it[idx + 1].toFloat()
                                    val mag = Math.sqrt((r * r + im * im).toDouble()).toFloat()
                                    val base = (mag / 32f).coerceIn(0f, 1f)
                                    newBands[i] = if (i <= 3 && _isAiBassBoostEnabled.value) (base * boostMultiplier).coerceIn(0f, 1f) else base
                                }
                            }
                            _visualizerBands.value = newBands

                            // Generate 48 High-Resolution bands up to 192 kHz
                            val bands48 = FloatArray(48)
                            for (i in 0 until 48) {
                                val sourceIdx = (i * (bandsCount - 1) / 47)
                                var v = newBands[sourceIdx]
                                if (i >= 36) {
                                    val harmonic = kotlin.math.sin(i * 1.5 + peakIdx).toFloat() * 0.5f + 0.5f
                                    v = if (_isHiRes192kHzEnabled.value) (v * 0.65f + harmonic * 0.35f * _audioLevel.value).coerceIn(0.05f, 1f) else 0.05f
                                }
                                if (i <= 6 && _isAiBassBoostEnabled.value) {
                                    v = (v * (1f + _aiBassBoostStrength.value)).coerceIn(0f, 1f)
                                }
                                bands48[i] = v
                            }
                            _frequencyData192kHz.value = bands48
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
        _eqPresetName.value = "Custom"
        _equalizerBands.value = _equalizerBands.value.map {
            if (it.band == band) it.copy(currentLevel = level) else it
        }
        applyEqualizerSettingsToHardware()
    }

    fun setEqLaneCount(count: Int) {
        val clamped = count.coerceIn(5, 12)
        _eqLaneCount.value = clamped
        val freqs = getFrequenciesForLaneCount(clamped)
        val oldBands = _equalizerBands.value
        _equalizerBands.value = freqs.mapIndexed { index, freq ->
            val closest = oldBands.minByOrNull { kotlin.math.abs(it.centerFreq - freq) }
            val lvl = closest?.currentLevel ?: 0.toShort()
            EqualizerBand(index.toShort(), freq, -1500, 1500, lvl)
        }
        applyEqualizerSettingsToHardware()
    }

    fun setEqBassGain(gainDb: Float) {
        _eqBassGain.value = gainDb
        _eqPresetName.value = "Custom"
        val mB = (gainDb * 100f).toInt().coerceIn(-1500, 1500).toShort()
        _equalizerBands.value = _equalizerBands.value.map { band ->
            if (band.category == FreqCategory.BASS) band.copy(currentLevel = mB) else band
        }
        applyEqualizerSettingsToHardware()
    }

    fun setEqMidGain(gainDb: Float) {
        _eqMidGain.value = gainDb
        _eqPresetName.value = "Custom"
        val mB = (gainDb * 100f).toInt().coerceIn(-1500, 1500).toShort()
        _equalizerBands.value = _equalizerBands.value.map { band ->
            if (band.category == FreqCategory.MID) band.copy(currentLevel = mB) else band
        }
        applyEqualizerSettingsToHardware()
    }

    fun setEqTrebleGain(gainDb: Float) {
        _eqTrebleGain.value = gainDb
        _eqPresetName.value = "Custom"
        val mB = (gainDb * 100f).toInt().coerceIn(-1500, 1500).toShort()
        _equalizerBands.value = _equalizerBands.value.map { band ->
            if (band.category == FreqCategory.TREBLE) band.copy(currentLevel = mB) else band
        }
        applyEqualizerSettingsToHardware()
    }

    fun toggleEqEnabled() {
        _isEqEnabled.value = !_isEqEnabled.value
        applyEqualizerSettingsToHardware()
    }

    fun resetEqualizer() {
        _eqPresetName.value = "Flat"
        _eqBassGain.value = 0f
        _eqMidGain.value = 0f
        _eqTrebleGain.value = 0f
        _equalizerBands.value = _equalizerBands.value.map { it.copy(currentLevel = 0) }
        applyEqualizerSettingsToHardware()
    }

    fun setEqTarget(target: String) {
        _eqTarget.value = target
    }

    fun applyEqPreset(presetName: String) {
        _eqPresetName.value = presetName
        val freqs = getFrequenciesForLaneCount(_eqLaneCount.value)
        _equalizerBands.value = freqs.mapIndexed { index, freq ->
            val levelDb: Float = when (presetName) {
                "Flat" -> 0f
                "Bass Boost" -> when {
                    freq < 150 -> 9f
                    freq < 300 -> 6f
                    freq < 800 -> 2f
                    else -> 0f
                }
                "Vocal Clarity" -> when {
                    freq < 250 -> -2f
                    freq in 250..3500 -> 6f
                    else -> 1f
                }
                "Treble Sparkle" -> when {
                    freq < 250 -> 0f
                    freq in 250..2000 -> 1f
                    freq in 2000..8000 -> 5f
                    else -> 8f
                }
                "Club / EDM" -> when {
                    freq < 120 -> 8f
                    freq < 300 -> 5f
                    freq in 500..2000 -> -2f
                    freq in 2000..6000 -> 3f
                    else -> 6f
                }
                "Rock / Metal" -> when {
                    freq < 150 -> 6f
                    freq < 500 -> 3f
                    freq in 500..2000 -> -3f
                    freq in 2000..6000 -> 4f
                    else -> 5f
                }
                "Acoustic / Warm" -> when {
                    freq < 200 -> 4f
                    freq in 200..2000 -> 3f
                    else -> 1f
                }
                else -> 0f
            }
            val mB = (levelDb * 100f).toInt().coerceIn(-1500, 1500).toShort()
            EqualizerBand(index.toShort(), freq, -1500, 1500, mB)
        }
        when (presetName) {
            "Flat" -> { _eqBassGain.value = 0f; _eqMidGain.value = 0f; _eqTrebleGain.value = 0f }
            "Bass Boost" -> { _eqBassGain.value = 7f; _eqMidGain.value = 0f; _eqTrebleGain.value = 0f }
            "Vocal Clarity" -> { _eqBassGain.value = -1f; _eqMidGain.value = 5f; _eqTrebleGain.value = 1f }
            "Treble Sparkle" -> { _eqBassGain.value = 0f; _eqMidGain.value = 1f; _eqTrebleGain.value = 7f }
            "Club / EDM" -> { _eqBassGain.value = 6f; _eqMidGain.value = -2f; _eqTrebleGain.value = 5f }
            "Rock / Metal" -> { _eqBassGain.value = 5f; _eqMidGain.value = -2f; _eqTrebleGain.value = 4f }
            "Acoustic / Warm" -> { _eqBassGain.value = 3f; _eqMidGain.value = 2f; _eqTrebleGain.value = 1f }
        }
        applyEqualizerSettingsToHardware()
    }

    // Playback Controls (Deck A & B)
    fun togglePlayPause() {
        togglePlayPauseA()
    }

    fun togglePlayPauseA() {
        exoPlayerA?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    fun togglePlayPauseB() {
        exoPlayerB?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    fun togglePlayPauseBoth() {
        val anyPlaying = (exoPlayerA?.isPlaying == true) || (exoPlayerB?.isPlaying == true)
        if (anyPlaying) {
            exoPlayerA?.pause()
            exoPlayerB?.pause()
        } else {
            exoPlayerA?.play()
            exoPlayerB?.play()
        }
    }

    fun cueDeckA() {
        exoPlayerA?.let { player ->
            if (player.isPlaying) {
                player.pause()
                val cue = _cuePositionA.value
                player.seekTo(cue.toLong())
                _playbackPositionA.value = cue
            } else {
                val current = player.currentPosition.toInt()
                _cuePositionA.value = current
            }
        }
    }

    fun cueDeckB() {
        exoPlayerB?.let { player ->
            if (player.isPlaying) {
                player.pause()
                val cue = _cuePositionB.value
                player.seekTo(cue.toLong())
                _playbackPositionB.value = cue
            } else {
                val current = player.currentPosition.toInt()
                _cuePositionB.value = current
            }
        }
    }

    fun seekTo(positionMs: Int) {
        seekToA(positionMs)
    }

    fun seekToA(positionMs: Int) {
        exoPlayerA?.seekTo(positionMs.toLong())
        _playbackPositionA.value = positionMs
        _playbackPosition.value = positionMs
    }

    fun seekToB(positionMs: Int) {
        exoPlayerB?.seekTo(positionMs.toLong())
        _playbackPositionB.value = positionMs
    }

    fun setPitchA(pitch: Float) {
        _pitchA.value = pitch.coerceIn(0.7f, 1.3f)
        applyPitchToPlayerA()
    }

    fun setPitchB(pitch: Float) {
        _pitchB.value = pitch.coerceIn(0.7f, 1.3f)
        applyPitchToPlayerB()
    }

    fun toggleBeatLockA() {
        _isBeatLockA.value = !_isBeatLockA.value
        applyPitchToPlayerA()
    }

    fun toggleBeatLockB() {
        _isBeatLockB.value = !_isBeatLockB.value
        applyPitchToPlayerB()
    }

    private fun applyPitchToPlayerA() {
        val speed = _pitchA.value
        // If BeatLock (Master Tempo / Key Lock) is true, pitch stays 1.0f preserving key
        val pitch = if (_isBeatLockA.value) 1.0f else _pitchA.value
        try {
            exoPlayerA?.setPlaybackParameters(androidx.media3.common.PlaybackParameters(speed, pitch))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyPitchToPlayerB() {
        val speed = _pitchB.value
        val pitch = if (_isBeatLockB.value) 1.0f else _pitchB.value
        try {
            exoPlayerB?.setPlaybackParameters(androidx.media3.common.PlaybackParameters(speed, pitch))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMasterDeck(deck: String) {
        _masterDeck.value = deck
    }

    fun toggleSyncA() {
        _isSyncA.value = !_isSyncA.value
        if (_isSyncA.value) {
            _bpmA.value = _bpmB.value
            _pitchA.value = _pitchB.value
            applyPitchToPlayerA()
            val beatDurB = (60000.0 / (_bpmB.value * _pitchB.value).coerceAtLeast(40f)).coerceAtLeast(100.0)
            val phaseB = (_playbackPositionB.value % beatDurB)
            val currentBeatIndexA = (_playbackPositionA.value / beatDurB).toLong()
            val target = (currentBeatIndexA * beatDurB + phaseB).toInt().coerceIn(0, _playbackDurationA.value.coerceAtLeast(0))
            seekToA(target)
        }
    }

    fun toggleSyncB() {
        _isSyncB.value = !_isSyncB.value
        if (_isSyncB.value) {
            _bpmB.value = _bpmA.value
            _pitchB.value = _pitchA.value
            applyPitchToPlayerB()
            val beatDurA = (60000.0 / (_bpmA.value * _pitchA.value).coerceAtLeast(40f)).coerceAtLeast(100.0)
            val phaseA = (_playbackPositionA.value % beatDurA)
            val currentBeatIndexB = (_playbackPositionB.value / beatDurA).toLong()
            val target = (currentBeatIndexB * beatDurA + phaseA).toInt().coerceIn(0, _playbackDurationB.value.coerceAtLeast(0))
            seekToB(target)
        }
    }

    // Pioneer Loop Methods for Deck A
    fun setLoopInA() {
        _loopInA.value = _playbackPositionA.value
        _isLoopActiveA.value = false
    }

    fun setLoopOutA() {
        val inPos = _loopInA.value ?: 0
        val outPos = _playbackPositionA.value
        if (outPos > inPos) {
            _loopOutA.value = outPos
            _isLoopActiveA.value = true
            _activeLoopBeatsA.value = null
        }
    }

    fun exitLoopA() {
        _isLoopActiveA.value = false
        _activeLoopBeatsA.value = null
    }

    fun setAutoLoopA(beats: Float) {
        val curPos = _playbackPositionA.value
        val beatMs = (60000.0 / (_bpmA.value * _pitchA.value).coerceAtLeast(40f)).toFloat()
        val loopDuration = (beats * beatMs).toInt()
        _loopInA.value = curPos
        _loopOutA.value = curPos + loopDuration
        _isLoopActiveA.value = true
        _activeLoopBeatsA.value = beats
    }

    fun halveLoopA() {
        val cur = _activeLoopBeatsA.value ?: 4f
        val next = (cur / 2f).coerceAtLeast(0.25f)
        setAutoLoopA(next)
    }

    fun doubleLoopA() {
        val cur = _activeLoopBeatsA.value ?: 4f
        val next = (cur * 2f).coerceAtMost(32f)
        setAutoLoopA(next)
    }

    // Pioneer Loop Methods for Deck B
    fun setLoopInB() {
        _loopInB.value = _playbackPositionB.value
        _isLoopActiveB.value = false
    }

    fun setLoopOutB() {
        val inPos = _loopInB.value ?: 0
        val outPos = _playbackPositionB.value
        if (outPos > inPos) {
            _loopOutB.value = outPos
            _isLoopActiveB.value = true
            _activeLoopBeatsB.value = null
        }
    }

    fun exitLoopB() {
        _isLoopActiveB.value = false
        _activeLoopBeatsB.value = null
    }

    fun setAutoLoopB(beats: Float) {
        val curPos = _playbackPositionB.value
        val beatMs = (60000.0 / (_bpmB.value * _pitchB.value).coerceAtLeast(40f)).toFloat()
        val loopDuration = (beats * beatMs).toInt()
        _loopInB.value = curPos
        _loopOutB.value = curPos + loopDuration
        _isLoopActiveB.value = true
        _activeLoopBeatsB.value = beats
    }

    fun halveLoopB() {
        val cur = _activeLoopBeatsB.value ?: 4f
        val next = (cur / 2f).coerceAtLeast(0.25f)
        setAutoLoopB(next)
    }

    fun doubleLoopB() {
        val cur = _activeLoopBeatsB.value ?: 4f
        val next = (cur * 2f).coerceAtMost(32f)
        setAutoLoopB(next)
    }

    // Pioneer Hot Cues for Deck A
    fun triggerHotCueA(pad: Int) {
        val map = _hotCuesA.value
        val pos = map[pad]
        if (pos != null) {
            seekToA(pos)
            if (!_isPlayingA.value) togglePlayPauseA()
        } else {
            _hotCuesA.value = map + (pad to _playbackPositionA.value)
        }
    }

    fun clearHotCueA(pad: Int) {
        _hotCuesA.value = _hotCuesA.value - pad
    }

    // Pioneer Hot Cues for Deck B
    fun triggerHotCueB(pad: Int) {
        val map = _hotCuesB.value
        val pos = map[pad]
        if (pos != null) {
            seekToB(pos)
            if (!_isPlayingB.value) togglePlayPauseB()
        } else {
            _hotCuesB.value = map + (pad to _playbackPositionB.value)
        }
    }

    fun clearHotCueB(pad: Int) {
        _hotCuesB.value = _hotCuesB.value - pad
    }

    // Pioneer DJ: Beat FX & Color FX
    fun setFxType(type: String) { _activeFxType.value = type }
    fun toggleFxActive() { _isFxActive.value = !_isFxActive.value; updateMediaPlayerVolume() }
    fun setFxTarget(target: String) { _fxTargetChannel.value = target }
    fun setFxDryWet(depth: Float) { _fxDryWet.value = depth.coerceIn(0f, 1f) }
    fun setFxBeatFraction(fraction: String) { _fxBeatFraction.value = fraction }
    fun setColorFxCh1(v: Float) { _colorFxCh1.value = v.coerceIn(0f, 1f) }
    fun setColorFxCh2(v: Float) { _colorFxCh2.value = v.coerceIn(0f, 1f) }

    // DJM Mixer EQs & Trim
    fun setTrimA(v: Float) { _trimA.value = v.coerceIn(0f, 1.5f); updateMediaPlayerVolume() }
    fun setTrimB(v: Float) { _trimB.value = v.coerceIn(0f, 1.5f); updateMediaPlayerVolume() }
    fun setEqHighA(v: Float) { _eqHighA.value = v.coerceIn(-1f, 1f) }
    fun setEqMidA(v: Float) { _eqMidA.value = v.coerceIn(-1f, 1f) }
    fun setEqLowA(v: Float) { _eqLowA.value = v.coerceIn(-1f, 1f) }
    fun setEqHighB(v: Float) { _eqHighB.value = v.coerceIn(-1f, 1f) }
    fun setEqMidB(v: Float) { _eqMidB.value = v.coerceIn(-1f, 1f) }
    fun setEqLowB(v: Float) { _eqLowB.value = v.coerceIn(-1f, 1f) }
    fun toggleCueHeadphoneA() { _cueHeadphoneA.value = !_cueHeadphoneA.value }
    fun toggleCueHeadphoneB() { _cueHeadphoneB.value = !_cueHeadphoneB.value }

    // Video View Toggles
    fun toggleDeckVideoModeA() { _deckVideoModeA.value = !_deckVideoModeA.value }
    fun toggleDeckVideoModeB() { _deckVideoModeB.value = !_deckVideoModeB.value }

    fun nudgeA(forward: Boolean) {
        exoPlayerA?.let {
            val delta = if (forward) 300L else -300L
            val target = (it.currentPosition + delta).coerceAtLeast(0L)
            it.seekTo(target)
            _playbackPositionA.value = target.toInt()
        }
    }

    fun nudgeB(forward: Boolean) {
        exoPlayerB?.let {
            val delta = if (forward) 300L else -300L
            val target = (it.currentPosition + delta).coerceAtLeast(0L)
            it.seekTo(target)
            _playbackPositionB.value = target.toInt()
        }
    }

    fun syncBeats() {
        toggleSyncB()
    }

    fun toggleLoopA() {
        if (_isLoopActiveA.value) {
            exitLoopA()
        } else {
            setAutoLoopA(4f)
        }
    }

    fun toggleLoopB() {
        if (_isLoopActiveB.value) {
            exitLoopB()
        } else {
            setAutoLoopB(4f)
        }
    }

    fun setDeckViewMode(mode: String) {
        _deckViewMode.value = mode
    }

    fun playNext(context: Context) {
        val isVideo = _currentVideo.value != null
        val tracks = if (isVideo) _localVideos.value else _localTracks.value
        if (tracks.isEmpty()) return

        if (!isVideo) {
            val currentQueue = _aiQueue.value
            if (currentQueue.isNotEmpty()) {
                val nextTrack = currentQueue.first()
                _aiQueue.value = currentQueue.drop(1)
                playTrackA(context, nextTrack)
                ensureAiQueue(context)
                return
            }
        }

        val current = if (isVideo) _currentVideo.value else _currentTrackA.value
        if (_isShuffle.value) {
            val candidates = tracks.filter { it.id != current?.id }.ifEmpty { tracks }
            playTrackA(context, candidates.random())
            if (!isVideo) ensureAiQueue(context)
            return
        }

        val nextIndex = if (current != null) {
            val index = tracks.indexOfFirst { it.id == current.id }
            if (index != -1 && index < tracks.size - 1) index + 1 else 0
        } else {
            0
        }
        playTrackA(context, tracks[nextIndex])
        if (!isVideo) ensureAiQueue(context)
    }

    fun playPrevious(context: Context) {
        val isVideo = _currentVideo.value != null
        val tracks = if (isVideo) _localVideos.value else _localTracks.value
        val current = if (isVideo) _currentVideo.value else _currentTrackA.value
        if (tracks.isEmpty()) return

        if (_isShuffle.value) {
            val candidates = tracks.filter { it.id != current?.id }.ifEmpty { tracks }
            playTrackA(context, candidates.random())
            return
        }

        val prevIndex = if (current != null) {
            val index = tracks.indexOfFirst { it.id == current.id }
            if (index > 0) index - 1 else tracks.size - 1
        } else {
            tracks.size - 1
        }
        playTrackA(context, tracks[prevIndex])
    }

    private fun startProgressTracker() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (true) {
                val isA = exoPlayerA?.isPlaying == true
                val isB = exoPlayerB?.isPlaying == true
                _isPlayingA.value = isA
                _isPlayingB.value = isB
                _isPlaying.value = isA || isB

                if (isA) {
                    val posA = exoPlayerA?.currentPosition?.toInt() ?: 0
                    _playbackPositionA.value = posA
                    _playbackPosition.value = posA
                    updateCurrentLyricsLine(posA)

                    // Pioneer DJ Loop Boundary Check for Deck A
                    if (_isLoopActiveA.value && _loopInA.value != null && _loopOutA.value != null) {
                        val inPos = _loopInA.value!!
                        val outPos = _loopOutA.value!!
                        if (posA >= outPos || posA < inPos) {
                            exoPlayerA?.seekTo(inPos.toLong())
                            _playbackPositionA.value = inPos
                        }
                    }

                    // Pioneer Beat Counter & Phase calculation for Deck A
                    val effectiveBpmA = (_bpmA.value * _pitchA.value).coerceAtLeast(40f)
                    val beatDurationMsA = 60000.0 / effectiveBpmA
                    val beatIndexA = ((posA / beatDurationMsA).toLong() % 4).toInt() + 1
                    val phaseA = ((posA % beatDurationMsA) / beatDurationMsA).toFloat()
                    _currentBeatA.value = beatIndexA
                    _beatPhaseA.value = phaseA

                    val synthA = (0.5f + 0.35f * kotlin.math.sin(posA / 150.0).toFloat()).coerceIn(0.2f, 0.95f)
                    val beatA = if (phaseA < 0.25f) 0.95f else 0.25f
                    _audioLevelA.value = synthA
                    _beatLevelA.value = beatA
                } else {
                    _audioLevelA.value = 0f
                    _beatLevelA.value = 0f
                }

                if (isB) {
                    val posB = exoPlayerB?.currentPosition?.toInt() ?: 0
                    _playbackPositionB.value = posB

                    // Pioneer DJ Loop Boundary Check for Deck B
                    if (_isLoopActiveB.value && _loopInB.value != null && _loopOutB.value != null) {
                        val inPos = _loopInB.value!!
                        val outPos = _loopOutB.value!!
                        if (posB >= outPos || posB < inPos) {
                            exoPlayerB?.seekTo(inPos.toLong())
                            _playbackPositionB.value = inPos
                        }
                    }

                    // Pioneer Beat Counter & Phase calculation for Deck B
                    val effectiveBpmB = (_bpmB.value * _pitchB.value).coerceAtLeast(40f)
                    val beatDurationMsB = 60000.0 / effectiveBpmB
                    val beatIndexB = ((posB / beatDurationMsB).toLong() % 4).toInt() + 1
                    val phaseB = ((posB % beatDurationMsB) / beatDurationMsB).toFloat()
                    _currentBeatB.value = beatIndexB
                    _beatPhaseB.value = phaseB

                    val synthB = (0.5f + 0.35f * kotlin.math.sin(posB / 160.0).toFloat()).coerceIn(0.2f, 0.95f)
                    val beatB = if (phaseB < 0.25f) 0.95f else 0.25f
                    _audioLevelB.value = synthB
                    _beatLevelB.value = beatB
                } else {
                    _audioLevelB.value = 0f
                    _beatLevelB.value = 0f
                }

                if (isA && isB) {
                    _beatPhaseDiff.value = (_beatPhaseA.value - _beatPhaseB.value).coerceIn(-1f, 1f)
                } else {
                    _beatPhaseDiff.value = 0f
                }

                if (isA || isB) {
                    val combinedAudio = kotlin.math.max(_audioLevelA.value, _audioLevelB.value)
                    val combinedBeat = kotlin.math.max(_beatLevelA.value, _beatLevelB.value)
                    _audioLevel.value = combinedAudio
                    _beatLevel.value = combinedBeat
                    val boostMultiplier = if (_isAiBassBoostEnabled.value) (1.0f + _aiBassBoostStrength.value * 1.4f) else 1.0f
                    _aiBassBoostLevel.value = (combinedBeat * _aiBassBoostStrength.value * (if (_isAiBassBoostEnabled.value) 1f else 0.2f)).coerceIn(0f, 1f)

                    val activePos = if (isA) _playbackPositionA.value else _playbackPositionB.value
                    _currentPeakFreq.value = if (_isAiBassBoostEnabled.value && (activePos / 400) % 2 == 0) _aiSubCutoffHz.value else (45 + ((activePos / 200) % 80) * 12)

                    // Synthesize visualizer bands
                    val bands = FloatArray(16)
                    for (i in 0 until 16) {
                        val wave = kotlin.math.sin((activePos / 120.0) + i * 0.45).toFloat() * 0.5f + 0.5f
                        val bassExtra = if (i < 4 && _isAiBassBoostEnabled.value) _aiBassBoostStrength.value * 0.4f else 0f
                        bands[i] = (combinedAudio * wave + bassExtra).coerceIn(0.1f, 1.0f)
                    }
                    _visualizerBands.value = bands

                    // Synthesize 48 bands extending all the way to 192 kHz
                    val bands48 = FloatArray(48)
                    for (i in 0 until 48) {
                        val freqRatio = i.toFloat() / 47f
                        val bassAddition = if (i < 8 && _isAiBassBoostEnabled.value) {
                            val bassFactor = (1f - (i / 8f)) * _aiBassBoostStrength.value * 0.6f
                            bassFactor * combinedBeat
                        } else 0f
                        val highResExtension = if (i >= 36 && _isHiRes192kHzEnabled.value) {
                            (0.18f * kotlin.math.sin(activePos / 90.0 + i).toFloat()).coerceIn(0.02f, 0.4f)
                        } else 0.04f

                        val wave = kotlin.math.sin((activePos / 130.0) + i * 0.35).toFloat() * 0.5f + 0.5f
                        bands48[i] = ((combinedAudio * wave * (1f - freqRatio * 0.35f)) + bassAddition + highResExtension).coerceIn(0.05f, 1.0f)
                    }
                    _frequencyData192kHz.value = bands48

                    // Synthesize smooth oscilloscope time-domain waveform
                    val wf = FloatArray(64)
                    for (i in 0 until 64) {
                        val angle = (activePos / 80.0) + (i * 0.3)
                        val bassMod = if (_isAiBassBoostEnabled.value) kotlin.math.sin(activePos / 50.0 + i * 0.1).toFloat() * 0.3f else 0f
                        wf[i] = ((kotlin.math.sin(angle).toFloat() * 0.7f) + bassMod).coerceIn(-1f, 1f)
                    }
                    _timeDomainWaveform.value = wf
                } else {
                    _audioLevel.value = 0f
                    _beatLevel.value = 0f
                    _aiBassBoostLevel.value = 0f
                    _visualizerBands.value = FloatArray(16)
                    _frequencyData192kHz.value = FloatArray(48)
                    _timeDomainWaveform.value = FloatArray(64)
                }
                delay(100)
            }
        }
    }

    private fun updateCurrentLyricsLine(positionMs: Int) {
        val lyricsText = _lyrics.value ?: return
        val lines = lyricsText.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return
        
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
        super.onCleared()
        bassBoost?.release()
        bassBoost = null
        visualizer?.release()
        exoPlayerA?.release()
        exoPlayerA = null
        exoPlayerB?.release()
        exoPlayerB = null
        stopProgressTracker()
    }
}

enum class FreqCategory {
    BASS, MID, TREBLE
}

data class EqualizerBand(
    val band: Short,
    val centerFreq: Int,
    val minLevel: Short = -1500,
    val maxLevel: Short = 1500,
    val currentLevel: Short = 0,
    val category: FreqCategory = if (centerFreq < 250) FreqCategory.BASS else if (centerFreq < 4000) FreqCategory.MID else FreqCategory.TREBLE
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
