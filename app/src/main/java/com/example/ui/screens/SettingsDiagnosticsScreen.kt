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
import com.example.settings.*
import com.example.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsDiagnosticsScreen() {
    val state by SettingsDiagnosticsEngine.state.collectAsState()
    val perf = state.performance
    val dev = state.devOptions
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Themes & UI, 1: Performance HUD, 2: Developer Options, 3: Diagnostics Test Suite

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
                text = { Text("Themes & UI", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Palette, contentDescription = "Themes") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("set_tab_themes")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Performance", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Speed, contentDescription = "Perf") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("set_tab_perf")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Developer", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.DeveloperMode, contentDescription = "Dev") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("set_tab_dev")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Diagnostics", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Biotech, contentDescription = "Diag") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("set_tab_diag")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Themes & Visual Customization
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
                                Text("App Visual Theme Profiles", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text("Select display mode and color palette", color = TextMuted, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                AppThemeMode.values().forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = state.activeTheme == mode,
                                            onClick = { SettingsDiagnosticsEngine.setTheme(mode) },
                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan),
                                            modifier = Modifier.testTag("theme_radio_${mode.name}")
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(mode.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(mode.description, color = TextMuted, fontSize = 11.sp)
                                        }
                                    }
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
                                        Icon(Icons.Default.Vibration, contentDescription = "Vib", tint = PrimaryCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Haptic Feedback Vibration", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("Tactile feedback on key clicks & trackpad taps", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Switch(
                                        checked = state.isVibrationFeedbackEnabled,
                                        onCheckedChange = { SettingsDiagnosticsEngine.toggleVibration() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_haptics_switch")
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WbSunny, contentDescription = "Screen", tint = SecondarySky)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Keep Screen Awaken", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("Prevent device lock during active remote stream", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Switch(
                                        checked = state.isKeepScreenOnEnabled,
                                        onCheckedChange = { SettingsDiagnosticsEngine.toggleKeepScreenOn() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SecondarySky, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_keep_screen_switch")
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
                                Text("Display Framerate Target (${state.targetFpsCap} FPS)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(30, 60, 90, 120).forEach { fps ->
                                        FilterChip(
                                            selected = state.targetFpsCap == fps,
                                            onClick = { SettingsDiagnosticsEngine.setTargetFpsCap(fps) },
                                            label = { Text("$fps FPS", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryCyan,
                                                selectedLabelColor = BackgroundDark,
                                                containerColor = SurfaceVariantDark,
                                                labelColor = TextPrimary
                                            ),
                                            modifier = Modifier.testTag("fps_cap_$fps")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Real-time System Telemetry & Performance Dashboard
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, PrimaryCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("LIVE PERFORMANCE TELEMETRY", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Badge(containerColor = StatusGreen, contentColor = BackgroundDark) {
                                        Text("${perf.currentFps} FPS", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // CPU & Memory Gauge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("App CPU Usage", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.cpuUsagePercent}%", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
                                    }

                                    Column {
                                        Text("Heap Memory", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.ramUsedMb} / ${perf.totalRamMb} MB", color = SecondarySky, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("GPU Frame Time", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.gpuRenderMs} ms", color = StatusGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column {
                                    Text("CPU Workload Allocation", color = TextMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { perf.cpuUsagePercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = PrimaryCyan,
                                        trackColor = CardBorder
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
                                Text("Network & Protocol Health", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Wi-Fi Latency (RTT)", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.wifiLatencyMs} ms", color = StatusGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                                    }

                                    Column {
                                        Text("Frame Jitter", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.frameJitterMs} ms", color = SecondarySky, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Stream Bandwidth", color = TextMuted, fontSize = 11.sp)
                                        Text("${perf.networkThroughputKbps / 1000f} Mbps", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Developer Options & Protocol Inspector
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
                                Text("Protocol & Listener Ports", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("DirectShow Server TCP Control Port: ${dev.tcpPort}", color = PrimaryCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                Text("UDP Ultra Stream Port: ${dev.udpStreamPort}", color = SecondarySky, fontFamily = FontFamily.Monospace, fontSize = 12.sp)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ADB Wireless Debugging Bridge", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("Allow ADB shell remote commands", color = TextMuted, fontSize = 11.sp)
                                    }

                                    Switch(
                                        checked = dev.isAdbWirelessBridgeEnabled,
                                        onCheckedChange = { SettingsDiagnosticsEngine.toggleAdbWirelessBridge() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_adb_bridge_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Raw Packet PCAP Inspector", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("Dump binary network payload logs to storage", color = TextMuted, fontSize = 11.sp)
                                    }

                                    Switch(
                                        checked = dev.isRawPacketLoggingEnabled,
                                        onCheckedChange = { SettingsDiagnosticsEngine.toggleRawPacketLogging() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_pcap_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Protobuf Message Debugger", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("Print serialized packet structures to logcat", color = TextMuted, fontSize = 11.sp)
                                    }

                                    Switch(
                                        checked = dev.isProtocolBufferDebugMode,
                                        onCheckedChange = { SettingsDiagnosticsEngine.toggleProtocolBufferDebug() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("toggle_protobuf_debug_switch")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Automated Diagnostic Test Runner
                Column(modifier = Modifier.weight(1f)) {
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
                                    Text("System Diagnostics Suite", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text("Run hardware, socket, and video decoder audits", color = TextMuted, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { SettingsDiagnosticsEngine.runFullDiagnosticSuite(context) },
                                    enabled = !state.isDiagnosticsRunning,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                    modifier = Modifier.testTag("run_diag_suite_btn")
                                ) {
                                    if (state.isDiagnosticsRunning) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundDark, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = BackgroundDark)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RUN AUDIT", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("AUDIT TEST RESULTS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.diagnosticResults) { diag ->
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
                                        Icon(
                                            imageVector = when (diag.status) {
                                                "PASSED" -> Icons.Default.CheckCircle
                                                "WARNING" -> Icons.Default.Warning
                                                else -> Icons.Default.HourglassEmpty
                                            },
                                            contentDescription = "Status",
                                            tint = when (diag.status) {
                                                "PASSED" -> StatusGreen
                                                "WARNING" -> StatusYellow
                                                else -> PrimaryCyan
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(diag.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("${diag.category} • ${diag.details}", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }

                                    Badge(
                                        containerColor = when (diag.status) {
                                            "PASSED" -> StatusGreen.copy(alpha = 0.2f)
                                            "WARNING" -> StatusYellow.copy(alpha = 0.2f)
                                            else -> PrimaryCyan.copy(alpha = 0.2f)
                                        },
                                        contentColor = when (diag.status) {
                                            "PASSED" -> StatusGreen
                                            "WARNING" -> StatusYellow
                                            else -> PrimaryCyan
                                        }
                                    ) {
                                        Text(diag.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
