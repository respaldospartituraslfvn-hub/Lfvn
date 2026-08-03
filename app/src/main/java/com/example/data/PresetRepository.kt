package com.example.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<MetronomePresetEntity>> = presetDao.getAllPresets()
    val allSoundProfiles: Flow<List<SoundProfileEntity>> = presetDao.getAllSoundProfiles()

    suspend fun savePreset(preset: MetronomePresetEntity): Long {
        return presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(id: Int) {
        presetDao.deletePresetById(id)
    }

    suspend fun saveSoundProfile(profile: SoundProfileEntity): Long {
        return presetDao.insertSoundProfile(profile)
    }
}
