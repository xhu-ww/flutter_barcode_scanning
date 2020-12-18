package com.xhuww.flutter_barcode_scanning

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.flutter.plugin.common.MethodChannel
import java.util.*

class BarcodeAnalyzer(options: Map<String, Any>?) {
    private val barcodeScanner: BarcodeScanner

    init {
        val barcodeScannerOptions = parseOptions(options)
        barcodeScanner = if (barcodeScannerOptions == null) {
            BarcodeScanning.getClient()
        } else {
            BarcodeScanning.getClient(barcodeScannerOptions)
        }
    }

    private fun parseOptions(optionsData: Map<String, Any>?): BarcodeScannerOptions? {
        val barcodeFormats = optionsData?.get("barcodeFormats")
        return if (barcodeFormats is Int) {
            BarcodeScannerOptions.Builder().setBarcodeFormats(barcodeFormats).build()
        } else {
            null
        }
    }

    fun analyze(image: InputImage, result: MethodChannel.Result) {
        barcodeScanner.process(image)
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
                    result.success(barcodeList)
                }
                .addOnFailureListener {
                    result.error("barcodeAnalyzerError", it.localizedMessage, null)
                }
    }

    fun close() {
        barcodeScanner.close()
    }

    private fun checkEmpty(string: String?): String {
        return string ?: ""
    }
}
