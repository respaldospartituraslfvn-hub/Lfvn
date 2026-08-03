package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

enum class SubdivisionType(val displayName: String, val count: Int) {
    NONE("Negras (1/1)", 1),
    EIGHTH("Corcheas (1/2)", 2),
    TRIPLET("Tresillos (1/3)", 3),
    SIXTEENTH("Semicorcheas (1/4)", 4)
}

class PreciseMetronomeEngine(private val context: Context) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentBeat = MutableStateFlow(0)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    private val _currentSubBeat = MutableStateFlow(0)
    val currentSubBeat: StateFlow<Int> = _currentSubBeat.asStateFlow()

    // Configuration parameters
    var bpm: Int = 120
    var beatsPerMeasure: Int = 4
    var beatUnit: Int = 4
    var subdivision: SubdivisionType = SubdivisionType.NONE
    var soundType: SoundType = SoundType.WOODBLOCK
    var pattern: IntArray = intArrayOf(1, 0, 0, 0) // 1 = Accent, 0 = Normal, -1 = Mute, 2 = Sub
    var enableHaptics: Boolean = true

    var customAccentPitchHz: Float = 0f
    var customNormalPitchHz: Float = 0f
    var customDecayMs: Int = 0

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator by lazy {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun start() {
        if (_isPlaying.value) return
        _isPlaying.value = true

        playbackJob = scope.launch {
            runAudioLoop()
        }
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _currentBeat.value = 0
        _currentSubBeat.value = 0
    }

    fun toggle() {
        if (_isPlaying.value) stop() else start()
    }

    private fun runAudioLoop() {
        val sampleRate = AudioSynthesisEngine.SAMPLE_RATE
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val track = try {
            AudioTrack.Builder()
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) {
            _isPlaying.value = false
            return
        }

        audioTrack = track

        try {
            track.play()

            var beatIdx = 0
            var subIdx = 0

            while (scope.isActive && _isPlaying.value) {
                val subsCount = subdivision.count
                
                // Determine beat state from pattern
                val patternIdx = beatIdx % pattern.size.coerceAtLeast(1)
                val beatMode = if (patternIdx < pattern.size) pattern[patternIdx] else 0

                val isFirstSub = subIdx == 0
                val isAccent = isFirstSub && (beatMode == 1)
                val isMuted = isFirstSub && (beatMode == -1)
                val isSubdivision = !isFirstSub

                // Generate PCM click audio
                val clickPCM = if (isMuted) {
                    ShortArray(0)
                } else {
                    AudioSynthesisEngine.generateClickBuffer(
                        soundType = soundType,
                        isAccent = isAccent,
                        isSubdivision = isSubdivision,
                        customAccentPitchHz = customAccentPitchHz,
                        customNormalPitchHz = customNormalPitchHz,
                        customDecayMs = customDecayMs
                    )
                }

                // Calculate exact tick duration in samples
                val secondsPerBeat = 60.0 / bpm
                val secondsPerSubTick = secondsPerBeat / subsCount
                val totalSamplesForTick = (sampleRate * secondsPerSubTick).toInt()

                val silenceSamplesCount = max(0, totalSamplesForTick - clickPCM.size)
                val silencePCM = ShortArray(silenceSamplesCount)

                // Update UI state
                _currentBeat.value = beatIdx
                _currentSubBeat.value = subIdx

                // Trigger haptic vibration on accent beat
                if (isAccent && enableHaptics) {
                    triggerHaptic()
                }

                if (!_isPlaying.value || !scope.isActive) break

                // Write audio to track with exception handling
                if (clickPCM.isNotEmpty()) {
                    val res = track.write(clickPCM, 0, clickPCM.size)
                    if (res < 0) break
                }
                if (!_isPlaying.value || !scope.isActive) break
                if (silencePCM.isNotEmpty()) {
                    val res = track.write(silencePCM, 0, silencePCM.size)
                    if (res < 0) break
                }

                // Advance indices
                subIdx++
                if (subIdx >= subsCount) {
                    subIdx = 0
                    beatIdx = (beatIdx + 1) % beatsPerMeasure
                }
            }
        } catch (_: Exception) {
            // Catches any IllegalStateException when track is stopped or released
        } finally {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            } catch (_: Exception) {}
            if (audioTrack == track) {
                audioTrack = null
            }
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    fun playTone(freqHz: Float, durationMs: Int = 1000) {
        scope.launch {
            try {
                val pcm = AudioSynthesisEngine.generateReferenceTone(freqHz, durationMs)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(AudioSynthesisEngine.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
            } catch (_: Exception) {}
        }
    }
}
