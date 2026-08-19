package com.example.network

import android.util.Log
import com.example.protocol.BinaryPacket
import com.example.protocol.CryptoManager
import com.example.protocol.PacketType
import com.example.protocol.PacketFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TcpTransport(
    private val cryptoManager: CryptoManager,
    private val onPacketReceived: (BinaryPacket) -> Unit,
    private val onDisconnected: () -> Unit
) {
    private val TAG = "TcpTransport"
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    @Volatile
    private var isConnected = false

    suspend fun connect(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket(ip, port)
            inputStream = socket?.getInputStream()
            outputStream = socket?.getOutputStream()
            isConnected = true
            
            // Start listening thread
            Thread { listenLoop() }.start()
            
            // Initiate Auth Request
            val authReq = BinaryPacket(type = PacketType.AUTH_REQ)
            sendPacket(authReq)
            
            // Start heartbeat service
            startHeartbeat()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: \${e.message}")
            isConnected = false
            false
        }
    }

    fun disconnect() {
        isConnected = false
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing socket: \${e.message}")
        }
        onDisconnected()
    }

    fun sendPacket(packet: BinaryPacket) {
        if (!isConnected || outputStream == null) return
        
        Thread {
            try {
                if (packet.flags.toInt() and PacketFlags.ENCRYPTED.toInt() != 0) {
                    packet.payload = cryptoManager.encrypt(packet.payload)
                }
                val data = packet.serialize()
                outputStream?.write(data)
                outputStream?.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send packet: \${e.message}")
                disconnect()
            }
        }.start()
    }

    private fun listenLoop() {
        val headerBuffer = ByteArray(12)
        try {
            while (isConnected && socket?.isConnected == true) {
                val headerRead = readExact(headerBuffer, 12)
                if (headerRead < 12) break
                
                val packet = BinaryPacket.deserialize(headerBuffer) ?: continue
                
                // Read payload length from header
                val bb = ByteBuffer.wrap(headerBuffer).order(ByteOrder.LITTLE_ENDIAN)
                bb.position(8)
                val payloadLen = bb.int
                
                if (payloadLen > 0) {
                    val payloadBuffer = ByteArray(payloadLen)
                    val payloadRead = readExact(payloadBuffer, payloadLen)
                    if (payloadRead < payloadLen) break
                    
                    packet.payload = payloadBuffer
                    
                    if (packet.flags.toInt() and PacketFlags.ENCRYPTED.toInt() != 0) {
                        try {
                            packet.payload = cryptoManager.decrypt(packet.payload)
                        } catch (e: Exception) {
                            Log.e(TAG, "Decryption failed: \${e.message}")
                            continue
                        }
                    }
                }
                
                // Handle internal protocol auth states, or bubble up
                if (packet.type == PacketType.AUTH_CHALLENGE) {
                    val serverPub = cryptoManager.loadServerPublicKey(packet.payload)
                    val sessionKey = cryptoManager.generateSessionKey()
                    val encryptedSession = cryptoManager.encryptSessionKeyWithServerRsa(serverPub)
                    
                    val authResp = BinaryPacket(type = PacketType.AUTH_RESP, payload = encryptedSession)
                    sendPacket(authResp)
                } else if (packet.type == PacketType.HEARTBEAT) {
                    lastHeartbeatAckTime = System.currentTimeMillis()
                } else {
                    onPacketReceived(packet)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Listen loop error: ${e.message}")
        } finally {
            disconnect()
        }
    }

    private var lastHeartbeatAckTime: Long = 0
    private var heartbeatThread: Thread? = null

    private fun startHeartbeat() {
        lastHeartbeatAckTime = System.currentTimeMillis()
        heartbeatThread = Thread {
            while (isConnected) {
                try {
                    Thread.sleep(5000) // 5 seconds interval
                    if (System.currentTimeMillis() - lastHeartbeatAckTime > 15000) {
                        Log.e(TAG, "Heartbeat timeout! Disconnecting.")
                        disconnect()
                        break
                    }
                    sendPacket(BinaryPacket(type = PacketType.HEARTBEAT))
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        heartbeatThread?.start()
    }

    private fun readExact(buffer: ByteArray, length: Int): Int {
        var totalRead = 0
        while (totalRead < length) {
            val read = inputStream?.read(buffer, totalRead, length - totalRead) ?: -1
            if (read == -1) break
            totalRead += read
        }
        return totalRead
    }
}
