package com.example.mirroring

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class VideoCodec(val displayName: String, val mimeType: String) {
    H264_AVC("H.264 / AVC (Default)", "video/avc"),
    H265_HEVC("H.265 / HEVC (High Efficiency)", "video/hevc"),
    AV1("AV1 (Next-Gen Ultra Low Bandwidth)", "video/av01")
}

enum class ResolutionProfile(val label: String, val width: Int, val height: Int, val defaultFps: Int) {
    RES_720P_120FPS("720p @ 120 FPS (Ultra Low Latency Gaming)", 1280, 720, 120),
    RES_1080P_60FPS("1080p @ 60 FPS (Full HD Standard)", 1920, 1080, 60),
    RES_1440P_60FPS("1440p @ 60 FPS (2K Quad HD)", 2560, 1440, 60),
    RES_4K_30FPS("4K @ 30 FPS (Ultra HD Clarity)", 3840, 2160, 30)
}

enum class AudioCaptureMode(val title: String) {
    MUTE("Muted"),
    SYSTEM_AUDIO("Internal System Audio (Loopback)"),
    MIC_PLUS_SYSTEM("System Audio + Microphone Mix")
}

enum class MirroringState {
    IDLE,
    PROJECTION_GRANTED,
    INITIALIZING_ENCODER,
    STREAMING,
    PAUSED,
    ERROR
}

data class MirroringConfig(
    val codec: VideoCodec = VideoCodec.H264_AVC,
    val resolution: ResolutionProfile = ResolutionProfile.RES_1080P_60FPS,
    val fps: Int = 60,
    val targetBitrateKbps: Int = 8000,
    val isAdaptiveBitrateEnabled: Boolean = true,
    val isHardwareAccelerated: Boolean = true,
    val isUltraLowLatencyMode: Boolean = true,
    val audioMode: AudioCaptureMode = AudioCaptureMode.SYSTEM_AUDIO
)

data class MirroringTelemetry(
    val actualFps: Int = 59,
    val currentBitrateKbps: Int = 7850,
    val encoderDelayMs: Int = 4,
    val networkRttMs: Int = 8,
    val totalFramesSent: Long = 1420,
    val droppedFrames: Long = 2,
    val hardwareEncoderName: String = "c2.qcom.avc.encoder (Hardware Accelerated)",
    val keyFrameIntervalSec: Int = 1
)

data class MirroringEngineState(
    val mirroringState: MirroringState = MirroringState.IDLE,
    val config: MirroringConfig = MirroringConfig(),
    val telemetry: MirroringTelemetry = MirroringTelemetry(),
    val projectionVirtualDisplayId: String? = null,
    val statusMessage: String = "MediaProjection Ready. Press Start Screen Capture."
)

object ScreenMirroringEngine {
    private val _state = MutableStateFlow(MirroringEngineState())
    val state: StateFlow<MirroringEngineState> = _state.asStateFlow()

    private var telemetryThread: Thread? = null
    private var isSimulatingStream = false

    fun startProjection() {
        val curr = _state.value
        _state.value = curr.copy(
            mirroringState = MirroringState.INITIALIZING_ENCODER,
            statusMessage = "Initializing MediaCodec HW Encoder (${curr.config.codec.displayName})..."
        )

        // Simulate Hardware MediaCodec pipeline setup
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _state.value = _state.value.copy(
                mirroringState = MirroringState.STREAMING,
                projectionVirtualDisplayId = "VD-${UUID.randomUUID().toString().take(8)}",
                statusMessage = "Streaming 1080p video over MediaProjection HW Surface to Windows Host."
            )
            startTelemetrySimulation()
        }, 1200)
    }

    fun stopProjection() {
        isSimulatingStream = false
        telemetryThread?.interrupt()
        _state.value = _state.value.copy(
            mirroringState = MirroringState.IDLE,
            projectionVirtualDisplayId = null,
            statusMessage = "Screen mirroring stopped."
        )
    }

    fun updateConfig(newConfig: MirroringConfig) {
        val hwName = when (newConfig.codec) {
            VideoCodec.H264_AVC -> "c2.qcom.avc.encoder (Snapdragon HW)"
            VideoCodec.H265_HEVC -> "c2.qcom.hevc.encoder (Snapdragon HW)"
            VideoCodec.AV1 -> "c2.android.av1.encoder (Software/HW Hybrid)"
        }
        val curr = _state.value
        _state.value = curr.copy(
            config = newConfig,
            telemetry = curr.telemetry.copy(
                currentBitrateKbps = newConfig.targetBitrateKbps,
                hardwareEncoderName = hwName
            )
        )
    }

    fun setBitrate(bitrateKbps: Int) {
        val curr = _state.value
        val updatedConfig = curr.config.copy(targetBitrateKbps = bitrateKbps)
        _state.value = curr.copy(
            config = updatedConfig,
            telemetry = curr.telemetry.copy(currentBitrateKbps = bitrateKbps)
        )
    }

    fun toggleAdaptiveBitrate() {
        val curr = _state.value
        val updatedConfig = curr.config.copy(isAdaptiveBitrateEnabled = !curr.config.isAdaptiveBitrateEnabled)
        _state.value = curr.copy(config = updatedConfig)
    }

    fun toggleUltraLowLatency() {
        val curr = _state.value
        val updatedConfig = curr.config.copy(isUltraLowLatencyMode = !curr.config.isUltraLowLatencyMode)
        _state.value = curr.copy(config = updatedConfig)
    }

    private fun startTelemetrySimulation() {
        isSimulatingStream = true
        telemetryThread = Thread {
            var frameCount = 1420L
            while (isSimulatingStream) {
                try {
                    Thread.sleep(1000)
                    val curr = _state.value
                    if (curr.mirroringState == MirroringState.STREAMING) {
                        frameCount += curr.config.fps
                        val jitterFps = (curr.config.fps - 2..curr.config.fps).random()
                        val dynamicBitrate = if (curr.config.isAdaptiveBitrateEnabled) {
                            (curr.config.targetBitrateKbps - 300..curr.config.targetBitrateKbps + 200).random()
                        } else {
                            curr.config.targetBitrateKbps
                        }
                        val latency = if (curr.config.isUltraLowLatencyMode) (3..6).random() else (12..18).random()

                        _state.value = curr.copy(
                            telemetry = curr.telemetry.copy(
                                actualFps = jitterFps,
                                currentBitrateKbps = dynamicBitrate,
                                encoderDelayMs = latency,
                                networkRttMs = (6..12).random(),
                                totalFramesSent = frameCount,
                                droppedFrames = curr.telemetry.droppedFrames + if (Math.random() < 0.1) 1 else 0
                            )
                        )
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        telemetryThread?.start()
    }
}
