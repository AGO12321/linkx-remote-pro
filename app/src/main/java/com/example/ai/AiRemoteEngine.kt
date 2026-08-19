package com.example.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class AiUpscaleMode(val label: String, val scaleFactor: String) {
    BALANCED_2X("AI Balanced (2x Super Resolution)", "2x"),
    ULTRA_4X("AI Ultra Clarity (4x Neural Upscaling)", "4x"),
    PERFORMANCE_OFF("Off (Native Stream Resolution)", "1x")
}

data class VoiceCommandItem(
    val id: String = UUID.randomUUID().toString(),
    val spokenPhrase: String,
    val mappedAction: String,
    val confidence: Float = 0.0f,
    val timestampMs: Long = System.currentTimeMillis()
)

data class OcrScanResult(
    val id: String = UUID.randomUUID().toString(),
    val extractedText: String,
    val boundingBox: String,
    val detectedLanguage: String = "",
    val timestampMs: Long = System.currentTimeMillis()
)

data class BarcodeQrScanResult(
    val id: String = UUID.randomUUID().toString(),
    val rawContent: String,
    val format: String,
    val timestampMs: Long = System.currentTimeMillis()
)

data class AiEngineState(
    val isVoiceListening: Boolean = false,
    val lastVoiceCommand: String = "",
    val voiceHistory: List<VoiceCommandItem> = emptyList(),
    val isBandwidthOptimizationEnabled: Boolean = false,
    val currentOptimizedBitrateKbps: Int = 0,
    val networkBandwidthSavedPercent: Int = 0,
    val upscaleMode: AiUpscaleMode = AiUpscaleMode.PERFORMANCE_OFF,
    val isGesturePredictionEnabled: Boolean = false,
    val touchLatencyReductionMs: Float = 0f,
    val ocrResults: List<OcrScanResult> = emptyList(),
    val qrBarcodeResults: List<BarcodeQrScanResult> = emptyList(),
    val isFeatureAvailable: Boolean = false // AI features are not yet implemented
)

object AiRemoteEngine {
    private val _state = MutableStateFlow(AiEngineState())
    val state: StateFlow<AiEngineState> = _state.asStateFlow()

    fun toggleVoiceListening() {
        // Not yet implemented - no fake simulation
    }

    fun toggleBandwidthOptimization() {
        val curr = _state.value
        _state.value = curr.copy(isBandwidthOptimizationEnabled = !curr.isBandwidthOptimizationEnabled)
    }

    fun setUpscaleMode(mode: AiUpscaleMode) {
        _state.value = _state.value.copy(upscaleMode = mode)
    }

    fun toggleGesturePrediction() {
        val curr = _state.value
        _state.value = curr.copy(isGesturePredictionEnabled = !curr.isGesturePredictionEnabled)
    }

    fun triggerOcrScan() {
        // Not yet implemented - no fake data
    }

    fun triggerQrBarcodeScan() {
        // Not yet implemented - no fake data
    }

    fun clearOcrHistory() {
        _state.value = _state.value.copy(ocrResults = emptyList())
    }

    fun clearQrHistory() {
        _state.value = _state.value.copy(qrBarcodeResults = emptyList())
    }
}
