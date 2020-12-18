package com.xhuww.flutter_barcode_scanning

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.IOException

class BarcodePluginMethodCallHandler(private val context: Context) : MethodChannel.MethodCallHandler {
    private var barcodeAnalyzer: BarcodeAnalyzer? = null

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "analyze" -> {
                val options = call.argument<Map<String, Any>>("options")
                val inputImage: InputImage
                try {
                    val imageData = call.arguments<Map<String, Any>>()
                    inputImage = dataToVisionImage(imageData) ?: return
                    if (barcodeAnalyzer == null) {
                        barcodeAnalyzer = BarcodeAnalyzer(options)
                    }
                    barcodeAnalyzer!!.analyze(inputImage, result)
                } catch (exception: IOException) {
                    result.error("BarcodeAnalyzerIOError", exception.localizedMessage, null)
                }
            }
            "close" -> {
                barcodeAnalyzer?.close()
                barcodeAnalyzer = null
            }
            else -> result.notImplemented()
        }
    }

    @Throws(IOException::class)
    private fun dataToVisionImage(imageData: Map<String, Any>): InputImage? {
        val imageType = imageData["type"] as? String ?: return null

        when (imageType) {
            "file" -> {
                val filePath = imageData["path"] as? String ?: return null

                val rotation = getImageExifOrientation(filePath)
                return if (rotation == 0) {
                    val uri = Uri.fromFile(File(filePath))
                    InputImage.fromFilePath(context, uri)
                } else {
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    InputImage.fromBitmap(bitmap, rotation)
                }
            }
            "bytes" -> {
                val metadataData = imageData["metadata"] as? Map<*, *> ?: return null
                val bytes = imageData["bytes"] as? ByteArray ?: return null
                val width = metadataData["width"] as? Double ?: 480.0
                val height = metadataData["height"] as? Double ?: 360.0
                val rotation = metadataData["rotation"] as? Int ?: 0

                return InputImage.fromByteArray(
                        bytes, width.toInt(), height.toInt(), rotation, InputImage.IMAGE_FORMAT_NV21
                )
            }
            else -> throw IllegalArgumentException(String.format("No image type for: %s", imageType))
        }
    }

    @Throws(IOException::class)
    private fun getImageExifOrientation(imageFilePath: String): Int {
        val orientation = ExifInterface(imageFilePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
