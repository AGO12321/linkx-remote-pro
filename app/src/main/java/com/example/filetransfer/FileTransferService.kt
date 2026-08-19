package com.example.filetransfer

import android.content.Context
import android.os.Environment
import com.example.protocol.BinaryPacket
import com.example.protocol.PacketType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

class FileTransferService(private val context: Context) {
    private val chunkSize = 64 * 1024
    var onTransferProgress: ((String, Long, Long) -> Unit)? = null

    private var receivingFile: RandomAccessFile? = null
    private var receivingFilePath: String = ""
    private var expectedSha256: String = ""

    suspend fun sendFile(file: File, sendPacket: (BinaryPacket) -> Unit) = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext

        try {
            val fileName = file.name
            val fileSize = file.length()
            val sha256 = computeSha256(file)

            val startPayload = "$fileName|$fileSize|$sha256".toByteArray(Charsets.UTF_8)
            sendPacket(BinaryPacket(type = PacketType.FILE_TRANSFER_START, payload = startPayload))

            var totalBytesRead = 0L
            var sequence = 0
            file.inputStream().use { input ->
                val buffer = ByteArray(chunkSize)
                var bytesRead = input.read(buffer)
                while (bytesRead > 0 && coroutineContext.isActive) {
                    val payload = ByteBuffer.allocate(8 + bytesRead).order(ByteOrder.LITTLE_ENDIAN)
                    payload.putLong(totalBytesRead)
                    payload.put(buffer, 0, bytesRead)

                    sendPacket(
                        BinaryPacket(
                            type = PacketType.FILE_TRANSFER_CHUNK,
                            sequenceId = sequence++,
                            payload = payload.array()
                        )
                    )

                    totalBytesRead += bytesRead
                    onTransferProgress?.invoke(fileName, totalBytesRead, fileSize)
                    bytesRead = input.read(buffer)
                }
            }

            sendPacket(
                BinaryPacket(
                    type = PacketType.FILE_TRANSFER_END,
                    payload = fileName.toByteArray(Charsets.UTF_8)
                )
            )
            Timber.i("File sent successfully: $fileName")
        } catch (e: Exception) {
            Timber.e(e, "File transfer failed for ${file.name}")
        }
    }

    fun handleTransferStart(payload: ByteArray) {
        try {
            val startData = String(payload, Charsets.UTF_8)
            val parts = startData.split("|")
            if (parts.size >= 3) {
                val fileName = parts[0]
                val fileSize = parts[1].toLong()
                expectedSha256 = parts[2]

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val file = File(downloadsDir, fileName)
                receivingFilePath = file.absolutePath
                receivingFile = RandomAccessFile(file, "rw")
                
                Timber.i("Started receiving file: $fileName ($fileSize bytes)")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling FILE_TRANSFER_START")
        }
    }

    fun handleTransferChunk(payload: ByteArray) {
        try {
            receivingFile?.let { file ->
                val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
                val offset = buffer.long
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                
                file.seek(offset)
                file.write(data)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling FILE_TRANSFER_CHUNK")
        }
    }

    fun handleTransferEnd(payload: ByteArray) {
        try {
            receivingFile?.close()
            receivingFile = null

            val actualSha256 = computeSha256(File(receivingFilePath))
            if (actualSha256 == expectedSha256) {
                Timber.i("File received and verified successfully: $receivingFilePath")
            } else {
                Timber.w("File integrity verification failed for $receivingFilePath")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling FILE_TRANSFER_END")
        }
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead = fis.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = fis.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
