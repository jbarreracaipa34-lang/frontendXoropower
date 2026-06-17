package com.xoropower.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.sin

class PreviewMetronomoPlayer {
    private val sampleRate = 44100
    private var trackConteo: AudioTrack? = null
    private var trackAccent: AudioTrack? = null
    private var trackNormal: AudioTrack? = null

    init {
        trackConteo = crearTrack(660.0, 60)
        trackAccent = crearTrack(880.0, 80)
        trackNormal = crearTrack(440.0, 60)
    }

    private fun crearTrack(frequency: Double, durationMs: Int): AudioTrack {
        val numSamples = durationMs * sampleRate / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val damping = 1.0 - (i.toDouble() / numSamples)
            buffer[i] = (sin(2.0 * Math.PI * frequency * t) * 32767.0 * damping).toInt().toShort()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBufferSize, buffer.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        return track
    }

    fun playConteo() {
        playTrack(trackConteo)
    }

    fun playAccent() {
        playTrack(trackAccent)
    }

    fun playNormal() {
        playTrack(trackNormal)
    }

    private fun playTrack(track: AudioTrack?) {
        try {
            track?.stop()
            track?.reloadStaticData()
            track?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun liberar() {
        try {
            trackConteo?.stop()
            trackConteo?.release()
        } catch (e: Exception) {}
        try {
            trackAccent?.stop()
            trackAccent?.release()
        } catch (e: Exception) {}
        try {
            trackNormal?.stop()
            trackNormal?.release()
        } catch (e: Exception) {}
        trackConteo = null
        trackAccent = null
        trackNormal = null
    }
}
