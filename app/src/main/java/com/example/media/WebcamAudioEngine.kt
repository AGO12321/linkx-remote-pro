package com.example.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class CameraSource(val displayName: String, val sensorId: String) {
    BACK_MAIN("Primary 50MP Lens (Back)", "0"),
    BACK_ULTRA_WIDE("120° Ultra-Wide Lens (Back)", "1"),
    FRONT_SELFIE("32MP Portrait Lens (Front)", "2")
}

enum class CameraResolution(val label: String, val width: Int, val height: Int, val fps: Int) {
    RES_1080P_60("1080p @ 60 FPS (FHD Crystal)", 1920, 1080, 60),
    RES_4K_30("4K @ 30 FPS (UHD Ultra)", 3840, 2160, 30),
    RES_720P_120("720p @ 120 FPS (High Speed Stream)", 1280, 720, 120)
}

enum class AudioStreamDirection {
    PHONE_MIC_TO_PC,
    PC_SPEAKER_TO_PHONE,
    BIDIRECTIONAL_DUPLEX
}

data class WebcamConfig(
    val cameraSource: CameraSource = CameraSource.BACK_MAIN,
    val resolution: CameraResolution = CameraResolution.RES_1080P_60,
    val isTorchOn: Boolean = false,
    val isBokehBlurEnabled: Boolean = true,
    val isAutoFramingEnabled: Boolean = true,
    val isWebcamActive: Boolean = false,
    val virtualDriverName: String = "LinkX DirectShow Virtual Cam (Windows 11)"
)

data class AudioStreamingConfig(
    val isMicActive: Boolean = true,
    val isSpeakerActive: Boolean = true,
    val micGainDb: Float = 6.0f,
    val isNoiseSuppressionEnabled: Boolean = true,
    val isEchoCancellationEnabled: Boolean = true,
    val sampleRateHz: Int = 48000,
    val bitrateKbps: Int = 256,
    val direction: AudioStreamDirection = AudioStreamDirection.BIDIRECTIONAL_DUPLEX
)

data class MediaStreamingState(
    val webcam: WebcamConfig = WebcamConfig(),
    val audio: AudioStreamingConfig = AudioStreamingConfig(),
    val currentFps: Int = 60,
    val videoBitrateKbps: Int = 12500,
    val audioLatencyMs: Int = 5,
    val statusMessage: String = "Webcam & Low-Latency Audio Driver Standby"
)

object WebcamAudioEngine {
    private val _state = MutableStateFlow(MediaStreamingState())
    val state: StateFlow<MediaStreamingState> = _state.asStateFlow()

    private var streamThread: Thread? = null
    private var isStreaming = false

    fun startWebcam() {
        val curr = _state.value
        val updatedCam = curr.webcam.copy(isWebcamActive = true)
        _state.value = curr.copy(
            webcam = updatedCam,
            statusMessage = "Streaming ${updatedCam.resolution.label} to Windows virtual camera driver..."
        )
        startStreamSimulation()
    }

    fun stopWebcam() {
        isStreaming = false
        streamThread?.interrupt()
        val curr = _state.value
        _state.value = curr.copy(
            webcam = curr.webcam.copy(isWebcamActive = false),
            statusMessage = "Webcam stream stopped."
        )
    }

    fun updateCameraSource(source: CameraSource) {
        val curr = _state.value
        _state.value = curr.copy(webcam = curr.webcam.copy(cameraSource = source))
    }

    fun updateResolution(res: CameraResolution) {
        val curr = _state.value
        _state.value = curr.copy(webcam = curr.webcam.copy(resolution = res), currentFps = res.fps)
    }

    fun toggleTorch() {
        val curr = _state.value
        _state.value = curr.copy(webcam = curr.webcam.copy(isTorchOn = !curr.webcam.isTorchOn))
    }

    fun toggleBokehBlur() {
        val curr = _state.value
        _state.value = curr.copy(webcam = curr.webcam.copy(isBokehBlurEnabled = !curr.webcam.isBokehBlurEnabled))
    }

    fun toggleAutoFraming() {
        val curr = _state.value
        _state.value = curr.copy(webcam = curr.webcam.copy(isAutoFramingEnabled = !curr.webcam.isAutoFramingEnabled))
    }

    fun toggleMicrophone() {
        val curr = _state.value
        val updatedAudio = curr.audio.copy(isMicActive = !curr.audio.isMicActive)
        _state.value = curr.copy(audio = updatedAudio)
    }

    fun toggleSpeaker() {
        val curr = _state.value
        val updatedAudio = curr.audio.copy(isSpeakerActive = !curr.audio.isSpeakerActive)
        _state.value = curr.copy(audio = updatedAudio)
    }

    fun setMicGain(gainDb: Float) {
        val curr = _state.value
        _state.value = curr.copy(audio = curr.audio.copy(micGainDb = gainDb))
    }

    fun toggleNoiseSuppression() {
        val curr = _state.value
        _state.value = curr.copy(audio = curr.audio.copy(isNoiseSuppressionEnabled = !curr.audio.isNoiseSuppressionEnabled))
    }

    private fun startStreamSimulation() {
        isStreaming = true
        streamThread = Thread {
            while (isStreaming) {
                try {
                    Thread.sleep(1000)
                    val curr = _state.value
                    if (curr.webcam.isWebcamActive) {
                        val jitterFps = (curr.webcam.resolution.fps - 1..curr.webcam.resolution.fps).random()
                        val dynamicBitrate = (11800..13100).random()
                        _state.value = curr.copy(
                            currentFps = jitterFps,
                            videoBitrateKbps = dynamicBitrate,
                            audioLatencyMs = (4..7).random()
                        )
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        streamThread?.start()
    }
}
