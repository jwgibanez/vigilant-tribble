package io.github.jwgibanez.stb.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.jwgibanez.stb.R
import io.github.jwgibanez.stb.ui.result.FormState
import io.github.jwgibanez.stb.ui.scan.MyImageAnalyzer.Companion.isNricValid

class BarcodeScanViewModel : ViewModel() {

    val textValue = MutableLiveData<String?>(null)

    val bitmap = MutableLiveData<Bitmap?>(null)

    private val _form = MutableLiveData<FormState>()
    val formState: LiveData<FormState> = _form

    fun dataChanged(nric: String) {
        if (!isNricValid(nric)) {
            val state = FormState()
            state.nricError = R.string.nric_invalid
            _form.value = state
        } else {
            _form.value = FormState(isDataValid = true)
        }
    }
}