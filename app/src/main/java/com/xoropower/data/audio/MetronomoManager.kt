package com.xoropower.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

class MetronomoManager {
    private var bpm = 120
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var audioTrack: AudioTrack? = null

    // Generar tono sinusoidal de click percusivo
    private val sampleRate = 44100
    private val clickBytes: ShortArray

    init {
        // Duración de click de 50 ms a 1000 Hz
        val durationMs = 50
        val freqOfTone = 1000.0
        val numSamples = durationMs * sampleRate / 1000
        clickBytes = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            // Generar onda senoidal amortiguada exponencialmente para que suene más como un tick percusivo
            val t = i.toDouble() / sampleRate
            val damping = 1.0 - (i.toDouble() / numSamples)
            clickBytes[i] = (sin(2.0 * Math.PI * freqOfTone * t) * 32767.0 * damping).toInt().toShort()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(maxOf(minBufferSize, clickBytes.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.write(clickBytes, 0, clickBytes.size)
    }

    fun iniciar(bpmInicial: Int) {
        bpm = bpmInicial
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                playClick()
                val intervalMs = (60000.0 / bpm).toLong()
                delay(intervalMs)
            }
        }
    }

    fun detener() {
        job?.cancel()
        job = null
    }

    fun cambiarBpm(nuevoBpm: Int) {
        bpm = nuevoBpm
    }

    private fun playClick() {
        try {
            audioTrack?.stop()
            audioTrack?.reloadStaticData()
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun liberar() {
        detener()
        audioTrack?.release()
        audioTrack = null
    }
}
