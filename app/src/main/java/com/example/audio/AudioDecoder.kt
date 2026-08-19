package com.example.audio

import android.media.MediaCodec
import android.media.MediaFormat
import timber.log.Timber
import java.nio.ByteBuffer

interface AudioDecoder {
    fun start(sampleRate: Int, channels: Int, isAacFallback: Boolean = false)
    fun stop()
    fun decode(encodedData: ByteArray)
    var onPcmDataDecoded: ((ByteArray) -> Unit)?
}

class MediaCodecAudioDecoder : AudioDecoder {
    private var codec: MediaCodec? = null
    override var onPcmDataDecoded: ((ByteArray) -> Unit)? = null
    
    override fun start(sampleRate: Int, channels: Int, isAacFallback: Boolean) {
        val mimeType = if (isAacFallback) MediaFormat.MIMETYPE_AUDIO_AAC else MediaFormat.MIMETYPE_AUDIO_OPUS
        try {
            val format = MediaFormat.createAudioFormat(mimeType, sampleRate, channels)
            codec = MediaCodec.createDecoderByType(mimeType).apply {
                configure(format, null, null, 0)
                start()
            }
            Timber.i("MediaCodecAudioDecoder started with $mimeType")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start MediaCodecAudioDecoder")
        }
    }

    override fun stop() {
        try {
            codec?.stop()
            codec?.release()
            codec = null
            Timber.i("MediaCodecAudioDecoder stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaCodecAudioDecoder")
        }
    }

    override fun decode(encodedData: ByteArray) {
        val currentCodec = codec ?: return
        try {
            val inputBufferIndex = currentCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = currentCodec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(encodedData)
                currentCodec.queueInputBuffer(inputBufferIndex, 0, encodedData.size, System.nanoTime() / 1000, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = currentCodec.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null) {
                    val pcmData = ByteArray(bufferInfo.size)
                    outputBuffer.get(pcmData)
                    onPcmDataDecoded?.invoke(pcmData)
                }
                currentCodec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error decoding audio frame")
        }
    }
}
