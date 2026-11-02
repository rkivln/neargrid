package com.example.data.update

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Retrofit REST service interface for GitHub API releases.
 */
interface GitHubApiService {
    @Headers(
        "Accept: application/vnd.github.v3+json",
        "User-Agent: NearGrid-Android-App"
    )
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String = "rkivln",
        @Path("repo") repo: String = "neargrid"
    ): GitHubRelease
}

/**
 * Manages checking for app updates from GitHub Releases via Retrofit
 * and comparing with the local versionName.
 */
class AppUpdateManager(
    private val apiService: GitHubApiService = defaultApiService,
    private val owner: String = "rkivln",
    private val repo: String = "neargrid"
) {

    private val _updateState = MutableStateFlow<AppUpdateInfo?>(null)
    val updateState: StateFlow<AppUpdateInfo?> = _updateState.asStateFlow()

    private val _isBannerDismissed = MutableStateFlow(false)
    val isBannerDismissed: StateFlow<Boolean> = _isBannerDismissed.asStateFlow()

    fun dismissBanner() {
        _isBannerDismissed.value = true
    }

    fun resetBannerDismissed() {
        _isBannerDismissed.value = false
    }

    /**
     * Fetches the latest release from the GitHub API using Retrofit
     * and compares it with the local [versionName] to determine if an update is available.
     */
    suspend fun checkForUpdate(versionName: String): Result<AppUpdateInfo> {
        return runCatching {
            val release = apiService.getLatestRelease(owner, repo)
            val latestTag = release.tagName.trim()
            val isAvailable = isNewerVersion(latestTag, versionName)

            val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
            val apkDownloadUrl = apkAsset?.downloadUrl?.ifBlank { null }
                ?: "https://github.com/$owner/$repo/releases/download/$latestTag/NearGrid.apk"

            val info = AppUpdateInfo(
                currentVersion = versionName,
                latestVersion = latestTag.removePrefix("v").removePrefix("V"),
                releaseName = release.name ?: "Release $latestTag",
                releaseNotes = release.body?.trim() ?: "Bug fixes and performance improvements.",
                apkDownloadUrl = apkDownloadUrl,
                releaseHtmlUrl = release.htmlUrl,
                isUpdateAvailable = isAvailable,
                isChecking = false,
                errorMessage = null
            )
            _updateState.value = info
            info
        }.onFailure { error ->
            _updateState.value = AppUpdateInfo(
                currentVersion = versionName,
                isChecking = false,
                errorMessage = error.localizedMessage ?: "Failed to check update"
            )
        }
    }

    /**
     * Compares a remote version tag against the local [versionName].
     * Returns true if [remoteVersion] is strictly newer than [versionName].
     */
    fun isNewerVersion(remoteVersion: String, versionName: String): Boolean {
        val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
        val cleanLocal = versionName.trim().removePrefix("v").removePrefix("V")

        if (cleanRemote.isBlank() || cleanLocal.isBlank()) return false
        if (cleanRemote.equals(cleanLocal, ignoreCase = true)) return false

        val remoteParts = cleanRemote.split('.').mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }
        val localParts = cleanLocal.split('.').mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        return false
    }

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        val defaultMoshi: Moshi by lazy {
            Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }

        val defaultOkHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        }

        val defaultRetrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(defaultOkHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(defaultMoshi))
                .build()
        }

        val defaultApiService: GitHubApiService by lazy {
            defaultRetrofit.create(GitHubApiService::class.java)
        }

        /**
         * Shared singleton instance of [AppUpdateManager].
         */
        val instance: AppUpdateManager by lazy {
            AppUpdateManager()
        }
    }
}
