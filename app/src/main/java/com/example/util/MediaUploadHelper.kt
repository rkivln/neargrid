package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object MediaUploadHelper {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    /**
     * Downscales bitmap maintaining aspect ratio so neither width nor height exceeds [maxDimension].
     */
    fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int = MAX_DIMENSION): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val maxAspect = max(width, height).toFloat()
        val scale = maxDimension.toFloat() / maxAspect
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Formats raw byte array into a standard Base64 Data URL.
     */
    fun formatAsDataUrl(bytes: ByteArray, mimeType: String = "image/jpeg"): String {
        val base64 = try {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
        return "data:$mimeType;base64,$base64"
    }

    /**
     * Compresses and converts a local media URI string (content:// or file://)
     * into a portable Base64 Data URL viewable across all devices on Firestore.
     */
    suspend fun compressAndEncodeUri(context: Context, uriString: String): String = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext uriString

        // Return immediately if it's already a Data URL or http(s) URL
        if (uriString.startsWith("data:", ignoreCase = true) ||
            uriString.startsWith("http://", ignoreCase = true) ||
            uriString.startsWith("https://", ignoreCase = true)
        ) {
            return@withContext uriString
        }

        try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.w("MediaUploadHelper", "Could not open InputStream for URI: $uriString")
                return@withContext uriString
            }

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                android.util.Log.w("MediaUploadHelper", "Failed to decode bitmap from URI: $uriString")
                return@withContext uriString
            }

            val scaledBitmap = scaleBitmapDown(originalBitmap, MAX_DIMENSION)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            val dataUrl = formatAsDataUrl(bytes, "image/jpeg")
            android.util.Log.d("MediaUploadHelper", "Successfully encoded URI ($uriString) into Base64 Data URL (length: ${dataUrl.length})")
            dataUrl
        } catch (e: Throwable) {
            android.util.Log.e("MediaUploadHelper", "Error encoding media URI ($uriString): ${e.message}", e)
            uriString
        }
    }

    /**
     * Processes a list of media URIs asynchronously, converting any local content URIs
     * into portable Base64 Data URLs.
     */
    suspend fun processMediaUris(context: Context, uris: List<String>): List<String> = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext emptyList()
        uris.map { uriStr ->
            compressAndEncodeUri(context, uriStr)
        }
    }
}
