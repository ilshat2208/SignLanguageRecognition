package com.example.signlanguage

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.signlanguage.ml.SignClassifier
import com.example.signlanguage.ml.ClassificationResult
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvResult: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var classifier: SignClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        tvResult = findViewById(R.id.tvResult)
        tvConfidence = findViewById(R.id.tvConfidence)

        println("📱 Создаем SignClassifier...")
        classifier = SignClassifier(this)

        checkPermissions()
    }

    private fun checkPermissions() {
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        startCamera()
                    } else {
                        Toast.makeText(this@CameraActivity,
                            "Для работы приложения необходим доступ к камере",
                            Toast.LENGTH_LONG).show()
                        finish()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Настройка превью
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Настройка анализатора изображений
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            // Выбор задней камеры
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        // Конвертация ImageProxy в Bitmap
        val bitmap = imageProxy.toBitmap()
        imageProxy.close()

        if (bitmap != null) {
            // Классификация изображения
            val result = classifier.classify(bitmap)

            // Обновление UI
            runOnUiThread {
                tvResult.text = "Жест: ${result.label}"
                tvConfidence.text = "Уверенность: ${String.format("%.1f", result.confidence * 100)}%"
            }
        }
    }

    // Функция конвертации ImageProxy в Bitmap
    private fun ImageProxy.toBitmap(): android.graphics.Bitmap? {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        // Конвертация YUV в RGB (упрощенная версия)
        val yuvImage = android.graphics.YuvImage(bytes, android.graphics.ImageFormat.NV21, width, height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()

        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}