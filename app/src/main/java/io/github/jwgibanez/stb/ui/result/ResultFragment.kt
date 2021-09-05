package io.github.jwgibanez.stb.ui.result

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.github.jwgibanez.stb.databinding.FragmentResultBinding
import io.github.jwgibanez.stb.ui.BarcodeScanViewModel
import java.time.LocalDateTime

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BarcodeScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bitmap.observe(viewLifecycleOwner) {
            it?.let {
                binding.image.setImageBitmap(it)
            }
        }

        viewModel.textValue.observe(viewLifecycleOwner) {
            it?.let {
                binding.textValue.setText(it)
            }
        }

        viewModel.formState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            if (state.nricError != null) {
                binding.textValue.error = getString(state.nricError!!)
            }
        })

        binding.textValue.afterTextChanged {
            viewModel.dataChanged(
                requireContext(),
                binding.textValue.text.toString()
            )

            val estimatedAge: String = estimateAge(it)
            binding.ageEstimate.setText(estimatedAge)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun estimateAge(nric: String?) : String {
        return if (nric != null && nric.length > 3) {
            val current = LocalDateTime.now()
            when (nric[0]) {
                'S' -> {
                    (current.year - "19${nric.substring(1, 3)}".toInt()).toString()
                }
                'T' -> (current.year - "20${nric.substring(1, 3)}".toInt()).toString()
                else -> "n/a"
            }
        } else {
            "n/a"
        }
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}