package com.example.util

/**
 * Helper class to encode geographical coordinates (latitude and longitude) into
 * standard Base32 Geohash strings and compute geohash prefixes for spatial queries.
 */
object GeohashUtil {

    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Encodes a latitude and longitude pair into a standard Base32 geohash string.
     * Precision 7 represents ~153m x ~153m (hyperlocal block level).
     * Precision 5 represents ~4.9km x ~4.9km (neighborhood district level).
     */
    fun encode(latitude: Double, longitude: Double, precision: Int = 7): String {
        return LocationHelper.computeGeohash(latitude, longitude, precision)
    }

    /**
     * Computes the geohash prefix of length [precision] for spatial neighborhood range filtering.
     */
    fun getPrefix(latitude: Double, longitude: Double, precision: Int = 5): String {
        val fullHash = encode(latitude, longitude, precision.coerceAtLeast(1))
        return fullHash.take(precision)
    }
}
