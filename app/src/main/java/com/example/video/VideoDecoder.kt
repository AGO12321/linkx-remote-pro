package com.example.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import timber.log.Timber

interface VideoDecoder {
    fun start(surface: Surface, width: Int, height: Int)
    fun stop()
    fun decodeFrame(data: ByteArray)
}

class MediaCodecVideoDecoder : VideoDecoder {
    private var codec: MediaCodec? = null
    private val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC

    override fun start(surface: Surface, width: Int, height: Int) {
        try {
            val format = MediaFormat.createVideoFormat(mimeType, width, height)
            codec = MediaCodec.createDecoderByType(mimeType).apply {
                configure(format, surface, null, 0)
                start()
            }
            Timber.i("MediaCodecVideoDecoder started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start MediaCodecVideoDecoder")
        }
    }

    override fun stop() {
        try {
            codec?.stop()
            codec?.release()
            codec = null
            Timber.i("MediaCodecVideoDecoder stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaCodecVideoDecoder")
        }
    }

    override fun decodeFrame(data: ByteArray) {
        val currentCodec = codec ?: return
        try {
            val inputBufferIndex = currentCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = currentCodec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(data)
                currentCodec.queueInputBuffer(inputBufferIndex, 0, data.size, System.nanoTime() / 1000, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                // Render to surface
                currentCodec.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = currentCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error decoding video frame")
        }
    }
}
