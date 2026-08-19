package com.example.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class TransferStatus {
    QUEUED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class TransferDirection {
    UPLOAD_TO_HOST,
    DOWNLOAD_FROM_HOST
}

data class TransferTask(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val localPath: String,
    val remotePath: String,
    val fileSizeBytes: Long,
    val bytesTransferred: Long = 0L,
    val status: TransferStatus = TransferStatus.QUEUED,
    val direction: TransferDirection = TransferDirection.UPLOAD_TO_HOST,
    val chunkSizeBytes: Int = 1024 * 1024,
    val checksumSha256: String = "",
    val isAESEncrypted: Boolean = true,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val errorMessage: String? = null,
    val lastCheckpointOffset: Long = 0L
)

data class FileTransferEngineState(
    val tasks: List<TransferTask> = emptyList(),
    val isAES256GCMEnabled: Boolean = true,
    val activeConcurrencyLimit: Int = 3,
    val totalUploadSpeedBytesPerSec: Long = 0L,
    val totalDownloadSpeedBytesPerSec: Long = 0L,
    val autoResumeInterruptedTransfers: Boolean = true
)

object FileTransferEngine {
    private val _state = MutableStateFlow(FileTransferEngineState())
    val state: StateFlow<FileTransferEngineState> = _state.asStateFlow()

    fun enqueueTransfer(
        fileName: String,
        localPath: String,
        remotePath: String,
        fileSizeBytes: Long,
        direction: TransferDirection
    ) {
        val newTask = TransferTask(
            fileName = fileName,
            localPath = localPath,
            remotePath = remotePath,
            fileSizeBytes = fileSizeBytes,
            direction = direction,
            status = TransferStatus.QUEUED
        )
        val curr = _state.value
        _state.value = curr.copy(tasks = curr.tasks + newTask)
    }

    fun updateTaskProgress(taskId: String, bytesTransferred: Long, speedBytesPerSec: Long) {
        updateTask(taskId) { task ->
            val remaining = task.fileSizeBytes - bytesTransferred
            val eta = if (speedBytesPerSec > 0) remaining / speedBytesPerSec else 0L
            if (bytesTransferred >= task.fileSizeBytes) {
                task.copy(
                    bytesTransferred = task.fileSizeBytes,
                    status = TransferStatus.COMPLETED,
                    speedBytesPerSec = 0L,
                    etaSeconds = 0L
                )
            } else {
                task.copy(
                    bytesTransferred = bytesTransferred,
                    speedBytesPerSec = speedBytesPerSec,
                    etaSeconds = eta,
                    lastCheckpointOffset = bytesTransferred
                )
            }
        }
    }

    fun setTaskFailed(taskId: String, errorMessage: String) {
        updateTask(taskId) { task ->
            task.copy(status = TransferStatus.FAILED, errorMessage = errorMessage, speedBytesPerSec = 0L)
        }
    }

    fun pauseTransfer(taskId: String) {
        updateTask(taskId) { task ->
            task.copy(
                status = TransferStatus.PAUSED,
                speedBytesPerSec = 0L,
                etaSeconds = 0L,
                lastCheckpointOffset = task.bytesTransferred
            )
        }
    }

    fun resumeTransfer(taskId: String) {
        updateTask(taskId) { task ->
            task.copy(status = TransferStatus.QUEUED, errorMessage = null)
        }
    }

    fun cancelTransfer(taskId: String) {
        updateTask(taskId) { task ->
            task.copy(status = TransferStatus.CANCELLED, speedBytesPerSec = 0L, etaSeconds = 0L)
        }
    }

    fun clearCompleted() {
        val curr = _state.value
        _state.value = curr.copy(
            tasks = curr.tasks.filter {
                it.status != TransferStatus.COMPLETED && it.status != TransferStatus.CANCELLED
            }
        )
    }

    fun toggleAES256Encryption() {
        val curr = _state.value
        _state.value = curr.copy(isAES256GCMEnabled = !curr.isAES256GCMEnabled)
    }

    fun toggleAutoResume() {
        val curr = _state.value
        _state.value = curr.copy(autoResumeInterruptedTransfers = !curr.autoResumeInterruptedTransfers)
    }

    private fun updateTask(taskId: String, transform: (TransferTask) -> TransferTask) {
        val curr = _state.value
        val updatedList = curr.tasks.map { if (it.id == taskId) transform(it) else it }
        _state.value = curr.copy(tasks = updatedList)
    }

    fun encryptChunk(payload: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(payload)
        return iv + encrypted
    }
}
