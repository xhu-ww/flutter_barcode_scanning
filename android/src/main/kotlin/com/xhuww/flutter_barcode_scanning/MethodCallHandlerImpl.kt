package com.xhuww.flutter_barcode_scanning

import android.app.Activity
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.view.TextureRegistry
import io.flutter.view.TextureRegistry.SurfaceTextureEntry

class MethodCallHandlerImpl(
        activity: Activity,
        messenger: BinaryMessenger,
        private val textureRegistry: TextureRegistry
) : MethodCallHandler {
    private val methodChannel = MethodChannel(messenger, "plugins.flutter.io/barcodeScanning")
    private val analyzerStreamChannel = EventChannel(messenger, "plugins.flutter.io/barcodeScanning/analyzerStream")
    private var barcodeScanningController: BarcodeScanningController? = null

    init {
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initialize" -> {
                barcodeScanningController?.initialize()
            }
            "startBarcodeAnalyzerStream" -> {
                barcodeScanningController?.startBarcodeAnalyzerStream(analyzerStreamChannel)
            }
            "stopBarcodeAnalyzerStream" -> {

            }
            "dispose" -> {
            }
        }
    }

    fun stopListening() {
        methodChannel.setMethodCallHandler(null)
    }

    fun initializeCamera() {
        val flutterSurfaceTexture = textureRegistry.createSurfaceTexture()
    }
}