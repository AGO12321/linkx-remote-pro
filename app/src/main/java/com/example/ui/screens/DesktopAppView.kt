package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.server.CommandMapping
import com.example.server.ExecutionStatus
import com.example.server.LinkXServerEngine
import com.example.server.WindowsConnectionStatus
import com.example.ui.theme.*

@Composable
fun DesktopAppView() {
    val serverState by LinkXServerEngine.state.collectAsState()
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var showAddMappingDialog by remember { mutableStateOf(false) }
    var editingMapping by remember { mutableStateOf<CommandMapping?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Simulated Windows Desktop Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, PrimaryCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DesktopWindows, contentDescription = "Windows PC", tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Windows 11 PC Desktop Receiver", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "ws://${serverState.serverInfo.ipAddress}:${serverState.serverInfo.port} • Paired: ${serverState.connectedDeviceName}", color = SecondarySky, fontSize = 11.sp)
                    }
                }

                val statusColor = when (serverState.connectionStatus) {
                    WindowsConnectionStatus.PAIRED_CONNECTED -> StatusGreen
                    WindowsConnectionStatus.RECONNECTING -> StatusYellow
                    WindowsConnectionStatus.LISTENING -> SecondarySky
                    WindowsConnectionStatus.DISCONNECTED -> StatusRed
                }

                Badge(
                    containerColor = statusColor,
                    contentColor = BackgroundDark
                ) {
                    Text(
                        text = serverState.connectionStatus.name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-Tab Navigation Bar
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SurfaceDark,
            contentColor = PrimaryCyan,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = selectedSubTab == 0, onClick = { selectedSubTab = 0 }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tv, contentDescription = "Canvas", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Desktop Canvas", fontSize = 11.sp, color = if (selectedSubTab == 0) PrimaryCyan else TextSecondary)
                }
            }
            Tab(selected = selectedSubTab == 1, onClick = { selectedSubTab = 1 }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AltRoute, contentDescription = "Mapper", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Command Mapper (${serverState.customMappings.size})", fontSize = 11.sp, color = if (selectedSubTab == 1) PrimaryCyan else TextSecondary)
                }
            }
            Tab(selected = selectedSubTab == 2, onClick = { selectedSubTab = 2 }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Feedback", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Real-Time Feedback", fontSize = 11.sp, color = if (selectedSubTab == 2) PrimaryCyan else TextSecondary)
                }
            }
            Tab(selected = selectedSubTab == 3, onClick = { selectedSubTab = 3 }) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.WifiTethering, contentDescription = "Conn Manager", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connection Manager", fontSize = 11.sp, color = if (selectedSubTab == 3) PrimaryCyan else TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (serverState.isLocked) {
            // Security Lock Screen
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, StatusYellow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = StatusYellow, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Windows Host Workstation Locked", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Enter Security PIN to unlock desktop view (Default PIN: 1234)", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it; pinError = false },
                        placeholder = { Text("PIN", color = TextMuted) },
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusYellow,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.width(180.dp).testTag("pin_unlock_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (!LinkXServerEngine.unlockHost(pinInput)) {
                                pinError = true
                            } else {
                                pinInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusYellow),
                        modifier = Modifier.testTag("unlock_pin_btn")
                    ) {
                        Text("UNLOCK DESKTOP", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            when (selectedSubTab) {
                0 -> DesktopCanvasView(serverState)
                1 -> CommandMapperView(
                    serverState = serverState,
                    onAddClick = {
                        editingMapping = null
                        showAddMappingDialog = true
                    },
                    onEditClick = { mapping ->
                        editingMapping = mapping
                        showAddMappingDialog = true
                    }
                )
                2 -> RealtimeFeedbackView(serverState)
                3 -> ConnectionManagerView(serverState)
            }
        }
    }

    if (showAddMappingDialog) {
        CommandMappingDialog(
            existing = editingMapping,
            onDismiss = { showAddMappingDialog = false },
            onSave = { mapping ->
                if (editingMapping != null) {
                    LinkXServerEngine.updateCommandMapping(mapping)
                } else {
                    LinkXServerEngine.addCommandMapping(mapping)
                }
                showAddMappingDialog = false
            }
        )
    }
}

@Composable
fun DesktopCanvasView(serverState: com.example.server.WindowsServerState) {
    val clientState by com.example.client.LinkXClientEngine.connectionState.collectAsState()
    val screenBmp by com.example.client.LinkXClientEngine.screenStream.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Just for layout purposes, might need dynamic scaling
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (screenBmp != null) {
                    androidx.compose.foundation.Image(
                        bitmap = screenBmp!!.asImageBitmap(),
                        contentDescription = "Live Screen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("No Screen Signal", color = TextMuted)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .align(Alignment.TopStart)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Dvr, contentDescription = "App", tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Live Stream - Windows PC", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Live Protocol Message Inspector", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                items(serverState.packetLogs) { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (log.contains("ACTION")) SecondarySky else TextPrimary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommandMapperView(
    serverState: com.example.server.WindowsServerState,
    onAddClick: () -> Unit,
    onEditClick: (CommandMapping) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Windows Action Mappings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Map Android commands to custom Windows apps, hotkeys, or scripts", color = TextSecondary, fontSize = 11.sp)
            }

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_mapping_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD MAPPING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(serverState.customMappings) { mapping ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (mapping.isEnabled) PrimaryCyan.copy(alpha = 0.5f) else CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = mapping.isEnabled,
                                    onCheckedChange = { LinkXServerEngine.toggleCommandMapping(mapping.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimaryCyan,
                                        checkedTrackColor = AccentTeal
                                    ),
                                    modifier = Modifier.testTag("toggle_map_${mapping.id}")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(text = mapping.trigger, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = mapping.description, color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            Badge(
                                containerColor = AccentTeal,
                                contentColor = SecondarySky
                            ) {
                                Text(mapping.actionType, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target: ${mapping.targetValue}",
                                fontFamily = FontFamily.Monospace,
                                color = SecondarySky,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Row {
                                IconButton(
                                    onClick = { /* Coming soon */ },
                                    modifier = Modifier.size(32.dp).testTag("test_map_${mapping.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test Run", tint = StatusGreen)
                                }

                                IconButton(
                                    onClick = { onEditClick(mapping) },
                                    modifier = Modifier.size(32.dp).testTag("edit_map_${mapping.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
                                }

                                IconButton(
                                    onClick = { LinkXServerEngine.deleteCommandMapping(mapping.id) },
                                    modifier = Modifier.size(32.dp).testTag("delete_map_${mapping.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeFeedbackView(serverState: com.example.server.WindowsServerState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LATENCY", color = TextMuted, fontSize = 10.sp)
                    Text("${serverState.pingLatencyMs} ms", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL EVENTS", color = TextMuted, fontSize = 10.sp)
                    Text("${serverState.realTimeEvents.size}", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXECUTION RATE", color = TextMuted, fontSize = 10.sp)
                    Text("100%", color = SecondarySky, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Live Command Execution Feed", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(serverState.realTimeEvents) { evt ->
                val statusColor = when (evt.status) {
                    ExecutionStatus.RECEIVED -> SecondarySky
                    ExecutionStatus.EXECUTING -> StatusYellow
                    ExecutionStatus.SUCCESS -> StatusGreen
                    ExecutionStatus.FAILED -> StatusRed
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(statusColor.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, statusColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (evt.status == ExecutionStatus.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Sync,
                                contentDescription = "Status",
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = evt.trigger, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Text(text = evt.timestamp, color = TextMuted, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = evt.details, color = TextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Badge(containerColor = SurfaceVariantDark, contentColor = statusColor) {
                            Text("${evt.latencyMs}ms", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionManagerView(serverState: com.example.server.WindowsServerState) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                    Column {
                        Text("Connection Manager", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Active Pairing Engine & Protocol State", color = TextSecondary, fontSize = 11.sp)
                    }

                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = "Wifi",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Reconnect on Disconnect", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = serverState.autoReconnectEnabled,
                        onCheckedChange = { LinkXServerEngine.toggleAutoReconnect() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                        modifier = Modifier.testTag("auto_reconnect_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Paired Client Device:", color = TextMuted, fontSize = 12.sp)
                    Text(serverState.connectedDeviceName, color = SecondarySky, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("WebSocket Socket Address:", color = TextMuted, fontSize = 12.sp)
                    Text("ws://${serverState.serverInfo.ipAddress}:8080", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Coming soon */ },
                        border = BorderStroke(1.dp, StatusRed),
                        modifier = Modifier.weight(1f).testTag("sim_disconnect_btn")
                    ) {
                        Text("SIMULATE DISCONNECT", color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { /* Coming soon */ },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        modifier = Modifier.weight(1f).testTag("sim_reconnect_btn")
                    ) {
                        Text("TRIGGER RECONNECT", color = BackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CommandMappingDialog(
    existing: CommandMapping?,
    onDismiss: () -> Unit,
    onSave: (CommandMapping) -> Unit
) {
    var trigger by remember { mutableStateOf(existing?.trigger ?: "") }
    var actionType by remember { mutableStateOf(existing?.actionType ?: "Launch App") }
    var targetValue by remember { mutableStateOf(existing?.targetValue ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }

    val actionTypes = listOf("Launch App", "Keyboard Shortcut", "Media Control", "System Power", "Shell Script")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existing == null) "Create Command Mapping" else "Edit Command Mapping", color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Trigger Name (e.g. MUTE_MIC)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("mapping_trigger_input")
                )

                Text("Select Action Type:", color = TextSecondary, fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    actionTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = actionType == type,
                            onClick = { actionType = type },
                            label = { Text(type, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryCyan, selectedLabelColor = BackgroundDark)
                        )
                    }
                }

                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Action (e.g. chrome.exe, CTRL+SHIFT+M)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("mapping_target_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Friendly Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("mapping_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (trigger.isNotEmpty() && targetValue.isNotEmpty()) {
                        val mapping = CommandMapping(
                            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                            trigger = trigger,
                            actionType = actionType,
                            targetValue = targetValue,
                            description = description,
                            isEnabled = existing?.isEnabled ?: true
                        )
                        onSave(mapping)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                modifier = Modifier.testTag("save_mapping_btn")
            ) {
                Text("SAVE MAPPING", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

