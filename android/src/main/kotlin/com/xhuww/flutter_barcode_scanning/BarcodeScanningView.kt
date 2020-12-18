package com.xhuww.flutter_barcode_scanning

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BarcodeScanningView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleOwner {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val lifecycle = LifecycleRegistry(this)
    private lateinit var previewView: PreviewView

    var onResult: ((Barcode) -> Unit)? = null
    var onError: ((Exception) -> Unit)? = null

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val params = LayoutParams(context, attrs).also {
            it.height = LayoutParams.MATCH_PARENT
            it.width = LayoutParams.MATCH_PARENT
        }
        previewView = PreviewView(context).also { it.layoutParams = params }
        addView(previewView)
        startPreview()
    }

    private fun startPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also { it.setAnalyzer(cameraExecutor, { image -> analyze(image) }) }

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraExecutor.shutdown()
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener { barcodes -> barcodes.forEach { onResult?.invoke(it) } }
                    .addOnFailureListener { onError?.invoke(it) }
                    .addOnCompleteListener { imageProxy.close() }
        }
    }
}
