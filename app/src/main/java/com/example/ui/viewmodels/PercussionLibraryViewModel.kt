package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSynthesisEngine
import com.example.audio.PreciseMetronomeEngine
import com.example.audio.SoundType
import com.example.data.AppDatabase
import com.example.data.PresetRepository
import com.example.data.SoundProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PercussionLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresetRepository
    private val metronomeEngine = PreciseMetronomeEngine(application)

    val soundProfiles: StateFlow<List<SoundProfileEntity>>

    private val _selectedSoundType = MutableStateFlow(SoundType.WOODBLOCK)
    val selectedSoundType: StateFlow<SoundType> = _selectedSoundType.asStateFlow()

    private val _accentPitchHz = MutableStateFlow(1200f)
    val accentPitchHz: StateFlow<Float> = _accentPitchHz.asStateFlow()

    private val _normalPitchHz = MutableStateFlow(800f)
    val normalPitchHz: StateFlow<Float> = _normalPitchHz.asStateFlow()

    private val _decayMs = MutableStateFlow(80)
    val decayMs: StateFlow<Int> = _decayMs.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).presetDao()
        repository = PresetRepository(dao)

        soundProfiles = repository.allSoundProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectSoundType(type: SoundType) {
        _selectedSoundType.value = type
        when (type) {
            SoundType.WOODBLOCK -> { _accentPitchHz.value = 1200f; _normalPitchHz.value = 800f; _decayMs.value = 60 }
            SoundType.DIGITAL_CLICK -> { _accentPitchHz.value = 2400f; _normalPitchHz.value = 1600f; _decayMs.value = 40 }
            SoundType.COWBELL -> { _accentPitchHz.value = 900f; _normalPitchHz.value = 600f; _decayMs.value = 120 }
            SoundType.DRUM_KIT -> { _accentPitchHz.value = 150f; _normalPitchHz.value = 220f; _decayMs.value = 100 }
            SoundType.BONGO -> { _accentPitchHz.value = 650f; _normalPitchHz.value = 480f; _decayMs.value = 80 }
            SoundType.MARIMBA -> { _accentPitchHz.value = 880f; _normalPitchHz.value = 659f; _decayMs.value = 100 }
            SoundType.SYNTH_CLICK -> { _accentPitchHz.value = 1600f; _normalPitchHz.value = 1200f; _decayMs.value = 50 }
        }
    }

    fun setAccentPitch(pitchHz: Float) {
        _accentPitchHz.value = pitchHz
    }

    fun setNormalPitch(pitchHz: Float) {
        _normalPitchHz.value = pitchHz
    }

    fun setDecayMs(ms: Int) {
        _decayMs.value = ms
    }

    fun previewSound(isAccent: Boolean) {
        metronomeEngine.soundType = _selectedSoundType.value
        metronomeEngine.customAccentPitchHz = _accentPitchHz.value
        metronomeEngine.customNormalPitchHz = _normalPitchHz.value
        metronomeEngine.customDecayMs = _decayMs.value

        val pcm = AudioSynthesisEngine.generateClickBuffer(
            soundType = _selectedSoundType.value,
            isAccent = isAccent,
            isSubdivision = false,
            customAccentPitchHz = _accentPitchHz.value,
            customNormalPitchHz = _normalPitchHz.value,
            customDecayMs = _decayMs.value
        )

        // Play brief preview PCM
        val freq = if (isAccent) _accentPitchHz.value else _normalPitchHz.value
        metronomeEngine.playTone(freq, _decayMs.value.coerceAtLeast(30))
    }

    fun saveCustomProfile(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val profile = SoundProfileEntity(
                name = name,
                soundType = _selectedSoundType.value.name,
                accentPitchHz = _accentPitchHz.value,
                normalPitchHz = _normalPitchHz.value,
                decayMs = _decayMs.value,
                volume = _volume.value
            )
            repository.saveSoundProfile(profile)
        }
    }
}
