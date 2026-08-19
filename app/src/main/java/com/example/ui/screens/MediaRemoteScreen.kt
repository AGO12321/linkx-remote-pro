package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.LinkXClientEngine
import com.example.protocol.MediaCommand
import com.example.server.LinkXServerEngine
import com.example.ui.theme.*

@Composable
fun MediaRemoteScreen() {
    val serverState by LinkXServerEngine.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Switcher Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSwitcherBadge("Spotify", Icons.Default.MusicNote, true)
                AppSwitcherBadge("VLC", Icons.Default.Movie, false)
                AppSwitcherBadge("YouTube", Icons.Default.OndemandVideo, false)
                AppSwitcherBadge("Netflix", Icons.Default.Tv, false)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Active Album Art & Track Info Visualizer
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, PrimaryCyan),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceVariantDark)
                        .border(2.dp, SecondarySky, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Visualizer",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = serverState.mediaTrackName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = serverState.mediaArtist,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback Control Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.PREVIOUS) },
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceVariantDark, CircleShape)
                    .border(1.dp, CardBorder, CircleShape)
                    .testTag("media_prev_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.PLAY_PAUSE) },
                modifier = Modifier
                    .size(72.dp)
                    .background(PrimaryCyan, CircleShape)
                    .testTag("media_play_pause_btn")
            ) {
                Icon(
                    imageVector = if (serverState.isMediaPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.NEXT) },
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceVariantDark, CircleShape)
                    .border(1.dp, CardBorder, CircleShape)
                    .testTag("media_next_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Master Volume Control Panel
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
                        Icon(
                            imageVector = if (serverState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Volume Icon",
                            tint = if (serverState.isMuted) StatusRed else SecondarySky
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (serverState.isMuted) "MUTED" else "Volume Level",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "${serverState.volumeLevelPct}%",
                        color = SecondarySky,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.VOLUME_DOWN) },
                        modifier = Modifier.testTag("vol_down_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Vol Down", tint = TextSecondary)
                    }

                    Slider(
                        value = serverState.volumeLevelPct.toFloat(),
                        onValueChange = {
                            // Send Volume Up/Down
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = SecondarySky,
                            activeTrackColor = SecondarySky,
                            inactiveTrackColor = SurfaceVariantDark
                        ),
                        modifier = Modifier.weight(1f).testTag("volume_slider")
                    )

                    IconButton(
                        onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.VOLUME_UP) },
                        modifier = Modifier.testTag("vol_up_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Vol Up", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { LinkXClientEngine.sendMediaCommand(MediaCommand.MUTE) },
                    border = BorderStroke(1.dp, if (serverState.isMuted) StatusRed else CardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("mute_btn")
                ) {
                    Text(
                        text = if (serverState.isMuted) "UNMUTE AUDIO" else "MUTE PC AUDIO",
                        color = if (serverState.isMuted) StatusRed else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AppSwitcherBadge(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (isActive) PrimaryCyan else SurfaceVariantDark, CircleShape)
                .border(1.dp, if (isActive) SecondarySky else CardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = TextPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, color = if (isActive) TextPrimary else TextMuted, fontSize = 11.sp)
    }
}
