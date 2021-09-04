package io.github.jwgibanez.stb.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import io.github.jwgibanez.stb.R
import io.github.jwgibanez.stb.databinding.FragmentBarcodeScanBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanFragment : Fragment() {

    private var _binding: FragmentBarcodeScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BarcodeScanViewModel by viewModels()

    private var cameraExecutor: ExecutorService? = null

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when {
                it -> {
                    startCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showMissingPermissionError1()
                }
                else -> {
                    showMissingPermissionError2()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.textValue.observe(viewLifecycleOwner) {
            binding.text.text = it ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestForPermission()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onPause() {
        super.onPause()
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun allPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor!!, MyImageAnalyzer(viewModel))

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
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

    companion object {
        private const val TAG = "QrScanFragment"
        fun newInstance() = BarcodeScanFragment()
    }
}