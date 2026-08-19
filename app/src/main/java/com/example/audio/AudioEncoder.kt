package com.example.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import timber.log.Timber
import java.nio.ByteBuffer

interface AudioEncoder {
    fun start(sampleRate: Int, channels: Int, bitrate: Int)
    fun stop()
    fun encode(pcmData: ByteArray)
    var onFrameEncoded: ((ByteArray) -> Unit)?
}

class MediaCodecAudioEncoder : AudioEncoder {
    private var codec: MediaCodec? = null
    override var onFrameEncoded: ((ByteArray) -> Unit)? = null
    
    // Try OPUS first, fallback to AAC
    private var mimeType = MediaFormat.MIMETYPE_AUDIO_OPUS

    override fun start(sampleRate: Int, channels: Int, bitrate: Int) {
        try {
            var format = MediaFormat.createAudioFormat(mimeType, sampleRate, channels).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            }
            
            try {
                codec = MediaCodec.createEncoderByType(mimeType)
            } catch (e: Exception) {
                Timber.w("Opus encoding not supported on this device. Falling back to AAC.")
                mimeType = MediaFormat.MIMETYPE_AUDIO_AAC
                format = MediaFormat.createAudioFormat(mimeType, sampleRate, channels).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                }
                codec = MediaCodec.createEncoderByType(mimeType)
            }

            codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec?.start()
            Timber.i("MediaCodecAudioEncoder started with $mimeType")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start MediaCodecAudioEncoder")
        }
    }

    override fun stop() {
        try {
            codec?.stop()
            codec?.release()
            codec = null
            Timber.i("MediaCodecAudioEncoder stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaCodecAudioEncoder")
        }
    }

    override fun encode(pcmData: ByteArray) {
        val currentCodec = codec ?: return
        try {
            val inputBufferIndex = currentCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = currentCodec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(pcmData)
                currentCodec.queueInputBuffer(inputBufferIndex, 0, pcmData.size, System.nanoTime() / 1000, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = currentCodec.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null) {
                    val outData = ByteArray(bufferInfo.size)
                    outputBuffer.get(outData)
                    onFrameEncoded?.invoke(outData)
                }
                currentCodec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error encoding audio frame")
        }
    }
}
