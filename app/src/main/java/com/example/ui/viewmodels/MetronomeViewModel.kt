package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSynthesisEngine
import com.example.audio.PreciseMetronomeEngine
import com.example.audio.SoundType
import com.example.audio.SubdivisionType
import com.example.data.AppDatabase
import com.example.data.MetronomePresetEntity
import com.example.data.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TempoPreset(val name: String, val bpm: Int)

class MetronomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresetRepository
    val engine: PreciseMetronomeEngine = PreciseMetronomeEngine(application)

    val isPlaying: StateFlow<Boolean> = engine.isPlaying
    val currentBeat: StateFlow<Int> = engine.currentBeat
    val currentSubBeat: StateFlow<Int> = engine.currentSubBeat

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _beatsPerMeasure = MutableStateFlow(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure.asStateFlow()

    private val _beatUnit = MutableStateFlow(4)
    val beatUnit: StateFlow<Int> = _beatUnit.asStateFlow()

    private val _subdivision = MutableStateFlow(SubdivisionType.NONE)
    val subdivision: StateFlow<SubdivisionType> = _subdivision.asStateFlow()

    private val _soundType = MutableStateFlow(SoundType.WOODBLOCK)
    val soundType: StateFlow<SoundType> = _soundType.asStateFlow()

    private val _pattern = MutableStateFlow(intArrayOf(1, 0, 0, 0)) // 1=Accent, 0=Normal, -1=Mute, 2=Sub
    val pattern: StateFlow<IntArray> = _pattern.asStateFlow()

    private val _enableHaptics = MutableStateFlow(true)
    val enableHaptics: StateFlow<Boolean> = _enableHaptics.asStateFlow()

    val savedPresets: StateFlow<List<MetronomePresetEntity>>

    private val tapTimestamps = mutableListOf<Long>()

    val tempoMarks = listOf(
        TempoPreset("Largo", 45),
        TempoPreset("Adagio", 65),
        TempoPreset("Andante", 92),
        TempoPreset("Moderato", 112),
        TempoPreset("Allegro", 135),
        TempoPreset("Presto", 175)
    )

    init {
        val dao = AppDatabase.getDatabase(application).presetDao()
        repository = PresetRepository(dao)

        savedPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        syncEngineParameters()
    }

    private fun syncEngineParameters() {
        engine.bpm = _bpm.value
        engine.beatsPerMeasure = _beatsPerMeasure.value
        engine.beatUnit = _beatUnit.value
        engine.subdivision = _subdivision.value
        engine.soundType = _soundType.value
        engine.pattern = _pattern.value
        engine.enableHaptics = _enableHaptics.value
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(30, 300)
        _bpm.value = clamped
        engine.bpm = clamped
    }

    fun adjustBpm(delta: Int) {
        setBpm(_bpm.value + delta)
    }

    fun setTimeSignature(beats: Int, unit: Int = 4) {
        _beatsPerMeasure.value = beats.coerceIn(1, 16)
        _beatUnit.value = unit

        // Default accent on beat 1, normal on rest
        val newPattern = IntArray(beats) { if (it == 0) 1 else 0 }
        _pattern.value = newPattern

        syncEngineParameters()
    }

    fun setSubdivision(sub: SubdivisionType) {
        _subdivision.value = sub
        engine.subdivision = sub
    }

    fun setSoundType(sound: SoundType) {
        _soundType.value = sound
        engine.soundType = sound
    }

    fun toggleBeatPatternState(stepIndex: Int) {
        val current = _pattern.value.clone()
        if (stepIndex in current.indices) {
            // Cycle: Accent (1) -> Normal (0) -> Mute (-1) -> Sub-Accent (2) -> Accent (1)
            current[stepIndex] = when (current[stepIndex]) {
                1 -> 0
                0 -> -1
                -1 -> 2
                else -> 1
            }
            _pattern.value = current
            engine.pattern = current
        }
    }

    fun toggleHaptics() {
        _enableHaptics.value = !_enableHaptics.value
        engine.enableHaptics = _enableHaptics.value
    }

    fun registerTapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)

        // Clear taps older than 3 seconds
        tapTimestamps.removeAll { now - it > 3000L }

        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgIntervalMs = intervals.average()
            if (avgIntervalMs > 0) {
                val calculatedBpm = (60000.0 / avgIntervalMs).toInt().coerceIn(30, 300)
                setBpm(calculatedBpm)
            }
        }
    }

    fun toggleMetronome() {
        engine.toggle()
    }

    fun saveCurrentPreset(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val preset = MetronomePresetEntity(
                name = name,
                bpm = _bpm.value,
                beatsPerMeasure = _beatsPerMeasure.value,
                beatUnit = _beatUnit.value,
                subdivision = _subdivision.value.name,
                soundSet = _soundType.value.name,
                patternJson = _pattern.value.joinToString(prefix = "[", postfix = "]")
            )
            repository.savePreset(preset)
        }
    }

    fun loadPreset(preset: MetronomePresetEntity) {
        setBpm(preset.bpm)
        _beatsPerMeasure.value = preset.beatsPerMeasure
        _beatUnit.value = preset.beatUnit

        val sub = try { SubdivisionType.valueOf(preset.subdivision) } catch (_: Exception) { SubdivisionType.NONE }
        _subdivision.value = sub

        val sound = try { SoundType.valueOf(preset.soundSet) } catch (_: Exception) { SoundType.WOODBLOCK }
        _soundType.value = sound

        // Parse pattern
        try {
            val clean = preset.patternJson.replace("[", "").replace("]", "").trim()
            if (clean.isNotEmpty()) {
                val array = clean.split(",").map { it.trim().toInt() }.toIntArray()
                _pattern.value = array
            }
        } catch (_: Exception) {}

        syncEngineParameters()
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
    }
}
