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
import com.example.client.LinkXClientEngine
import com.example.control.RemoteControlEngine
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*

@Composable
fun PresenterScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    val remoteState by RemoteControlEngine.state.collectAsState()
    var isBlackScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Air Mouse / Laser Pointer Header Switch Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Laser",
                        tint = if (remoteState.presenterLaser.isLaserPointerActive) StatusRed else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Virtual Laser Pointer",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (remoteState.presenterLaser.isLaserPointerActive) "ON" else "OFF",
                        color = if (remoteState.presenterLaser.isLaserPointerActive) StatusRed else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = remoteState.presenterLaser.isLaserPointerActive,
                        onCheckedChange = { RemoteControlEngine.toggleLaserPointer() },
                        colors = SwitchDefaults.colors(checkedThumbColor = StatusRed, checkedTrackColor = StatusRed.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("laser_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presentation Slide Card Display / Laser Pad
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isBlackScreen) Color.Black else SurfaceDark
            ),
            border = BorderStroke(1.dp, if (remoteState.presenterLaser.isLaserPointerActive) StatusRed else PrimaryCyan),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(remoteState.presenterLaser.isLaserPointerActive) {
                        if (remoteState.presenterLaser.isLaserPointerActive) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                RemoteControlEngine.processTouchpadGesture(dragAmount.x * 0.8f, dragAmount.y * 0.8f, 1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isBlackScreen) {
                    Text(text = "[ SLIDESHOW BLACK SCREEN ]", color = TextMuted, fontSize = 14.sp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Project_Presentation.pptx",
                            color = SecondarySky,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SLIDE",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${serverState.presentationSlide} / ${serverState.totalPresentationSlides}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp
                        )
                        if (remoteState.presenterLaser.isLaserPointerActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🔴 Laser Active - Drag to Point Cursor",
                                color = StatusRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Next & Previous Slide Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { LinkXServerEngine.setPresentationSlide(-1) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("prev_slide_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = "Prev", modifier = Modifier.size(32.dp))
                    Text("PREV SLIDE", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { LinkXServerEngine.setPresentationSlide(1) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("next_slide_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT SLIDE", fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.NavigateNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presenter Tools Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { isBlackScreen = !isBlackScreen },
                border = BorderStroke(1.dp, if (isBlackScreen) StatusRed else CardBorder),
                modifier = Modifier.weight(1f).testTag("black_screen_btn")
            ) {
                Icon(
                    imageVector = if (isBlackScreen) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Black Screen",
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBlackScreen) "UNBLANK" else "BLACK OUT", color = TextPrimary, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { LinkXClientEngine.sendKeyPress("F5") },
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.weight(1f).testTag("start_presentation_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start F5", tint = PrimaryCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("START F5", color = TextPrimary, fontSize = 12.sp)
            }
        }
    }
}
