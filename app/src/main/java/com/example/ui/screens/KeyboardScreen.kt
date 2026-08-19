package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.theme.*

@Composable
fun KeyboardScreen() {
    var inputText by remember { mutableStateOf("") }
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }
    var winActive by remember { mutableStateOf(false) }

    val fKeys = listOf("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12")
    val quickShortcuts = listOf(
        "Copy" to listOf("Ctrl", "C"),
        "Paste" to listOf("Ctrl", "V"),
        "Alt+Tab" to listOf("Alt", "Tab"),
        "Task Mgr" to listOf("Ctrl", "Shift", "Esc"),
        "Win+D" to listOf("Win", "D"),
        "Lock PC" to listOf("Win", "L"),
        "Snip" to listOf("Win", "Shift", "S")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Text Input Box for full string send
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Remote Text Sender", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type text to send to PC...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keyboard_text_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotEmpty()) {
                                LinkXClientEngine.sendKeyPress(inputText)
                                inputText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("keyboard_send_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Text")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Hotkey Combo Shortcuts Bar
        Text(text = "Quick Combo Macros", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickShortcuts) { (label, keys) ->
                OutlinedButton(
                    onClick = { LinkXClientEngine.sendKeyCombo(keys) },
                    border = BorderStroke(1.dp, SecondarySky),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("macro_btn_$label")
                ) {
                    Text(label, color = SecondarySky, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Function Keys F1 - F12
        Text(text = "Function Keys", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(fKeys) { key ->
                Button(
                    onClick = { LinkXClientEngine.sendKeyPress(key) },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("fkey_$key")
                ) {
                    Text(key, color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modifier Toggles Row (Ctrl, Alt, Shift, Win)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = ctrlActive,
                onClick = { ctrlActive = !ctrlActive },
                label = { Text("CTRL") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryCyan,
                    selectedLabelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f).testTag("mod_ctrl")
            )
            FilterChip(
                selected = altActive,
                onClick = { altActive = !altActive },
                label = { Text("ALT") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryCyan,
                    selectedLabelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f).testTag("mod_alt")
            )
            FilterChip(
                selected = shiftActive,
                onClick = { shiftActive = !shiftActive },
                label = { Text("SHIFT") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryCyan,
                    selectedLabelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f).testTag("mod_shift")
            )
            FilterChip(
                selected = winActive,
                onClick = { winActive = !winActive },
                label = { Text("WIN") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryCyan,
                    selectedLabelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f).testTag("mod_win")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Navigation Keys & Special Keys
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                KeyButton("ESC", Modifier.weight(1f)) { sendWithModifiers("ESC", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("TAB", Modifier.weight(1f)) { sendWithModifiers("TAB", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("DEL", Modifier.weight(1f)) { sendWithModifiers("DELETE", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("BKSP", Modifier.weight(1f)) { sendWithModifiers("BACKSPACE", ctrlActive, altActive, shiftActive, winActive) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                KeyButton("PRT SCR", Modifier.weight(1f)) { sendWithModifiers("PRINTSCREEN", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("HOME", Modifier.weight(1f)) { sendWithModifiers("HOME", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("▲ UP", Modifier.weight(1f)) { sendWithModifiers("UP", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("END", Modifier.weight(1f)) { sendWithModifiers("END", ctrlActive, altActive, shiftActive, winActive) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                KeyButton("◄ LEFT", Modifier.weight(1f)) { sendWithModifiers("LEFT", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("▼ DOWN", Modifier.weight(1f)) { sendWithModifiers("DOWN", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("RIGHT ►", Modifier.weight(1f)) { sendWithModifiers("RIGHT", ctrlActive, altActive, shiftActive, winActive) }
                KeyButton("ENTER ↵", Modifier.weight(1f), isPrimary = true) { sendWithModifiers("ENTER", ctrlActive, altActive, shiftActive, winActive) }
            }
        }
    }
}

private fun sendWithModifiers(key: String, ctrl: Boolean, alt: Boolean, shift: Boolean, win: Boolean) {
    val combo = mutableListOf<String>()
    if (ctrl) combo.add("Ctrl")
    if (alt) combo.add("Alt")
    if (shift) combo.add("Shift")
    if (win) combo.add("Win")
    combo.add(key)
    if (combo.size == 1) {
        LinkXClientEngine.sendKeyPress(key)
    } else {
        LinkXClientEngine.sendKeyCombo(combo)
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) PrimaryCyan else SurfaceVariantDark
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxHeight()
            .testTag("key_btn_$label")
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) TextPrimary else TextPrimary,
            fontSize = 12.sp
        )
    }
}
