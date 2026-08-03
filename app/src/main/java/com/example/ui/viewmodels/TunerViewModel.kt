package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.AudioProcessor
import com.example.audio.PitchResult
import com.example.audio.PreciseMetronomeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TargetString(val name: String, val frequencyHz: Float, val noteName: String)

data class InstrumentPreset(
    val id: String,
    val name: String,
    val iconName: String,
    val strings: List<TargetString>
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    val processor = AudioProcessor()
    private val metronomeEngine = PreciseMetronomeEngine(application)

    val pitchState: StateFlow<PitchResult> = processor.pitchState
    val isRecording: StateFlow<Boolean> = processor.isRecording

    private val _referenceA4 = MutableStateFlow(440f)
    val referenceA4: StateFlow<Float> = _referenceA4.asStateFlow()

    val instruments = listOf(
        InstrumentPreset(
            id = "CHROMATIC",
            name = "Cromático",
            iconName = "tune",
            strings = emptyList()
        ),
        InstrumentPreset(
            id = "GUITAR_STD",
            name = "Guitarra (E A D G B E)",
            iconName = "music_note",
            strings = listOf(
                TargetString("6ª E", 82.41f, "E2"),
                TargetString("5ª A", 110.0f, "A2"),
                TargetString("4ª D", 146.83f, "D3"),
                TargetString("3ª G", 196.0f, "G3"),
                TargetString("2ª B", 246.94f, "B3"),
                TargetString("1ª E", 329.63f, "E4")
            )
        ),
        InstrumentPreset(
            id = "BASS_STD",
            name = "Bajo 4C (E A D G)",
            iconName = "graphic_eq",
            strings = listOf(
                TargetString("4ª E", 41.20f, "E1"),
                TargetString("3ª A", 55.00f, "A1"),
                TargetString("2ª D", 73.42f, "D2"),
                TargetString("1ª G", 98.00f, "G2")
            )
        ),
        InstrumentPreset(
            id = "UKULELE",
            name = "Ukulele (G C E A)",
            iconName = "album",
            strings = listOf(
                TargetString("4ª G", 392.00f, "G4"),
                TargetString("3ª C", 261.63f, "C4"),
                TargetString("2ª E", 329.63f, "E4"),
                TargetString("1ª A", 440.00f, "A4")
            )
        ),
        InstrumentPreset(
            id = "VIOLIN",
            name = "Violín (G D A E)",
            iconName = "spatial_audio",
            strings = listOf(
                TargetString("4ª G", 196.00f, "G3"),
                TargetString("3ª D", 293.66f, "D4"),
                TargetString("2ª A", 440.00f, "A4"),
                TargetString("1ª E", 659.25f, "E5")
            )
        ),
        InstrumentPreset(
            id = "CELLO",
            name = "Cello (C G D A)",
            iconName = "surround_sound",
            strings = listOf(
                TargetString("4ª C", 65.41f, "C2"),
                TargetString("3ª G", 98.00f, "G2"),
                TargetString("2ª D", 146.83f, "D3"),
                TargetString("1ª A", 220.00f, "A3")
            )
        )
    )

    private val _selectedInstrument = MutableStateFlow(instruments[1]) // Default Guitar
    val selectedInstrument: StateFlow<InstrumentPreset> = _selectedInstrument.asStateFlow()

    private val _selectedString = MutableStateFlow<TargetString?>(null) // null = Auto
    val selectedString: StateFlow<TargetString?> = _selectedString.asStateFlow()

    fun setInstrument(preset: InstrumentPreset) {
        _selectedInstrument.value = preset
        _selectedString.value = null
    }

    fun selectString(target: TargetString?) {
        _selectedString.value = target
    }

    fun setReferenceA4(freqHz: Float) {
        _referenceA4.value = freqHz
        processor.referenceA4 = freqHz
    }

    fun startTuner() {
        processor.startProcessing(modeTuner = true)
    }

    fun stopTuner() {
        processor.stopProcessing()
    }

    fun playReferenceStringTone(string: TargetString) {
        metronomeEngine.playTone(string.frequencyHz, 1200)
    }

    override fun onCleared() {
        super.onCleared()
        processor.stopProcessing()
    }
}
