package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.example.app")
          .setProjectId("ai-studio-android-neargrid-b22d8170-446c-4884-bbde-b75aba51eee5")
          .setApiKey("fake-api-key-for-test")
          .build()
      )
    }
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NearGrid", appName)
  }

  @Test
  fun `verify user profile and privacy radius settings in ViewModel`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.NearGridViewModel(app)
    
    val initial = viewModel.uiState.value
    assertEquals("Local Resident", initial.userNickname)
    assertEquals(1000, initial.neighborhoodPrivacyRadiusMeters)
    
    viewModel.updateUserProfile(
        nickname = "Elena Rostova",
        handle = "elena_garden",
        bio = "Urban garden volunteer",
        avatarUri = "content://media/picker/image/1",
        avatarPresetIndex = 1,
        privacyRadiusMeters = 1000,
        fuzzyCoords = true,
        incognito = true,
        allowDMs = true
    )
    
    val updated = viewModel.uiState.value
    assertEquals("Elena Rostova", updated.userNickname)
    assertEquals("@elena_garden", updated.userHandle)
    assertEquals("Urban garden volunteer", updated.userBio)
    assertEquals("content://media/picker/image/1", updated.avatarUri)
    assertEquals(1, updated.avatarPresetIndex)
    assertEquals(1000, updated.neighborhoodPrivacyRadiusMeters)
    assertEquals(1000, updated.discoveryRadiusMeters)
    assertEquals(true, updated.fuzzyCoordinateOffset)
    assertEquals(true, updated.incognitoGridPresence)
  }

  @Test
  fun `verify LocationHelper distance formatting`() {
    val closeMeters = com.example.util.LocationHelper.formatDistance(35f)
    assertEquals("< 50 m", closeMeters)

    val midMeters = com.example.util.LocationHelper.formatDistance(350f)
    assertEquals("350 m", midMeters)

    val kmDist = com.example.util.LocationHelper.formatDistance(1400f)
    assertEquals("1.4 km", kmDist)
  }
}
