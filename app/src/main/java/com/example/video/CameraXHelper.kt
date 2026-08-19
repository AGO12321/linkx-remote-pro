package com.example.video

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.util.concurrent.Executors

class CameraXHelper(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageAnalysis: ImageAnalysis? = null
    var onFrameCaptured: ((ImageProxy) -> Unit)? = null

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera(isFrontCamera: Boolean = true) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { image ->
                            onFrameCaptured?.invoke(image)
                            // IMPORTANT: The consumer must close the image: image.close()
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)

                Timber.i("CameraX started successfully (${if(isFrontCamera) "Front" else "Back"})")
            } catch (e: Exception) {
                Timber.e(e, "CameraX initialization failed.")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider.unbindAll()
            Timber.i("CameraX stopped.")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping CameraX")
        }
    }
}
