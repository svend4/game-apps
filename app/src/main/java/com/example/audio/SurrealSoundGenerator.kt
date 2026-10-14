package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * A real-time pure Kotlin synthesizer that creates smooth musical sine-wave tones
 * with attack/decay envelope to prevent clicking. Ideal for the non-violent,
 * soundscape-generating Surreal Circus attractions.
 */
class SurrealSoundGenerator {

    private val sampleRate = 22050 // Lower sample rate for efficiency and clean synth sound
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pentatonic scale frequencies for perfectly harmonious, non-dissonant melodies
    val pentatonicNotes = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f  // A5
    )

    fun playNoteIndex(index: Int, durationMs: Long = 350L) {
        val safeIndex = index.coerceIn(0, pentatonicNotes.lastIndex)
        val frequency = pentatonicNotes[safeIndex]
        playTone(frequency, durationMs)
    }

    fun playTone(frequency: Float, durationMs: Long) {
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0f)).toInt()
                val samples = ShortArray(numSamples)

                // Fill buffer with sine wave modulated by a smooth attack-decay envelope
                for (i in 0 until numSamples) {
                    val time = i.toFloat() / sampleRate
                    // Sine wave calculation
                    val rawSine = sin(2.0 * Math.PI * frequency * time)

                    // Linear fade-in and fade-out to prevent "pops" or Clicking:
                    val envelope: Double = when {
                        i < numSamples * 0.1 -> {
                            // Rise over 10% of duration
                            i.toDouble() / (numSamples * 0.1)
                        }
                        i > numSamples * 0.7 -> {
                            // Decay over last 30% of duration
                            (numSamples - i).toDouble() / (numSamples * 0.3)
                        }
                        else -> 1.0
                    }

                    val sampleValue = (rawSine * envelope * Short.MAX_VALUE * 0.45).toInt().toShort()
                    samples[i] = sampleValue
                }

                // Create AudioTrack
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize.coerceAtLeast(samples.size * 2),
                    AudioTrack.MODE_STATIC
                )

                // Write and Play
                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Wait for playback to complete to release resource
                kotlinx.coroutines.delay(durationMs + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
