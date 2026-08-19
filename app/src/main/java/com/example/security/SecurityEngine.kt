package com.example.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.util.UUID

enum class BiometricType {
    FINGERPRINT,
    FACE_UNLOCK,
    BIOMETRIC_NONE
}

enum class SecurityLevel(val label: String) {
    ULTRA_SECURE("Ultra Secure (E2EE + Biometric + OTP)"),
    STANDARD("Standard Encryption (AES-256-GCM)"),
    LEGACY("Legacy Direct Connect (LAN Only)")
}

data class TrustedDevice(
    val id: String = UUID.randomUUID().toString(),
    val deviceName: String,
    val ipAddress: String,
    val macAddress: String,
    val rsaPublicKeyFingerprint: String,
    val pairedTimestampMs: Long = System.currentTimeMillis(),
    val lastActiveTimestampMs: Long = System.currentTimeMillis(),
    val isTrusted: Boolean = true
)

data class SecuritySessionLog(
    val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val eventType: String,
    val sourceIp: String,
    val deviceName: String,
    val cipherSuite: String = "AES-256-GCM / RSA-4096",
    val status: String = "SUCCESS"
)

data class SecurityState(
    val isAES256GCMEnabled: Boolean = true,
    val isRSA4096KeyPairGenerated: Boolean = false,
    val rsaPublicKeyFingerprint: String = "",
    val sessionSha256Checksum: String = "",
    val isBiometricAuthRequired: Boolean = false,
    val activeBiometricType: BiometricType = BiometricType.BIOMETRIC_NONE,
    val isFaceUnlockSupported: Boolean = false,
    val activeOtpPin: String = "",
    val otpRemainingSeconds: Int = 0,
    val securityLevel: SecurityLevel = SecurityLevel.STANDARD,
    val trustedDevices: List<TrustedDevice> = emptyList(),
    val sessionLogs: List<SecuritySessionLog> = emptyList()
)

object SecurityEngine {
    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    private var otpTimerThread: Thread? = null

    fun initialize() {
        generateRsaKeyPair()
        regenerateOtpPin()
    }

    private fun generateRsaKeyPair() {
        try {
            val keyPairGen = KeyPairGenerator.getInstance("RSA")
            keyPairGen.initialize(4096)
            val keyPair = keyPairGen.generateKeyPair()
            val publicKeyBytes = keyPair.public.encoded
            val digest = MessageDigest.getInstance("SHA-256")
            val fingerprint = digest.digest(publicKeyBytes)
                .joinToString("") { "%02x".format(it) }

            _state.value = _state.value.copy(
                isRSA4096KeyPairGenerated = true,
                rsaPublicKeyFingerprint = "SHA256:$fingerprint"
            )
            addLog("RSA-4096 Key Pair Generated", "Local Device", "Android Phone", "SUCCESS")
        } catch (e: Exception) {
            addLog("RSA Key Generation Failed", "Local Device", "Android Phone", "FAILED: ${e.message}")
        }
    }

    fun toggleBiometricRequirement() {
        val curr = _state.value
        _state.value = curr.copy(isBiometricAuthRequired = !curr.isBiometricAuthRequired)
        addLog("Biometric Auth Policy Changed", "Local Device", "Android Phone",
            if (!curr.isBiometricAuthRequired) "ENABLED" else "DISABLED")
    }

    fun setBiometricType(type: BiometricType) {
        _state.value = _state.value.copy(activeBiometricType = type)
    }

    fun regenerateOtpPin() {
        val newPin = (100000..999999).random().toString()
        _state.value = _state.value.copy(activeOtpPin = newPin, otpRemainingSeconds = 60)
        startOtpCountdown()
    }

    fun addTrustedDevice(deviceName: String, ipAddress: String, fingerprint: String) {
        val device = TrustedDevice(
            deviceName = deviceName,
            ipAddress = ipAddress,
            macAddress = "",
            rsaPublicKeyFingerprint = fingerprint
        )
        val curr = _state.value
        _state.value = curr.copy(trustedDevices = curr.trustedDevices + device)
        addLog("Device Paired", ipAddress, deviceName, "TRUSTED")
    }

    fun revokeDeviceTrust(deviceId: String) {
        val curr = _state.value
        val updatedDevices = curr.trustedDevices.map {
            if (it.id == deviceId) it.copy(isTrusted = false) else it
        }
        _state.value = curr.copy(trustedDevices = updatedDevices)
        addLog("Device Trust Revoked", "", "", "REVOKED")
    }

    fun grantDeviceTrust(deviceId: String) {
        val curr = _state.value
        val updatedDevices = curr.trustedDevices.map {
            if (it.id == deviceId) it.copy(isTrusted = true) else it
        }
        _state.value = curr.copy(trustedDevices = updatedDevices)
        addLog("Device Trust Granted", "", "", "TRUSTED")
    }

    fun clearAuditLogs() {
        _state.value = _state.value.copy(sessionLogs = emptyList())
    }

    fun addLog(eventType: String, sourceIp: String, deviceName: String, status: String) {
        val curr = _state.value
        val newLog = SecuritySessionLog(
            eventType = eventType,
            sourceIp = sourceIp,
            deviceName = deviceName,
            status = status
        )
        _state.value = curr.copy(sessionLogs = (listOf(newLog) + curr.sessionLogs).take(100))
    }

    private fun startOtpCountdown() {
        otpTimerThread?.interrupt()
        otpTimerThread = Thread {
            while (true) {
                try {
                    Thread.sleep(1000)
                    val curr = _state.value
                    if (curr.otpRemainingSeconds > 1) {
                        _state.value = curr.copy(otpRemainingSeconds = curr.otpRemainingSeconds - 1)
                    } else {
                        val newPin = (100000..999999).random().toString()
                        _state.value = curr.copy(activeOtpPin = newPin, otpRemainingSeconds = 60)
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        otpTimerThread?.isDaemon = true
        otpTimerThread?.start()
    }
}
