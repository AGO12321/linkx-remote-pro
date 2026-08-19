package com.example.network

import android.util.Log
import com.example.protocol.BinaryPacket
import com.example.protocol.CryptoManager
import com.example.protocol.PacketFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpTransport(
    private val cryptoManager: CryptoManager,
    private val onPacketReceived: (BinaryPacket) -> Unit
) {
    private val TAG = "UdpTransport"
    private var socket: DatagramSocket? = null
    
    @Volatile
    private var isConnected = false
    private var serverIp: InetAddress? = null
    private var serverPort: Int = 8081

    suspend fun connect(ip: String, port: Int = 8081): Boolean = withContext(Dispatchers.IO) {
        try {
            serverIp = InetAddress.getByName(ip)
            serverPort = port
            socket = DatagramSocket()
            isConnected = true
            
            Thread { listenLoop() }.start()
            true
        } catch (e: Exception) {
            Log.e(TAG, "UDP Connection failed: \${e.message}")
            isConnected = false
            false
        }
    }

    fun disconnect() {
        isConnected = false
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "UDP socket close error: \${e.message}")
        }
    }

    fun sendPacket(packet: BinaryPacket) {
        if (!isConnected || socket == null || serverIp == null) return
        
        Thread {
            try {
                if (packet.flags.toInt() and PacketFlags.ENCRYPTED.toInt() != 0) {
                    packet.payload = cryptoManager.encrypt(packet.payload)
                }
                
                val data = packet.serialize()
                val datagram = DatagramPacket(data, data.size, serverIp, serverPort)
                socket?.send(datagram)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send UDP packet: \${e.message}")
            }
        }.start()
    }

    private fun listenLoop() {
        val buffer = ByteArray(65535)
        try {
            while (isConnected) {
                val datagram = DatagramPacket(buffer, buffer.size)
                socket?.receive(datagram)
                
                val receivedData = datagram.data.copyOfRange(0, datagram.length)
                val packet = BinaryPacket.deserialize(receivedData) ?: continue
                
                if (packet.flags.toInt() and PacketFlags.ENCRYPTED.toInt() != 0) {
                    try {
                        packet.payload = cryptoManager.decrypt(packet.payload)
                    } catch (e: Exception) {
                        Log.e(TAG, "UDP Decryption failed: \${e.message}")
                        continue
                    }
                }
                
                onPacketReceived(packet)
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDP Listen error: \${e.message}")
        } finally {
            disconnect()
        }
    }
}
