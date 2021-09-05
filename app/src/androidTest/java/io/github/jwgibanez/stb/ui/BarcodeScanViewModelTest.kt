package io.github.jwgibanez.stb.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.jwgibanez.stb.R
import io.github.jwgibanez.stb.ui.result.FormState
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class BarcodeScanViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val viewModel = BarcodeScanViewModel()

    @Test
    fun validation_validNric() {
        var formState: FormState? = null
        val observer = Observer<FormState?> {
            formState = it
        }
        viewModel.formState.observeForever(observer)
        runBlocking {
            viewModel.dataChanged(
                nric = "S8767632N"
            )
        }
        sleep(100) // wait a bit for observer to complete
        assertThat("Form validation passed", formState?.isDataValid == true)
        viewModel.formState.removeObserver(observer)
    }

    @Test
    fun validation_invalidNric() {
        var formState: FormState? = null
        val observer = Observer<FormState?> {
            formState = it
        }
        viewModel.formState.observeForever(observer)
        runBlocking {
            viewModel.dataChanged(
                nric = "Z876763N"
            )
        }
        sleep(100) // wait a bit for observer to complete
        assertThat("Form validation failed", formState?.isDataValid == false)
        assertThat("Error message is correct", formState?.nricError == R.string.nric_invalid)
        viewModel.formState.removeObserver(observer)
    }
}