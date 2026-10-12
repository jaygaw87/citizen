package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesRepository
import com.example.data.model.TestConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PreferencesRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: PreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = PreferencesRepository.getInstance(context)
        repository.resetOnboarding()
        repository.setThemeMode("SYSTEM")
    }

    @Test
    fun `test initial state and onboarding persistence`() {
        assertFalse(repository.onboardingCompleted.value)

        repository.setOnboardingCompleted(true)
        assertTrue(repository.onboardingCompleted.value)

        repository.resetOnboarding()
        assertFalse(repository.onboardingCompleted.value)
    }

    @Test
    fun `test theme mode persistence`() {
        assertEquals("SYSTEM", repository.themeMode.value)

        repository.setThemeMode("DARK")
        assertEquals("DARK", repository.themeMode.value)

        repository.setThemeMode("LIGHT")
        assertEquals("LIGHT", repository.themeMode.value)
    }

    @Test
    fun `test audio preferences persistence`() {
        assertFalse(repository.audioAutoplay.value)
        repository.setAudioAutoplay(true)
        assertTrue(repository.audioAutoplay.value)

        repository.setPlaybackSpeed(1.25f)
        assertEquals(1.25f, repository.playbackSpeed.value, 0.001f)
    }

    @Test
    fun `test civics test specifications constants`() {
        assertEquals(128, TestConstants.TOTAL_CIVICS_QUESTIONS)
        assertEquals(20, TestConstants.FULL_PRACTICE_QUESTIONS)
        assertEquals(12, TestConstants.PASSING_SCORE)
        assertEquals(20, TestConstants.SPECIAL_CONSIDERATION_TOTAL_QUESTIONS)
        assertEquals(6, TestConstants.SPECIAL_CONSIDERATION_PASSING_SCORE)
    }
}
