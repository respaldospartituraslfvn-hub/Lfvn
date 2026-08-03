package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundType
import com.example.audio.SubdivisionType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.VividRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.MetronomeViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MetronomeScreen(viewModel: MetronomeViewModel) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val bpm by viewModel.bpm.collectAsStateWithLifecycle()
    val currentBeat by viewModel.currentBeat.collectAsStateWithLifecycle()
    val beatsPerMeasure by viewModel.beatsPerMeasure.collectAsStateWithLifecycle()
    val beatUnit by viewModel.beatUnit.collectAsStateWithLifecycle()
    val subdivision by viewModel.subdivision.collectAsStateWithLifecycle()
    val soundType by viewModel.soundType.collectAsStateWithLifecycle()
    val pattern by viewModel.pattern.collectAsStateWithLifecycle()
    val enableHaptics by viewModel.enableHaptics.collectAsStateWithLifecycle()
    val savedPresets by viewModel.savedPresets.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showPresetsMenu by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

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

        // Top bar actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MetroPulse",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Text(
                    text = "Metrónomo de Precisión Studio",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row {
                IconButton(
                    onClick = { showPresetsMenu = true },
                    modifier = Modifier
                        .testTag("presets_button")
                        .background(StudioCard, CircleShape)
                        .border(1.dp, StudioCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Presets Guardados",
                        tint = CyanAccent
                    )
                }

                DropdownMenu(
                    expanded = showPresetsMenu,
                    onDismissRequest = { showPresetsMenu = false }
                ) {
                    Text(
                        text = "Presets Guardados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                    if (savedPresets.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Sin presets guardados", color = TextMuted) },
                            onClick = {}
                        )
                    } else {
                        savedPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("${preset.bpm} BPM | ${preset.beatsPerMeasure}/${preset.beatUnit}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    viewModel.loadPreset(preset)
                                    showPresetsMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .testTag("save_preset_button")
                        .background(StudioCard, CircleShape)
                        .border(1.dp, StudioCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar Preset",
                        tint = AmberAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pendulum / Visual Metronome Dial
        VisualMetronomePendulum(
            bpm = bpm,
            isPlaying = isPlaying,
            currentBeat = currentBeat,
            beatsPerMeasure = beatsPerMeasure,
            pattern = pattern,
            onBpmChange = { viewModel.setBpm(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BPM Quick Controls (+1, +5, -1, -5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.adjustBpm(-5) },
                modifier = Modifier.testTag("btn_minus_5"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Text("-5", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.adjustBpm(-1) },
                modifier = Modifier.testTag("btn_minus_1"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos 1")
            }

            // Big Play/Stop FAB
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isPlaying) listOf(VividRed, Color(0xFFB30036))
                            else listOf(CyanAccent, Color(0xFF00A8B3))
                        )
                    )
                    .clickable { viewModel.toggleMetronome() }
                    .testTag("play_stop_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Iniciar",
                    tint = StudioBackground,
                    modifier = Modifier.size(42.dp)
                )
            }

            OutlinedButton(
                onClick = { viewModel.adjustBpm(1) },
                modifier = Modifier.testTag("btn_plus_1"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Más 1")
            }

            OutlinedButton(
                onClick = { viewModel.adjustBpm(5) },
                modifier = Modifier.testTag("btn_plus_5"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Text("+5", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tap Tempo & Haptics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.registerTapTempo() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tap_tempo_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberAccent,
                    contentColor = StudioBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TAP TEMPO", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { viewModel.toggleHaptics() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (enableHaptics) StudioSurfaceVariant else StudioCard,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        if (enableHaptics) AmberAccent else StudioCardBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    imageVector = if (enableHaptics) Icons.Default.GraphicEq else Icons.Default.VolumeMute,
                    contentDescription = "Vibración Háptica",
                    tint = if (enableHaptics) AmberAccent else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Beat Pattern Matrix Step Row
        Text(
            text = "Patrón del Compás (Toca para cambiar Acento)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pattern.size) { idx ->
                val mode = pattern[idx]
                val isActive = isPlaying && (currentBeat == idx)

                Card(
                    modifier = Modifier
                        .size(width = 68.dp, height = 76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleBeatPatternState(idx) }
                        .testTag("beat_step_$idx"),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isActive && mode == 1 -> AmberAccent
                            isActive && mode != -1 -> CyanAccent
                            mode == 1 -> Color(0xFF332A00)
                            mode == 0 -> StudioCard
                            mode == -1 -> Color(0xFF1F1A24)
                            else -> PurpleAccent.copy(alpha = 0.2f)
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (isActive) CyanAccent else if (mode == 1) AmberAccent else StudioCardBorder,
                                StudioCardBorder
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive && mode == 1) StudioBackground else TextSecondary
                        )

                        Text(
                            text = when (mode) {
                                1 -> "ACENTO"
                                0 -> "Normal"
                                -1 -> "MUTE"
                                else -> "SUB"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isActive && mode == 1 -> StudioBackground
                                mode == 1 -> AmberAccent
                                mode == 0 -> CyanAccent
                                mode == -1 -> VividRed
                                else -> PurpleAccent
                            }
                        )

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) CyanAccent
                                    else if (mode == 1) AmberAccent
                                    else TextMuted
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time Signature & Subdivision Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time Signature Selector Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Firma de Tiempo", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val timeSigs = listOf(
                        Pair(4, 4), Pair(3, 4), Pair(2, 4),
                        Pair(6, 8), Pair(7, 8), Pair(5, 4), Pair(9, 8)
                    )

                    var expandedSigs by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { expandedSigs = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("$beatsPerMeasure / $beatUnit", fontWeight = FontWeight.Bold, color = CyanAccent)
                        }

                        DropdownMenu(
                            expanded = expandedSigs,
                            onDismissRequest = { expandedSigs = false }
                        ) {
                            timeSigs.forEach { (b, u) ->
                                DropdownMenuItem(
                                    text = { Text("$b / $u", fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        viewModel.setTimeSignature(b, u)
                                        expandedSigs = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Subdivision Selector Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Subdivisión", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    var expandedSubs by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { expandedSubs = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(subdivision.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                        }

                        DropdownMenu(
                            expanded = expandedSubs,
                            onDismissRequest = { expandedSubs = false }
                        ) {
                            SubdivisionType.entries.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.displayName, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setSubdivision(sub)
                                        expandedSubs = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Percussion Sound Type Quick Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Sonido del Metrónomo", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SoundType.entries.toTypedArray()) { sound ->
                        val isSelected = sound == soundType
                        Button(
                            onClick = { viewModel.setSoundType(sound) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CyanAccent else StudioSurfaceVariant,
                                contentColor = if (isSelected) StudioBackground else TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(sound.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Italian Tempo Presets Bar
        Text(
            text = "Marcas de Tempo Clásicas",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.tempoMarks) { mark ->
                OutlinedButton(
                    onClick = { viewModel.setBpm(mark.bpm) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (bpm == mark.bpm) AmberAccent else TextPrimary
                    )
                ) {
                    Text("${mark.name} (${mark.bpm})", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar Preset de Ritmo") },
            text = {
                Column {
                    Text("Ingresa un nombre para guardar esta configuración de metrónomo:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Nombre de Canción / Ritmo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentPreset(presetNameInput)
                        presetNameInput = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBackground)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VisualMetronomePendulum(
    bpm: Int,
    isPlaying: Boolean,
    currentBeat: Int,
    beatsPerMeasure: Int,
    pattern: IntArray,
    onBpmChange: (Int) -> Unit
) {
    val pendulumAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying, bpm) {
        if (isPlaying) {
            val periodMs = (60000.0 / bpm).toInt()
            pendulumAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = periodMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pendulumAnim.snapTo(0.5f)
        }
    }

    val angleDegree = (pendulumAnim.value - 0.5f) * 60f

    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        StudioCard,
                        StudioBackground
                    )
                )
            )
            .border(2.dp, StudioCardBorder, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pos = change.position
                    val angle = atan2(pos.y - center.y, pos.x - center.x)
                    val normalizedAngle = ((angle * 180 / PI + 360) % 360).toFloat()
                    val newBpm = (30 + (normalizedAngle / 360f * 270)).toInt().coerceIn(30, 300)
                    onBpmChange(newBpm)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 12.dp.toPx()

            // Outer Ring
            drawCircle(
                color = StudioCardBorder,
                radius = radius,
                style = Stroke(width = 6.dp.toPx())
            )

            // Dynamic Active Beat Flash Ring
            val isAccent = isPlaying && (currentBeat < pattern.size && pattern[currentBeat] == 1)
            val ringColor = if (isPlaying) {
                if (isAccent) AmberAccent else CyanAccent
            } else StudioCardBorder

            drawCircle(
                color = ringColor.copy(alpha = if (isPlaying) 0.8f else 0.2f),
                radius = radius - 4.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )

            // Pendulum Arm Line
            val pendulumRad = Math.toRadians((angleDegree - 90).toDouble())
            val armLength = radius - 20.dp.toPx()
            val endX = center.x + (armLength * cos(pendulumRad)).toFloat()
            val endY = center.y + (armLength * sin(pendulumRad)).toFloat()

            drawLine(
                color = if (isPlaying) CyanAccent else TextMuted,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pendulum Weight Bob
            drawCircle(
                color = if (isAccent) AmberAccent else CyanAccent,
                radius = 12.dp.toPx(),
                center = Offset(endX, endY)
            )
        }

        // Center BPM & Tempo Name Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$bpm",
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "BPM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Text(
                text = when {
                    bpm < 60 -> "Largo"
                    bpm in 60..79 -> "Adagio"
                    bpm in 80..108 -> "Andante"
                    bpm in 109..132 -> "Moderato"
                    bpm in 133..160 -> "Allegro"
                    else -> "Presto"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
