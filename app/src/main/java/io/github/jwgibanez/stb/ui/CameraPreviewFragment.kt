package io.github.jwgibanez.stb.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.common.util.concurrent.ListenableFuture
import io.github.jwgibanez.stb.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class CameraPreviewFragment : Fragment() {

    protected val viewModel: BarcodeScanViewModel by activityViewModels()

    protected var cameraExecutor: ExecutorService? = null

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when {
                it -> {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
                    startCamera(cameraProviderFuture)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showMissingPermissionError1()
                }
                else -> {
                    showMissingPermissionError2()
                }
            }
        }

    abstract fun startCamera(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            startCamera(cameraProviderFuture)
        } else {
            requestForPermission()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }

    private fun allPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestForPermission() {
        permissionRequestLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun showMissingPermissionError1() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.error_permission)
            .setMessage(R.string.location_permission_required1)
            .setPositiveButton(R.string.allow) { _, _ ->
                requestForPermission()
            }
            .setNegativeButton(R.string.close) { _, _ ->
                // User cancelled the dialog, go back to main fragment
                //findNavController().popBackStack()
                requireActivity().finish()
            }
        builder.create().show()
    }

    private fun showMissingPermissionError2() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.error_permission)
            .setMessage(R.string.location_permission_required2)
            .setNegativeButton(R.string.close) { _, _ ->
                // User cancelled the dialog, go back to main fragment
                //findNavController().popBackStack()
                requireActivity().finish()
            }
        builder.create().show()
    }
}