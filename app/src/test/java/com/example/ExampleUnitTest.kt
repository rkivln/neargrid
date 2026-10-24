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
}
