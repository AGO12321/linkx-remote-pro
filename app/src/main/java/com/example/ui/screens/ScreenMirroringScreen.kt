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
import com.example.mirroring.*
import com.example.ui.theme.*

@Composable
fun ScreenMirroringScreen() {
    val engineState by ScreenMirroringEngine.state.collectAsState()
    val config = engineState.config
    val telemetry = engineState.telemetry

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stream Master Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(
                    1.dp,
                    if (engineState.mirroringState == MirroringState.STREAMING) StatusGreen else CardBorder
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
                                    .background(
                                        when (engineState.mirroringState) {
                                            MirroringState.STREAMING -> StatusGreen
                                            MirroringState.INITIALIZING_ENCODER -> StatusYellow
                                            else -> TextMuted
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Android Screen Capture (MediaProjection)",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = engineState.statusMessage,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (engineState.projectionVirtualDisplayId != null) {
                            Badge(containerColor = AccentTeal, contentColor = SecondarySky) {
                                Text(
                                    engineState.projectionVirtualDisplayId!!,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (engineState.mirroringState == MirroringState.STREAMING) {
                                ScreenMirroringEngine.stopProjection()
                            } else {
                                ScreenMirroringEngine.startProjection()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (engineState.mirroringState == MirroringState.STREAMING) StatusRed else PrimaryCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_screen_mirror_btn")
                    ) {
                        Icon(
                            imageVector = if (engineState.mirroringState == MirroringState.STREAMING) Icons.Default.Stop else Icons.Default.CastConnected,
                            contentDescription = "Cast",
                            tint = BackgroundDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (engineState.mirroringState == MirroringState.STREAMING) "STOP MIRRORING STREAM" else "START SCREEN MIRRORING",
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Live Encoder Stream Preview HUD Canvas
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Simulated Screen Stream Canvas Background Grid
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // HUD Overlay Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Cam",
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE STREAM FEED (${config.resolution.width}x${config.resolution.height})",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCyan,
                                    fontSize = 12.sp
                                )
                            }

                            Badge(
                                containerColor = if (engineState.mirroringState == MirroringState.STREAMING) StatusGreen.copy(alpha = 0.2f) else SurfaceVariantDark,
                                contentColor = if (engineState.mirroringState == MirroringState.STREAMING) StatusGreen else TextMuted
                            ) {
                                Text(
                                    text = "${telemetry.actualFps} FPS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // HUD Latency & Bitrate Overlay Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ENCODER LATENCY", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "${telemetry.encoderDelayMs} ms",
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DYNAMIC BITRATE", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "${telemetry.currentBitrateKbps / 1000f} Mbps",
                                    color = SecondarySky,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NETWORK RTT", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "${telemetry.networkRttMs} ms",
                                    color = PrimaryCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DROPPED FRAMES", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "${telemetry.droppedFrames}",
                                    color = if (telemetry.droppedFrames > 10) StatusRed else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Driver telemetry footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HW Driver: ${telemetry.hardwareEncoderName}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Frames Sent: ${telemetry.totalFramesSent}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Video Codec & Hardware Acceleration Configuration
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
                        Text(
                            text = "Video Codec & Hardware Acceleration",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )

                        Badge(containerColor = AccentTeal, contentColor = PrimaryCyan) {
                            Text("MediaCodec HW", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VideoCodec.values().forEach { codec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.codec == codec,
                                onClick = { ScreenMirroringEngine.updateConfig(config.copy(codec = codec)) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan),
                                modifier = Modifier.testTag("codec_radio_${codec.name}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(codec.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("MIME: ${codec.mimeType}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Resolution & Frame Rate Profiles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resolution & Performance Profile",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ResolutionProfile.values().forEach { profile ->
                        FilterChip(
                            selected = config.resolution == profile,
                            onClick = {
                                ScreenMirroringEngine.updateConfig(
                                    config.copy(
                                        resolution = profile,
                                        fps = profile.defaultFps
                                    )
                                )
                            },
                            label = { Text(profile.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCyan,
                                selectedLabelColor = BackgroundDark,
                                containerColor = SurfaceVariantDark,
                                labelColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag("res_profile_${profile.name}")
                        )
                    }
                }
            }
        }

        // Adaptive Bitrate & Latency Controls
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
                        Column {
                            Text(
                                text = "Target Encoding Bitrate",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${config.targetBitrateKbps / 1000} Mbps (${config.targetBitrateKbps} Kbps)",
                                color = SecondarySky,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Adaptive Auto-Tune", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = config.isAdaptiveBitrateEnabled,
                                onCheckedChange = { ScreenMirroringEngine.toggleAdaptiveBitrate() },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                modifier = Modifier.testTag("adaptive_bitrate_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = config.targetBitrateKbps.toFloat(),
                        onValueChange = { ScreenMirroringEngine.setBitrate(it.toInt()) },
                        valueRange = 2000f..25000f,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryCyan,
                            activeTrackColor = PrimaryCyan,
                            inactiveTrackColor = CardBorder
                        ),
                        modifier = Modifier.testTag("bitrate_slider")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ultra Low Latency Mode (Sub-10ms Slices)", color = TextPrimary, fontSize = 13.sp)
                            Text("Disables B-frames and enables immediate slice delivery", color = TextMuted, fontSize = 11.sp)
                        }

                        Switch(
                            checked = config.isUltraLowLatencyMode,
                            onCheckedChange = { ScreenMirroringEngine.toggleUltraLowLatency() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                            modifier = Modifier.testTag("ultra_low_latency_switch")
                        )
                    }
                }
            }
        }

        // Audio Loopback Capture Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Audio Capture & Loopback Streaming",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AudioCaptureMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.audioMode == mode,
                                onClick = { ScreenMirroringEngine.updateConfig(config.copy(audioMode = mode)) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan),
                                modifier = Modifier.testTag("audio_mode_${mode.name}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mode.title, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
