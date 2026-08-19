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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.LinkXClientEngine
import com.example.protocol.FileEntry
import com.example.server.LinkXServerEngine
import com.example.transfer.*
import com.example.ui.theme.*

@Composable
fun FileManagerScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    val transferState by FileTransferEngine.state.collectAsState()
    val clipboardState by ClipboardSyncEngine.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Remote Files, 1: Transfer Queue, 2: Clipboard Sync
    var searchQuery by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var newClipboardInput by remember { mutableStateOf("") }

    val filteredFiles = serverState.fileEntries.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Sub-Navigation Tabs: Files | Transfers (3) | Clipboard
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
                text = { Text("Remote Directory", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_remote_files")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Transfers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (transferState.tasks.any { it.status == TransferStatus.ACTIVE }) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = PrimaryCyan, contentColor = BackgroundDark) {
                                Text("${transferState.tasks.count { it.status == TransferStatus.ACTIVE }}", fontSize = 9.sp)
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.SwapVert, contentDescription = "Transfers") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_transfers")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Clipboard Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_clipboard")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Remote Host File Explorer
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = "Path", tint = PrimaryCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Remote Host Path (Windows 11)", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = serverState.currentDrivePath,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search files or folders...", color = TextMuted) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (actionMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryCyan.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, PrimaryCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = actionMessage!!,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredFiles) { file ->
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
                                        .size(40.dp)
                                        .background(
                                            if (file.isDirectory) SecondarySky.copy(alpha = 0.15f) else PrimaryCyan.copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                        contentDescription = file.name,
                                        tint = if (file.isDirectory) SecondarySky else PrimaryCyan
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = file.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text(
                                        text = if (file.isDirectory) "Directory" else formatFileSize(file.sizeBytes),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                if (!file.isDirectory) {
                                    IconButton(
                                        onClick = {
                                            FileTransferEngine.enqueueTransfer(
                                                fileName = file.name,
                                                localPath = "/sdcard/Download/${file.name}",
                                                remotePath = file.path,
                                                fileSizeBytes = file.sizeBytes,
                                                direction = TransferDirection.DOWNLOAD_FROM_HOST
                                            )
                                            actionMessage = "Queued download: ${file.name}"
                                        },
                                        modifier = Modifier.testTag("download_file_${file.name}")
                                    ) {
                                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Download", tint = PrimaryCyan)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        actionMessage = "Opening '${file.name}' on Windows PC"
                                        LinkXClientEngine.sendCmd("start \"\" \"${file.path}\"")
                                    },
                                    modifier = Modifier.testTag("open_file_${file.name}")
                                ) {
                                    Icon(imageVector = Icons.Default.Launch, contentDescription = "Open", tint = SecondarySky)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: High-Speed File Transfer Engine Queue & AES-256 HUD
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = "Speed", tint = PrimaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Transfer Engine HUD", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text("Unlimited File Size | 1MB Slices", color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AES-256 GCM", fontSize = 10.sp, color = StatusGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = transferState.isAES256GCMEnabled,
                                    onCheckedChange = { FileTransferEngine.toggleAES256Encryption() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StatusGreen, checkedTrackColor = StatusGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.testTag("aes_encryption_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UPLOAD: ${formatFileSize(transferState.totalUploadSpeedBytesPerSec)}/s",
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "DOWNLOAD: ${formatFileSize(transferState.totalDownloadSpeedBytesPerSec)}/s",
                                color = SecondarySky,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ACTIVE QUEUE (${transferState.tasks.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                    TextButton(
                        onClick = { FileTransferEngine.clearCompleted() },
                        modifier = Modifier.testTag("clear_completed_transfers_btn")
                    ) {
                        Text("Clear Completed", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(transferState.tasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(
                                1.dp,
                                when (task.status) {
                                    TransferStatus.ACTIVE -> PrimaryCyan
                                    TransferStatus.COMPLETED -> StatusGreen
                                    TransferStatus.PAUSED -> StatusYellow
                                    else -> CardBorder
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (task.direction == TransferDirection.UPLOAD_TO_HOST) Icons.Default.FileUpload else Icons.Default.FileDownload,
                                            contentDescription = "Dir",
                                            tint = if (task.direction == TransferDirection.UPLOAD_TO_HOST) PrimaryCyan else SecondarySky
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(task.fileName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("${formatFileSize(task.bytesTransferred)} / ${formatFileSize(task.fileSizeBytes)}", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Badge(
                                        containerColor = when (task.status) {
                                            TransferStatus.ACTIVE -> PrimaryCyan.copy(alpha = 0.2f)
                                            TransferStatus.COMPLETED -> StatusGreen.copy(alpha = 0.2f)
                                            TransferStatus.PAUSED -> StatusYellow.copy(alpha = 0.2f)
                                            else -> SurfaceVariantDark
                                        },
                                        contentColor = when (task.status) {
                                            TransferStatus.ACTIVE -> PrimaryCyan
                                            TransferStatus.COMPLETED -> StatusGreen
                                            TransferStatus.PAUSED -> StatusYellow
                                            else -> TextMuted
                                        }
                                    ) {
                                        Text(task.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val progressFloat = if (task.fileSizeBytes > 0) task.bytesTransferred.toFloat() / task.fileSizeBytes.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = if (task.status == TransferStatus.COMPLETED) StatusGreen else PrimaryCyan,
                                    trackColor = CardBorder
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (task.status == TransferStatus.ACTIVE) "${formatFileSize(task.speedBytesPerSec)}/s | ETA ${task.etaSeconds}s" else "Checkpoint Offset: ${formatFileSize(task.lastCheckpointOffset)}",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Row {
                                        if (task.status == TransferStatus.ACTIVE) {
                                            IconButton(
                                                onClick = { FileTransferEngine.pauseTransfer(task.id) },
                                                modifier = Modifier.size(28.dp).testTag("pause_transfer_${task.id}")
                                            ) {
                                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = StatusYellow, modifier = Modifier.size(18.dp))
                                            }
                                        } else if (task.status == TransferStatus.PAUSED) {
                                            IconButton(
                                                onClick = { FileTransferEngine.resumeTransfer(task.id) },
                                                modifier = Modifier.size(28.dp).testTag("resume_transfer_${task.id}")
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = StatusGreen, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        IconButton(
                                            onClick = { FileTransferEngine.cancelTransfer(task.id) },
                                            modifier = Modifier.size(28.dp).testTag("cancel_transfer_${task.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = StatusRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Live Bidirectional Clipboard Synchronization Hub
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SyncAlt, contentDescription = "Sync", tint = PrimaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Clipboard Synchronization", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text(clipboardState.syncStatusMessage, color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Auto Sync", fontSize = 10.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = clipboardState.isAutoSyncEnabled,
                                    onCheckedChange = { ClipboardSyncEngine.toggleAutoSync() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                    modifier = Modifier.testTag("auto_clipboard_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newClipboardInput,
                                onValueChange = { newClipboardInput = it },
                                placeholder = { Text("Enter text or URL to sync with Windows...", color = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clipboard_input_field")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    ClipboardSyncEngine.sendToWindowsHost(newClipboardInput)
                                    newClipboardInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("push_clipboard_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Push", tint = BackgroundDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CLIPBOARD HISTORY", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                    TextButton(
                        onClick = { ClipboardSyncEngine.clearHistory() },
                        modifier = Modifier.testTag("clear_clipboard_history_btn")
                    ) {
                        Text("Clear History", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(clipboardState.history) { item ->
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
                                        .background(PrimaryCyan.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.type) {
                                            ClipboardType.URL -> Icons.Default.Link
                                            ClipboardType.CODE_SNIPPET -> Icons.Default.Code
                                            else -> Icons.Default.Notes
                                        },
                                        contentDescription = "Type",
                                        tint = PrimaryCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.content,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "${item.sourceDevice} • ${item.type.label} (${item.charCount} chars)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(item.content))
                                    },
                                    modifier = Modifier.testTag("copy_clip_${item.id}")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SecondarySky)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024
    if (kb < 1024) return "$kb KB"
    val mb = kb / 1024
    if (mb < 1024) return "$mb MB"
    val gb = mb / 1024
    return "$gb GB"
}
