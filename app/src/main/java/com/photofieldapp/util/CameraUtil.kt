package com.photofieldapp.util

import android.content.Context
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraUtil private constructor(
    private val context: Context,
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    private val cameraExecutor: ExecutorService
) {

    companion object {
        @Volatile
        private var INSTANCE: CameraUtil? = null

        fun getInstance(
            context: Context,
            cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
        ): CameraUtil {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CameraUtil(
                    context.applicationContext,
                    cameraProviderFuture,
                    Executors.newSingleThreadExecutor()
                ).also { INSTANCE = it }
            }
        }
    }

    interface CameraSetupCallback {
        fun onCameraStarted()
        fun onError(error: String)
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        imageCapture: ImageCapture,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        enableAutoFocus: Boolean = true,
        callback: CameraSetupCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cameraProvider = cameraProviderFuture.await()

                // Unbind any existing use cases
                cameraProvider.unbindAll()

                // Configure Preview
                val preview = Preview.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // Configure ImageCapture with autofocus
                val imageCaptureBuilder = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                    .setTargetRotation(previewView.display.rotation)

                // Enable autofocus if requested
                if (enableAutoFocus) {
                    imageCaptureBuilder.setTargetResolution(Size(1920, 1080))
                }

                val configuredImageCapture = imageCaptureBuilder.build()

                // Bind use cases to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    configuredImageCapture
                )

                // Apply autofocus after binding
                if (enableAutoFocus) {
                    cameraExecutor.execute {
                        try {
                            val camera = cameraProvider.getCamera(cameraSelector)
                            camera.cameraControl.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    camera.cameraInfo.getMeteringPointFactory()
                                        .createPoint(
                                            previewView.width / 2f,
                                            previewView.height / 2f
                                        )
                                ).build()
                            )
                        } catch (e: Exception) {
                            Log.w("CameraUtil", "Autofocus trigger failed", e)
                        }
                    }
                }

                // Update the imageCapture reference (caller needs to handle this)
                // Note: In Compose, we'd use a different pattern

                // Callback on main thread
                android.os.Handler(context.mainLooper).post {
                    callback.onCameraStarted()
                }

            } catch (e: Exception) {
                Log.e("CameraUtil", "Camera bind failed", e)
                android.os.Handler(context.mainLooper).post {
                    callback.onError(e.message ?: "Unknown camera error")
                }
            }
        }
    }

    fun takePicture(
        imageCapture: ImageCapture,
        outputFileOptions: ImageCapture.OutputFileOptions,
        onSuccess: (ImageCapture.OutputFileResults) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSuccess(output)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })
    }

    fun takePictureInMemory(
        imageCapture: ImageCapture,
        onSuccess: (ImageProxy) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        imageCapture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback {
            override fun onCaptureSuccess(image: ImageProxy) {
                onSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })
    }

    fun switchCamera(
        currentSelector: CameraSelector,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        imageCapture: ImageCapture,
        callback: CameraSetupCallback
    ): CameraSelector {
        val newSelector = when (currentSelector) {
            CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> CameraSelector.DEFAULT_BACK_CAMERA
        }
        // Rebind with new selector
        bindCamera(lifecycleOwner, previewView, imageCapture, newSelector, true, callback)
        return newSelector
    }

    fun setFlashMode(imageCapture: ImageCapture, mode: Int) {
        imageCapture.flashMode = mode
    }

    fun setZoomRatio(cameraProvider: ProcessCameraProvider, cameraSelector: CameraSelector, zoomRatio: Float) {
        cameraExecutor.execute {
            try {
                val camera = cameraProvider.getCamera(cameraSelector)
                val cameraInfo = camera.cameraInfo
                val zoomState = cameraInfo.zoomState.value
                if (zoomState != null) {
                    val maxZoom = zoomState.maxZoomRatio
                    val clampedZoom = zoomRatio.coerceIn(1f, maxZoom)
                    camera.cameraControl.setZoomRatio(clampedZoom)
                }
            } catch (e: Exception) {
                Log.w("CameraUtil", "Zoom failed", e)
            }
        }
    }

    fun getZoomRange(cameraProvider: ProcessCameraProvider, cameraSelector: CameraSelector): Pair<Float, Float>? {
        try {
            val camera = cameraProvider.getCamera(cameraSelector)
            val zoomState = camera.cameraInfo.zoomState.value
            return zoomState?.let { Pair(it.minZoomRatio, it.maxZoomRatio) }
        } catch (e: Exception) {
            return null
        }
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}