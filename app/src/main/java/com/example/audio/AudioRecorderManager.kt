package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioRecorderManager(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    @SuppressLint("MissingPermission")
    suspend fun startRecordingWav(outputFile: File) = withContext(Dispatchers.IO) {
        var sampleRate = 192000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            // Fallback to 48000 if 192000 is not supported
            sampleRate = 48000
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                // Fallback to 44100
                sampleRate = 44100
                bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    return@withContext // Give up gracefully instead of crashing
                }
            }
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext
            }

            audioRecord?.startRecording()
            isRecording = true

            FileOutputStream(outputFile).use { os ->
                // Write placeholder WAV header
                writeWavHeader(os, 0, sampleRate, 1, 16)
                
                val data = ByteArray(bufferSize)
                var totalAudioLen = 0L

                while (isRecording) {
                    val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                    if (read > 0) {
                        os.write(data, 0, read)
                        totalAudioLen += read
                    }
                }

                // Update WAV header with correct sizes
                os.channel.position(0)
                writeWavHeader(os, totalAudioLen, sampleRate, 1, 16)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioRecord?.stop()
            } catch (e: Exception) {}
            audioRecord?.release()
            audioRecord = null
            isRecording = false
        }
    }

    suspend fun startRecordingMp3(outputFile: File) = withContext(Dispatchers.IO) {
        mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // We use MPEG_4 and AAC as Android native MediaRecorder doesn't support MP3 encoding natively
            // but AAC is widely supported and 48kHz is standard.
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(48000)
            setOutputFile(outputFile.absolutePath)
            
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                e.printStackTrace()
                release()
                mediaRecorder = null
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        if (mediaRecorder != null) {
            try {
                mediaRecorder?.stop()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        sampleRate: Int,
        channels: Int,
        bitRate: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitRate / 8

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitRate / 8).toByte() // block align
        header[33] = 0
        header[34] = bitRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}
