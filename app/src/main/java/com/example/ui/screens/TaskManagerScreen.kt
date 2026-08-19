package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*

@Composable
fun TaskManagerScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    var newTaskCommand by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Top New Task Action Row
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Launch New Windows Task", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTaskCommand,
                        onValueChange = { newTaskCommand = it },
                        placeholder = { Text("e.g. notepad.exe, chrome.exe", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("new_task_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTaskCommand.isNotEmpty()) {
                                LinkXClientEngine.sendCmd("start $newTaskCommand")
                                newTaskCommand = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("run_task_btn")
                    ) {
                        Text("RUN")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Active Processes (${serverState.processList.size})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "CPU / RAM", color = TextMuted, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(serverState.processList) { proc ->
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
                                .background(PrimaryCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Dvr, contentDescription = proc.name, tint = PrimaryCyan)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = proc.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(text = "PID: ${proc.pid}", color = TextMuted, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${proc.cpuUsagePct}% CPU", color = SecondarySky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${proc.memoryMb} MB", color = TextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { LinkXClientEngine.killProcess(proc.pid) },
                            modifier = Modifier.testTag("kill_proc_${proc.pid}")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "End Task", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}
