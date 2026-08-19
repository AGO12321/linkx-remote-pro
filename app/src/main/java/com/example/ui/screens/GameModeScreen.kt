package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.control.RemoteControlEngine
import com.example.ui.theme.*

@Composable
fun GameModeScreen() {
    val remoteState by RemoteControlEngine.state.collectAsState()
    val gamepad = remoteState.gamepad

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header - Game Pad Status HUD
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Gamepad",
                        tint = PrimaryCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Low Latency Game Controller",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Layout: ${gamepad.activeLayout} | Polling Rate: 1000Hz",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Badge(containerColor = StatusGreen.copy(alpha = 0.2f), contentColor = StatusGreen) {
                    Text(
                        text = "UDP Turbo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // L1 / R1 Shoulder Triggers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { RemoteControlEngine.sendGamepadButton("L2", "CTRL") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .testTag("gamepad_btn_L2")
                ) {
                    Text("L2", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = { RemoteControlEngine.sendGamepadButton("L1", "q") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .testTag("gamepad_btn_L1")
                ) {
                    Text("L1", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { RemoteControlEngine.sendGamepadButton("R1", "e") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .testTag("gamepad_btn_R1")
                ) {
                    Text("R1", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = { RemoteControlEngine.sendGamepadButton("R2", "SPACE") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .testTag("gamepad_btn_R2")
                ) {
                    Text("R2", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        // Main Controller Area: Left D-Pad & Analog Joystick, Center System, Right Action ABXY Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left D-Pad Cross
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(SurfaceDark, RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                ) {
                    // D-Pad Up
                    IconButton(
                        onClick = { RemoteControlEngine.sendGamepadButton("DPAD_UP", "w") },
                        modifier = Modifier
                            .size(42.dp)
                            .background(SurfaceVariantDark, CircleShape)
                            .testTag("dpad_up")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = PrimaryCyan)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // D-Pad Left
                        IconButton(
                            onClick = { RemoteControlEngine.sendGamepadButton("DPAD_LEFT", "a") },
                            modifier = Modifier
                                .size(42.dp)
                                .background(SurfaceVariantDark, CircleShape)
                                .testTag("dpad_left")
                        ) {
                            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = PrimaryCyan)
                        }

                        // Center D-Pad Icon
                        Icon(imageVector = Icons.Default.Gamepad, contentDescription = "DPad", tint = TextMuted, modifier = Modifier.size(20.dp))

                        // D-Pad Right
                        IconButton(
                            onClick = { RemoteControlEngine.sendGamepadButton("DPAD_RIGHT", "d") },
                            modifier = Modifier
                                .size(42.dp)
                                .background(SurfaceVariantDark, CircleShape)
                                .testTag("dpad_right")
                        ) {
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = PrimaryCyan)
                        }
                    }

                    // D-Pad Down
                    IconButton(
                        onClick = { RemoteControlEngine.sendGamepadButton("DPAD_DOWN", "s") },
                        modifier = Modifier
                            .size(42.dp)
                            .background(SurfaceVariantDark, CircleShape)
                            .testTag("dpad_down")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = PrimaryCyan)
                    }
                }
            }

            // Center Option / Select / Start / Home Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { RemoteControlEngine.sendGamepadButton("SELECT", "ESC") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceVariantDark, CircleShape)
                        .testTag("btn_select")
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Select", tint = TextPrimary)
                }

                IconButton(
                    onClick = { RemoteControlEngine.sendGamepadButton("START", "ENTER") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceVariantDark, CircleShape)
                        .testTag("btn_start")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = StatusGreen)
                }
            }

            // Right Action ABXY Diamond
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(SurfaceDark, RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                ) {
                    // Y Button (Top)
                    IconButton(
                        onClick = { RemoteControlEngine.sendGamepadButton("BTN_Y", "y") },
                        modifier = Modifier
                            .size(42.dp)
                            .background(StatusYellow.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, StatusYellow, CircleShape)
                            .testTag("btn_Y")
                    ) {
                        Text("Y", fontWeight = FontWeight.Bold, color = StatusYellow, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // X Button (Left)
                        IconButton(
                            onClick = { RemoteControlEngine.sendGamepadButton("BTN_X", "x") },
                            modifier = Modifier
                                .size(42.dp)
                                .background(SecondarySky.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, SecondarySky, CircleShape)
                                .testTag("btn_X")
                        ) {
                            Text("X", fontWeight = FontWeight.Bold, color = SecondarySky, fontSize = 16.sp)
                        }

                        // B Button (Right)
                        IconButton(
                            onClick = { RemoteControlEngine.sendGamepadButton("BTN_B", "b") },
                            modifier = Modifier
                                .size(42.dp)
                                .background(StatusRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, StatusRed, CircleShape)
                                .testTag("btn_B")
                        ) {
                            Text("B", fontWeight = FontWeight.Bold, color = StatusRed, fontSize = 16.sp)
                        }
                    }

                    // A Button (Bottom)
                    IconButton(
                        onClick = { RemoteControlEngine.sendGamepadButton("BTN_A", "SPACE") },
                        modifier = Modifier
                            .size(42.dp)
                            .background(StatusGreen.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, StatusGreen, CircleShape)
                            .testTag("btn_A")
                    ) {
                        Text("A", fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 16.sp)
                    }
                }
            }
        }

        // Dual Virtual Analog Thumbsticks (Left Aim / Movement, Right Mouse Aim)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Stick Pad
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, PrimaryCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(160.dp)
                    .height(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                RemoteControlEngine.updateLeftJoystick(
                                    (dragAmount.x / 50f).coerceIn(-1f, 1f),
                                    (dragAmount.y / 50f).coerceIn(-1f, 1f)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.OpenWith, contentDescription = "Left Stick", tint = PrimaryCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("LEFT ANALOG (Move)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Right Stick Pad
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SecondarySky),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(160.dp)
                    .height(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                RemoteControlEngine.updateRightJoystick(
                                    (dragAmount.x / 30f).coerceIn(-1f, 1f),
                                    (dragAmount.y / 30f).coerceIn(-1f, 1f)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Mouse, contentDescription = "Right Stick", tint = SecondarySky)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("RIGHT ANALOG (Look/Aim)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
