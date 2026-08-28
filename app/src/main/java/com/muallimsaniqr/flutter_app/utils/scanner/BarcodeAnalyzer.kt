package com.muallimsaniqr.flutter_app.utils.scanner

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.muallimsaniqr.flutter_app.ui.extensions.showToast

class BarcodeAnalyzer(private val context: Context, private val result: (String?) -> Unit = {}) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                barcode?.takeIf { it.isNotEmpty() }
                    ?.firstNotNullOf { it.rawValue } // takes first non-null value
                    ?.let {
                        result(it)
                    }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}