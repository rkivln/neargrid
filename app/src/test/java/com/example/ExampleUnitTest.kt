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
}
