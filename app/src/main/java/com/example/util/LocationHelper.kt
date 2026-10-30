package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val localityName: String,
    val subLocality: String = "",
    val isLiveGps: Boolean = false,
    val accuracyMeters: Float = 0f
) {
    val displayAreaName: String
        get() {
            val cleanSub = subLocality.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim(' ', ',', '-')
            val cleanLoc = localityName.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim(' ', ',', '-')
            return when {
                cleanSub.isNotBlank() && cleanLoc.isNotBlank() && cleanSub != cleanLoc ->
                    "$cleanSub, $cleanLoc"
                cleanLoc.isNotBlank() -> cleanLoc
                cleanSub.isNotBlank() -> cleanSub
                else -> "Local Coordinate Area"
            }
        }
}

object LocationHelper {
    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun formatDistance(meters: Float): String {
        return when {
            meters < 50 -> "< 50 m"
            meters < 1000 -> "${meters.toInt()} m"
            meters < 10_000 -> String.format(Locale.US, "%.1f km", meters / 1000f)
            else -> "${(meters / 1000f).toInt()} km"
        }
    }

    /**
     * Computes a standard Base32 geohash for given latitude and longitude coordinates.
     * Precision 7 represents ~153m x ~153m (hyperlocal neighborhood block).
     * Precision 6 represents ~1.2km x ~0.6km (district neighborhood level).
     */
    fun computeGeohash(latitude: Double, longitude: Double, precision: Int = 7): String {
        val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"
        var latInterval0 = -90.0
        var latInterval1 = 90.0
        var lonInterval0 = -180.0
        var lonInterval1 = 180.0
        val geohash = StringBuilder()
        var isEven = true
        var ch = 0
        var bit = 0

        while (geohash.length < precision) {
            val mid: Double
            if (isEven) {
                mid = (lonInterval0 + lonInterval1) / 2.0
                if (longitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    lonInterval0 = mid
                } else {
                    lonInterval1 = mid
                }
            } else {
                mid = (latInterval0 + latInterval1) / 2.0
                if (latitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    latInterval0 = mid
                } else {
                    latInterval1 = mid
                }
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(base32[ch])
                bit = 0
                ch = 0
            }
        }
        return geohash.toString()
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f° %s, %.4f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshDeviceLocation(context: Context): UserLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.d(TAG, "Location permission not granted")
            return@withContext null
        }

        try {
            val fusedClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            val cts = CancellationTokenSource()
            val freshLocation: Location? = try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "getCurrentLocation failed, trying lastLocation", e)
                fusedClient.lastLocation.await()
            }

            if (freshLocation != null) {
                val geocoded = reverseGeocode(context, freshLocation.latitude, freshLocation.longitude)
                UserLocation(
                    latitude = freshLocation.latitude,
                    longitude = freshLocation.longitude,
                    localityName = geocoded.first,
                    subLocality = geocoded.second,
                    isLiveGps = true,
                    accuracyMeters = freshLocation.accuracy
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring fresh device location", e)
            null
        }
    }

    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                return@withContext Pair("Local Area", "")
            }

            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val addr = addresses.firstOrNull()
                                continuation.resume(extractLocalityPair(addr))
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(Pair("Local Area", ""))
                            }
                        }
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                extractLocalityPair(addresses?.firstOrNull())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Reverse geocoding failed: ${e.message}")
            Pair("Local Area", "")
        }
    }

    private fun extractLocalityPair(address: Address?): Pair<String, String> {
        if (address == null) return Pair("Local Area", "")

        val rawLocality = address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: "Local Area"

        val rawSubLocality = address.subLocality
            ?: address.thoroughfare
            ?: address.featureName
            ?: ""

        val locality = rawLocality.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim(' ', ',', '-')
        val subLocality = rawSubLocality.replace("[\r\n\t]+".toRegex(), " ").replace("\\s+".toRegex(), " ").trim(' ', ',', '-')

        return Pair(locality, subLocality)
    }
}
