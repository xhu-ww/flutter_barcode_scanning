package com.xhuww.flutter_barcode_scanning

import android.app.Activity
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.view.TextureRegistry

class MethodCallHandlerImpl(
        activity: Activity,
        messenger: BinaryMessenger,
        textureRegistry: TextureRegistry
) : MethodCallHandler {
    private val methodChannel = MethodChannel(messenger, "plugins.flutter.io/barcodeScanning")
    private val analyzerStreamChannel = EventChannel(messenger, "plugins.flutter.io/barcodeScanning/analyzerStream")
    private val barcodeScanningController = BarcodeScanningController(activity, textureRegistry.createSurfaceTexture())

    init {
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initialize" ->
                barcodeScanningController.initialize(result)
            "startBarcodeAnalyzerStream" ->
                barcodeScanningController.startBarcodeAnalyzerStream(analyzerStreamChannel)
            "stopBarcodeAnalyzerStream" ->
                barcodeScanningController.stopBarcodeAnalyzerStream(analyzerStreamChannel)
            "dispose" -> {

            }
        }
    }

    fun stopListening() {
        methodChannel.setMethodCallHandler(null)
    }
}