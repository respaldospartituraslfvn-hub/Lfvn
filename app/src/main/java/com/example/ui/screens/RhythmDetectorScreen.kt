package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.RhythmDetectorViewModel

@Composable
fun RhythmDetectorScreen(viewModel: RhythmDetectorViewModel) {
    val context = LocalContext.current
    val rhythmState by viewModel.rhythmState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.startRhythmDetection()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.startRhythmDetection()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRhythmDetection()
        }
    }

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

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Detector de Ritmo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent
                )
                Text(
                    text = "Reconocimiento y Análisis Acústico",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row {
                IconButton(
                    onClick = { viewModel.clearRhythmHistory() },
                    modifier = Modifier
                        .background(StudioCard, CircleShape)
                        .testTag("reset_rhythm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Limpiar Registro",
                        tint = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (!hasPermission) {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isRecording) viewModel.stopRhythmDetection() else viewModel.startRhythmDetection()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) AmberAccent else StudioCard,
                        contentColor = if (isRecording) MaterialTheme.colorScheme.background else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("rhythm_mic_toggle")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRecording) "Escuchando" else "Grabar")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Recognized BPM Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AmberAccent, StudioCardBorder))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TEMPO RECONOCIDO EN TIEMPO REAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                if (rhythmState.detectedBpm > 0) {
                    Text(
                        text = "${rhythmState.detectedBpm}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent
                    )
                    Text("BPM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanAccent)

                    Spacer(modifier = Modifier.height(12.dp))

                    val stabilityPct = (rhythmState.confidence * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (stabilityPct >= 80) EmeraldGreen else AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Constancia del Ritmo: $stabilityPct%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca tu instrumento o da golpes rítmicos frente al micrófono",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real-Time Waveform Energy Canvas
        Text(
            text = "Onda de Sonido y Transitorios de Percución",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val wave = rhythmState.amplitudeWaveform
                    val stepX = width / wave.size.coerceAtLeast(1)

                    for (i in 0 until wave.size - 1) {
                        val x1 = i * stepX
                        val y1 = centerY - (wave[i] * height * 0.45f)

                        val x2 = (i + 1) * stepX
                        val y2 = centerY - (wave[i + 1] * height * 0.45f)

                        drawLine(
                            color = if (wave[i] > 0.08f) AmberAccent else CyanAccent,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Draw mirror lower half for audio waveform
                        drawLine(
                            color = (if (wave[i] > 0.08f) AmberAccent else CyanAccent).copy(alpha = 0.4f),
                            start = Offset(x1, centerY + (wave[i] * height * 0.45f)),
                            end = Offset(x2, centerY + (wave[i + 1] * height * 0.45f)),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Detected Beats Pulse List / History
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Golpes Detectados Recientes (${rhythmState.onsetTimestamps.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (rhythmState.onsetTimestamps.isEmpty()) {
                    Text("No se han registrado golpes aún.", fontSize = 12.sp, color = TextMuted)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rhythmState.onsetTimestamps.takeLast(8).forEachIndexed { idx, t ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PurpleAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
