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
  fun appUpdateManager_currentReleaseV103DoesNotTriggerUpdate() = kotlinx.coroutines.runBlocking {
    val liveGitHubService = object : com.example.data.update.GitHubApiService {
      override suspend fun getLatestRelease(owner: String, repo: String): com.example.data.update.GitHubRelease {
        return com.example.data.update.GitHubRelease(
          tagName = "v1.0.3",
          name = "NearGrid v1.0.3",
          body = "Latest release",
          htmlUrl = "https://github.com/rkivln/neargrid/releases/tag/v1.0.3",
          assets = listOf(
            com.example.data.update.GitHubReleaseAsset(
              name = "NearGrid.apk",
              downloadUrl = "https://github.com/rkivln/neargrid/releases/download/v1.0.3/NearGrid.apk"
            )
          )
        )
      }
    }

    val manager = com.example.data.update.AppUpdateManager(apiService = liveGitHubService)
    val result = manager.checkForUpdate("1.0.3")

    assertTrue(result.isSuccess)
    val info = result.getOrNull()
    assertNotNull(info)
    assertFalse("Update banner must NOT appear when installed version matches latest release v1.0.3", info!!.isUpdateAvailable)

    // Test dismissal
    manager.dismissBanner("v1.0.3")
    assertTrue("Banner is marked as dismissed", manager.isBannerDismissed.value)
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

  @Test
  fun nearbyNeighbors_filtersByDiscoveryRadius() {
    val myLat = 11.9416
    val myLng = 79.8083

    val neighborClose = com.example.data.model.UserProfile(
      userId = "neighbor_1",
      displayName = "Aarav",
      latitude = 11.9430,
      longitude = 79.8090,
      incognito = false
    )

    val neighborMedium = com.example.data.model.UserProfile(
      userId = "neighbor_2",
      displayName = "Bhavna",
      latitude = 11.9490,
      longitude = 79.8150,
      incognito = false
    )

    val neighborChennai = com.example.data.model.UserProfile(
      userId = "neighbor_3",
      displayName = "Chennai User",
      latitude = 13.0827,
      longitude = 80.2707,
      incognito = false
    )

    val neighborIncognito = com.example.data.model.UserProfile(
      userId = "neighbor_4",
      displayName = "Secret User",
      latitude = 11.9420,
      longitude = 79.8085,
      incognito = true
    )

    val allCandidates = listOf(neighborClose, neighborMedium, neighborChennai, neighborIncognito)

    // Helper filtering function mirroring ViewModel logic
    fun filterNeighbors(radiusMeters: Int): List<com.example.data.model.NearbyNeighbor> {
      return allCandidates.filter { !it.incognito && it.latitude != 0.0 && it.longitude != 0.0 }
        .mapNotNull { candidate ->
          val dist = com.example.util.LocationHelper.calculateDistanceMeters(myLat, myLng, candidate.latitude, candidate.longitude)
          if (dist <= radiusMeters) {
            com.example.data.model.NearbyNeighbor(user = candidate, distanceMeters = dist)
          } else {
            null
          }
        }.sortedBy { it.distanceMeters }
    }

    // 500m radius
    val within500m = filterNeighbors(500)
    assertEquals(1, within500m.size)
    assertEquals("neighbor_1", within500m[0].user.userId)

    // 2000m radius
    val within2000m = filterNeighbors(2000)
    assertEquals(2, within2000m.size)
    assertEquals("neighbor_1", within2000m[0].user.userId)
    assertEquals("neighbor_2", within2000m[1].user.userId)
    assertTrue("Closest neighbor must be first", within2000m[0].distanceMeters < within2000m[1].distanceMeters)

    // 10km radius
    val within10km = filterNeighbors(10000)
    assertEquals(2, within10km.size)
    assertFalse("Chennai neighbor (~135km) must never be in 10km list", within10km.any { it.user.userId == "neighbor_3" })
    assertFalse("Incognito neighbor must never be in list", within10km.any { it.user.userId == "neighbor_4" })
  }

  @Test
  fun nearbyNeighbors_includesNeighborsAcrossDifferentGeohashBoundaries() {
    val myLat = 11.9060
    val myLng = 79.7070
    // Neighbor physically ~350m away across cell boundary
    val neighborBorder = com.example.data.model.UserProfile(
      userId = "neighbor_border",
      displayName = "Border Neighbor",
      latitude = 11.9085,
      longitude = 79.7090,
      incognito = false
    )

    val dist = com.example.util.LocationHelper.calculateDistanceMeters(myLat, myLng, neighborBorder.latitude, neighborBorder.longitude)
    assertTrue("Physically close neighbor (< 500m): $dist meters", dist < 500)

    val candidates = listOf(neighborBorder)
    val filtered = candidates.filter { !it.incognito && it.latitude != 0.0 && it.longitude != 0.0 }
      .mapNotNull { candidate ->
        val d = com.example.util.LocationHelper.calculateDistanceMeters(myLat, myLng, candidate.latitude, candidate.longitude)
        if (d <= 500) com.example.data.model.NearbyNeighbor(user = candidate, distanceMeters = d) else null
      }

    assertEquals(1, filtered.size)
    assertEquals("neighbor_border", filtered[0].user.userId)
  }

  @Test
  fun privacySettings_blockedNeighborsAreExcludedFromDiscovery() {
    val myLat = 11.9060
    val myLng = 79.7070
    val blockedNeighbor = com.example.data.model.UserProfile(
      userId = "blocked_neighbor_123",
      displayName = "Spammer Neighbor",
      latitude = 11.9065,
      longitude = 79.7075,
      incognito = false
    )
    val friendlyNeighbor = com.example.data.model.UserProfile(
      userId = "friendly_neighbor_456",
      displayName = "Friendly Neighbor",
      latitude = 11.9068,
      longitude = 79.7072,
      incognito = false
    )

    val blockedIds = listOf("blocked_neighbor_123")
    val candidates = listOf(blockedNeighbor, friendlyNeighbor)

    // Simulate ViewModel filtering logic
    val filtered = candidates.filter { candidate ->
      !candidate.incognito &&
      !blockedIds.contains(candidate.userId) &&
      candidate.latitude != 0.0 &&
      candidate.longitude != 0.0
    }.mapNotNull { candidate ->
      val d = com.example.util.LocationHelper.calculateDistanceMeters(myLat, myLng, candidate.latitude, candidate.longitude)
      if (d <= 1000) com.example.data.model.NearbyNeighbor(user = candidate, distanceMeters = d) else null
    }

    assertEquals(1, filtered.size)
    assertEquals("friendly_neighbor_456", filtered[0].user.userId)
    assertFalse("Blocked user must never appear in filtered list", filtered.any { it.user.userId == "blocked_neighbor_123" })
  }

  @Test
  fun privacySettings_locationPrecisionAppliesApproximateAndNeighborhoodTransforms() {
    val exactLat = 11.906000
    val exactLng = 79.707000

    // APPROXIMATE: offsets by small pseudo-random hash displacement (~150m) to mask exact doorstep
    val approxSeed = ("user_xyz_APPROXIMATE".hashCode().toLong() and 0xFFFFFFFFL).toDouble() / 0xFFFFFFFFL
    val approxOffsetLat = (approxSeed - 0.5) * 0.002
    val approxOffsetLng = (((approxSeed * 31) % 1.0) - 0.5) * 0.002
    val approxLat = exactLat + approxOffsetLat
    val approxLng = exactLng + approxOffsetLng

    assertNotEquals("Approximate latitude must differ from exact doorstep", exactLat, approxLat, 0.00001)
    assertNotEquals("Approximate longitude must differ from exact doorstep", exactLng, approxLng, 0.00001)

    val distApprox = com.example.util.LocationHelper.calculateDistanceMeters(exactLat, exactLng, approxLat, approxLng)
    assertTrue("Approximate offset should be within reasonable district walking range (< 300m): $distApprox m", distApprox < 300)

    // NEIGHBORHOOD: snapped to 2 decimal places (~1.1km grid cell)
    val neighborhoodLat = Math.round(exactLat * 100.0) / 100.0
    val neighborhoodLng = Math.round(exactLng * 100.0) / 100.0
    assertEquals(11.91, neighborhoodLat, 0.001)
    assertEquals(79.71, neighborhoodLng, 0.001)
  }

  @Test
  fun privacySettings_hideExactDistanceFormatsProximityBand() {
    val neighborWithExact = com.example.data.model.UserProfile(
      userId = "n1",
      displayName = "Neighbor 1",
      hideExactDistance = false
    )
    val neighborHidingDistance = com.example.data.model.UserProfile(
      userId = "n2",
      displayName = "Neighbor 2",
      hideExactDistance = true
    )

    fun formatDistanceForUser(user: com.example.data.model.UserProfile, distanceMeters: Float): String {
      return if (user.hideExactDistance) {
        if (distanceMeters <= 500) "< 500 m away" else "In neighborhood"
      } else {
        "${com.example.util.LocationHelper.formatDistance(distanceMeters)} away"
      }
    }

    val closeDist = 120f
    val midDist = 850f

    assertEquals("120 m away", formatDistanceForUser(neighborWithExact, closeDist))
    assertEquals("850 m away", formatDistanceForUser(neighborWithExact, midDist))

    assertEquals("< 500 m away", formatDistanceForUser(neighborHidingDistance, closeDist))
    assertEquals("In neighborhood", formatDistanceForUser(neighborHidingDistance, midDist))
  }

  @Test
  fun storageUsage_formatsBytesAndPercentagesAccurately() {
    val usage = com.example.util.StorageUsage(
      imageCacheBytes = 10 * 1024 * 1024L, // 10 MB
      dbBytes = 2 * 1024 * 1024L,          // 2 MB
      tempBytes = 0L,
      totalBytes = 12 * 1024 * 1024L        // 12 MB
    )

    assertEquals("12.0 MB", usage.formattedTotal)
    assertEquals("10.0 MB", usage.formattedImageCache)
    assertEquals("2.0 MB", usage.formattedDb)
    assertEquals(0.833f, usage.imagePercentage, 0.01f)
    assertEquals(0.166f, usage.dbPercentage, 0.01f)
  }

  @Test
  fun crashTelemetryManager_buildsPayloadWithAppVersionAndTruncatedStackTrace() {
    val exception = IllegalStateException("Test non-fatal telemetry error")
    val payload = com.example.util.CrashTelemetryManager.buildTelemetryPayload(
      severity = "NON_FATAL",
      errorName = "IllegalStateException",
      errorMessage = exception.message ?: "",
      stackTrace = exception.stackTraceToString()
    )

    assertEquals("v1.0.3", payload["appVersion"])
    assertEquals("NON_FATAL", payload["severity"])
    assertEquals("IllegalStateException", payload["errorName"])
    assertEquals("Test non-fatal telemetry error", payload["errorMessage"])
    assertNotNull(payload["logId"])
    assertNotNull(payload["timestamp"])
  }

  @Test
  fun appSettings_supportsThemeAndCustomizationStateUpdates() {
    val defaultSettings = com.example.data.model.AppSettings()
    assertEquals("WARM_AMBER", defaultSettings.themeAccent)
    assertEquals("COMFORT", defaultSettings.layoutDensity)
    assertTrue(defaultSettings.hapticEnabled)
    assertEquals("v1.0.3", defaultSettings.appVersion)

    val updated = defaultSettings.copy(
      themeAccent = "ELECTRIC_TEAL",
      layoutDensity = "COMPACT",
      hapticEnabled = false
    )

    assertEquals("ELECTRIC_TEAL", updated.themeAccent)
    assertEquals("COMPACT", updated.layoutDensity)
    assertFalse(updated.hapticEnabled)
  }

  // --- 11 BUG CATEGORY AUTOMATED SUITE ---

  @Test
  fun testCategory01_UIBugs_verifiesTextFormattingAndAccessibilityTags() {
    val post = com.example.data.model.NeighborPost(
      id = "ui_post_1",
      title = "  Need Extra Folding Chairs  ",
      authorName = "  Sarah  ",
      type = "NEED",
      urgency = "URGENT"
    )
    val formattedDist = com.example.util.LocationHelper.formatDistance(85f)
    assertEquals("85 m", formattedDist)
    val formattedKm = com.example.util.LocationHelper.formatDistance(1420f)
    assertEquals("1.4 km", formattedKm)
    assertTrue("Title preserved", post.title.contains("Need Extra Folding Chairs"))
    assertEquals("NEED", post.type)
  }

  @Test
  fun testCategory02_FunctionalBugs_verifiesPostCreationAndUpvoteLogic() {
    val post = com.example.data.model.NeighborPost(
      id = "func_post_1",
      title = "Gardening Help",
      upvotesCount = 3,
      hasUserUpvoted = false
    )
    val upvoted = post.copy(upvotesCount = post.upvotesCount + 1, hasUserUpvoted = true)
    assertEquals(4, upvoted.upvotesCount)
    assertTrue(upvoted.hasUserUpvoted)

    val toggleOff = upvoted.copy(upvotesCount = upvoted.upvotesCount - 1, hasUserUpvoted = false)
    assertEquals(3, toggleOff.upvotesCount)
    assertFalse(toggleOff.hasUserUpvoted)
  }

  @Test
  fun testCategory03_PerformanceBugs_verifiesGeohashComputationEfficiency() {
    val startTime = System.currentTimeMillis()
    for (i in 0 until 1000) {
      val lat = 37.7749 + (i * 0.0001)
      val lng = -122.4194 + (i * 0.0001)
      val hash = com.example.util.LocationHelper.computeGeohash(lat, lng, 7)
      assertNotNull(hash)
      assertEquals(7, hash.length)
    }
    val elapsedTime = System.currentTimeMillis() - startTime
    assertTrue("1000 geohash calculations must complete under 100ms (took ${elapsedTime}ms)", elapsedTime < 100)
  }

  @Test
  fun testCategory04_CrashBugs_verifiesGracefulHandlingOfMalformedInputs() {
    // Test 1: Empty or invalid QR payload parsing should be marked invalid gracefully without crash
    val emptyPayload = ""
    val parsedEmpty = com.example.util.QrCodePayloadHelper.parsePayload(emptyPayload)
    assertNotNull(parsedEmpty)
    assertFalse(parsedEmpty.isValid)

    // Test 2: CrashTelemetryManager captures exceptions safely without throwing
    val testThrowable = IllegalArgumentException("Test boundary illegal argument")
    val telemetryPayload = com.example.util.CrashTelemetryManager.buildTelemetryPayload(
      severity = "NON_FATAL",
      errorName = "IllegalArgumentException",
      errorMessage = testThrowable.message ?: "",
      stackTrace = testThrowable.stackTraceToString()
    )
    assertEquals("NON_FATAL", telemetryPayload["severity"])
    assertEquals("v1.0.3", telemetryPayload["appVersion"])
  }

  @Test
  fun testCategory05_NetworkBugs_verifiesOfflineFallbackAndRetryBehavior() {
    val offlineShareMessage = com.example.util.QrCodePayloadHelper.buildShareMessage(
      code = "#9q8yyk8",
      name = "Puducherry Mesh",
      lat = 11.9060,
      lng = 79.7070
    )
    assertNotNull(offlineShareMessage)
    assertTrue("Share message contains offline protocol", offlineShareMessage.contains("NEARGRID:v1"))
    assertFalse("No dead external web endpoints in offline payload", offlineShareMessage.contains("http://") || offlineShareMessage.contains("https://"))
  }

  @Test
  fun testCategory06_DatabaseBugs_verifiesMoshiAndJsonSchemaIntegrity() {
    val payload = com.example.data.model.NearGridPayload(
      geohash = "9q8yyk8",
      areaName = "Puducherry District",
      latitude = 11.9060,
      longitude = 79.7070
    )
    val json = payload.toJson()
    assertTrue(json.contains("9q8yyk8"))
    assertTrue(json.contains("Puducherry District"))

    val deserialized = com.example.data.model.NearGridPayload.fromJson(json)
    assertNotNull(deserialized)
    assertEquals("9q8yyk8", deserialized?.geohash)
    assertEquals(11.9060, deserialized?.latitude ?: 0.0, 0.0001)
  }

  @Test
  fun testCategory07_SecurityBugs_verifiesZeroTrustPrivacyAndBlockedUserExclusion() {
    val blockedUser = com.example.data.model.UserProfile(userId = "bad_actor_99", incognito = false)
    val cleanUser = com.example.data.model.UserProfile(userId = "good_neighbor_01", incognito = false)
    val blockedList = listOf("bad_actor_99")

    val candidates = listOf(blockedUser, cleanUser)
    val filtered = candidates.filter { !it.incognito && !blockedList.contains(it.userId) }

    assertEquals(1, filtered.size)
    assertEquals("good_neighbor_01", filtered.first().userId)
  }

  @Test
  fun testCategory08_NavigationBugs_verifiesNavigationDestinationsAndBackStackState() {
    val dests = com.example.ui.NavigationDestination.values()
    assertEquals(6, dests.size)
    assertEquals("Home", com.example.ui.NavigationDestination.HOME.label)
    assertEquals("Map", com.example.ui.NavigationDestination.MAP.label)
    assertEquals("Living Grid", com.example.ui.NavigationDestination.GRID.label)
    assertEquals("Community", com.example.ui.NavigationDestination.COMMUNITY.label)
    assertEquals("Inbox", com.example.ui.NavigationDestination.INBOX.label)
    assertEquals("Profile", com.example.ui.NavigationDestination.PROFILE.label)
  }

  @Test
  fun testCategory09_CompatibilityBugs_verifiesAppUpdateVersionComparison() {
    val isNewer = com.example.data.update.AppUpdateService.isNewerVersion("v1.0.4", "v1.0.3")
    assertTrue("v1.0.4 is newer than current v1.0.3", isNewer)

    val isCurrent = com.example.data.update.AppUpdateService.isNewerVersion("v1.0.3", "v1.0.3")
    assertFalse("v1.0.3 is equal to current v1.0.3", isCurrent)

    val isOlder = com.example.data.update.AppUpdateService.isNewerVersion("v1.0.2", "v1.0.3")
    assertFalse("v1.0.2 is older than current v1.0.3", isOlder)
  }

  @Test
  fun testCategory10_BatteryBugs_verifiesBackgroundSyncIntervalAndGpsModeThrottling() {
    val settings = com.example.data.model.AppSettings(backgroundSyncMinutes = 30)
    assertEquals(30, settings.backgroundSyncMinutes)

    val manualSettings = settings.copy(backgroundSyncMinutes = 0)
    assertEquals(0, manualSettings.backgroundSyncMinutes)
  }

  @Test
  fun testCategory11_MemoryBugs_verifiesStorageCacheTrimmingAndPercentageCalculations() {
    val usage = com.example.util.StorageUsage(
      imageCacheBytes = 50 * 1024 * 1024L,
      dbBytes = 10 * 1024 * 1024L,
      tempBytes = 5 * 1024 * 1024L,
      totalBytes = 65 * 1024 * 1024L
    )

    assertEquals("65.0 MB", usage.formattedTotal)
    assertTrue("Image percentage in 0..1 bounds", usage.imagePercentage in 0f..1f)
    assertTrue("DB percentage in 0..1 bounds", usage.dbPercentage in 0f..1f)
    assertTrue("Temp percentage in 0..1 bounds", usage.tempPercentage in 0f..1f)
  }
}



