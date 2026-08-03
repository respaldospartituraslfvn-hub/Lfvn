package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM metronome_presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<MetronomePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: MetronomePresetEntity): Long

    @Query("DELETE FROM metronome_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    @Query("SELECT * FROM sound_profiles ORDER BY name ASC")
    fun getAllSoundProfiles(): Flow<List<SoundProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoundProfile(profile: SoundProfileEntity): Long
}
