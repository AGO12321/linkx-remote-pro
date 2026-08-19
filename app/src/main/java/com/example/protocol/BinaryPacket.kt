package com.example.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class PacketType(val code: Byte) {
    AUTH_REQ(0x01),
    AUTH_CHALLENGE(0x02),
    AUTH_RESP(0x03),
    AUTH_RESULT(0x04),
    HEARTBEAT(0x05),
    DISCONNECT(0x06),
    
    MOUSE_MOVE(0x10),
    MOUSE_CLICK(0x11),
    MOUSE_SCROLL(0x12),
    KEYBOARD_EVENT(0x13),
    TOUCH_EVENT(0x14),
    
    SCREEN_FRAME(0x20),
    AUDIO_FRAME(0x21),
    WEBCAM_FRAME(0x22),
    
    CLIPBOARD_DATA(0x30),
    FILE_TRANSFER_START(0x31),
    FILE_TRANSFER_CHUNK(0x32),
    FILE_TRANSFER_END(0x33),
    
    TERMINAL_CMD(0x40),
    TASK_MANAGER_REQ(0x41),
    POWER_CMD(0x42);

    companion object {
        fun fromCode(code: Byte): PacketType? = values().find { it.code == code }
    }
}

object PacketFlags {
    const val NONE: Byte = 0
    const val ENCRYPTED: Byte = 1
    const val COMPRESSED: Byte = 2
    const val ACK_REQUIRED: Byte = 4
    const val IS_ACK: Byte = 8
    const val FRAGMENTED: Byte = 16
}

class BinaryPacket(
    var type: PacketType = PacketType.HEARTBEAT,
    var flags: Byte = PacketFlags.NONE,
    var sequenceId: Int = 0,
    var payload: ByteArray = ByteArray(0)
) {
    companion object {
        const val MAGIC: Short = 0x4C58 // 'LX'
        const val VERSION: Byte = 1

        fun deserialize(data: ByteArray): BinaryPacket? {
            if (data.size < 12) return null
            
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val magic = buffer.short
            if (magic != MAGIC) return null
            
            val version = buffer.get()
            if (version != VERSION) return null
            
            val typeCode = buffer.get()
            val type = PacketType.fromCode(typeCode) ?: return null
            
            val flags = buffer.get()
            val sequenceId = buffer.int
            val payloadLength = buffer.int
            
            var payload = ByteArray(0)
            if (payloadLength > 0 && buffer.remaining() >= payloadLength) {
                payload = ByteArray(payloadLength)
                buffer.get(payload)
            }
            
            return BinaryPacket(type, flags, sequenceId, payload)
        }
    }

    fun serialize(): ByteArray {
        val payloadLen = payload.size
        val buffer = ByteBuffer.allocate(12 + payloadLen).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.putShort(MAGIC)
        buffer.put(VERSION)
        buffer.put(type.code)
        buffer.put(flags)
        buffer.putInt(sequenceId)
        buffer.putInt(payloadLen)
        
        if (payloadLen > 0) {
            buffer.put(payload)
        }
        
        return buffer.array()
    }
}
