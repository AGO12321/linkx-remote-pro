package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.LinkXClientEngine
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    var cmdInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val quickCommands = listOf("ipconfig", "dir", "systeminfo", "whoami", "hostname", "netstat -an")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Console Output Screen
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, PrimaryCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = "Terminal", tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Windows PowerShell Remote Console", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(serverState.cmdOutputLogs) { line ->
                        Text(
                            text = line,
                            color = if (line.startsWith("PS")) SecondarySky else TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Command Pills
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            quickCommands.take(4).forEach { cmd ->
                OutlinedButton(
                    onClick = {
                        LinkXClientEngine.sendCmd(cmd)
                        scope.launch { listState.animateScrollToItem(serverState.cmdOutputLogs.size) }
                    },
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("quick_cmd_$cmd")
                ) {
                    Text(cmd, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Command Entry Row
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                placeholder = { Text("Enter PowerShell / CMD command...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("cmd_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (cmdInput.isNotEmpty()) {
                        LinkXClientEngine.sendCmd(cmdInput)
                        cmdInput = ""
                        scope.launch { listState.animateScrollToItem(serverState.cmdOutputLogs.size) }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("execute_cmd_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
            }
        }
    }
}
