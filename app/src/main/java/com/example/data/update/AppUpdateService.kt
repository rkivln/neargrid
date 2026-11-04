package com.example.data.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GitHubReleaseAsset(
    @Json(name = "name") val name: String = "",
    @Json(name = "browser_download_url") val downloadUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "tag_name") val tagName: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "assets") val assets: List<GitHubReleaseAsset> = emptyList()
)

data class AppUpdateInfo(
    val currentVersion: String = "1.0.3",
    val latestVersion: String = "",
    val releaseName: String = "",
    val releaseNotes: String = "",
    val apkDownloadUrl: String = "",
    val releaseHtmlUrl: String = "",
    val isUpdateAvailable: Boolean = false,
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)

object AppUpdateService {

    /**
     * Checks GitHub Releases for the latest version and compares against [currentVersion]
     * using Retrofit via [AppUpdateManager].
     */
    suspend fun checkUpdate(currentVersion: String): AppUpdateInfo {
        return AppUpdateManager.instance.checkForUpdate(currentVersion).getOrElse { error ->
            AppUpdateInfo(
                currentVersion = currentVersion,
                isChecking = false,
                errorMessage = error.localizedMessage ?: "Failed to connect to update server"
            )
        }
    }

    /**
     * Compares two semantic version strings.
     * Returns true if [remoteVersion] is strictly greater than [localVersion].
     */
    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        return AppUpdateManager.instance.isNewerVersion(remoteVersion, localVersion)
    }
}
