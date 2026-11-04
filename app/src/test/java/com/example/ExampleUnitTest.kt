package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun computeGeohash_computesValidStandardBase32Geohash() {
    val lat = 37.7749
    val lon = -122.4194
    val geohash = com.example.util.LocationHelper.computeGeohash(lat, lon, 7)
    assertNotNull(geohash)
    assertEquals(7, geohash.length)
    assertTrue("Geohash starts with 9q8y: $geohash", geohash.startsWith("9q8y"))

    val formatted = com.example.util.LocationHelper.formatCoordinates(lat, lon)
    assertTrue("Formatted contains N and W: $formatted", formatted.contains("N") && formatted.contains("W"))
  }

  @Test
  fun qrCodePayloadHelper_encodesAndParsesOfflinePayloadAccurately() {
    val code = "#9q8yyk8"
    val name = "Oakwood Pavilion"
    val lat = 11.9341
    val lng = 79.8291

    val payload = com.example.util.QrCodePayloadHelper.buildPayload(code, name, lat, lng)
    assertTrue("Starts with protocol: $payload", payload.startsWith("NEARGRID:v1"))
    assertTrue("Contains code: $payload", payload.contains("code=9q8yyk8"))
    assertTrue("Contains name: $payload", payload.contains("name=Oakwood Pavilion"))
    assertFalse("Does not contain fake web URLs: $payload", payload.contains("neargrid.com") || payload.contains("neargrid.app"))

    val parsed = com.example.util.QrCodePayloadHelper.parsePayload(payload)
    assertEquals("#9q8yyk8", parsed.code)
    assertEquals("Oakwood Pavilion", parsed.name)
    assertEquals(11.9341, parsed.lat, 0.0001)
    assertEquals(79.8291, parsed.lng, 0.0001)
    assertTrue(parsed.isValid)
  }

  @Test
  fun qrCodePayloadHelper_handlesRawGeohashFallback() {
    val raw = "#tdr1v9k"
    val parsed = com.example.util.QrCodePayloadHelper.parsePayload(raw)
    assertEquals("#tdr1v9k", parsed.code)
    assertEquals("Neighbor Grid #tdr1v9k", parsed.name)
    assertTrue(parsed.isValid)
  }

  @Test
  fun qrCodePayloadHelper_shareMessageHasNoDeadWebLinks() {
    val share = com.example.util.QrCodePayloadHelper.buildShareMessage("#9q8yyk8", "Pangur", 11.93, 79.82)
    assertFalse("No dead neargrid.com", share.contains("neargrid.com"))
    assertFalse("No dead neargrid.app", share.contains("neargrid.app"))
    assertTrue("Contains offline pass payload", share.contains("NEARGRID:v1"))
  }

  @Test
  fun nearGridPayload_serializesAndDeserializesWithMoshiAdapter() {
    val original = com.example.data.model.NearGridPayload(
        geohash = "9q8yyk8",
        areaName = "Oakwood Pavilion",
        latitude = 11.9341,
        longitude = 79.8291
    )

    // Test serialization
    val json = original.toJson()
    assertTrue("JSON contains geohash", json.contains("\"geohash\":\"9q8yyk8\""))
    assertTrue("JSON contains area_name", json.contains("\"area_name\":\"Oakwood Pavilion\""))
    assertTrue("JSON contains latitude", json.contains("\"latitude\":11.9341"))
    assertTrue("JSON contains longitude", json.contains("\"longitude\":79.8291"))

    // Test deserialization
    val parsed = com.example.data.model.NearGridPayload.fromJson(json)
    assertNotNull(parsed)
    assertEquals("9q8yyk8", parsed!!.geohash)
    assertEquals("Oakwood Pavilion", parsed.areaName)
    assertEquals(11.9341, parsed.latitude, 0.0001)
    assertEquals(79.8291, parsed.longitude, 0.0001)

    // Test integration with QrCodePayloadHelper.parsePayload
    val parsedFromHelper = com.example.util.QrCodePayloadHelper.parsePayload(json)
    assertEquals("#9q8yyk8", parsedFromHelper.code)
    assertEquals("Oakwood Pavilion", parsedFromHelper.name)
    assertEquals(11.9341, parsedFromHelper.lat, 0.0001)
    assertEquals(79.8291, parsedFromHelper.lng, 0.0001)
    assertTrue(parsedFromHelper.isValid)
  }

  @Test
  fun appUpdateService_isNewerVersionCalculatesCorrectly() {
    // Newer remote versions
    assertTrue(com.example.data.update.AppUpdateService.isNewerVersion("v1.0.3", "1.0"))
    assertTrue(com.example.data.update.AppUpdateService.isNewerVersion("1.0.3", "1.0.2"))
    assertTrue(com.example.data.update.AppUpdateService.isNewerVersion("v2.0.0", "1.9.9"))
    assertTrue(com.example.data.update.AppUpdateService.isNewerVersion("v1.1", "1.0"))

    // Equal or older remote versions
    assertFalse(com.example.data.update.AppUpdateService.isNewerVersion("v1.0.3", "1.0.3"))
    assertFalse(com.example.data.update.AppUpdateService.isNewerVersion("1.0", "1.0"))
    assertFalse(com.example.data.update.AppUpdateService.isNewerVersion("1.0.0", "1.0"))
    assertFalse(com.example.data.update.AppUpdateService.isNewerVersion("v1.0.1", "1.0.3"))
    assertFalse(com.example.data.update.AppUpdateService.isNewerVersion("", "1.0"))
  }

  @Test
  fun appUpdateManager_checkForUpdateWithRetrofitServiceDetectsNewerVersion() = kotlinx.coroutines.runBlocking {
    val fakeService = object : com.example.data.update.GitHubApiService {
      override suspend fun getLatestRelease(owner: String, repo: String): com.example.data.update.GitHubRelease {
        return com.example.data.update.GitHubRelease(
          tagName = "v1.0.4",
          name = "NearGrid v1.0.4",
          body = "Faster local grid synchronization",
          htmlUrl = "https://github.com/rkivln/neargrid/releases/tag/v1.0.4",
          assets = listOf(
            com.example.data.update.GitHubReleaseAsset(
              name = "NearGrid.apk",
              downloadUrl = "https://github.com/rkivln/neargrid/releases/download/v1.0.4/NearGrid.apk"
            )
          )
        )
      }
    }

    val manager = com.example.data.update.AppUpdateManager(apiService = fakeService)
    val result = manager.checkForUpdate("1.0.3")

    assertTrue(result.isSuccess)
    val info = result.getOrNull()
    assertNotNull(info)
    assertTrue("Update must be detected as available", info!!.isUpdateAvailable)
    assertEquals("1.0.4", info.latestVersion)
    assertEquals("1.0.3", info.currentVersion)
    assertEquals("NearGrid v1.0.4", info.releaseName)
    assertEquals("https://github.com/rkivln/neargrid/releases/download/v1.0.4/NearGrid.apk", info.apkDownloadUrl)

    // Check with equal or newer local version
    val upToDateResult = manager.checkForUpdate("1.0.4")
    assertTrue(upToDateResult.isSuccess)
    assertFalse("Update must NOT be flagged when version is equal", upToDateResult.getOrNull()!!.isUpdateAvailable)
  }

  @Test
  fun mediaUploadHelper_formatsBase64DataUrlCorrectly() {
    val sampleBytes = "Hello NearGrid".toByteArray()
    val dataUrl = com.example.util.MediaUploadHelper.formatAsDataUrl(sampleBytes, "image/jpeg")

    assertTrue("Data URL must start with data:image/jpeg;base64,", dataUrl.startsWith("data:image/jpeg;base64,"))
    assertTrue("Data URL must contain non-empty base64 string", dataUrl.length > "data:image/jpeg;base64,".length)
  }

  @Test
  fun geohashUtil_encodesAndCalculatesPrefixAccurately() {
    val lat = 37.7749
    val lng = -122.4194
    val fullHash = com.example.util.GeohashUtil.encode(lat, lng, 7)
    val prefix = com.example.util.GeohashUtil.getPrefix(lat, lng, 5)

    assertEquals(7, fullHash.length)
    assertEquals(5, prefix.length)
    assertTrue("Prefix must match the first 5 characters of full geohash", fullHash.startsWith(prefix))
  }

  @Test
  fun appUpdateManager_remembersDismissedVersion() {
    val fakeService = object : com.example.data.update.GitHubApiService {
      override suspend fun getLatestRelease(owner: String, repo: String): com.example.data.update.GitHubRelease {
        return com.example.data.update.GitHubRelease(
          tagName = "v1.0.5",
          name = "NearGrid v1.0.5"
        )
      }
    }

    val manager = com.example.data.update.AppUpdateManager(apiService = fakeService)
    
    // Dismiss version 1.0.5
    manager.dismissBanner("1.0.5")
    assertTrue(manager.isBannerDismissed.value)

    // Run check for update with local version 1.0.0
    val result = kotlinx.coroutines.test.runTest { manager.checkForUpdate("1.0.0") }
    assertTrue(manager.isBannerDismissed.value)

    // Manual reset shows banner again
    manager.resetBannerDismissed()
    assertFalse(manager.isBannerDismissed.value)
  }

  @Test
  fun neighborPost_copyAndEditUpdatesFieldsAccurately() {
    val post = com.example.data.model.NeighborPost(
      id = "test_post_1",
      userId = "user_123",
      type = "NEED",
      title = "Need lawn mower",
      description = "Mowing lawn this Saturday",
      category = "TOOLS",
      authorName = "Alice",
      authorInitials = "AL",
      authorCellId = "cell_1",
      cellName = "Oakwood Grove",
      urgency = "NORMAL",
      expiresInHours = 24
    )

    val updatedPost = post.copy(
      title = "Need electric lawn mower",
      description = "Updated description with battery power preference",
      category = "HOUSEHOLD",
      urgency = "URGENT"
    )

    assertEquals("test_post_1", updatedPost.id)
    assertEquals("user_123", updatedPost.userId)
    assertEquals("Need electric lawn mower", updatedPost.title)
    assertEquals("Updated description with battery power preference", updatedPost.description)
    assertEquals("HOUSEHOLD", updatedPost.category)
    assertEquals("URGENT", updatedPost.urgency)
  }

  @Test
  fun chatMessage_copyAndEditUpdatesMessageContent() {
    val originalMessage = com.example.data.model.ChatMessage(
      id = "msg_123",
      conversationId = "conv_456",
      senderId = "user_1",
      senderName = "Alice",
      isFromCurrentUser = true,
      text = "Can we meet at 4 PM?",
      timestamp = 1000L
    )

    val editedMessage = originalMessage.copy(
      text = "Can we meet at 4:30 PM instead?"
    )

    assertEquals("msg_123", editedMessage.id)
    assertEquals("conv_456", editedMessage.conversationId)
    assertEquals("Can we meet at 4:30 PM instead?", editedMessage.text)
    assertEquals(1000L, editedMessage.timestamp)
    assertTrue(editedMessage.isFromCurrentUser)
  }

  @Test
  fun coordinateFiltering_rejectsChennaiPostFromPuducherry() {
    // Puducherry device location
    val puducherryLat = 11.9416
    val puducherryLng = 79.8083

    // Chennai post location
    val chennaiLat = 13.0827
    val chennaiLng = 80.2707

    val distanceMeters = com.example.util.LocationHelper.calculateDistanceMeters(
      puducherryLat, puducherryLng,
      chennaiLat, chennaiLng
    )

    // Chennai and Puducherry are ~135km apart
    assertTrue("Distance between Chennai and Puducherry should be > 130 km, was: $distanceMeters", distanceMeters > 130_000f)

    // Verification against typical discovery radii (1km, 5km, 10km)
    val maxDiscoveryRadiusMeters = 10_000
    assertFalse("Puducherry user must reject Chennai post under 10km radius", distanceMeters <= maxDiscoveryRadiusMeters)

    // Geohash prefix separation check
    val puducherryHash = com.example.util.LocationHelper.computeGeohash(puducherryLat, puducherryLng, 7)
    val chennaiHash = com.example.util.LocationHelper.computeGeohash(chennaiLat, chennaiLng, 7)

    assertFalse("Geohash prefixes must be completely distinct between Chennai and Puducherry",
      chennaiHash.take(3) == puducherryHash.take(3)
    )
  }

  @Test
  fun coordinateFiltering_acceptsNearbyLocalPost() {
    val userLat = 11.9416
    val userLng = 79.8083

    // Neighbor within ~200 meters in Puducherry
    val neighborLat = 11.9430
    val neighborLng = 79.8090

    val distanceMeters = com.example.util.LocationHelper.calculateDistanceMeters(
      userLat, userLng,
      neighborLat, neighborLng
    )

    assertTrue("Neighbor within walking distance (< 500m), was: $distanceMeters", distanceMeters < 500f)

    val userHash = com.example.util.LocationHelper.computeGeohash(userLat, userLng, 7)
    val neighborHash = com.example.util.LocationHelper.computeGeohash(neighborLat, neighborLng, 7)

    assertEquals("Local neighbors share 4-character geohash prefix",
      userHash.take(4), neighborHash.take(4)
    )
  }
}

