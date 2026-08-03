package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MetronomePresetEntity::class, SoundProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metropulse_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with standard presets
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).presetDao()
                            dao.insertPreset(
                                MetronomePresetEntity(
                                    name = "Rock Estándar (4/4)",
                                    bpm = 120,
                                    beatsPerMeasure = 4,
                                    beatUnit = 4,
                                    subdivision = "QUARTER",
                                    soundSet = "WOODBLOCK",
                                    patternJson = "[1,0,0,0]"
                                )
                            )
                            dao.insertPreset(
                                MetronomePresetEntity(
                                    name = "Vals Clásico (3/4)",
                                    bpm = 90,
                                    beatsPerMeasure = 3,
                                    beatUnit = 4,
                                    subdivision = "QUARTER",
                                    soundSet = "WOODBLOCK",
                                    patternJson = "[1,0,0]"
                                )
                            )
                            dao.insertPreset(
                                MetronomePresetEntity(
                                    name = "Bossa Nova (6/8)",
                                    bpm = 140,
                                    beatsPerMeasure = 6,
                                    beatUnit = 8,
                                    subdivision = "EIGHTH",
                                    soundSet = "COWBELL",
                                    patternJson = "[1,0,0,1,0,0]"
                                )
                            )
                            dao.insertPreset(
                                MetronomePresetEntity(
                                    name = "Ritmo Asimétrico (7/8)",
                                    bpm = 160,
                                    beatsPerMeasure = 7,
                                    beatUnit = 8,
                                    subdivision = "QUARTER",
                                    soundSet = "SYNTH_CLICK",
                                    patternJson = "[1,0,1,0,1,0,0]"
                                )
                            )
                            
                            // Default sound profiles
                            dao.insertSoundProfile(
                                SoundProfileEntity(
                                    name = "Madera Tradicional",
                                    soundType = "WOODBLOCK",
                                    accentPitchHz = 1200f,
                                    normalPitchHz = 800f,
                                    decayMs = 60,
                                    volume = 1.0f
                                )
                            )
                            dao.insertSoundProfile(
                                SoundProfileEntity(
                                    name = "Cinta Digital High",
                                    soundType = "DIGITAL_CLICK",
                                    accentPitchHz = 2400f,
                                    normalPitchHz = 1600f,
                                    decayMs = 40,
                                    volume = 0.9f
                                )
                            )
                            dao.insertSoundProfile(
                                SoundProfileEntity(
                                    name = "Cencerro de Estudio",
                                    soundType = "COWBELL",
                                    accentPitchHz = 900f,
                                    normalPitchHz = 600f,
                                    decayMs = 120,
                                    volume = 1.0f
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
