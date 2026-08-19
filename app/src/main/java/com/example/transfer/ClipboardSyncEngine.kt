package com.example.transfer

import com.example.client.LinkXClientEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ClipboardType(val label: String) {
    TEXT("Plain Text"),
    URL("Web Link / URL"),
    CODE_SNIPPET("Code Snippet"),
    IMAGE_DATA("Image / Media URI")
}

data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: ClipboardType = ClipboardType.TEXT,
    val timestampMs: Long = System.currentTimeMillis(),
    val sourceDevice: String = "Android Phone",
    val isEncrypted: Boolean = true,
    val charCount: Int = content.length
)

data class ClipboardSyncState(
    val isAutoSyncEnabled: Boolean = true,
    val isE2EEncryptionEnabled: Boolean = true,
    val history: List<ClipboardItem> = listOf(
        ClipboardItem(
            content = "https://github.com/google-ai-studio/linkx-remote-pro",
            type = ClipboardType.URL,
            sourceDevice = "Windows 11 PC",
            timestampMs = System.currentTimeMillis() - 120_000
        ),
        ClipboardItem(
            content = "adb shell pm grant com.example android.permission.WRITE_SECURE_SETTINGS",
            type = ClipboardType.CODE_SNIPPET,
            sourceDevice = "Android Phone",
            timestampMs = System.currentTimeMillis() - 600_000
        ),
        ClipboardItem(
            content = "LinkX Remote Pro Access Security PIN: 849201",
            type = ClipboardType.TEXT,
            sourceDevice = "Windows 11 PC",
            timestampMs = System.currentTimeMillis() - 1_800_000
        )
    ),
    val lastSyncedText: String = "https://github.com/google-ai-studio/linkx-remote-pro",
    val syncStatusMessage: String = "Bidirectional Clipboard Sync Engine Active (AES-256 Encrypted)"
)

object ClipboardSyncEngine {
    private val _state = MutableStateFlow(ClipboardSyncState())
    val state: StateFlow<ClipboardSyncState> = _state.asStateFlow()

    fun toggleAutoSync() {
        val curr = _state.value
        _state.value = curr.copy(isAutoSyncEnabled = !curr.isAutoSyncEnabled)
    }

    fun toggleE2EEncryption() {
        val curr = _state.value
        _state.value = curr.copy(isE2EEncryptionEnabled = !curr.isE2EEncryptionEnabled)
    }

    fun sendToWindowsHost(text: String) {
        if (text.isBlank()) return
        val type = when {
            text.startsWith("http://") || text.startsWith("https://") -> ClipboardType.URL
            text.contains("\n") || text.contains("{") || text.contains(";") -> ClipboardType.CODE_SNIPPET
            else -> ClipboardType.TEXT
        }

        val newItem = ClipboardItem(
            content = text,
            type = type,
            sourceDevice = "Android Phone (Local)",
            isEncrypted = _state.value.isE2EEncryptionEnabled
        )

        val curr = _state.value
        _state.value = curr.copy(
            history = listOf(newItem) + curr.history,
            lastSyncedText = text,
            syncStatusMessage = "Pushed to Windows Clipboard: '${text.take(25)}...'"
        )

        // Send over WebSocket protocol
        LinkXClientEngine.sendCmd("echo $text | clip")
    }

    fun receiveFromWindowsHost(text: String) {
        if (text.isBlank()) return
        val newItem = ClipboardItem(
            content = text,
            type = ClipboardType.TEXT,
            sourceDevice = "Windows 11 PC (Remote)",
            isEncrypted = _state.value.isE2EEncryptionEnabled
        )

        val curr = _state.value
        _state.value = curr.copy(
            history = listOf(newItem) + curr.history,
            lastSyncedText = text,
            syncStatusMessage = "Received from Windows PC: '${text.take(25)}...'"
        )
    }

    fun clearHistory() {
        val curr = _state.value
        _state.value = curr.copy(history = emptyList())
    }
}
