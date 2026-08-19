package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.example.protocol.ClickType
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*

@Composable
fun TrackpadScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()
    var sensitivity by remember { mutableFloatStateOf(1.5f) }
    var isScrollingMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Top Toolbar Status
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
                        imageVector = Icons.Default.Mouse,
                        contentDescription = "Mouse",
                        tint = PrimaryCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Trackpad Controller",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Cursor: (${serverState.cursorX}, ${serverState.cursorY})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isScrollingMode,
                        onClick = { isScrollingMode = !isScrollingMode },
                        label = { Text(if (isScrollingMode) "Scroll Mode" else "Move Mode") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isScrollingMode) Icons.Default.UnfoldMore else Icons.Default.OpenWith,
                                contentDescription = "Mode"
                            )
                        },
                        modifier = Modifier.testTag("scroll_mode_chip")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trackpad Surface Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, if (isScrollingMode) SecondarySky else PrimaryCyan, RoundedCornerShape(16.dp))
                .pointerInput(isScrollingMode, sensitivity) {
                    if (isScrollingMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            LinkXClientEngine.sendMouseScroll(dragAmount.y * -0.05f)
                        }
                    } else {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            LinkXClientEngine.sendMouseMove(
                                dragAmount.x * sensitivity,
                                dragAmount.y * sensitivity
                            )
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            LinkXClientEngine.sendMouseClick(ClickType.LEFT)
                        },
                        onDoubleTap = {
                            LinkXClientEngine.sendMouseClick(ClickType.DOUBLE_LEFT)
                        },
                        onLongPress = {
                            LinkXClientEngine.sendMouseClick(ClickType.RIGHT)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isScrollingMode) Icons.Default.ImportExport else Icons.Default.TouchApp,
                    contentDescription = "Trackpad Icon",
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isScrollingMode) "Drag vertically to scroll" else "Tap = Left Click | 2-Tap = Double | Long Press = Right",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sensitivity Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Speed",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.width(50.dp)
            )
            Slider(
                value = sensitivity,
                onValueChange = { sensitivity = it },
                valueRange = 0.5f..3.5f,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryCyan,
                    activeTrackColor = PrimaryCyan,
                    inactiveTrackColor = SurfaceVariantDark
                ),
                modifier = Modifier.weight(1f).testTag("sensitivity_slider")
            )
            Text(
                text = String.format("%.1fx", sensitivity),
                color = PrimaryCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mouse Buttons Row (Left, Middle, Right Click)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { LinkXClientEngine.sendMouseClick(ClickType.LEFT) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("mouse_left_click_btn")
            ) {
                Text("LEFT CLICK", fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Button(
                onClick = { LinkXClientEngine.sendMouseClick(ClickType.MIDDLE) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .testTag("mouse_middle_click_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PanoramaFishEye,
                    contentDescription = "Middle Click",
                    tint = SecondarySky
                )
            }

            Button(
                onClick = { LinkXClientEngine.sendMouseClick(ClickType.RIGHT) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("mouse_right_click_btn")
            ) {
                Text("RIGHT CLICK", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}
