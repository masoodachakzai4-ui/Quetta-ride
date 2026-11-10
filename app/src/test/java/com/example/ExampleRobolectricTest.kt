package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.QuettaRideViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and initialize QuettaRideViewModel`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Quetta Ride", appName)

        val viewModel = QuettaRideViewModel(context)
        val state = viewModel.uiState.value
        assertEquals("Quetta", state.selectedCity)
        assertTrue(state.locations.isNotEmpty())
        assertTrue(state.drivers.isNotEmpty())
    }
}
