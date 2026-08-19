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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.*
import com.example.ui.theme.*

@Composable
fun SecurityScreen() {
    val secState by SecurityEngine.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Encryption & Biometrics, 1: OTP Pairing, 2: Trusted Devices, 3: Session Logs

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
                text = { Text("E2EE & Biometrics", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Security, contentDescription = "E2EE") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("sec_tab_e2ee")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("OTP Pairing", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.VpnKey, contentDescription = "OTP") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("sec_tab_otp")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Trusted Devices", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Trusted") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("sec_tab_trusted")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Audit Logs", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                selectedContentColor = PrimaryCyan,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("sec_tab_logs")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Cryptographic Engine HUD & Biometric Controls
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, StatusGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Shield, contentDescription = "Shield", tint = StatusGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("AES-256-GCM + RSA-4096 E2EE", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("Zero-Trust End-to-End Encrypted Tunnel", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Badge(containerColor = StatusGreen.copy(alpha = 0.2f), contentColor = StatusGreen) {
                                        Text("ACTIVE", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("RSA-4096 Public Key Fingerprint", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = secState.rsaPublicKeyFingerprint,
                                    color = PrimaryCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { SecurityEngine.rotateRsaKeys() },
                                    border = BorderStroke(1.dp, PrimaryCyan),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("rotate_rsa_keys_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Rotate", tint = PrimaryCyan)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rotate RSA Key Pair", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                        Icon(Icons.Default.Fingerprint, contentDescription = "Bio", tint = PrimaryCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Biometric Authentication", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("Require Fingerprint / Face Unlock for session connect", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Switch(
                                        checked = secState.isBiometricAuthRequired,
                                        onCheckedChange = { SecurityEngine.toggleBiometricRequirement() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = AccentTeal),
                                        modifier = Modifier.testTag("biometric_auth_switch")
                                    )
                                }

                                if (secState.isBiometricAuthRequired) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Preferred Sensor Type", color = TextSecondary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = secState.activeBiometricType == BiometricType.FINGERPRINT,
                                            onClick = { SecurityEngine.setBiometricType(BiometricType.FINGERPRINT) },
                                            label = { Text("Fingerprint") },
                                            leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = "FP") },
                                            modifier = Modifier.testTag("chip_fingerprint")
                                        )

                                        FilterChip(
                                            selected = secState.activeBiometricType == BiometricType.FACE_UNLOCK,
                                            onClick = { SecurityEngine.setBiometricType(BiometricType.FACE_UNLOCK) },
                                            label = { Text("Face Unlock") },
                                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = "Face") },
                                            modifier = Modifier.testTag("chip_face_unlock")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Live 6-Digit OTP Pairing Generator
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, PrimaryCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LockClock, contentDescription = "OTP", tint = PrimaryCyan, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dynamic 6-Digit Pairing PIN", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Text("Enter this code on the Windows LinkX Host app to pair instantly", color = TextMuted, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Styled 6-Digit Display
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            secState.activeOtpPin.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp, 56.dp)
                                        .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                                        .border(1.dp, PrimaryCyan, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "PIN expires in ${secState.otpRemainingSeconds} seconds",
                            color = SecondarySky,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { secState.otpRemainingSeconds / 60f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = PrimaryCyan,
                            trackColor = CardBorder
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { SecurityEngine.regenerateOtpPin() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("regenerate_otp_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "New PIN", tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REGENERATE PIN NOW", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Trusted Devices Management
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PAIRED TRUSTED WINDOWS HOSTS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(secState.trustedDevices) { device ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, if (device.isTrusted) StatusGreen else StatusRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Computer,
                                            contentDescription = "PC",
                                            tint = if (device.isTrusted) PrimaryCyan else TextMuted
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(device.deviceName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                            Text("IP: ${device.ipAddress} | MAC: ${device.macAddress}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }

                                    Badge(
                                        containerColor = if (device.isTrusted) StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f),
                                        contentColor = if (device.isTrusted) StatusGreen else StatusRed
                                    ) {
                                        Text(if (device.isTrusted) "TRUSTED" else "REVOKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Key Fingerprint: ${device.rsaPublicKeyFingerprint.take(30)}...", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (device.isTrusted) {
                                        OutlinedButton(
                                            onClick = { SecurityEngine.revokeDeviceTrust(device.id) },
                                            border = BorderStroke(1.dp, StatusRed),
                                            modifier = Modifier.testTag("revoke_trust_${device.id}")
                                        ) {
                                            Text("Revoke Trust", color = StatusRed, fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = { SecurityEngine.grantDeviceTrust(device.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                            modifier = Modifier.testTag("grant_trust_${device.id}")
                                        ) {
                                            Text("Trust Device", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Security Audit & Session Event Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SECURITY AUDIT TRAIL LOGS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                    TextButton(
                        onClick = { SecurityEngine.clearAuditLogs() },
                        modifier = Modifier.testTag("clear_audit_logs_btn")
                    ) {
                        Text("Clear Logs", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(secState.sessionLogs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Log", tint = PrimaryCyan)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.eventType, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text("Source: ${log.deviceName} (${log.sourceIp}) | Cipher: ${log.cipherSuite}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }

                                Badge(containerColor = StatusGreen.copy(alpha = 0.2f), contentColor = StatusGreen) {
                                    Text(log.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
