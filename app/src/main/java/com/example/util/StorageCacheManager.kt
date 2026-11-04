package com.example.util

import android.content.Context
import java.io.File
import java.util.Locale

data class StorageUsage(
    val imageCacheBytes: Long = 0L,
    val dbBytes: Long = 0L,
    val tempBytes: Long = 0L,
    val totalBytes: Long = 0L
) {
    val formattedTotal: String
        get() = formatBytes(totalBytes)

    val formattedImageCache: String
        get() = formatBytes(imageCacheBytes)

    val formattedDb: String
        get() = formatBytes(dbBytes)

    val formattedTemp: String
        get() = formatBytes(tempBytes)

    val imagePercentage: Float
        get() = if (totalBytes > 0) (imageCacheBytes.toFloat() / totalBytes) else 0f

    val dbPercentage: Float
        get() = if (totalBytes > 0) (dbBytes.toFloat() / totalBytes) else 0f

    val tempPercentage: Float
        get() = if (totalBytes > 0) (tempBytes.toFloat() / totalBytes) else 0f

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 KB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                else -> String.format(Locale.US, "%.0f KB", kb)
            }
        }
    }
}

data class TrimResult(
    val freedBytes: Long,
    val filesRemovedCount: Int
) {
    val formattedFreed: String
        get() = StorageUsage.formatBytes(freedBytes)
}

object StorageCacheManager {

    fun getStorageUsage(context: Context): StorageUsage {
        var imageCacheSize = 0L
        var dbSize = 0L
        var tempSize = 0L

        try {
            // 1. Calculate image & web tile cache
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                imageCacheSize = getFolderSize(cacheDir)
            }

            // Coil image cache dir if separate
            val imageCacheDir = File(context.cacheDir, "image_cache")
            if (imageCacheDir.exists()) {
                imageCacheSize += getFolderSize(imageCacheDir)
            }

            // 2. Calculate SQLite / Room DB size
            val dbDir = context.getDatabasePath("neargrid_database")?.parentFile
            if (dbDir != null && dbDir.exists()) {
                dbSize = getFolderSize(dbDir)
            }

            // 3. Temporary files
            val tempDir = File(context.filesDir, "temp")
            if (tempDir.exists()) {
                tempSize = getFolderSize(tempDir)
            }
        } catch (ignored: Exception) {}

        val total = imageCacheSize + dbSize + tempSize
        return StorageUsage(
            imageCacheBytes = imageCacheSize,
            dbBytes = dbSize,
            tempBytes = tempSize,
            totalBytes = total
        )
    }

    fun trimCache(context: Context): TrimResult {
        var freedBytes = 0L
        var filesCount = 0

        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                val files = cacheDir.listFiles() ?: emptyArray()
                val now = System.currentTimeMillis()
                val maxAgeMillis = 3 * 24 * 60 * 60 * 1000L // 3 days

                for (file in files) {
                    if (file.isFile) {
                        val age = now - file.lastModified()
                        val size = file.length()
                        if (age > maxAgeMillis || file.name.startsWith("tile_") || file.name.endsWith(".tmp")) {
                            if (file.delete()) {
                                freedBytes += size
                                filesCount++
                            }
                        }
                    } else if (file.isDirectory && file.name == "image_cache") {
                        val imgFiles = file.listFiles() ?: emptyArray()
                        for (img in imgFiles) {
                            val size = img.length()
                            if (img.delete()) {
                                freedBytes += size
                                filesCount++
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {}

        return TrimResult(freedBytes = freedBytes, filesRemovedCount = filesCount)
    }

    private fun getFolderSize(folder: File): Long {
        var length = 0L
        val files = folder.listFiles() ?: return 0L
        for (file in files) {
            length += if (file.isFile) {
                file.length()
            } else {
                getFolderSize(file)
            }
        }
        return length
    }
}
