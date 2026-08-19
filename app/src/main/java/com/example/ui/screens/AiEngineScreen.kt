package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ai.*
import com.example.ui.theme.*

@Composable
fun AiEngineScreen() {
    val aiState by AiRemoteEngine.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Voice Commands, 1: Upscaling & Bandwidth, 2: Gesture AI, 3: OCR & QR Scanner

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Sub-Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PrimaryCyan,
            divider = { HorizontalDivider(color = CardBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Voice Control", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("ai_tab_voice")
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Upscaling & Saver", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.HighQuality, contentDescription = "Upscale") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("ai_tab_upscale")
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Gesture AI", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.TouchApp, contentDescription = "Gesture") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("ai_tab_gesture")
            )

            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("OCR & QR", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "OCR QR") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("ai_tab_ocr")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: AI Voice Remote Command Assistant
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (aiState.isVoiceListening) PrimaryCyan else CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    if (aiState.isVoiceListening) PrimaryCyan.copy(alpha = 0.2f) else SurfaceVariantDark,
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (aiState.isVoiceListening) PrimaryCyan else CardBorder,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic Icon",
                                tint = if (aiState.isVoiceListening) PrimaryCyan else TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (aiState.isVoiceListening) "LISTENING FOR VOICE COMMAND..." else "AI Voice Command Engine Standby",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "Say actions like 'Lock PC', 'Open Chrome', 'Mute Host Volume', 'Switch Profile'",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { AiRemoteEngine.toggleVoiceListening() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (aiState.isVoiceListening) StatusRed else PrimaryCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("toggle_voice_cmd_btn")
                        ) {
                            Icon(
                                imageVector = if (aiState.isVoiceListening) Icons.Default.Stop else Icons.Default.GraphicEq,
                                contentDescription = "Voice",
                                tint = BackgroundDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (aiState.isVoiceListening) "CANCEL VOICE LISTENING" else "START VOICE COMMAND LISTENING",
                                fontWeight = FontWeight.Bold,
                                color = BackgroundDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("RECENT VOICE COMMAND LOGS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(aiState.voiceHistory) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice", tint = PrimaryCyan)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("\"${item.spokenPhrase}\"", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("Mapped: ${item.mappedAction}", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }

                                Badge(containerColor = StatusGreen.copy(alpha = 0.2f), contentColor = StatusGreen) {
                                    Text("${(item.confidence * 100).toInt()}% match", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: AI Super Resolution Upscaling & Neural Bandwidth Saver
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("AI Super Resolution Video Upscaling", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text("Real-time neural tensor upscaling for crisp 4K desktop streams", color = TextMuted, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                AiUpscaleMode.values().forEach { mode ->
                                    FilterChip(
                                        selected = aiState.upscaleMode == mode,
                                        onClick = { AiRemoteEngine.setUpscaleMode(mode) },
                                        label = { Text(mode.label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryCyan,
                                            selectedLabelColor = BackgroundDark,
                                            containerColor = SurfaceVariantDark,
                                            labelColor = TextPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .testTag("chip_upscale_${mode.name}")
                                    )
                                }
                            }
                        }
                    }

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
                                        Icon(Icons.Default.NetworkCheck, contentDescription = "Net", tint = SecondarySky)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("AI Bandwidth Optimization", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("Dynamic codec bitrate adjustment & delta compression", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Switch(
                                        checked = aiState.isBandwidthOptimizationEnabled,
                                        onCheckedChange = { AiRemoteEngine.toggleBandwidthOptimization() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SecondarySky, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_bandwidth_opt_switch")
                                    )
                                }

                                if (aiState.isBandwidthOptimizationEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Optimized Bitrate", color = TextMuted, fontSize = 11.sp)
                                            Text(
                                                text = "${aiState.currentOptimizedBitrateKbps / 1000f} Mbps",
                                                color = PrimaryCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Data Saved", color = TextMuted, fontSize = 11.sp)
                                            Text(
                                                text = "${aiState.networkBandwidthSavedPercent}% Reduced",
                                                color = StatusGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Predictive Gesture Touch Engine
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
                                Icon(Icons.Default.Gesture, contentDescription = "Gesture", tint = PrimaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Sub-Millisecond Gesture Prediction", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text("Predictive touch vector trajectory interpolation", color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            Switch(
                                checked = aiState.isGesturePredictionEnabled,
                                onCheckedChange = { AiRemoteEngine.toggleGesturePrediction() },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                modifier = Modifier.testTag("toggle_gesture_pred_switch")
                            )
                        }

                        if (aiState.isGesturePredictionEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Perceived Touch Latency Reduction", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "-${aiState.touchLatencyReductionMs} ms Zero-Lag Smoothing",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Screen OCR Scanner & QR / Barcode Lookup Engine
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { AiRemoteEngine.triggerOcrScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scan_ocr_btn")
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = "OCR", tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCAN SCREEN OCR", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { AiRemoteEngine.triggerQrBarcodeScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondarySky),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scan_qr_barcode_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR", tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCAN QR / BARCODE", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("EXTRACTED OCR TEXT & CODE RESULTS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(aiState.ocrResults) { ocr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Badge(containerColor = PrimaryCyan.copy(alpha = 0.2f), contentColor = PrimaryCyan) {
                                            Text("OCR TEXT", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }

                                        Text(ocr.detectedLanguage, color = TextMuted, fontSize = 10.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(ocr.extractedText, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("BBox: ${ocr.boundingBox}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        items(aiState.qrBarcodeResults) { qr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, SecondarySky),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Badge(containerColor = SecondarySky.copy(alpha = 0.2f), contentColor = SecondarySky) {
                                            Text(qr.format, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(qr.rawContent, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
