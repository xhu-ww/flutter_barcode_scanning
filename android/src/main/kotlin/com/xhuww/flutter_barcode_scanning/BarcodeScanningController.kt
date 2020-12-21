package com.xhuww.flutter_barcode_scanning

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import java.util.*
import java.util.concurrent.Executors


class BarcodeScanningController(
        private val activity: Activity,
        private val flutterTexture: SurfaceTextureEntry
) : LifecycleOwner {
    private val lifecycle = LifecycleRegistry(this)

    val imageAnalyzer = ImageAnalysis.Builder().build()
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = ContextCompat.getMainExecutor(activity.baseContext)
    override fun getLifecycle(): Lifecycle = lifecycle

    fun initialize(result: MethodChannel.Result) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val preview = Preview.Builder().build()
                    .apply {
                        setSurfaceProvider { request ->
                            val surface = Surface(flutterTexture.surfaceTexture())
                            request.provideSurface(surface, mainExecutor, { })
                        }
                    }

            cameraProviderFuture.get().runCatching {
                unbindAll()
                bindToLifecycle(
                        this@BarcodeScanningController,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                )
            }
        }, mainExecutor)

        lifecycle.currentState = Lifecycle.State.RESUMED
        val reply = mapOf(Pair("textureId", flutterTexture.id()))
        result.success(reply)
    }

    fun startBarcodeAnalyzerStream(streamChannel: EventChannel) {
        streamChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                imageAnalyzer.setAnalyzer(cameraExecutor, { image -> analyze(image, events) })
            }

            override fun onCancel(arguments: Any?) {
                imageAnalyzer.clearAnalyzer()
            }
        })
    }

    fun stopBarcodeAnalyzerStream(streamChannel: EventChannel) {
        imageAnalyzer.clearAnalyzer()
        streamChannel.setStreamHandler(null)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun analyze(imageProxy: ImageProxy, events: EventChannel.EventSink) {
        val image = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        BarcodeScanning.getClient().process(inputImage)
                .addOnSuccessListener {
                    val barcodeList = ArrayList<Map<String, Any>>()
                    for (barcode in it) {
                        barcode.run {
                            val barcodeMap = HashMap<String, Any>()
                            boundingBox?.run {
                                barcodeMap["left"] = left.toDouble()
                                barcodeMap["top"] = top.toDouble()
                                barcodeMap["width"] = width().toDouble()
                                barcodeMap["height"] = height().toDouble()
                            }

                            val points = cornerPoints
                                    ?.map { point -> doubleArrayOf(point.x.toDouble(), point.y.toDouble()) }
                                    ?.toList()
                                    ?: ArrayList()
                            barcodeMap["points"] = points
                            barcodeMap["rawValue"] = rawValue ?: ""
                            barcodeList.add(barcodeMap)
                        }
                    }
                    events.success(barcodeList)
                }
                .addOnFailureListener {
                    events.error("barcodeAnalyzerError", it.localizedMessage, null)
                }
                .addOnCompleteListener { imageProxy.close() }
    }

    fun dispose(streamChannel: EventChannel) {

    }
}