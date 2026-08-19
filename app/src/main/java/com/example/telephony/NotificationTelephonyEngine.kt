package com.example.telephony

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class PhoneNotification(
    val id: String = UUID.randomUUID().toString(),
    val appName: String,
    val appPackage: String,
    val title: String,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false,
    val categoryLabel: String = ""
)

data class SmsMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderNumber: String,
    val contactName: String,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isFromUser: Boolean = false
)

data class SmsThread(
    val contactName: String,
    val phoneNumber: String,
    val lastMessage: String,
    val lastTimestampMs: Long,
    val messages: List<SmsMessage>
)

enum class CallStatus {
    IDLE,
    INCOMING_RINGING,
    OUTGOING_DIALING,
    ACTIVE_CALL,
    ENDED
}

data class ActiveCallInfo(
    val contactName: String = "",
    val phoneNumber: String = "",
    val status: CallStatus = CallStatus.IDLE,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true
)

data class TelephonyState(
    val notifications: List<PhoneNotification> = emptyList(),
    val smsThreads: List<SmsThread> = emptyList(),
    val activeThreadContact: String = "",
    val activeCall: ActiveCallInfo = ActiveCallInfo(),
    val isNotificationSyncEnabled: Boolean = false,
    val isCallBridgeEnabled: Boolean = false,
    val isFeatureAvailable: Boolean = false // Telephony features require NotificationListenerService
)

object NotificationTelephonyEngine {
    private val _state = MutableStateFlow(TelephonyState())
    val state: StateFlow<TelephonyState> = _state.asStateFlow()

    private var callTimerThread: Thread? = null

    // Called when real notifications arrive from NotificationListenerService
    fun addNotification(notification: PhoneNotification) {
        val curr = _state.value
        _state.value = curr.copy(notifications = listOf(notification) + curr.notifications)
    }

    fun dismissNotification(id: String) {
        val curr = _state.value
        _state.value = curr.copy(notifications = curr.notifications.filter { it.id != id })
    }

    fun clearAllNotifications() {
        _state.value = _state.value.copy(notifications = emptyList())
    }

    fun selectSmsThread(contactName: String) {
        _state.value = _state.value.copy(activeThreadContact = contactName)
    }

    fun sendSmsMessage(contactName: String, text: String) {
        if (text.isBlank()) return
        val curr = _state.value
        val updatedThreads = curr.smsThreads.map { thread ->
            if (thread.contactName == contactName) {
                val newMsg = SmsMessage(
                    senderNumber = thread.phoneNumber,
                    contactName = contactName,
                    text = text,
                    isFromUser = true
                )
                thread.copy(
                    lastMessage = text,
                    lastTimestampMs = System.currentTimeMillis(),
                    messages = thread.messages + newMsg
                )
            } else thread
        }
        _state.value = curr.copy(smsThreads = updatedThreads)
    }

    fun endCall() {
        callTimerThread?.interrupt()
        val curr = _state.value
        _state.value = curr.copy(activeCall = curr.activeCall.copy(status = CallStatus.ENDED))
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _state.value = _state.value.copy(activeCall = ActiveCallInfo(status = CallStatus.IDLE))
        }, 1500)
    }

    fun toggleMuteCall() {
        val curr = _state.value
        _state.value = curr.copy(activeCall = curr.activeCall.copy(isMuted = !curr.activeCall.isMuted))
    }
}
