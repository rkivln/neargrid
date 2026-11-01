package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Structured data payload for NearGrid QR codes, containing
 * cell geohash, area name, and geographic coordinates.
 *
 * Includes Moshi code-generated adapter and convenient serialization methods.
 */
@JsonClass(generateAdapter = true)
data class NearGridPayload(
    @Json(name = "geohash") val geohash: String,
    @Json(name = "area_name") val areaName: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
) {
    /**
     * Serializes this payload to a JSON string using the Moshi adapter.
     */
    fun toJson(): String = adapter.toJson(this)

    companion object {
        private val moshi: Moshi by lazy {
            Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }

        /**
         * Reusable Moshi JsonAdapter for [NearGridPayload].
         */
        val adapter: JsonAdapter<NearGridPayload> by lazy {
            moshi.adapter(NearGridPayload::class.java)
        }

        /**
         * Deserializes a JSON string into a [NearGridPayload], returning null if malformed.
         */
        fun fromJson(json: String): NearGridPayload? {
            return try {
                adapter.fromJson(json.trim())
            } catch (e: Exception) {
                null
            }
        }
    }
}
