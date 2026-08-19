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
import com.example.telephony.*
import com.example.ui.theme.*

@Composable
fun TelephonyNotificationScreen() {
    val state by NotificationTelephonyEngine.state.collectAsState()
    val activeCall = state.activeCall

    var selectedTab by remember { mutableStateOf(0) } // 0: Notifications, 1: SMS Threads, 2: Calls & Dialer
    var replyInput by remember { mutableStateOf("") }
    var dialerInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Active Call Banner overlay if call is ringing or active
        if (activeCall.status != CallStatus.IDLE) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (activeCall.status) {
                        CallStatus.INCOMING_RINGING -> StatusYellow.copy(alpha = 0.2f)
                        CallStatus.ACTIVE_CALL -> StatusGreen.copy(alpha = 0.2f)
                        CallStatus.OUTGOING_DIALING -> PrimaryCyan.copy(alpha = 0.2f)
                        else -> SurfaceDark
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    when (activeCall.status) {
                        CallStatus.INCOMING_RINGING -> StatusYellow
                        CallStatus.ACTIVE_CALL -> StatusGreen
                        CallStatus.OUTGOING_DIALING -> PrimaryCyan
                        else -> CardBorder
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = when (activeCall.status) {
                                    CallStatus.INCOMING_RINGING -> StatusYellow
                                    CallStatus.ACTIVE_CALL -> StatusGreen
                                    else -> PrimaryCyan
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeCall.contactName,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${activeCall.phoneNumber} • ${activeCall.status.name}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (activeCall.status == CallStatus.ACTIVE_CALL) {
                            Text(
                                text = "${activeCall.durationSeconds / 60}:${(activeCall.durationSeconds % 60).toString().padStart(2, '0')}",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeCall.status == CallStatus.INCOMING_RINGING) {
                            Button(
                                onClick = { /* Coming soon */ },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("accept_call_btn")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Accept", tint = BackgroundDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ACCEPT", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (activeCall.status == CallStatus.ACTIVE_CALL) {
                            IconButton(
                                onClick = { NotificationTelephonyEngine.toggleMuteCall() },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("mute_call_btn")
                            ) {
                                Icon(
                                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (activeCall.isMuted) StatusRed else TextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { NotificationTelephonyEngine.endCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            modifier = Modifier.testTag("end_call_btn")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End", tint = TextPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DECLINE / END", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sub Navigation Tabs: Notifications | SMS Messenger | Calls
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
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (state.notifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = PrimaryCyan, contentColor = BackgroundDark) {
                                Text("${state.notifications.size}", fontSize = 9.sp)
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifs") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_notifs")
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("SMS Messages", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Sms, contentDescription = "SMS") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_sms")
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Dialer & Calls", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.PhoneCallback, contentDescription = "Calls") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_calls")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Phone Notifications Mirror Hub
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MIRRORED PHONE NOTIFICATIONS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                    TextButton(
                        onClick = { NotificationTelephonyEngine.clearAllNotifications() },
                        modifier = Modifier.testTag("clear_all_notifs_btn")
                    ) {
                        Text("Clear All", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (state.notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "None", tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No active notifications", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.notifications) { notif ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(PrimaryCyan.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.ChatBubble, contentDescription = "App", tint = PrimaryCyan, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(notif.appName, fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 12.sp)
                                        }

                                        IconButton(
                                            onClick = { NotificationTelephonyEngine.dismissNotification(notif.id) },
                                            modifier = Modifier.size(24.dp).testTag("dismiss_notif_${notif.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(notif.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text(notif.message, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Full SMS Messenger & Thread Viewer
                val activeThread = state.smsThreads.firstOrNull { it.contactName == state.activeThreadContact } ?: state.smsThreads.first()

                Column(modifier = Modifier.weight(1f)) {
                    // Contact Thread Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.smsThreads.forEach { thread ->
                            FilterChip(
                                selected = thread.contactName == activeThread.contactName,
                                onClick = { NotificationTelephonyEngine.selectSmsThread(thread.contactName) },
                                label = { Text(thread.contactName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryCyan,
                                    selectedLabelColor = BackgroundDark,
                                    containerColor = SurfaceDark,
                                    labelColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("sms_chip_${thread.contactName}")
                            )
                        }
                    }

                    // SMS Thread Chat Bubble View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(activeThread.messages) { msg ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (msg.isFromUser) PrimaryCyan.copy(alpha = 0.2f) else SurfaceVariantDark
                                        ),
                                        border = BorderStroke(1.dp, if (msg.isFromUser) PrimaryCyan else CardBorder),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(msg.text, color = TextPrimary, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Reply Composer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyInput,
                            onValueChange = { replyInput = it },
                            placeholder = { Text("Send SMS reply to ${activeThread.contactName}...", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sms_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                NotificationTelephonyEngine.sendSmsMessage(activeThread.contactName, replyInput)
                                replyInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("send_sms_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = BackgroundDark)
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Windows Phone Call Dialer & Bridge
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Direct Call Bridge", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dialerInput,
                            onValueChange = { dialerInput = it },
                            placeholder = { Text("Enter phone number (e.g., +1 555 234 8901)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialer_phone_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { /* Coming soon */ },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("dial_call_btn")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Dial", tint = BackgroundDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DIAL PHONE CALL", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { /* Coming soon */ },
                                border = BorderStroke(1.dp, StatusYellow),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("simulate_incoming_call_btn")
                            ) {
                                Text("Simulate Incoming Call", color = StatusYellow, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
