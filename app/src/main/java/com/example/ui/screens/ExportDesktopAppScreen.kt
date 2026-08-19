package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.desktop_code.WindowsDesktopAppGenerator
import com.example.ui.theme.*

@Composable
fun ExportDesktopAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val codeText = when (selectedTab) {
        0 -> WindowsDesktopAppGenerator.pythonServerScript
        1 -> WindowsDesktopAppGenerator.batchLauncherScript
        2 -> WindowsDesktopAppGenerator.cSharpWpfSourceCode
        else -> ""
    }

    val fileName = when (selectedTab) {
        0 -> "linkx_remote_server.py"
        1 -> "start_linkx_server.bat"
        2 -> "LinkXServer.cs"
        else -> "Setup Guide"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = "Code", tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Windows Desktop Application Generator",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Copy & run this exact server script on any Windows 10/11 PC to allow LinkX Remote Pro to control your desktop.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Code Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PrimaryCyan,
            edgePadding = 0.dp
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Python Host (.py)", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 0) PrimaryCyan else TextSecondary)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Start Script (.bat)", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 1) PrimaryCyan else TextSecondary)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("C# .NET Host (.cs)", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 2) PrimaryCyan else TextSecondary)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Setup Guide", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 3) PrimaryCyan else TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 3) {
            // Setup Guide
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text("LinkX Remote Pro Windows Setup Guide", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        SetupStep("1. Install Python 3.9+", "Download Python from python.org and check 'Add Python to PATH' during installation.")
                        SetupStep("2. Save the Host Script", "Copy 'linkx_remote_server.py' and 'start_linkx_server.bat' into a folder on your Windows PC.")
                        SetupStep("3. Launch the Server", "Double-click 'start_linkx_server.bat'. It automatically installs websockets and starts the server on port 8080.")
                        SetupStep("4. Configure Windows Firewall", "If prompted, allow Python or LinkX on Private Networks through Windows Defender Firewall.")
                        SetupStep("5. Connect Android App", "Ensure your phone and PC are on the same Wi-Fi network, enter your PC's IP address in the app connection bar, and tap Connect!")
                    }
                }
            }
        } else {
            // Code Viewer Container
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, PrimaryCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = "File", tint = SecondarySky)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = fileName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(fileName, codeText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "$fileName copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("copy_code_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = codeText,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupStep(stepNum: String, stepText: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = stepNum, fontWeight = FontWeight.Bold, color = SecondarySky, fontSize = 14.sp)
        Text(text = stepText, color = TextSecondary, fontSize = 13.sp)
    }
}
