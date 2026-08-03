package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.PercussionLibraryViewModel

@Composable
fun PercussionLibraryScreen(viewModel: PercussionLibraryViewModel) {
    val soundProfiles by viewModel.soundProfiles.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedSoundType.collectAsStateWithLifecycle()
    val accentPitchHz by viewModel.accentPitchHz.collectAsStateWithLifecycle()
    val normalPitchHz by viewModel.normalPitchHz.collectAsStateWithLifecycle()
    val decayMs by viewModel.decayMs.collectAsStateWithLifecycle()

    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var profileNameInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Biblioteca de Percusión",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent
                )
                Text(
                    text = "Sonidos Personalizables y Síntesis Audio",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showSaveProfileDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_sound_profile")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Category Cards Grid
        Text(
            text = "Tipo de Instrumento / Sonido",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        SoundType.entries.forEach { sound ->
            val isSelected = sound == selectedType
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("sound_type_${sound.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF2B1B40) else StudioCard
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(if (isSelected) PurpleAccent else StudioCardBorder, StudioCardBorder)
                    )
                ),
                onClick = { viewModel.selectSoundType(sound) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = if (isSelected) PurpleAccent else TextMuted
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sound.displayName,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = {
                                viewModel.selectSoundType(sound)
                                viewModel.previewSound(isAccent = true)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Acento", fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.selectSoundType(sound)
                                viewModel.previewSound(isAccent = false)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Normal", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sound Customizer Sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = PurpleAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ajuste Fino de Parámetros Sintetizados",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accent Pitch
                Text("Tono Acento: ${accentPitchHz.toInt()} Hz", fontSize = 12.sp, color = AmberAccent)
                Slider(
                    value = accentPitchHz,
                    onValueChange = { viewModel.setAccentPitch(it) },
                    valueRange = 200f..3000f,
                    colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Normal Pitch
                Text("Tono Normal: ${normalPitchHz.toInt()} Hz", fontSize = 12.sp, color = CyanAccent)
                Slider(
                    value = normalPitchHz,
                    onValueChange = { viewModel.setNormalPitch(it) },
                    valueRange = 100f..2400f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Decay Time
                Text("Duración / Resonancia (Decay): $decayMs ms", fontSize = 12.sp, color = PurpleAccent)
                Slider(
                    value = decayMs.toFloat(),
                    onValueChange = { viewModel.setDecayMs(it.toInt()) },
                    valueRange = 20f..250f,
                    colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurpleAccent)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Sound Profiles Section
        Text(
            text = "Perfiles de Sonido Guardados",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (soundProfiles.isEmpty()) {
            Text("No hay perfiles personalizados guardados aún.", fontSize = 12.sp, color = TextMuted)
        } else {
            soundProfiles.forEach { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(profile.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                "Acento: ${profile.accentPitchHz.toInt()}Hz | Normal: ${profile.normalPitchHz.toInt()}Hz",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                try {
                                    val sound = SoundType.valueOf(profile.soundType)
                                    viewModel.selectSoundType(sound)
                                    viewModel.setAccentPitch(profile.accentPitchHz)
                                    viewModel.setNormalPitch(profile.normalPitchHz)
                                    viewModel.setDecayMs(profile.decayMs)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cargar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Save Sound Profile Dialog
    if (showSaveProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProfileDialog = false },
            title = { Text("Guardar Perfil de Sonido") },
            text = {
                Column {
                    Text("Ingresa un nombre para tu perfil de percusión:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profileNameInput,
                        onValueChange = { profileNameInput = it },
                        label = { Text("Nombre del Perfil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomProfile(profileNameInput)
                        profileNameInput = ""
                        showSaveProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProfileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
