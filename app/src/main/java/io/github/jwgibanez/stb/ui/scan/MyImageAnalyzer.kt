package io.github.jwgibanez.stb.ui.scan

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.jwgibanez.stb.ui.BarcodeScanViewModel
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

import android.app.Activity
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata.LENS_FACING_BACK
import android.os.Build
import android.util.SparseIntArray
import android.view.Surface
import androidx.annotation.RequiresApi

class MyImageAnalyzer(
    private val activity: Activity,
    private val viewModel: BarcodeScanViewModel
) : ImageAnalysis.Analyzer {

    private var scanner: BarcodeScanner

    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_39)
            .build()

        scanner = BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val compensation = getRotationCompensation(activity)

        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image).addOnSuccessListener { barcodes ->
                val isValid = process(barcodes)
                if (isValid && viewModel.bitmap.value == null) {
                    viewModel.bitmap.value?.recycle()
                    viewModel.bitmap.postValue(mediaImage.toBitmap(compensation.toFloat()))
                }
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

    private fun process(barcodes: List<Barcode>) : Boolean {
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
                            return true
                        }
                    }
                }
            }
        } else {
            viewModel.textValue.postValue(null)
        }
        return false
    }

    private fun Image.toBitmap(rotation: Float): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()

        val unrotated = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val matrix = Matrix()
        matrix.postRotate(rotation)
        return Bitmap.createBitmap(
            unrotated,
            0,
            0,
            this.width,
            this.height,
            matrix,
            false
        )
    }

    // For rotation compensation
    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(activity: Activity): Int {
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        // Get the device's sensor orientation.
        val cameraManager = activity.getSystemService(CAMERA_SERVICE) as CameraManager

        val id =  cameraManager.cameraIdList.first {
            cameraManager
                .getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == LENS_FACING_BACK
        }
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(id)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360

        return rotationCompensation
    }

    companion object {
        val PATTERN = Regex("^[STFG]\\d{7}[A-Z]")
        fun isNricValid(value: String): Boolean {
            val isValid = PATTERN.containsMatchIn(value)
            return isValid
        }
    }
}