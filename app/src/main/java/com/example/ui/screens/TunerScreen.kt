package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.PitchResult
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividRed
import com.example.ui.viewmodels.TargetString
import com.example.ui.viewmodels.TunerViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerScreen(viewModel: TunerViewModel) {
    val context = LocalContext.current
    val pitchState by viewModel.pitchState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedInstrument by viewModel.selectedInstrument.collectAsStateWithLifecycle()
    val selectedString by viewModel.selectedString.collectAsStateWithLifecycle()
    val referenceA4 by viewModel.referenceA4.collectAsStateWithLifecycle()

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
            viewModel.startTuner()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.startTuner()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTuner()
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

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Afinador Cromático",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
                Text(
                    text = "Detección de Frecuencia con Micrófono",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Permission / Mic Toggle Button
            Button(
                onClick = {
                    if (!hasPermission) {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        if (isRecording) viewModel.stopTuner() else viewModel.startTuner()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) EmeraldGreen else StudioCard,
                    contentColor = if (isRecording) MaterialTheme.colorScheme.background else TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("mic_toggle_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (!hasPermission) "Permiso" else if (isRecording) "Escuchando" else "Iniciar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instrument Presets Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.instruments) { inst ->
                val isSelected = inst.id == selectedInstrument.id
                Button(
                    onClick = { viewModel.setInstrument(inst) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) EmeraldGreen else StudioCard,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.background else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("instrument_${inst.id}")
                ) {
                    Text(inst.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Tuner Meter Gauge
        TunerGaugeMeter(
            pitchState = pitchState,
            targetString = selectedString
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Target Strings (Headstock / Strings bar)
        if (selectedInstrument.strings.isNotEmpty()) {
            Text(
                text = "CuerdasObjetivo - ${selectedInstrument.name}",
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
                // Auto detect chip
                item {
                    val isAuto = selectedString == null
                    OutlinedButton(
                        onClick = { viewModel.selectString(null) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isAuto) EmeraldGreen else TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("AUTO", fontWeight = FontWeight.Bold)
                    }
                }

                items(selectedInstrument.strings) { str ->
                    val isSelected = selectedString?.noteName == str.noteName
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectString(str) }
                            .testTag("target_string_${str.noteName}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF00381C) else StudioCard
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(if (isSelected) EmeraldGreen else StudioCardBorder, StudioCardBorder)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(str.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${str.frequencyHz} Hz", fontSize = 10.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.playReferenceStringTone(str) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Escuchar Tono",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reference Pitch Calibration (A4 = 440 Hz)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calibración de Referencia (A4)", fontSize = 12.sp, color = TextSecondary)
                    Text("${referenceA4.toInt()} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(432f, 440f, 442f, 444f).forEach { hz ->
                        val isSelected = referenceA4 == hz
                        Button(
                            onClick = { viewModel.setReferenceA4(hz) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) EmeraldGreen else StudioSurfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.background else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("${hz.toInt()}Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun TunerGaugeMeter(
    pitchState: PitchResult,
    targetString: TargetString?
) {
    val cents = if (pitchState.isSignalDetected) pitchState.centsOffset else 0f
    val isInTune = pitchState.isSignalDetected && abs(cents) <= 3.5f

    val animatedCents by animateFloatAsState(
        targetValue = cents,
        animationSpec = tween(durationMillis = 150),
        label = "CentsAnim"
    )

    val gaugeColor by animateColorAsState(
        targetValue = when {
            !pitchState.isSignalDetected -> TextMuted
            isInTune -> EmeraldGreen
            abs(cents) <= 15f -> AmberAccent
            else -> VividRed
        },
        animationSpec = tween(durationMillis = 200),
        label = "GaugeColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(gaugeColor, StudioCardBorder)
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.72f)
                val gaugeRadius = size.width * 0.38f

                // Gauge Arc
                drawArc(
                    color = StudioCardBorder,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - gaugeRadius, center.y - gaugeRadius),
                    size = Size(gaugeRadius * 2, gaugeRadius * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )

                // In-Tune Center Zone Arc
                drawArc(
                    color = EmeraldGreen.copy(alpha = 0.8f),
                    startAngle = 260f,
                    sweepAngle = 20f,
                    useCenter = false,
                    topLeft = Offset(center.x - gaugeRadius, center.y - gaugeRadius),
                    size = Size(gaugeRadius * 2, gaugeRadius * 2),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )

                // Ticks for -50, -25, 0, +25, +50 cents
                for (c in -50..50 step 10) {
                    val angleDeg = 270f + (c / 50f) * 80f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val innerR = gaugeRadius - (if (c % 25 == 0) 18.dp.toPx() else 10.dp.toPx())
                    val outerR = gaugeRadius - 2.dp.toPx()

                    val startX = center.x + (innerR * cos(rad)).toFloat()
                    val startY = center.y + (innerR * sin(rad)).toFloat()
                    val endX = center.x + (outerR * cos(rad)).toFloat()
                    val endY = center.y + (outerR * sin(rad)).toFloat()

                    drawLine(
                        color = if (c == 0) EmeraldGreen else TextMuted,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (c % 25 == 0) 3.dp.toPx() else 1.5f.dp.toPx()
                    )
                }

                // Meter Needle Pointer
                val needleAngleDeg = 270f + (animatedCents / 50f) * 80f
                val needleRad = Math.toRadians(needleAngleDeg.toDouble())
                val needleLength = gaugeRadius - 14.dp.toPx()

                val needleX = center.x + (needleLength * cos(needleRad)).toFloat()
                val needleY = center.y + (needleLength * sin(needleRad)).toFloat()

                drawLine(
                    color = gaugeColor,
                    start = center,
                    end = Offset(needleX, needleY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = gaugeColor,
                    radius = 8.dp.toPx(),
                    center = center
                )
            }

            // Note Name & Status Text
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pitchState.isSignalDetected) {
                    Text(
                        text = pitchState.noteName,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gaugeColor
                    )
                    Text(
                        text = "${String.format("%.1f", pitchState.frequencyHz)} Hz",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isInTune -> "✓ PERFECTAMENTE AFINADO"
                            cents < 0 -> "▲ AFINAR MÁS ALTO (+${abs(cents.toInt())} Cents)"
                            else -> "▼ AFINAR MÁS BAJO (-${abs(cents.toInt())} Cents)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor
                    )
                } else {
                    Text(
                        text = targetString?.noteName ?: "--",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "Toca una nota frente al micrófono",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
