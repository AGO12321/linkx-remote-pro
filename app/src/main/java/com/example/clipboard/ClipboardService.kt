package com.example.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import timber.log.Timber

class ClipboardService(private val context: Context) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var lastClipboardText: String = ""
    
    var onClipboardTextChanged: ((String) -> Unit)? = null
    
    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        processClipboardUpdate()
    }

    fun start() {
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener)
        Timber.i("Clipboard synchronization service started.")
    }

    fun stop() {
        clipboardManager.removePrimaryClipChangedListener(clipChangedListener)
    }

    private fun processClipboardUpdate() {
        try {
            if (clipboardManager.hasPrimaryClip()) {
                val clip = clipboardManager.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val newText = clip.getItemAt(0).text?.toString() ?: ""
                    if (newText.isNotEmpty() && newText != lastClipboardText) {
                        lastClipboardText = newText
                        Timber.d("Clipboard text changed, length: ${newText.length}")
                        onClipboardTextChanged?.invoke(newText)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read clipboard contents")
        }
    }

    fun setClipboardText(text: String) {
        if (lastClipboardText == text) return
        
        try {
            lastClipboardText = text
            val clip = ClipData.newPlainText("LinkX Remote Clip", text)
            clipboardManager.setPrimaryClip(clip)
            Timber.d("Updated local clipboard from remote. Length: ${text.length}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set local clipboard contents")
        }
    }
}
