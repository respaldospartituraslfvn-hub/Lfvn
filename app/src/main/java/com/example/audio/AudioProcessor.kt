package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PitchResult(
    val frequencyHz: Float,
    val noteName: String,
    val octave: Int,
    val centsOffset: Float, // -50.0 to +50.0
    val targetFrequencyHz: Float,
    val isSignalDetected: Boolean
)

data class RhythmResult(
    val detectedBpm: Int,
    val confidence: Float, // 0.0 to 1.0 (Rhythm consistency)
    val onsetTimestamps: List<Long>,
    val amplitudeWaveform: FloatArray
)

class AudioProcessor {

    private val _pitchState = MutableStateFlow(
        PitchResult(0f, "--", 0, 0f, 0f, false)
    )
    val pitchState: StateFlow<PitchResult> = _pitchState.asStateFlow()

    private val _rhythmState = MutableStateFlow(
        RhythmResult(0, 0f, emptyList(), FloatArray(50))
    )
    val rhythmState: StateFlow<RhythmResult> = _rhythmState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    var referenceA4: Float = 440f // Standard pitch reference (Hz)

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val noteNames = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val noteNamesEnglish = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Onset detection tracking
    private val onsetTimestamps = mutableListOf<Long>()
    private val waveformHistory = FloatArray(50)
    private var lastOnsetMs: Long = 0L

    @SuppressLint("MissingPermission")
    fun startProcessing(modeTuner: Boolean = true) {
        if (_isRecording.value) return
        _isRecording.value = true

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _isRecording.value = false
                return
            }

            audioRecord?.startRecording()

            recordJob = scope.launch {
                val buffer = ShortArray(2048)

                while (scope.isActive && _isRecording.value) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        if (modeTuner) {
                            processPitch(buffer, readSize, sampleRate)
                        }
                        processRhythm(buffer, readSize, sampleRate)
                    }
                }
            }
        } catch (_: Exception) {
            _isRecording.value = false
        }
    }

    fun stopProcessing() {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioTrackCleanup()
        audioRecord = null
    }

    private fun audioTrackCleanup() {
        _pitchState.value = PitchResult(0f, "--", 0, 0f, 0f, false)
    }

    /**
     * Pitch detection using normalized autocorrelation.
     */
    private fun processPitch(buffer: ShortArray, size: Int, sampleRate: Int) {
        // Calculate RMS to ensure signal presence
        var sumSquare = 0.0
        for (i in 0 until size) {
            val sample = buffer[i] / 32768.0
            sumSquare += sample * sample
        }
        val rms = sqrt(sumSquare / size)

        if (rms < 0.012) { // Silence or background noise threshold
            _pitchState.value = PitchResult(0f, "--", 0, 0f, 0f, false)
            return
        }

        // Min frequency ~ 40Hz (E1 = 41.2Hz), Max frequency ~ 2000Hz (C7)
        val minLag = sampleRate / 2000
        val maxLag = sampleRate / 40

        var maxCorr = 0.0
        var bestLag = -1

        for (lag in minLag..maxLag) {
            var corr = 0.0
            for (i in 0 until size - lag) {
                corr += buffer[i].toDouble() * buffer[i + lag].toDouble()
            }

            if (corr > maxCorr) {
                maxCorr = corr
                bestLag = lag
            }
        }

        if (bestLag > 0 && maxCorr > 1e7) {
            val fundamentalFreq = sampleRate.toFloat() / bestLag
            val pitchInfo = calculatePitchDetails(fundamentalFreq)
            _pitchState.value = pitchInfo
        } else {
            _pitchState.value = PitchResult(0f, "--", 0, 0f, 0f, false)
        }
    }

    private fun calculatePitchDetails(freq: Float): PitchResult {
        if (freq <= 0f) return PitchResult(0f, "--", 0, 0f, 0f, false)

        // MIDI note number equation: n = 69 + 12 * log2(f / A4)
        val midiNumber = 69.0 + 12.0 * log2(freq / referenceA4.toDouble())
        val roundedMidi = midiNumber.roundToInt()

        // Nearest target frequency
        val targetFreq = (referenceA4 * 2.0.pow((roundedMidi - 69) / 12.0)).toFloat()

        // Cents offset: 100 * 12 * log2(f / target)
        val cents = (1200.0 * log2(freq / targetFreq)).toFloat().coerceIn(-50f, 50f)

        val noteIndex = (roundedMidi % 12 + 12) % 12
        val octave = (roundedMidi / 12) - 1
        val noteName = noteNames[noteIndex] + " (" + noteNamesEnglish[noteIndex] + ")"

        return PitchResult(
            frequencyHz = freq,
            noteName = noteName,
            octave = octave,
            centsOffset = cents,
            targetFrequencyHz = targetFreq,
            isSignalDetected = true
        )
    }

    /**
     * Real-time rhythm & onset detection.
     */
    private fun processRhythm(buffer: ShortArray, size: Int, sampleRate: Int) {
        var sumSquare = 0.0
        for (i in 0 until size) {
            val norm = buffer[i] / 32768.0
            sumSquare += norm * norm
        }
        val energy = sqrt(sumSquare / size).toFloat()

        // Update scrolling waveform
        System.arraycopy(waveformHistory, 1, waveformHistory, 0, waveformHistory.size - 1)
        waveformHistory[waveformHistory.size - 1] = energy.coerceIn(0f, 1f)

        val nowMs = System.currentTimeMillis()

        // Onset peak threshold
        if (energy > 0.08f && (nowMs - lastOnsetMs) > 130L) { // Max ~ 460 BPM
            lastOnsetMs = nowMs
            onsetTimestamps.add(nowMs)

            // Keep last 16 onsets
            if (onsetTimestamps.size > 16) {
                onsetTimestamps.removeAt(0)
            }

            // Estimate BPM from intervals
            if (onsetTimestamps.size >= 3) {
                val intervals = mutableListOf<Long>()
                for (k in 1 until onsetTimestamps.size) {
                    intervals.add(onsetTimestamps[k] - onsetTimestamps[k - 1])
                }

                intervals.sort()
                val medianIntervalMs = intervals[intervals.size / 2]

                if (medianIntervalMs in 150..2000) { // 30 BPM to 400 BPM
                    val calculatedBpm = (60000.0 / medianIntervalMs).roundToInt()

                    // Calculate stability confidence (std dev)
                    val avgInterval = intervals.average()
                    var varianceSum = 0.0
                    for (inv in intervals) {
                        varianceSum += (inv - avgInterval) * (inv - avgInterval)
                    }
                    val stdDev = sqrt(varianceSum / intervals.size)
                    val confidence = (1.0 - (stdDev / avgInterval)).toFloat().coerceIn(0f, 1f)

                    _rhythmState.value = RhythmResult(
                        detectedBpm = calculatedBpm,
                        confidence = confidence,
                        onsetTimestamps = onsetTimestamps.toList(),
                        amplitudeWaveform = waveformHistory.clone()
                    )
                }
            }
        } else {
            // Update waveform without new onset
            _rhythmState.value = _rhythmState.value.copy(
                amplitudeWaveform = waveformHistory.clone()
            )
        }
    }

    fun clearRhythmHistory() {
        onsetTimestamps.clear()
        _rhythmState.value = RhythmResult(0, 0f, emptyList(), FloatArray(50))
    }
}
