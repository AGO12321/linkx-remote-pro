package com.example.server

import com.example.protocol.ClickType
import com.example.protocol.FileEntry
import com.example.protocol.MediaCommand
import com.example.protocol.MessageType
import com.example.protocol.PowerAction
import com.example.protocol.ProcessItem
import com.example.protocol.ProtocolPacket
import com.example.protocol.ServerInfo
import com.example.protocol.SystemMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

enum class WindowsConnectionStatus {
    PAIRED_CONNECTED,
    RECONNECTING,
    LISTENING,
    DISCONNECTED
}

enum class ExecutionStatus {
    RECEIVED,
    EXECUTING,
    SUCCESS,
    FAILED
}

data class CommandMapping(
    val id: String = UUID.randomUUID().toString(),
    val trigger: String,
    val actionType: String,
    val targetValue: String,
    val description: String,
    val isEnabled: Boolean = true
)

data class RealtimeExecutionEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val trigger: String,
    val actionType: String,
    val status: ExecutionStatus,
    val latencyMs: Long,
    val details: String
)

data class WindowsServerState(
    val serverInfo: ServerInfo = ServerInfo(),
    val metrics: SystemMetrics = SystemMetrics(),
    val connectionStatus: WindowsConnectionStatus = WindowsConnectionStatus.DISCONNECTED,
    val connectedDeviceName: String = "",
    val autoReconnectEnabled: Boolean = true,
    val reconnectAttempts: Int = 0,
    val pingLatencyMs: Long = 0,
    val cursorX: Int = 0,
    val cursorY: Int = 0,
    val isMouseLeftDown: Boolean = false,
    val activeWindow: String = "",
    val isMediaPlaying: Boolean = false,
    val mediaTrackName: String = "",
    val mediaArtist: String = "",
    val volumeLevelPct: Int = 50,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val currentDrivePath: String = "",
    val fileEntries: List<FileEntry> = emptyList(),
    val processList: List<ProcessItem> = emptyList(),
    val customMappings: List<CommandMapping> = emptyList(),
    val realTimeEvents: List<RealtimeExecutionEvent> = emptyList(),
    val cmdOutputLogs: List<String> = emptyList(),
    val packetLogs: List<String> = emptyList(),
    val securityPin: String = "",
    val presentationSlide: Int = 1,
    val totalPresentationSlides: Int = 1
)

object LinkXServerEngine {

    private val _state = MutableStateFlow(WindowsServerState())
    val state: StateFlow<WindowsServerState> = _state.asStateFlow()

    // Called when real data arrives from the Windows host over the network
    fun updateProcessList(processes: List<ProcessItem>) {
        val curr = _state.value
        _state.value = curr.copy(processList = processes)
        addPacketLog("TASK_MANAGER_RESP -> Received ${processes.size} processes")
    }

    fun updateFileEntries(path: String, entries: List<FileEntry>) {
        val curr = _state.value
        _state.value = curr.copy(currentDrivePath = path, fileEntries = entries)
        addPacketLog("FILE_LIST_RESP -> Received ${entries.size} entries for $path")
    }

    fun updateConnectionStatus(status: WindowsConnectionStatus, deviceName: String = "") {
        val curr = _state.value
        _state.value = curr.copy(
            connectionStatus = status,
            connectedDeviceName = deviceName
        )
        if (status == WindowsConnectionStatus.PAIRED_CONNECTED) {
            addEvent("CONNECT", "Connection", ExecutionStatus.SUCCESS, "Connected to $deviceName")
        } else if (status == WindowsConnectionStatus.DISCONNECTED) {
            addEvent("DISCONNECT", "Connection", ExecutionStatus.FAILED, "Disconnected from host")
        }
    }

    fun updateLatency(latencyMs: Long) {
        _state.value = _state.value.copy(pingLatencyMs = latencyMs)
    }

    fun appendCmdOutput(output: String) {
        val curr = _state.value
        _state.value = curr.copy(cmdOutputLogs = curr.cmdOutputLogs + output)
    }

    fun updateSecurityPin(pin: String) {
        _state.value = _state.value.copy(securityPin = pin)
    }

    fun processIncomingPacket(packet: ProtocolPacket): ProtocolPacket {
        val curr = _state.value
        val packetLog = "[${getFormattedTime()}] IN: ${packet.type} -> ${packet.payload}"
        val updatedPacketLogs = (listOf(packetLog) + curr.packetLogs).take(50)

        when (packet.type) {
            MessageType.MOUSE_MOVE -> {
                val dx = packet.payload.optDouble("dx", 0.0).toInt()
                val dy = packet.payload.optDouble("dy", 0.0).toInt()
                val newX = (curr.cursorX + dx).coerceIn(0, 3840)
                val newY = (curr.cursorY + dy).coerceIn(0, 2160)
                _state.value = curr.copy(
                    cursorX = newX, cursorY = newY,
                    packetLogs = updatedPacketLogs
                )
            }
            MessageType.MOUSE_CLICK -> {
                _state.value = curr.copy(packetLogs = updatedPacketLogs)
            }
            MessageType.MOUSE_SCROLL -> {
                _state.value = curr.copy(packetLogs = updatedPacketLogs)
            }
            MessageType.MEDIA_CONTROL -> {
                val cmdStr = packet.payload.optString("command", "")
                val cmd = try { MediaCommand.valueOf(cmdStr) } catch (e: Exception) { null }
                when (cmd) {
                    MediaCommand.PLAY_PAUSE -> _state.value = curr.copy(isMediaPlaying = !curr.isMediaPlaying, packetLogs = updatedPacketLogs)
                    MediaCommand.VOLUME_UP -> _state.value = curr.copy(volumeLevelPct = (curr.volumeLevelPct + 5).coerceAtMost(100), packetLogs = updatedPacketLogs)
                    MediaCommand.VOLUME_DOWN -> _state.value = curr.copy(volumeLevelPct = (curr.volumeLevelPct - 5).coerceAtLeast(0), packetLogs = updatedPacketLogs)
                    MediaCommand.MUTE -> _state.value = curr.copy(isMuted = !curr.isMuted, packetLogs = updatedPacketLogs)
                    else -> _state.value = curr.copy(packetLogs = updatedPacketLogs)
                }
            }
            MessageType.CMD_REQ -> {
                // Real command execution happens on Windows host; we just log the request
                val cmd = packet.payload.optString("command", "")
                addPacketLog("CMD_REQ -> Sent command to host: $cmd")
                _state.value = _state.value.copy(packetLogs = updatedPacketLogs)
            }
            MessageType.PROC_KILL -> {
                val pid = packet.payload.optInt("pid", 0)
                addPacketLog("PROC_KILL -> Requested kill PID $pid on host")
                _state.value = _state.value.copy(packetLogs = updatedPacketLogs)
            }
            else -> {
                _state.value = curr.copy(packetLogs = updatedPacketLogs)
            }
        }

        val ackPayload = JSONObject().apply { put("status", "OK") }
        return ProtocolPacket(MessageType.CONNECT_ACK, payload = ackPayload)
    }

    // Command Mapping Management
    fun addCommandMapping(mapping: CommandMapping) {
        val curr = _state.value
        _state.value = curr.copy(customMappings = curr.customMappings + mapping)
    }

    fun updateCommandMapping(mapping: CommandMapping) {
        val curr = _state.value
        val updated = curr.customMappings.map { if (it.id == mapping.id) mapping else it }
        _state.value = curr.copy(customMappings = updated)
    }

    fun deleteCommandMapping(id: String) {
        val curr = _state.value
        _state.value = curr.copy(customMappings = curr.customMappings.filter { it.id != id })
    }

    fun toggleCommandMapping(id: String) {
        val curr = _state.value
        val updated = curr.customMappings.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
        _state.value = curr.copy(customMappings = updated)
    }

    fun toggleAutoReconnect() {
        val curr = _state.value
        _state.value = curr.copy(autoReconnectEnabled = !curr.autoReconnectEnabled)
    }

    fun unlockHost(pin: String): Boolean {
        if (pin == _state.value.securityPin) {
            _state.value = _state.value.copy(isLocked = false)
            return true
        }
        return false
    }

    fun setPresentationSlide(slideDelta: Int) {
        val curr = _state.value
        val newSlide = (curr.presentationSlide + slideDelta).coerceIn(1, curr.totalPresentationSlides)
        _state.value = curr.copy(presentationSlide = newSlide)
    }

    private fun addPacketLog(message: String) {
        val curr = _state.value
        val log = "[${getFormattedTime()}] $message"
        _state.value = curr.copy(packetLogs = (listOf(log) + curr.packetLogs).take(50))
    }

    private fun addEvent(trigger: String, actionType: String, status: ExecutionStatus, details: String) {
        val curr = _state.value
        val event = RealtimeExecutionEvent(
            timestamp = getFormattedTime(),
            trigger = trigger,
            actionType = actionType,
            status = status,
            latencyMs = curr.pingLatencyMs,
            details = details
        )
        _state.value = curr.copy(realTimeEvents = (listOf(event) + curr.realTimeEvents).take(50))
    }

    private fun getFormattedTime(): String {
        val calendar = java.util.Calendar.getInstance()
        return String.format("%02d:%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), calendar.get(java.util.Calendar.SECOND))
    }
}
