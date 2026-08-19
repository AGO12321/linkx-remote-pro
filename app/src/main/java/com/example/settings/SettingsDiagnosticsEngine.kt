package com.example.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class AppThemeMode(val label: String, val description: String) {
    CYBERPUNK_DARK("Cyberpunk Cyan (Dark)", "High contrast neon dark aesthetic"),
    MIDNIGHT_OLED("Midnight OLED (Pure Black)", "Deep black pixels optimized for battery"),
    SLATE_GRAY("Slate Gray (Minimal)", "Subtle slate tones for professional use"),
    LIGHT_NEUTRAL("Light Mode (Clean)", "High visibility daylight theme")
}

data class PerformanceTelemetry(
    val cpuUsagePercent: Int = 0,
    val ramUsedMb: Int = 0,
    val totalRamMb: Int = 0,
    val gpuRenderMs: Float = 0f,
    val wifiLatencyMs: Int = 0,
    val frameJitterMs: Float = 0f,
    val currentFps: Int = 0,
    val maxFpsTarget: Int = 60,
    val networkThroughputKbps: Int = 0
)

data class DeveloperOptionsConfig(
    val tcpPort: Int = 8080,
    val udpStreamPort: Int = 8082,
    val isAdbWirelessBridgeEnabled: Boolean = false,
    val isRawPacketLoggingEnabled: Boolean = false,
    val isProtocolBufferDebugMode: Boolean = false,
    val socketBufferSizeKb: Int = 1024,
    val hardwareCodecName: String = ""
)

data class DiagnosticItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val status: String, // "PASSED", "RUNNING", "WARNING", "FAILED", "IDLE"
    val details: String
)

data class SettingsDiagnosticsState(
    val activeTheme: AppThemeMode = AppThemeMode.CYBERPUNK_DARK,
    val isVibrationFeedbackEnabled: Boolean = true,
    val isKeepScreenOnEnabled: Boolean = true,
    val targetFpsCap: Int = 60,
    val performance: PerformanceTelemetry = PerformanceTelemetry(),
    val devOptions: DeveloperOptionsConfig = DeveloperOptionsConfig(),
    val diagnosticResults: List<DiagnosticItem> = emptyList(),
    val isDiagnosticsRunning: Boolean = false
)

object SettingsDiagnosticsEngine {
    private val _state = MutableStateFlow(SettingsDiagnosticsState())
    val state: StateFlow<SettingsDiagnosticsState> = _state.asStateFlow()

    private var perfThread: Thread? = null

    fun initialize(context: Context) {
        detectHardwareCodec()
        detectDeviceMemory(context)
    }

    private fun detectHardwareCodec() {
        val codecName = try {
            val codecList = android.media.MediaCodecList(android.media.MediaCodecList.ALL_CODECS)
            val decoder = codecList.codecInfos.firstOrNull {
                !it.isEncoder && it.supportedTypes.any { type -> type.equals("video/avc", true) }
            }
            decoder?.name ?: "Unknown"
        } catch (e: Exception) {
            "Detection failed"
        }
        _state.value = _state.value.copy(
            devOptions = _state.value.devOptions.copy(hardwareCodecName = codecName)
        )
    }

    private fun detectDeviceMemory(context: Context) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalMb = (memInfo.totalMem / (1024 * 1024)).toInt()
            val availMb = (memInfo.availMem / (1024 * 1024)).toInt()
            _state.value = _state.value.copy(
                performance = _state.value.performance.copy(
                    totalRamMb = totalMb,
                    ramUsedMb = totalMb - availMb
                )
            )
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setTheme(mode: AppThemeMode) {
        _state.value = _state.value.copy(activeTheme = mode)
    }

    fun toggleVibration() {
        _state.value = _state.value.copy(isVibrationFeedbackEnabled = !_state.value.isVibrationFeedbackEnabled)
    }

    fun toggleKeepScreenOn() {
        _state.value = _state.value.copy(isKeepScreenOnEnabled = !_state.value.isKeepScreenOnEnabled)
    }

    fun setTargetFpsCap(fps: Int) {
        _state.value = _state.value.copy(
            targetFpsCap = fps,
            performance = _state.value.performance.copy(maxFpsTarget = fps)
        )
    }

    fun toggleAdbWirelessBridge() {
        val curr = _state.value
        _state.value = curr.copy(devOptions = curr.devOptions.copy(isAdbWirelessBridgeEnabled = !curr.devOptions.isAdbWirelessBridgeEnabled))
    }

    fun toggleRawPacketLogging() {
        val curr = _state.value
        _state.value = curr.copy(devOptions = curr.devOptions.copy(isRawPacketLoggingEnabled = !curr.devOptions.isRawPacketLoggingEnabled))
    }

    fun toggleProtocolBufferDebug() {
        val curr = _state.value
        _state.value = curr.copy(devOptions = curr.devOptions.copy(isProtocolBufferDebugMode = !curr.devOptions.isProtocolBufferDebugMode))
    }

    fun updateTcpPort(port: Int) {
        _state.value = _state.value.copy(devOptions = _state.value.devOptions.copy(tcpPort = port))
    }

    fun runFullDiagnosticSuite(context: Context) {
        _state.value = _state.value.copy(isDiagnosticsRunning = true)
        Thread {
            val results = mutableListOf<DiagnosticItem>()

            // Real: Check device memory
            try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                am.getMemoryInfo(memInfo)
                results.add(DiagnosticItem(
                    name = "Device Memory",
                    category = "Hardware",
                    status = "PASSED",
                    details = "${memInfo.totalMem / (1024*1024)} MB total, ${memInfo.availMem / (1024*1024)} MB available"
                ))
            } catch (e: Exception) {
                results.add(DiagnosticItem(name = "Device Memory", category = "Hardware", status = "FAILED", details = e.message ?: "Unknown error"))
            }

            // Real: Check Android version
            results.add(DiagnosticItem(
                name = "Android Version",
                category = "System",
                status = "PASSED",
                details = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            ))

            // Real: Check hardware codec
            val codecName = _state.value.devOptions.hardwareCodecName
            results.add(DiagnosticItem(
                name = "H.264 Video Decoder",
                category = "Video Engine",
                status = if (codecName.isNotEmpty() && codecName != "Unknown") "PASSED" else "WARNING",
                details = codecName.ifEmpty { "No hardware decoder found" }
            ))

            // Real: Check device model
            results.add(DiagnosticItem(
                name = "Device",
                category = "Hardware",
                status = "PASSED",
                details = "${Build.MANUFACTURER} ${Build.MODEL}"
            ))

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                _state.value = _state.value.copy(
                    isDiagnosticsRunning = false,
                    diagnosticResults = results
                )
            }
        }.start()
    }
}
