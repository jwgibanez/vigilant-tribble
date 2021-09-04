package io.github.jwgibanez.stb.ui.scan

import android.annotation.SuppressLint
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class MyImageAnalyzer(private val viewModel: BarcodeScanViewModel) : ImageAnalysis.Analyzer {

    private var scanner: BarcodeScanner

    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_39)
            .build()

        scanner = BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image).addOnSuccessListener { barcodes ->
                process(barcodes)
            }.addOnFailureListener { exception ->
                viewModel.textValue.postValue(null)
                Log.e("MyImageAnalyzer", exception.toString())
            }.addOnCompleteListener {
                mediaImage.close()
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    private fun process(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            for (barcode in barcodes) {
                val bounds = barcode.boundingBox
                val corners = barcode.cornerPoints

                val rawValue = barcode.rawValue
                val valueType = barcode.valueType
                val format = barcode.format

                Log.d("MyImageAnalyzer", "rawValue: $rawValue")
                Log.d("MyImageAnalyzer", "valueType: $valueType")
                Log.d("MyImageAnalyzer", "format: $format")

                rawValue?.let {
                    if (it.length >= 9) {
                        val nric = it.substring(0, 9)
                        if (isNricValid(nric)) {
                            viewModel.textValue.postValue(nric)
                        }
                    }
                }
            }
        } else {
            viewModel.textValue.postValue(null)
        }
    }

    companion object {
        val PATTERN = Regex("^[STFG]\\d{7}[A-Z]")
        fun isNricValid(value: String): Boolean {
            val isValid = PATTERN.containsMatchIn(value)
            return isValid
        }
    }
}