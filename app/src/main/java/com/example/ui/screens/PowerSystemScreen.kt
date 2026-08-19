package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.LinkXClientEngine
import com.example.protocol.PowerAction
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*

@Composable
fun PowerSystemScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    var pendingAction by remember { mutableStateOf<PowerAction?>(null) }

    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("Confirm Power Action", color = TextPrimary) },
            text = { Text("Are you sure you want to execute '${pendingAction?.name}' on ${serverState.serverInfo.serverName}?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction?.let { LinkXClientEngine.sendPowerAction(it) }
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("EXECUTE", color = TextPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingAction = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // System Hardware Metrics Dashboard
        Text(text = "Target System Metrics", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("CPU LOAD", "${serverState.metrics.cpuUsagePct}%", Icons.Default.Memory, PrimaryCyan, Modifier.weight(1f))
            MetricCard("RAM USED", "${serverState.metrics.ramUsagePct}%", Icons.Default.Storage, SecondarySky, Modifier.weight(1f))
            MetricCard("BATTERY", "${serverState.metrics.batteryPct}%", Icons.Default.BatteryChargingFull, StatusGreen, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Computer, contentDescription = "PC", tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = serverState.serverInfo.serverName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "OS: ${serverState.serverInfo.osVersion}", color = TextSecondary, fontSize = 12.sp)
                Text(text = "Active User: ${serverState.serverInfo.activeUser}", color = TextSecondary, fontSize = 12.sp)
                Text(text = "Active Window: ${serverState.activeWindow}", color = TextSecondary, fontSize = 12.sp)
                Text(text = "System Uptime: ${serverState.metrics.uptimeHours} hours", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Power Commands Grid
        Text(text = "System Power Operations", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PowerActionButton(
                    title = "Lock PC",
                    subtitle = "Lock Workstation",
                    icon = Icons.Default.Lock,
                    accentColor = PrimaryCyan,
                    modifier = Modifier.weight(1f).testTag("power_lock_btn")
                ) {
                    LinkXClientEngine.sendPowerAction(PowerAction.LOCK_WORKSTATION)
                }

                PowerActionButton(
                    title = "Sleep Mode",
                    subtitle = "Low Power Standby",
                    icon = Icons.Default.NightsStay,
                    accentColor = SecondarySky,
                    modifier = Modifier.weight(1f).testTag("power_sleep_btn")
                ) {
                    pendingAction = PowerAction.SLEEP
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PowerActionButton(
                    title = "Restart",
                    subtitle = "Reboot System",
                    icon = Icons.Default.RestartAlt,
                    accentColor = StatusYellow,
                    modifier = Modifier.weight(1f).testTag("power_restart_btn")
                ) {
                    pendingAction = PowerAction.RESTART
                }

                PowerActionButton(
                    title = "Shutdown",
                    subtitle = "Power Off Host",
                    icon = Icons.Default.PowerSettingsNew,
                    accentColor = StatusRed,
                    modifier = Modifier.weight(1f).testTag("power_shutdown_btn")
                ) {
                    pendingAction = PowerAction.SHUTDOWN
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            Text(text = label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
fun PowerActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
