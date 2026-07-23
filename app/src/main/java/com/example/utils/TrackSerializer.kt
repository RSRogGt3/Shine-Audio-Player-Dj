package com.example.utils

import androidx.compose.ui.graphics.Color
import com.example.audio.InstrumentType
import com.example.ui.viewmodels.SequencerTrack
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SerializableTrackData(
    val id: String,
    val name: String,
    val instrumentType: String,
    val colorHex: String,
    val steps: List<Boolean>,
    val volume: Float,
    val pan: Float,
    val pitch: Float,
    val isMuted: Boolean,
    val isSoloed: Boolean,
    val appliedStylePreset: String? = null,
    val distortion: Float = 0.0f,
    val delayMix: Float = 0.0f,
    val reverbMix: Float = 0.0f,
    val filterCutoff: Float = 1.0f
)

object TrackSerializer {
    private val jsonInstance = Json { ignoreUnknownKeys = true }

    fun serializeTracks(tracks: List<SequencerTrack>): String {
        val serializableList = tracks.map { track ->
            SerializableTrackData(
                id = track.id,
                name = track.name,
                instrumentType = track.instrumentType.name,
                colorHex = String.format("#%08X", track.color.value.toLong()),
                steps = track.steps.toList(),
                volume = track.volume,
                pan = track.pan,
                pitch = track.pitch,
                isMuted = track.isMuted,
                isSoloed = track.isSoloed,
                appliedStylePreset = track.appliedStylePreset,
                distortion = track.distortion,
                delayMix = track.delayMix,
                reverbMix = track.reverbMix,
                filterCutoff = track.filterCutoff
            )
        }
        return jsonInstance.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(SerializableTrackData.serializer()),
            serializableList
        )
    }

    fun deserializeTracks(jsonString: String): List<SequencerTrack> {
        return try {
            val list = jsonInstance.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(SerializableTrackData.serializer()),
                jsonString
            )
            list.map { item ->
                val instType = try {
                    InstrumentType.valueOf(item.instrumentType)
                } catch (e: Exception) {
                    InstrumentType.SYNTH
                }

                val parsedColor = try {
                    val colorLong = item.colorHex.removePrefix("#").toLong(16)
                    Color(colorLong.toULong())
                } catch (e: Exception) {
                    Color(0xFF33FF99)
                }

                val stepsArray = BooleanArray(16)
                item.steps.take(16).forEachIndexed { index, b ->
                    stepsArray[index] = b
                }

                SequencerTrack(
                    id = item.id,
                    name = item.name,
                    instrumentType = instType,
                    color = parsedColor,
                    steps = stepsArray,
                    volume = item.volume,
                    pan = item.pan,
                    pitch = item.pitch,
                    isMuted = item.isMuted,
                    isSoloed = item.isSoloed,
                    appliedStylePreset = item.appliedStylePreset,
                    distortion = item.distortion,
                    delayMix = item.delayMix,
                    reverbMix = item.reverbMix,
                    filterCutoff = item.filterCutoff
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
