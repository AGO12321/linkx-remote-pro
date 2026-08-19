package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.LinkXClientEngine
import com.example.ui.theme.*

data class NavigationTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val connectionState by LinkXClientEngine.connectionState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var ipInput by remember { mutableStateOf(connectionState.serverIp) }
    var isConnectionBarExpanded by remember { mutableStateOf(false) }

    val navTabs = listOf(
        NavigationTabItem("Trackpad", Icons.Default.Mouse, "nav_trackpad"),
        NavigationTabItem("Keyboard", Icons.Default.Keyboard, "nav_keyboard"),
        NavigationTabItem("Media", Icons.Default.PlayCircle, "nav_media"),
        NavigationTabItem("Power", Icons.Default.PowerSettingsNew, "nav_power"),
        NavigationTabItem("Files", Icons.Default.Folder, "nav_files"),
        NavigationTabItem("Tasks", Icons.Default.Dvr, "nav_tasks"),
        NavigationTabItem("Terminal", Icons.Default.Terminal, "nav_terminal"),
        NavigationTabItem("Presenter", Icons.Default.Slideshow, "nav_presenter"),
        NavigationTabItem("Game Pad", Icons.Default.SportsEsports, "nav_gamepad"),
        NavigationTabItem("Screen Mirror", Icons.Default.CastConnected, "nav_screen_mirror"),
        NavigationTabItem("Webcam & Mic", Icons.Default.Videocam, "nav_webcam"),
        NavigationTabItem("Calls & Notifs", Icons.Default.PhoneCallback, "nav_telephony"),
        NavigationTabItem("Security & E2EE", Icons.Default.Security, "nav_security"),
        NavigationTabItem("AI Engine", Icons.Default.AutoAwesome, "nav_ai_engine"),
        NavigationTabItem("Settings & Dev", Icons.Default.Settings, "nav_settings_dev"),
        NavigationTabItem("Windows PC View", Icons.Default.DesktopWindows, "nav_desktop_view"),
        NavigationTabItem("Get Windows App", Icons.Default.Code, "nav_get_app")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsRemote,
                                contentDescription = "Logo",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LinkX Remote Pro",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (connectionState.isConnected) StatusGreen else StatusRed,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (connectionState.isConnected)
                                        "Connected (${connectionState.latencyMs}ms)"
                                    else "Disconnected",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isConnectionBarExpanded = !isConnectionBarExpanded },
                        modifier = Modifier.testTag("toggle_connection_bar")
                    ) {
                        Icon(
                            imageVector = if (isConnectionBarExpanded) Icons.Default.ExpandLess else Icons.Default.Wifi,
                            contentDescription = "Connection",
                            tint = if (connectionState.isConnected) PrimaryCyan else TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expandable Connection Manager Bar
            if (isConnectionBarExpanded) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Target Windows Host IP & Port",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = ipInput,
                                onValueChange = { ipInput = it },
                                placeholder = { Text("192.168.1.105") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ip_input_field")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (connectionState.isConnected) {
                                        LinkXClientEngine.disconnect()
                                    } else {
                                        LinkXClientEngine.connect(ipInput)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (connectionState.isConnected) StatusRed else PrimaryCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("connect_toggle_btn")
                            ) {
                                Text(if (connectionState.isConnected) "DISCONNECT" else "CONNECT")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { LinkXClientEngine.searchServers() },
                                border = BorderStroke(1.dp, SecondarySky),
                                modifier = Modifier.testTag("auto_discover_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "Search",
                                    tint = SecondarySky,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AUTO-DISCOVER HOSTS", color = SecondarySky, fontSize = 11.sp)
                            }

                            if (connectionState.isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SecondarySky,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Tab Selection Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceDark,
                contentColor = PrimaryCyan,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                navTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.testTag(tab.testTag)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selectedTabIndex == index) PrimaryCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = if (selectedTabIndex == index) PrimaryCyan else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Screen Content View Switcher
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Crossfade(targetState = selectedTabIndex, label = "ScreenTransition") { screenIndex ->
                    when (screenIndex) {
                        0 -> TrackpadScreen()
                        1 -> KeyboardScreen()
                        2 -> MediaRemoteScreen()
                        3 -> PowerSystemScreen()
                        4 -> FileManagerScreen()
                        5 -> TaskManagerScreen()
                        6 -> TerminalScreen()
                        7 -> PresenterScreen()
                        8 -> GameModeScreen()
                        9 -> ScreenMirroringScreen()
                        10 -> WebcamMediaScreen()
                        11 -> TelephonyNotificationScreen()
                        12 -> SecurityScreen()
                        13 -> AiEngineScreen()
                        14 -> SettingsDiagnosticsScreen()
                        15 -> DesktopAppView()
                        16 -> ExportDesktopAppScreen()
                        else -> TrackpadScreen()
                    }
                }
            }
        }
    }
}
