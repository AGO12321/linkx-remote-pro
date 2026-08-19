package com.example.protocol

import org.json.JSONArray
import org.json.JSONObject

enum class MessageType {
    CONNECT_REQ,
    CONNECT_ACK,
    AUTH_REQ,
    AUTH_RESP,
    MOUSE_MOVE,
    MOUSE_CLICK,
    MOUSE_SCROLL,
    KEY_PRESS,
    KEY_COMBO,
    MEDIA_CONTROL,
    POWER_ACTION,
    FILE_REQ,
    FILE_RESP,
    PROC_REQ,
    PROC_RESP,
    PROC_KILL,
    CMD_REQ,
    CMD_RESP,
    SCREEN_REQ,
    SCREEN_RESP,
    METRICS_PUSH,
    PING,
    PONG
}

enum class ClickType {
    LEFT,
    RIGHT,
    MIDDLE,
    DOUBLE_LEFT
}

enum class MediaCommand {
    PLAY_PAUSE,
    NEXT,
    PREVIOUS,
    STOP,
    VOLUME_UP,
    VOLUME_DOWN,
    MUTE,
    SEEK_FORWARD,
    SEEK_BACKWARD
}

enum class PowerAction {
    SHUTDOWN,
    RESTART,
    SLEEP,
    LOCK_WORKSTATION,
    LOG_OFF,
    DISPLAY_OFF
}

data class ServerInfo(
    val serverName: String = "Windows 11 PC (LinkX Host)",
    val ipAddress: String = "192.168.1.105",
    val port: Int = 8080,
    val isConnected: Boolean = true,
    val latencyMs: Long = 12,
    val requiresPin: Boolean = true,
    val activeUser: String = "Administrator",
    val osVersion: String = "Windows 11 Pro 23H2"
)

data class SystemMetrics(
    val cpuUsagePct: Int = 24,
    val ramUsagePct: Int = 58,
    val ramUsedGb: Double = 9.3,
    val ramTotalGb: Double = 16.0,
    val batteryPct: Int = 92,
    val isCharging: Boolean = true,
    val uptimeHours: Int = 18,
    val activeWindow: String = "Visual Studio Code - LinkXServer.cs"
)

data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val lastModified: String = "2026-08-02"
)

data class ProcessItem(
    val pid: Int,
    val name: String,
    val cpuUsagePct: Double,
    val memoryMb: Int
)

data class CustomMacro(
    val id: String,
    val title: String,
    val keys: List<String>,
    val iconName: String = "Terminal"
)

data class ProtocolPacket(
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: JSONObject = JSONObject()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("type", type.name)
        root.put("timestamp", timestamp)
        root.put("payload", payload)
        return root.toString()
    }

    companion object {
        fun parse(jsonStr: String): ProtocolPacket? {
            return try {
                val root = JSONObject(jsonStr)
                val typeStr = root.getString("type")
                val type = MessageType.valueOf(typeStr)
                val ts = root.optLong("timestamp", System.currentTimeMillis())
                val payload = root.optJSONObject("payload") ?: JSONObject()
                ProtocolPacket(type, ts, payload)
            } catch (e: Exception) {
                null
            }
        }

        fun createMouseMove(dx: Float, dy: Float): ProtocolPacket {
            val payload = JSONObject().apply {
                put("dx", dx.toDouble())
                put("dy", dy.toDouble())
            }
            return ProtocolPacket(MessageType.MOUSE_MOVE, payload = payload)
        }

        fun createMouseClick(clickType: ClickType): ProtocolPacket {
            val payload = JSONObject().apply {
                put("click", clickType.name)
            }
            return ProtocolPacket(MessageType.MOUSE_CLICK, payload = payload)
        }

        fun createMouseScroll(deltaY: Float): ProtocolPacket {
            val payload = JSONObject().apply {
                put("deltaY", deltaY.toDouble())
            }
            return ProtocolPacket(MessageType.MOUSE_SCROLL, payload = payload)
        }

        fun createKeyPress(key: String, isDown: Boolean = true): ProtocolPacket {
            val payload = JSONObject().apply {
                put("key", key)
                put("isDown", isDown)
            }
            return ProtocolPacket(MessageType.KEY_PRESS, payload = payload)
        }

        fun createKeyCombo(keys: List<String>): ProtocolPacket {
            val arr = JSONArray(keys)
            val payload = JSONObject().apply {
                put("keys", arr)
            }
            return ProtocolPacket(MessageType.KEY_COMBO, payload = payload)
        }

        fun createMediaControl(command: MediaCommand, param: Int = 0): ProtocolPacket {
            val payload = JSONObject().apply {
                put("command", command.name)
                put("param", param)
            }
            return ProtocolPacket(MessageType.MEDIA_CONTROL, payload = payload)
        }

        fun createPowerAction(action: PowerAction): ProtocolPacket {
            val payload = JSONObject().apply {
                put("action", action.name)
            }
            return ProtocolPacket(MessageType.POWER_ACTION, payload = payload)
        }

        fun createCmdReq(command: String): ProtocolPacket {
            val payload = JSONObject().apply {
                put("command", command)
            }
            return ProtocolPacket(MessageType.CMD_REQ, payload = payload)
        }
    }
}
