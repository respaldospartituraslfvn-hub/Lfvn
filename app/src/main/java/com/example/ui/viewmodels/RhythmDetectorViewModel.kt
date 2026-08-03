package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.AudioProcessor
import com.example.audio.RhythmResult
import kotlinx.coroutines.flow.StateFlow

class RhythmDetectorViewModel(application: Application) : AndroidViewModel(application) {

    val processor = AudioProcessor()
    val rhythmState: StateFlow<RhythmResult> = processor.rhythmState
    val isRecording: StateFlow<Boolean> = processor.isRecording

    fun startRhythmDetection() {
        processor.startProcessing(modeTuner = false)
    }

    fun stopRhythmDetection() {
        processor.stopProcessing()
    }

    fun clearRhythmHistory() {
        processor.clearRhythmHistory()
    }

    override fun onCleared() {
        super.onCleared()
        processor.stopProcessing()
    }
}
