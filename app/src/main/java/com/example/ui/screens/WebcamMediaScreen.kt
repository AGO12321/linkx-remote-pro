package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.*
import com.example.ui.theme.*

@Composable
fun WebcamMediaScreen() {
    val mediaState by WebcamAudioEngine.state.collectAsState()
    val webcam = mediaState.webcam
    val audio = mediaState.audio

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Webcam Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(
                    1.dp,
                    if (webcam.isWebcamActive) StatusGreen else CardBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(if (webcam.isWebcamActive) StatusGreen else TextMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Phone as PC Webcam (DirectShow OBS)",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = mediaState.statusMessage,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (webcam.isWebcamActive) {
                            Badge(containerColor = PrimaryCyan.copy(alpha = 0.2f), contentColor = PrimaryCyan) {
                                Text("${mediaState.currentFps} FPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (webcam.isWebcamActive) {
                                WebcamAudioEngine.stopWebcam()
                            } else {
                                WebcamAudioEngine.startWebcam()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (webcam.isWebcamActive) StatusRed else PrimaryCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_webcam_btn")
                    ) {
                        Icon(
                            imageVector = if (webcam.isWebcamActive) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Cam",
                            tint = BackgroundDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (webcam.isWebcamActive) "STOP WEBCAM STREAM" else "START WEBCAM STREAM",
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Camera Feed Canvas & HUD Overlay
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (webcam.isWebcamActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(containerColor = StatusGreen, contentColor = BackgroundDark) {
                                    Text("LIVE STREAMING", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (webcam.isBokehBlurEnabled) {
                                        Badge(containerColor = SecondarySky.copy(alpha = 0.2f), contentColor = SecondarySky) {
                                            Text("Bokeh Blur", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                    if (webcam.isAutoFramingEnabled) {
                                        Badge(containerColor = PrimaryCyan.copy(alpha = 0.2f), contentColor = PrimaryCyan) {
                                            Text("AI Framing", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.CenterFocusWeak,
                                contentDescription = "Focus",
                                tint = PrimaryCyan.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(72.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sensor: ${webcam.cameraSource.displayName}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${mediaState.videoBitrateKbps / 1000f} Mbps | ${mediaState.audioLatencyMs}ms",
                                    color = PrimaryCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cam Standby",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera Feed Standby", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Camera Sensor & Quick Toggles (Flashlight, Bokeh, Framing)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Camera Sensor & AI Optics", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    CameraSource.values().forEach { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = webcam.cameraSource == source,
                                onClick = { WebcamAudioEngine.updateCameraSource(source) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan),
                                modifier = Modifier.testTag("camera_source_${source.name}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(source.displayName, color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterChip(
                            selected = webcam.isTorchOn,
                            onClick = { WebcamAudioEngine.toggleTorch() },
                            label = { Text("Flashlight") },
                            leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = "Flash") },
                            modifier = Modifier.testTag("toggle_torch_chip")
                        )

                        FilterChip(
                            selected = webcam.isBokehBlurEnabled,
                            onClick = { WebcamAudioEngine.toggleBokehBlur() },
                            label = { Text("Portrait Blur") },
                            leadingIcon = { Icon(Icons.Default.BlurOn, contentDescription = "Blur") },
                            modifier = Modifier.testTag("toggle_bokeh_chip")
                        )

                        FilterChip(
                            selected = webcam.isAutoFramingEnabled,
                            onClick = { WebcamAudioEngine.toggleAutoFraming() },
                            label = { Text("AI Framing") },
                            leadingIcon = { Icon(Icons.Default.CropFree, contentDescription = "Framing") },
                            modifier = Modifier.testTag("toggle_framing_chip")
                        )
                    }
                }
            }
        }

        // Resolution & FPS Profiles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Video Output Format", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    CameraResolution.values().forEach { res ->
                        FilterChip(
                            selected = webcam.resolution == res,
                            onClick = { WebcamAudioEngine.updateResolution(res) },
                            label = { Text(res.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCyan,
                                selectedLabelColor = BackgroundDark,
                                containerColor = SurfaceVariantDark,
                                labelColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag("cam_res_${res.name}")
                        )
                    }
                }
            }
        }

        // Microphone & PC Speaker Audio Streaming Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic", tint = PrimaryCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Microphone Stream to PC", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        }

                        Switch(
                            checked = audio.isMicActive,
                            onCheckedChange = { WebcamAudioEngine.toggleMicrophone() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                            modifier = Modifier.testTag("mic_stream_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Mic Gain (+${audio.micGainDb} dB)", color = SecondarySky, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = audio.micGainDb,
                        onValueChange = { WebcamAudioEngine.setMicGain(it) },
                        valueRange = 0f..18f,
                        colors = SliderDefaults.colors(thumbColor = PrimaryCyan, activeTrackColor = PrimaryCyan),
                        modifier = Modifier.testTag("mic_gain_slider")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = SecondarySky)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PC Speaker Pass-Through to Phone", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        }

                        Switch(
                            checked = audio.isSpeakerActive,
                            onCheckedChange = { WebcamAudioEngine.toggleSpeaker() },
                            colors = SwitchDefaults.colors(checkedThumbColor = SecondarySky, checkedTrackColor = AccentTeal),
                            modifier = Modifier.testTag("speaker_passthrough_switch")
                        )
                    }
                }
            }
        }
    }
}
