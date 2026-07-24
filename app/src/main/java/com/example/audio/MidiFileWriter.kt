package com.example.audio

import java.io.ByteArrayOutputStream

data class MidiNote(
    val pitch: Int,       // 0-127
    val startBeats: Float, // start time in beats
    val durationBeats: Float, // duration in beats
    val velocity: Int = 100
)

object MidiFileWriter {
    fun createMidi(bpm: Int, notes: List<MidiNote>): ByteArray {
        val resolution = 480 // ticks per quarter note
        val out = ByteArrayOutputStream()

        // 1. Header Chunk
        out.write("MThd".toByteArray())
        out.write(byteArrayOf(0, 0, 0, 6)) // length
        out.write(byteArrayOf(0, 0)) // format 0
        out.write(byteArrayOf(0, 1)) // 1 track
        out.write(byteArrayOf((resolution shr 8).toByte(), (resolution and 0xFF).toByte())) // division

        // 2. Track Chunk
        val trackData = ByteArrayOutputStream()

        // Tempo event (Meta 0x51)
        val microsecondsPerBeat = 60_000_000 / bpm
        trackData.write(0x00) // delta 0
        trackData.write(0xFF) // meta
        trackData.write(0x51) // tempo
        trackData.write(0x03) // length 3
        trackData.write(byteArrayOf(
            ((microsecondsPerBeat shr 16) and 0xFF).toByte(),
            ((microsecondsPerBeat shr 8) and 0xFF).toByte(),
            (microsecondsPerBeat and 0xFF).toByte()
        ))

        // Sort notes to create a sequence of events
        val events = mutableListOf<Pair<Int, ByteArray>>() // Pair of absolute tick and event data
        for (note in notes) {
            val startTick = (note.startBeats * resolution).toInt()
            val endTick = ((note.startBeats + note.durationBeats) * resolution).toInt()

            events.add(startTick to byteArrayOf(0x90.toByte(), note.pitch.toByte(), note.velocity.toByte()))
            events.add(endTick to byteArrayOf(0x80.toByte(), note.pitch.toByte(), 0.toByte()))
        }

        // Sort by time, then Note Offs before Note Ons if at the same time
        events.sortWith(compareBy({ it.first }, { if (it.second[0] == 0x80.toByte()) 0 else 1 }))

        var currentTick = 0
        for (event in events) {
            val deltaTick = event.first - currentTick
            writeVlq(trackData, deltaTick)
            trackData.write(event.second)
            currentTick = event.first
        }

        // End of track
        trackData.write(0x00) // delta 0
        trackData.write(byteArrayOf(0xFF.toByte(), 0x2F.toByte(), 0x00.toByte()))

        val trackBytes = trackData.toByteArray()
        out.write("MTrk".toByteArray())
        out.write(byteArrayOf(
            ((trackBytes.size shr 24) and 0xFF).toByte(),
            ((trackBytes.size shr 16) and 0xFF).toByte(),
            ((trackBytes.size shr 8) and 0xFF).toByte(),
            (trackBytes.size and 0xFF).toByte()
        ))
        out.write(trackBytes)

        return out.toByteArray()
    }

    private fun writeVlq(out: ByteArrayOutputStream, value: Int) {
        var buffer = value and 0x7F
        var v = value shr 7
        while (v > 0) {
            buffer = (buffer shl 8) or 0x80 or (v and 0x7F)
            v = v shr 7
        }
        while (true) {
            out.write(buffer and 0xFF)
            if ((buffer and 0x80) != 0) {
                buffer = buffer shr 8
            } else {
                break
            }
        }
    }
}
