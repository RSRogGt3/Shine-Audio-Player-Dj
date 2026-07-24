package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class InstrumentType(val displayName: String, val icon: String, val defaultPitch: Float) {
    KICK("Kick Drum", "🥁", 1.0f),
    SNARE("Snare Drum", "🪘", 1.0f),
    HIHAT("Hi-Hat", "🎩", 1.0f),
    CLAP("Hand Clap", "👏", 1.0f),
    BASS("Sub Bass", "🎸", 1.0f),
    SYNTH("Synth Lead", "🎹", 1.0f),
    PERC("Percussion", "🔔", 1.0f),
    FX("Sound FX", "⚡", 1.0f),
    SAMPLE("Custom Sample", "🎤", 1.0f)
}

class SequencerAudioEngine {
    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-generated PCM buffers for fast low-latency playback
    private val soundBuffers = mutableMapOf<Pair<InstrumentType, Int>, ByteArray>()

    init {
        // Pre-render default sounds
        InstrumentType.values().forEach { instrument ->
            if (instrument != InstrumentType.SAMPLE) {
                // Precompute pitch 1.0x
                generateBuffer(instrument, 1.0f)
            }
        }
    }

    private fun generateFloatSamples(type: InstrumentType, pitch: Float): FloatArray {
        val durationMs = when (type) {
            InstrumentType.HIHAT -> 50
            InstrumentType.KICK -> 120
            InstrumentType.SNARE -> 140
            InstrumentType.CLAP -> 120
            InstrumentType.PERC -> 80
            InstrumentType.BASS -> 180
            InstrumentType.SYNTH -> 180
            InstrumentType.FX -> 220
            InstrumentType.SAMPLE -> 300
        }

        val numSamples = (sampleRate * durationMs / 1000)
        val samples = FloatArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples

            val sample: Float = when (type) {
                InstrumentType.KICK -> {
                    val freq = (160f * pitch * exp(-progress * 18f)).coerceAtLeast(35f)
                    val env = exp(-progress * 12f)
                    val phase = 2f * PI.toFloat() * freq * t
                    (sin(phase) * env)
                }

                InstrumentType.SNARE -> {
                    val freq = (180f * pitch * exp(-progress * 12f)).coerceAtLeast(80f)
                    val toneEnv = exp(-progress * 15f)
                    val noiseEnv = exp(-progress * 10f)
                    val tone = sin(2f * PI.toFloat() * freq * t) * toneEnv
                    val noise = (Random.nextFloat() * 2f - 1f) * noiseEnv
                    (tone * 0.4f + noise * 0.6f)
                }

                InstrumentType.HIHAT -> {
                    val env = exp(-progress * 30f)
                    val noise = (Random.nextFloat() * 2f - 1f)
                    (noise * env * 0.8f)
                }

                InstrumentType.CLAP -> {
                    val burstEnv = when {
                        progress < 0.15f -> exp(-(progress % 0.05f) * 40f) * 0.8f
                        else -> exp(-progress * 12f)
                    }
                    val noise = (Random.nextFloat() * 2f - 1f)
                    (noise * burstEnv)
                }

                InstrumentType.BASS -> {
                    val freq = 55f * pitch
                    val env = exp(-progress * 4f)
                    val sub = sin(2f * PI.toFloat() * freq * t)
                    val harmonic = sin(2f * PI.toFloat() * freq * 2f * t) * 0.3f
                    ((sub + harmonic) * env)
                }

                InstrumentType.SYNTH -> {
                    val freq = 261.63f * pitch
                    val env = exp(-progress * 8f)
                    val square = if (sin(2f * PI.toFloat() * freq * t) > 0) 0.6f else -0.6f
                    val sine = sin(2f * PI.toFloat() * freq * t) * 0.4f
                    ((square + sine) * env)
                }

                InstrumentType.PERC -> {
                    val freq = (320f * pitch * exp(-progress * 10f)).coerceAtLeast(120f)
                    val env = exp(-progress * 20f)
                    (sin(2f * PI.toFloat() * freq * t) * env)
                }

                InstrumentType.SAMPLE -> {
                    val freq = 440f * pitch
                    val env = exp(-progress * 4f)
                    (sin(2f * PI.toFloat() * freq * t) * env)
                }
                InstrumentType.FX -> {
                    val freq = 100f + progress * 800f * pitch
                    val env = exp(-progress * 2f)
                    (sin(2f * PI.toFloat() * freq * t) * env)
                }
            }

            samples[i] = sample.coerceIn(-1f, 1f)
        }

        return samples
    }

    private fun generateBuffer(type: InstrumentType, pitch: Float): ByteArray {
        val key = Pair(type, (pitch * 100).toInt())
        soundBuffers[key]?.let { return it }

        val samples = generateFloatSamples(type, pitch)
        val pcmBytes = convertFloatToPcmBytes(samples)
        soundBuffers[key] = pcmBytes
        return pcmBytes
    }

    private fun applyAudioEffects(
        samples: FloatArray,
        distortion: Float,
        delayMix: Float,
        reverbMix: Float,
        filterCutoff: Float
    ): FloatArray {
        if (distortion <= 0.01f && delayMix <= 0.01f && reverbMix <= 0.01f && filterCutoff >= 0.98f) {
            return samples
        }

        val working = samples.copyOf()

        // 1. Low-Pass Filter (Cutoff)
        if (filterCutoff < 0.98f) {
            val alpha = (filterCutoff * filterCutoff).coerceIn(0.02f, 1.0f)
            var lastVal = 0f
            for (i in working.indices) {
                lastVal += alpha * (working[i] - lastVal)
                working[i] = lastVal
            }
        }

        // 2. Overdrive / Distortion Waveshaper
        if (distortion > 0.01f) {
            val drive = 1.0f + distortion * 6.0f
            for (i in working.indices) {
                val x = (working[i] * drive).coerceIn(-2.0f, 2.0f)
                val clipped = if (x > 1.0f) 0.666f else if (x < -1.0f) -0.666f else (x - (x * x * x) / 3.0f)
                working[i] = (clipped / (1.0f + distortion * 0.4f)).coerceIn(-1.0f, 1.0f)
            }
        }

        // 3. Tail Expansion for Delay & Reverb
        val extraTail = when {
            delayMix > 0.01f -> (sampleRate * 0.35f).toInt()
            reverbMix > 0.01f -> (sampleRate * 0.25f).toInt()
            else -> 0
        }

        val output = FloatArray(working.size + extraTail)
        System.arraycopy(working, 0, output, 0, working.size)

        // 3a. Reverb Hall Ambiance (Comb Filter Taps)
        if (reverbMix > 0.01f) {
            val tap1 = (sampleRate * 0.022f).toInt() // 22ms
            val tap2 = (sampleRate * 0.045f).toInt() // 45ms
            val tap3 = (sampleRate * 0.068f).toInt() // 68ms
            val revAmount = reverbMix * 0.45f

            for (i in output.indices) {
                var revVal = 0f
                if (i >= tap1) revVal += output[i - tap1] * 0.5f
                if (i >= tap2) revVal += output[i - tap2] * 0.35f
                if (i >= tap3) revVal += output[i - tap3] * 0.25f
                output[i] = (output[i] + revVal * revAmount).coerceIn(-1.0f, 1.0f)
            }
        }

        // 3b. Delay Echo Repetitions
        if (delayMix > 0.01f) {
            val delaySamples = (sampleRate * 0.12f).toInt() // 120ms
            val echoGain = delayMix * 0.55f
            for (i in delaySamples until output.size) {
                output[i] = (output[i] + output[i - delaySamples] * echoGain).coerceIn(-1.0f, 1.0f)
            }
        }

        return output
    }

    private fun convertFloatToPcmBytes(samples: FloatArray): ByteArray {
        val pcmData = ShortArray(samples.size)
        for (i in samples.indices) {
            pcmData[i] = (samples[i].coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }

        val byteArray = ByteArray(pcmData.size * 2)
        for (i in pcmData.indices) {
            val shortVal = pcmData[i].toInt()
            byteArray[i * 2] = (shortVal and 0x00FF).toByte()
            byteArray[i * 2 + 1] = ((shortVal shr 8) and 0x00FF).toByte()
        }
        return byteArray
    }

    fun playSound(
        type: InstrumentType,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        pan: Float = 0.0f,
        distortion: Float = 0.0f,
        delayMix: Float = 0.0f,
        reverbMix: Float = 0.0f,
        filterCutoff: Float = 1.0f,
        isBassKilled: Boolean = false,
        isMidKilled: Boolean = false,
        isHighKilled: Boolean = false,
        samplePath: String? = null
    ) {
        if (volume <= 0.01f) return

        // Quick check for instrument class vs kill switches
        if (isBassKilled && (type == InstrumentType.KICK || type == InstrumentType.BASS)) return
        if (isMidKilled && (type == InstrumentType.SNARE || type == InstrumentType.CLAP || type == InstrumentType.PERC || type == InstrumentType.SYNTH)) return
        if (isHighKilled && (type == InstrumentType.HIHAT || type == InstrumentType.FX)) return

        scope.launch {
            try {
                val baseSamples = if (type == InstrumentType.SAMPLE && samplePath != null) { AudioDecoder.decodeToFloatArray(samplePath, sampleRate, pitch) ?: generateFloatSamples(type, pitch) } else { generateFloatSamples(type, pitch) }
                
                // Adjust filter cutoff or attenuation if mid/high kills active on synth/kick
                var effectiveCutoff = filterCutoff
                if (isHighKilled) {
                    effectiveCutoff = effectiveCutoff.coerceAtMost(0.35f)
                }
                
                val processedSamples = applyAudioEffects(
                    samples = baseSamples,
                    distortion = distortion,
                    delayMix = delayMix,
                    reverbMix = reverbMix,
                    filterCutoff = effectiveCutoff
                )
                val pcmBytes = convertFloatToPcmBytes(processedSamples)

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBytes.size.coerceAtLeast(minBufferSize))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcmBytes, 0, pcmBytes.size)

                // Pan calculation for left/right balance
                val leftVol = (volume * (1.0f - pan.coerceIn(0f, 1f))).coerceIn(0f, 1f)
                val rightVol = (volume * (1.0f + pan.coerceIn(-1f, 0f))).coerceIn(0f, 1f)
                @Suppress("DEPRECATION")
                audioTrack.setStereoVolume(leftVol, rightVol)

                audioTrack.play()

                // Release audio track after playback ends
                val durationMs = (pcmBytes.size / 2) * 1000 / sampleRate
                Thread.sleep(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
