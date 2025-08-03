package com.example.app_recetas.src.Core.Hardware.Camara.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.app_recetas.src.Core.Hardware.Camara.domain.CameraRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraRepository {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(context)
    }

    fun initializeCamera(previewView: PreviewView) {
        startCamera(previewView)
    }

    private fun startCamera(previewView: PreviewView) {
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(previewView)
            } catch (exc: Exception) {

            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(previewView: PreviewView) {
        val cameraProvider = cameraProvider ?: return

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {

        }
    }

    override fun isCameraAvailable(): Boolean {
        return try {
            val provider = cameraProviderFuture.get()
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ||
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun takePhoto(outputDirectory: File): Result<File> =
        suspendCancellableCoroutine { continuation ->
            val imageCapture = imageCapture ?: run {
                continuation.resume(Result.failure(IllegalStateException("Cámara no inicializada")))
                return@suspendCancellableCoroutine
            }

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(Result.success(photoFile))
                    }
                }
            )
        }

    override suspend fun takePhotoCompressed(
        outputDirectory: File,
        compressionQuality: Int
    ): Result<File> {
        return try {
            val originalPhotoResult = takePhoto(outputDirectory)

            if (originalPhotoResult.isFailure) {
                return originalPhotoResult
            }

            val originalFile = originalPhotoResult.getOrThrow()
            val compressedFile = compressImage(originalFile, compressionQuality)

            if (compressedFile != null) {
                originalFile.delete()
                Result.success(compressedFile)
            } else {
                Result.success(originalFile)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun compressImage(originalFile: File, quality: Int): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
            val compressedFile = File(
                originalFile.parent,
                "compressed_${originalFile.name}"
            )

            FileOutputStream(compressedFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            bitmap.recycle()
            compressedFile
        } catch (e: Exception) {
            null
        }
    }

    fun shutdown() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        cameraProvider = null
        imageCapture = null
    }
}