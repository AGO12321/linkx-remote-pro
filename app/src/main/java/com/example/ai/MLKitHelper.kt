package com.example.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber

object MLKitHelper {
    private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanQrCode(bitmap: Bitmap): String? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = barcodeScanner.process(image).await()
            if (barcodes.isNotEmpty()) {
                val rawValue = barcodes[0].rawValue
                Timber.d("QR Code scanned successfully: $rawValue")
                rawValue
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error scanning QR code")
            null
        }
    }

    suspend fun recognizeText(bitmap: Bitmap): String? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            val text = result.text
            Timber.d("Text recognition completed. Length: ${text.length}")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error recognizing text")
            null
        }
    }
}
