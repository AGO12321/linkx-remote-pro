package com.example.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String
)

interface UpdateProvider {
    suspend fun checkForUpdates(currentVersion: String): UpdateInfo
}

class GitHubUpdateProvider(
    private val repoOwner: String,
    private val repoName: String
) : UpdateProvider {

    override suspend fun checkForUpdates(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "LinkX-Remote-Pro-Android")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val tagName = json.getString("tag_name")
                val isUpdate = tagName.compareTo(currentVersion, ignoreCase = true) > 0

                return@withContext UpdateInfo(
                    isUpdateAvailable = isUpdate,
                    latestVersion = tagName,
                    downloadUrl = json.getString("html_url"),
                    releaseNotes = json.optString("body", "No release notes provided.")
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check GitHub for updates")
        }
        return@withContext UpdateInfo(false, currentVersion, "", "")
    }
}

class JsonEndpointUpdateProvider(
    private val endpointUrl: String
) : UpdateProvider {

    override suspend fun checkForUpdates(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(endpointUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestVersion = json.getString("version")
                val isUpdate = latestVersion.compareTo(currentVersion, ignoreCase = true) > 0

                return@withContext UpdateInfo(
                    isUpdateAvailable = isUpdate,
                    latestVersion = latestVersion,
                    downloadUrl = json.getString("downloadUrl"),
                    releaseNotes = json.optString("releaseNotes", "")
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check JSON endpoint for updates")
        }
        return@withContext UpdateInfo(false, currentVersion, "", "")
    }
}

class UpdateService(private var provider: UpdateProvider) {
    fun setProvider(newProvider: UpdateProvider) {
        provider = newProvider
    }

    suspend fun checkAndUpdate(currentVersion: String) {
        val info = provider.checkForUpdates(currentVersion)
        if (info.isUpdateAvailable) {
            Timber.i("New update available: ${info.latestVersion}. URL: ${info.downloadUrl}")
            // Trigger UI flow to show update
        } else {
            Timber.i("Application is up to date. (Version $currentVersion)")
        }
    }
}
