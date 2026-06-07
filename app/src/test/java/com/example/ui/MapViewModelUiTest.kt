package com.example.ui

import com.example.domain.ParkingRepository
import com.example.ui.screens.MapViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelUiTest {
    private val repository: ParkingRepository = mockk(relaxed = true)
    private lateinit var viewModel: MapViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isDriving becomes true immediately when speed exceeds threshold`() = runTest {
        viewModel.setSpeed(20f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Should be in driving mode", viewModel.isDriving.value)
    }

    @Test
    fun `isDriving remains true during brief speed drops (hysteresis)`() = runTest {
        // Start driving
        viewModel.setSpeed(20f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.isDriving.value)

        // Stop briefly
        viewModel.setSpeed(0f)
        // Advance only 1 second (hysteresis is 2.5s)
        testDispatcher.scheduler.advanceTimeBy(1000)
        assertTrue("Should still be in driving mode due to hysteresis", viewModel.isDriving.value)

        // Advance past delay
        testDispatcher.scheduler.advanceTimeBy(2000)
        assertFalse("Should have exited driving mode after delay", viewModel.isDriving.value)
    }
}
