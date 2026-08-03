package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundType(val displayName: String) {
    WOODBLOCK("Bloque de Madera"),
    DIGITAL_CLICK("Click Digital"),
    COWBELL("Cencerro"),
    DRUM_KIT("Batería Acústica"),
    BONGO("Bongo / Conga"),
    MARIMBA("Marimba"),
    SYNTH_CLICK("Click Sintetizado")
}

object AudioSynthesisEngine {
    const val SAMPLE_RATE = 44100

    /**
     * Generates a 16-bit PCM ShortArray for a specific percussion click type and role (Accent, Normal, Subdivision).
     */
    fun generateClickBuffer(
        soundType: SoundType,
        isAccent: Boolean,
        isSubdivision: Boolean,
        customAccentPitchHz: Float = 0f,
        customNormalPitchHz: Float = 0f,
        customDecayMs: Int = 0
    ): ShortArray {
        val pitchMultiplier = if (isAccent) 1.5f else if (isSubdivision) 0.8f else 1.0f
        
        val durationMs = when {
            customDecayMs > 0 -> customDecayMs
            soundType == SoundType.COWBELL -> 120
            soundType == SoundType.DRUM_KIT -> 100
            else -> 60
        }
        
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
        val buffer = ShortArray(numSamples)

        val baseFreq = when {
            isAccent && customAccentPitchHz > 0 -> customAccentPitchHz
            !isAccent && customNormalPitchHz > 0 -> customNormalPitchHz
            else -> when (soundType) {
                SoundType.WOODBLOCK -> if (isAccent) 1200f else if (isSubdivision) 750f else 900f
                SoundType.DIGITAL_CLICK -> if (isAccent) 2400f else if (isSubdivision) 1400f else 1800f
                SoundType.COWBELL -> if (isAccent) 800f else if (isSubdivision) 500f else 620f
                SoundType.DRUM_KIT -> if (isAccent) 150f else if (isSubdivision) 2000f else 220f
                SoundType.BONGO -> if (isAccent) 650f else if (isSubdivision) 380f else 480f
                SoundType.MARIMBA -> if (isAccent) 880f else if (isSubdivision) 554f else 659f
                SoundType.SYNTH_CLICK -> if (isAccent) 1600f else if (isSubdivision) 900f else 1200f
            }
        } * pitchMultiplier

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-t * (1000.0 / (durationMs * 0.3))) // Exponential decay

            val sampleValue = when (soundType) {
                SoundType.WOODBLOCK -> {
                    val s1 = sin(2 * PI * baseFreq * t)
                    val s2 = 0.4 * sin(2 * PI * baseFreq * 2.1 * t)
                    (s1 + s2) * envelope
                }
                SoundType.DIGITAL_CLICK -> {
                    val s1 = sin(2 * PI * baseFreq * t)
                    val noise = (Random.nextFloat() * 2 - 1) * 0.2
                    (s1 + noise) * envelope
                }
                SoundType.COWBELL -> {
                    // Metallic cowbell dual harmonics
                    val f1 = baseFreq
                    val f2 = baseFreq * 1.48f
                    val s1 = sin(2 * PI * f1 * t)
                    val s2 = 0.8 * sin(2 * PI * f2 * t)
                    (s1 + s2) * 0.5 * envelope
                }
                SoundType.DRUM_KIT -> {
                    if (isAccent) {
                        // Kick drum frequency drop
                        val currentFreq = (baseFreq * exp(-t * 30.0)).toFloat().coerceAtLeast(40f)
                        sin(2 * PI * currentFreq * t) * envelope
                    } else if (isSubdivision) {
                        // Hi-hat metallic noise spike
                        val noise = (Random.nextFloat() * 2 - 1)
                        noise * exp(-t * 80.0)
                    } else {
                        // Snare pop + noise
                        val body = sin(2 * PI * baseFreq * t) * exp(-t * 25.0)
                        val noise = (Random.nextFloat() * 2 - 1) * exp(-t * 40.0) * 0.7
                        (body + noise) * 0.6
                    }
                }
                SoundType.BONGO -> {
                    val pitchDrop = baseFreq * (1.0 + 0.3 * exp(-t * 50.0))
                    sin(2 * PI * pitchDrop * t) * envelope
                }
                SoundType.MARIMBA -> {
                    val s1 = sin(2 * PI * baseFreq * t)
                    val s2 = 0.3 * sin(2 * PI * baseFreq * 3.0 * t) * exp(-t * 40.0)
                    (s1 + s2) * envelope
                }
                SoundType.SYNTH_CLICK -> {
                    val sq = if (sin(2 * PI * baseFreq * t) > 0) 0.8 else -0.8
                    sq * envelope
                }
            }

            val amplitude = if (isAccent) 32000 else if (isSubdivision) 18000 else 26000
            val pcm16 = (sampleValue * amplitude).toInt().coerceIn(-32768, 32767)
            buffer[i] = pcm16.toShort()
        }

        return buffer
    }

    /**
     * Generates a continuous pure sine wave buffer for reference tuning tone (e.g., A4 = 440Hz).
     */
    fun generateReferenceTone(freqHz: Float, durationMs: Int = 1000): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val fadeInOutSamples = (SAMPLE_RATE * 0.02).toInt() // 20ms fade in/out to eliminate pop

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var envelope = 1.0

            if (i < fadeInOutSamples) {
                envelope = i.toDouble() / fadeInOutSamples
            } else if (i > numSamples - fadeInOutSamples) {
                envelope = (numSamples - i).toDouble() / fadeInOutSamples
            }

            val sampleValue = sin(2 * PI * freqHz * t) * envelope
            val pcm16 = (sampleValue * 22000).toInt().coerceIn(-32768, 32767)
            buffer[i] = pcm16.toShort()
        }
        return buffer
    }
}
