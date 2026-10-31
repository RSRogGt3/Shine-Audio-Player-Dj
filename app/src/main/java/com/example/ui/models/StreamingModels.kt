package com.example.ui.models

import com.example.R
import com.example.ui.viewmodels.LocalTrack

enum class StreamingProvider(
    val displayName: String,
    val colorHex: Long,
    val badge: String,
    val iconRes: Int,
    val packageName: String,
    val defaultUrl: String
) {
    SPOTIFY(
        displayName = "Spotify",
        colorHex = 0xFF1DB954,
        badge = "SPOTIFY",
        iconRes = R.drawable.ic_spotify,
        packageName = "com.spotify.music",
        defaultUrl = "https://open.spotify.com"
    ),
    YOUTUBE_MUSIC(
        displayName = "YouTube Music",
        colorHex = 0xFFFF0000,
        badge = "YT MUSIC",
        iconRes = R.drawable.ic_youtube_music,
        packageName = "com.google.android.apps.youtube.music",
        defaultUrl = "https://music.youtube.com"
    ),
    AMAZON_MUSIC(
        displayName = "Amazon Music",
        colorHex = 0xFF00A8E1,
        badge = "AMAZON",
        iconRes = R.drawable.ic_amazon_music,
        packageName = "com.amazon.mp3",
        defaultUrl = "https://music.amazon.com"
    )
}

data class StreamingTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val provider: StreamingProvider,
    val streamUri: String,
    val bpm: Int,
    val durationMs: Int,
    val externalUrl: String,
    val genre: String = "DJ Club Mix"
) {
    fun toLocalTrack(idNumeric: Long): LocalTrack {
        return LocalTrack(
            id = idNumeric,
            title = title,
            artist = "$artist [${provider.displayName}]",
            uri = streamUri,
            duration = durationMs,
            isVideo = false
        )
    }
}

object DefaultStreamingCatalog {
    val tracks = listOf(
        // SPOTIFY CURATED STREAMS
        StreamingTrack(
            id = "spot_1",
            title = "Midnight Club Synth (Club Extended)",
            artist = "SoundHelix Spotify DJ Session",
            album = "Spotify Electronic Sessions",
            provider = StreamingProvider.SPOTIFY,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            bpm = 126,
            durationMs = 372000,
            externalUrl = "https://open.spotify.com/search/electronic%20dance",
            genre = "House / Club"
        ),
        StreamingTrack(
            id = "spot_2",
            title = "Deep Tech Groove Vol. 2",
            artist = "Nordic Beat Collective",
            album = "Spotify Dance Grooves",
            provider = StreamingProvider.SPOTIFY,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            bpm = 128,
            durationMs = 423000,
            externalUrl = "https://open.spotify.com/search/tech%20house",
            genre = "Tech House"
        ),
        StreamingTrack(
            id = "spot_3",
            title = "Acid Bass Overdrive",
            artist = "Cyber Berlin",
            album = "Spotify Bassline Lab",
            provider = StreamingProvider.SPOTIFY,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            bpm = 132,
            durationMs = 312000,
            externalUrl = "https://open.spotify.com/search/acid%20techno",
            genre = "Techno"
        ),

        // YOUTUBE MUSIC CURATED STREAMS
        StreamingTrack(
            id = "ytm_1",
            title = "Neon Skyline Live Festival Mix",
            artist = "YouTube Music Dance Lab",
            album = "YouTube Music DJ Originals",
            provider = StreamingProvider.YOUTUBE_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            bpm = 124,
            durationMs = 345000,
            externalUrl = "https://music.youtube.com/search?q=festival+dance+mix",
            genre = "EDM"
        ),
        StreamingTrack(
            id = "ytm_2",
            title = "Ibiza Sunset Melodic Drive",
            artist = "Balearic Waves",
            album = "YouTube Music Summer Mix",
            provider = StreamingProvider.YOUTUBE_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            bpm = 122,
            durationMs = 398000,
            externalUrl = "https://music.youtube.com/search?q=ibiza+sunset+mix",
            genre = "Melodic House"
        ),
        StreamingTrack(
            id = "ytm_3",
            title = "High Energy Peak Time 130",
            artist = "Mainstage Project",
            album = "YouTube Music Festival Anthems",
            provider = StreamingProvider.YOUTUBE_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            bpm = 130,
            durationMs = 330000,
            externalUrl = "https://music.youtube.com/search?q=mainstage+anthem",
            genre = "Mainstage"
        ),

        // AMAZON MUSIC CURATED STREAMS
        StreamingTrack(
            id = "amz_1",
            title = "Amazon Prime Bass Anthem",
            artist = "Prime Sound Lab",
            album = "Amazon Music HD Electronic",
            provider = StreamingProvider.AMAZON_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            bpm = 125,
            durationMs = 380000,
            externalUrl = "https://music.amazon.com/search/electronic",
            genre = "Future Bass"
        ),
        StreamingTrack(
            id = "amz_2",
            title = "Sub-Bass Chillout Lounge",
            artist = "Amazon Ambient Echoes",
            album = "Amazon Music Deep Relax",
            provider = StreamingProvider.AMAZON_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            bpm = 118,
            durationMs = 410000,
            externalUrl = "https://music.amazon.com/search/lounge+grooves",
            genre = "Chillout / Lounge"
        ),
        StreamingTrack(
            id = "amz_3",
            title = "Warehouse Underground 135",
            artist = "Sub Heavy Collective",
            album = "Amazon Music Underground",
            provider = StreamingProvider.AMAZON_MUSIC,
            streamUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            bpm = 135,
            durationMs = 360000,
            externalUrl = "https://music.amazon.com/search/underground+club",
            genre = "Underground"
        )
    )
}
