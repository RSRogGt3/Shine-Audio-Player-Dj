package com.example.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

object AudioDecoder {
    fun decodeToFloatArray(filePath: String, targetSampleRate: Int, targetPitch: Float): FloatArray? {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            var format: MediaFormat? = null
            var trackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    format = f
                    trackIndex = i
                    break
                }
            }
            if (format == null || trackIndex == -1) return null

            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val mime = format.getString(MediaFormat.KEY_MIME)!!

            extractor.selectTrack(trackIndex)
            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            var isEOS = false
            var outputEOS = false
            val pcmList = mutableListOf<Short>()

            while (!outputEOS) {
                if (!isEOS) {
                    val inIndex = codec.dequeueInputBuffer(10000)
                    if (inIndex >= 0) {
                        val buffer = codec.getInputBuffer(inIndex)
                        val sampleSize = extractor.readSampleData(buffer!!, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(info, 10000)
                if (outIndex >= 0) {
                    val buffer = codec.getOutputBuffer(outIndex)
                    if (buffer != null && info.size > 0) {
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        while (buffer.remaining() >= 2) {
                            val low = buffer.get().toInt() and 0xFF
                            val high = buffer.get().toInt() and 0xFF
                            val s = ((high shl 8) or low).toShort()
                            pcmList.add(s)
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputEOS = true
                    }
                }
            }
            codec.stop()
            codec.release()
            extractor.release()

            // Resample
            val numFrames = pcmList.size / channels
            val ratio = (sampleRate.toFloat() / targetSampleRate.toFloat()) / targetPitch
            val outSamples = (numFrames / ratio).toInt()
            val floatArray = FloatArray(outSamples)

            for (i in 0 until outSamples) {
                val srcIdx = (i * ratio).toInt()
                if (srcIdx >= numFrames) break
                val pcmIdx = srcIdx * channels
                if (pcmIdx < pcmList.size) {
                    floatArray[i] = pcmList[pcmIdx].toFloat() / 32768f
                }
            }
            return floatArray

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
