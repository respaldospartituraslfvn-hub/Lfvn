package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metronome_presets")
data class MetronomePresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bpm: Int,
    val beatsPerMeasure: Int = 4,
    val beatUnit: Int = 4,
    val subdivision: String = "QUARTER", // QUARTER, EIGHTH, TRIPLET, SIXTEENTH
    val soundSet: String = "WOODBLOCK",
    val patternJson: String = "[1,0,0,0]", // 1 = Accent, 0 = Normal, -1 = Mute, 2 = Sub-accent
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sound_profiles")
data class SoundProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val soundType: String,
    val accentPitchHz: Float = 1200f,
    val normalPitchHz: Float = 800f,
    val decayMs: Int = 80,
    val volume: Float = 1.0f
)
