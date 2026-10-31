package com.example.util

import java.util.Locale

/**
 * Helper for generating and parsing offline structured QR payloads for NearGrid.
 *
 * Payload format:
 * NEARGRID:v1;code=<geohash>;name=<area_name>;lat=<lat>;lng=<lng>
 */
object QrCodePayloadHelper {

    private const val PROTOCOL_PREFIX = "NEARGRID:v1"

    data class ParsedGridInvite(
        val code: String,
        val name: String,
        val lat: Double,
        val lng: Double,
        val isValid: Boolean = true
    )

    /**
     * Builds a structured offline text payload for QR encoding.
     */
    fun buildPayload(
        code: String,
        name: String,
        lat: Double,
        lng: Double
    ): String {
        val cleanCode = code.trim().removePrefix("#")
        val sanitizedName = name.trim().replace(";", " ")
        val formattedLat = String.format(Locale.US, "%.5f", lat)
        val formattedLng = String.format(Locale.US, "%.5f", lng)
        return "$PROTOCOL_PREFIX;code=$cleanCode;name=$sanitizedName;lat=$formattedLat;lng=$formattedLng"
    }

    /**
     * Parses an incoming QR code content, scanned text, or pasted code.
     * Supports structured NEARGRID:v1 format, query parameter format, and raw geohash codes.
     */
    fun parsePayload(raw: String): ParsedGridInvite {
        val trimmed = raw.trim()

        if (trimmed.startsWith(PROTOCOL_PREFIX, ignoreCase = true)) {
            val content = trimmed.removePrefix(PROTOCOL_PREFIX).removePrefix(";").removePrefix("?")
            val pairs = content.split(';', '&')
            val map = mutableMapOf<String, String>()
            for (pair in pairs) {
                val parts = pair.split('=', limit = 2)
                if (parts.size == 2) {
                    map[parts[0].trim().lowercase(Locale.ROOT)] = parts[1].trim()
                }
            }

            val code = map["code"]?.let { if (it.startsWith("#")) it else "#$it" } ?: "#GPS-GRID"
            val name = map["name"]?.ifBlank { null } ?: "Neighborhood Grid $code"
            val lat = map["lat"]?.toDoubleOrNull() ?: 0.0
            val lng = map["lng"]?.toDoubleOrNull() ?: 0.0

            return ParsedGridInvite(
                code = code,
                name = name,
                lat = lat,
                lng = lng,
                isValid = true
            )
        }

        // Backward compatibility for legacy cell query URLs or raw codes
        if (trimmed.contains("cell=", ignoreCase = true)) {
            val extracted = trimmed.substringAfter("cell=").substringBefore("&").substringBefore(";")
            val code = if (extracted.startsWith("#")) extracted else "#$extracted"
            return ParsedGridInvite(
                code = code,
                name = "Neighbor Grid $code",
                lat = 0.0,
                lng = 0.0,
                isValid = true
            )
        }

        // Raw geohash input (e.g. #9q8yyk8 or 9q8yyk8)
        val cleanRaw = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
        return ParsedGridInvite(
            code = cleanRaw,
            name = "Neighbor Grid $cleanRaw",
            lat = 0.0,
            lng = 0.0,
            isValid = trimmed.isNotBlank()
        )
    }

    /**
     * Formats a clear, offline invitation text message for sharing via Android share sheet or clipboard.
     */
    fun buildShareMessage(
        code: String,
        name: String,
        lat: Double,
        lng: Double
    ): String {
        val payload = buildPayload(code, name, lat, lng)
        val formattedCode = if (code.startsWith("#")) code else "#$code"
        val formattedCoords = if (lat != 0.0 && lng != 0.0) {
            String.format(Locale.US, " (%.4f, %.4f)", lat, lng)
        } else {
            ""
        }

        return "Join my local neighborhood grid $formattedCode - $name$formattedCoords on NearGrid!\n\n" +
                "Scan or paste this offline grid pass in NearGrid to join our local mesh:\n" +
                payload
    }
}
