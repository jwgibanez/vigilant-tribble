package io.github.jwgibanez.stb.ui.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BarcodeScanViewModel : ViewModel() {

    val textValue = MutableLiveData<String?>(null)
}