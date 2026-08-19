package com.example.client

import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.network.TcpTransport
import com.example.network.UdpTransport
import com.example.protocol.BinaryPacket
import com.example.protocol.CryptoManager
import com.example.protocol.PacketType
import com.example.protocol.PacketFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

enum class ConnectionMode { LAN_WEBSOCKET, WIFI_DIRECT, HOTSPOT, BLUETOOTH_RFCOMM }

data class DiscoveredHost(
    val name: String, val ip: String, val port: Int = 8080,
    val mode: ConnectionMode, val signalStrengthDbm: Int = -55, val isPaired: Boolean = true
)

data class ClientConnectionState(
    val activeMode: ConnectionMode = ConnectionMode.LAN_WEBSOCKET,
    val serverIp: String = "192.168.1.105",
    val serverPort: Int = 8080,
    val isConnected: Boolean = false,
    val isSearching: Boolean = false,
    val autoReconnectEnabled: Boolean = true,
    val reconnectAttempts: Int = 0,
    val latencyMs: Long = 0,
    val serverName: String = "LinkX Host",
    val discoveredHosts: List<DiscoveredHost> = emptyList(),
    val savedServers: List<String> = emptyList()
)

object LinkXClientEngine {
    private const val TAG = "LinkXClientEngine"
    private val _connectionState = MutableStateFlow(ClientConnectionState())
    val connectionState: StateFlow<ClientConnectionState> = _connectionState.asStateFlow()

    private val _screenStream = MutableStateFlow<Bitmap?>(null)
    val screenStream: StateFlow<Bitmap?> = _screenStream.asStateFlow()

    private val engineScope = CoroutineScope(Dispatchers.IO + Job())
    private val cryptoManager = CryptoManager()
    private var tcpTransport: TcpTransport? = null
    private var udpTransport: UdpTransport? = null

    fun setConnectionMode(mode: ConnectionMode) {
        _connectionState.value = _connectionState.value.copy(activeMode = mode)
    }

    fun toggleAutoReconnect() {
        val curr = _connectionState.value
        _connectionState.value = curr.copy(autoReconnectEnabled = !curr.autoReconnectEnabled)
    }

    fun connect(ip: String, port: Int = 8080) {
        engineScope.launch {
            _connectionState.value = _connectionState.value.copy(isSearching = true)
            
            tcpTransport = TcpTransport(cryptoManager, ::onPacketReceived, ::onDisconnected)
            udpTransport = UdpTransport(cryptoManager, ::onPacketReceived)
            
            val tcpSuccess = tcpTransport?.connect(ip, port) ?: false
            val udpSuccess = udpTransport?.connect(ip, port + 1) ?: false // Standard UDP port
            
            if (tcpSuccess) {
                _connectionState.value = _connectionState.value.copy(
                    serverIp = ip, serverPort = port, isConnected = true,
                    isSearching = false, reconnectAttempts = 0
                )
            } else {
                _connectionState.value = _connectionState.value.copy(isSearching = false, isConnected = false)
            }
        }
    }

    fun disconnect() {
        tcpTransport?.disconnect()
    }

    private fun onDisconnected() {
        _connectionState.value = _connectionState.value.copy(isConnected = false)
        triggerAutoReconnect()
    }

    private fun onPacketReceived(packet: BinaryPacket) {
        Log.d(TAG, "Received Packet: \${packet.type}")
        when (packet.type) {
            PacketType.AUTH_RESULT -> {
                Log.d(TAG, "Handshake complete, session secure.")
            }
            PacketType.HEARTBEAT -> {
                Log.d(TAG, "Heartbeat from server")
            }
            PacketType.SCREEN_FRAME -> {
                try {
                    val bmp = BitmapFactory.decodeByteArray(packet.payload, 0, packet.payload.size)
                    _screenStream.value = bmp
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode screen frame: ${e.message}")
                }
            }
            else -> {
                Log.d(TAG, "Received packet: ${packet.type}")
            }
        }
    }

    fun sendPacket(packet: BinaryPacket) {
        packet.flags = PacketFlags.ENCRYPTED // Encrypt everything by default now
        when (packet.type) {
            PacketType.MOUSE_MOVE, PacketType.MOUSE_SCROLL, PacketType.AUDIO_FRAME, PacketType.SCREEN_FRAME -> {
                udpTransport?.sendPacket(packet)
            }
            else -> {
                tcpTransport?.sendPacket(packet)
            }
        }
    }

    fun sendMouseMove(dx: Float, dy: Float) {
        val payload = java.nio.ByteBuffer.allocate(8)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .putFloat(dx).putFloat(dy).array()
        sendPacket(BinaryPacket(type = PacketType.MOUSE_MOVE, payload = payload))
    }

    fun sendMouseClick(clickType: com.example.protocol.ClickType) {
        val button = when (clickType) {
            com.example.protocol.ClickType.LEFT -> 0
            com.example.protocol.ClickType.RIGHT -> 1
            com.example.protocol.ClickType.MIDDLE -> 2
            com.example.protocol.ClickType.DOUBLE_LEFT -> 0 // Handle double tap as two clicks elsewhere or here
            else -> 0
        }
        val payload = java.nio.ByteBuffer.allocate(2)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .put(button.toByte()).put(1.toByte()).array() // 1 = isDown (we can send down/up explicitly later, simplified here)
        sendPacket(BinaryPacket(type = PacketType.MOUSE_CLICK, payload = payload))
        
        // Simulating immediate UP for now
        val payloadUp = java.nio.ByteBuffer.allocate(2)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .put(button.toByte()).put(0.toByte()).array()
        sendPacket(BinaryPacket(type = PacketType.MOUSE_CLICK, payload = payloadUp))
    }

    fun sendMouseScroll(deltaY: Float) {
        val payload = java.nio.ByteBuffer.allocate(4)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .putInt((deltaY * 120).toInt()).array()
        sendPacket(BinaryPacket(type = PacketType.MOUSE_SCROLL, payload = payload))
    }

    fun sendKeyPress(key: String) {
        // ... (can be mapped later)
        val payload = key.toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.KEYBOARD_EVENT, payload = payload))
    }

    fun sendKeyCombo(keys: List<String>) {
        val payload = keys.joinToString(",").toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.KEYBOARD_EVENT, payload = payload))
    }

    fun sendCmd(command: String) {
        val payload = command.toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.TERMINAL_CMD, payload = payload))
    }

    fun killProcess(pid: Int) {
        val payload = pid.toString().toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.TASK_MANAGER_REQ, payload = payload))
    }

    fun sendPowerAction(action: Any) {
        val payload = action.toString().toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.POWER_CMD, payload = payload))
    }

    fun sendMediaCommand(command: Any) {
        // We'll treat media commands as keyboard events for now
        val payload = command.toString().toByteArray(StandardCharsets.UTF_8)
        sendPacket(BinaryPacket(type = PacketType.KEYBOARD_EVENT, payload = payload))
    }

    fun triggerAutoReconnect() {
        val curr = _connectionState.value
        if (!curr.autoReconnectEnabled) return
        _connectionState.value = curr.copy(isSearching = true, reconnectAttempts = curr.reconnectAttempts + 1)
        engineScope.launch {
            delay(2000)
            connect(curr.serverIp, curr.serverPort)
        }
    }

    fun searchServers() {
        _connectionState.value = _connectionState.value.copy(isSearching = true)
        engineScope.launch(Dispatchers.IO) {
            try {
                val socket = java.net.DatagramSocket()
                socket.broadcast = true
                
                val pingStr = "LINKX_DISCOVERY_PING"
                val sendData = pingStr.toByteArray(StandardCharsets.UTF_8)
                val broadcastAddress = java.net.InetAddress.getByName("255.255.255.255")
                val sendPacket = java.net.DatagramPacket(sendData, sendData.size, broadcastAddress, 8082)
                
                socket.send(sendPacket)
                
                // Wait for response with a timeout
                socket.soTimeout = 2000
                val receiveData = ByteArray(1024)
                val receivePacket = java.net.DatagramPacket(receiveData, receiveData.size)
                
                val hosts = mutableListOf<DiscoveredHost>()
                try {
                    while (true) {
                        socket.receive(receivePacket)
                        val response = String(receivePacket.data, 0, receivePacket.length)
                        val json = JSONObject(response)
                        
                        hosts.add(
                            DiscoveredHost(
                                name = json.optString("serverName", "Unknown Host"),
                                ip = receivePacket.address.hostAddress ?: "",
                                port = json.optInt("port", 8080),
                                mode = ConnectionMode.LAN_WEBSOCKET
                            )
                        )
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    // Timeout expected when no more servers respond
                }
                
                socket.close()
                _connectionState.value = _connectionState.value.copy(
                    isSearching = false,
                    discoveredHosts = hosts
                )
            } catch (e: Exception) {
                Log.e(TAG, "Discovery error: ${e.message}")
                _connectionState.value = _connectionState.value.copy(isSearching = false)
            }
        }
    }
}
